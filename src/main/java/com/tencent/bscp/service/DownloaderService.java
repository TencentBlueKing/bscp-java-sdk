package com.tencent.bscp.service;

import com.tencent.bscp.api.DownloaderApi;
import com.tencent.bscp.api.UpstreamApi;
import com.tencent.bscp.helper.Helper;
import com.tencent.bscp.helper.OkhttpHelper;
import com.tencent.bscp.helper.TLSHelper;
import com.tencent.bscp.pbfs.FileMeta;
import com.tencent.bscp.pbfs.GetDownloadURLReq;
import com.tencent.bscp.pbfs.GetDownloadURLResp;
import com.tencent.bscp.pojo.DownloadTo;
import com.tencent.bscp.pojo.ExecDownload;
import com.tencent.bscp.pojo.TLSBytes;
import com.tencent.bscp.pojo.Vas;
import nl.altindag.ssl.SSLFactory;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

// downloader is used to download the configuration items from provider
public class DownloaderService implements DownloaderApi {

    public static DownloaderService INSTANCE;
    private final Vas vas;
    private final UpstreamApi upstream;
    private final int bizID;
    private final String token;
    private final SSLFactory tls;
    private final Semaphore sem;
    // balanceDownloadByteSize determines when to download the file with range
    // policy
    // if the configuration item's content size is larger than this, then it
    // will be downloaded with range policy, otherwise, it will be downloaded
    // directly
    // without range policy.
    private final long balanceDownloadByteSize;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public DownloaderService(
            Vas vas,
            UpstreamApi upstream,
            int bizID,
            String token,
            TLSBytes tlsBytes) throws Exception {
        this.vas = vas;
        this.upstream = upstream;
        this.bizID = bizID;
        this.token = token;
        this.tls = tlsConfigFromTLSBytes(tlsBytes);
        this.sem = new Semaphore(setupMaxHttpDownloadGoroutines());
        this.balanceDownloadByteSize = Helper.DEFAULT_RANGE_DOWNLOAD_BYTE_SIZE;
        INSTANCE = this;
    }

    @Override
    public Vas getVas() {
        return vas;
    }

    @Override
    public UpstreamApi getUpstream() {
        return upstream;
    }

    @Override
    public int getBizID() {
        return bizID;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public SSLFactory getTls() {
        return tls;
    }

    @Override
    public Semaphore getSem() {
        return sem;
    }

    @Override
    public long getBalanceDownloadByteSize() {
        return balanceDownloadByteSize;
    }

    @Override
    public void download(
            FileMeta fileMeta,
            String downloadUri,
            long fileSize,
            DownloadTo to,
            byte[] bytes,
            String toFile) throws Exception {

        ExecDownload exec = new ExecDownload(
                fileMeta, INSTANCE, to, OkhttpHelper.getDownloaderOkhttpClient(tls), downloadUri, fileSize);

        switch (to) {
            case DownloadToFile:
                if (toFile.isEmpty()) {
                    throw new IllegalArgumentException("Target file path is empty");
                }
                exec.setFile(new File(toFile));
                break;
            case DownloadToBytes:
                if (bytes.length != fileSize) {
                    throw new IllegalArgumentException("The size of bytes is not equal to the file size");
                }
                exec.setBytes(bytes);
                break;
            default:
                throw new IllegalArgumentException("Invalid download destination");
        }
        doDownload(exec);
    }

    /**
     * This method is used to download the file with the given configuration item's
     * meta.
     * 
     * @param exec the download execution object
     * 
     * @throws Exception if any error occurs
     */
    public void doDownload(ExecDownload exec) throws Exception {
        GetDownloadURLReq getUrlReq = GetDownloadURLReq.newBuilder()
                .setApiVersion(Helper.CURRENT_API_VERSION)
                .setBizId(exec.getDl().getBizID())
                .setFileMeta(exec.getFileMeta())
                .setToken(exec.getDl().getToken())
                .build();

        GetDownloadURLResp resp = exec.getDl().getUpstream().getDownloadURL(exec.getDl().getVas(), getUrlReq);
        exec.setDownloadUri(resp.getUrl());

        if (exec.getFileSize() <= exec.getDl().getBalanceDownloadByteSize()) {
            downloadDirectlyWithRetry(exec);
            return;
        }

        long size;
        boolean yes;
        try {
            Map.Entry<Long, Boolean> pair = isProviderSupportRangeDownload(exec);
            size = pair.getKey();
            yes = pair.getValue();
        } catch (Exception e) {
            LOGGER.warn("Check if provider supports range download failed. err {}.", e.getMessage());
            size = 0;
            yes = false;
        }

        if (yes) {
            if (size != exec.getFileSize()) {
                throw new IOException("The to be downloaded file size is not as expected");
            }
            downloadWithRange(exec);
            return;
        }

        LOGGER.warn("Provider does not support download with range policy, download directly now.");

        downloadDirectlyWithRetry(exec);
    }

    private void downloadWithRange(ExecDownload exec) throws InterruptedException {
        LOGGER.info(
                "start download file with range. file {}.",
                Paths.get(
                        exec.getFileMeta().getConfigItemSpec().getPath(),
                        exec.getFileMeta().getConfigItemSpec().getName()).toString());

        long start;
        long end;
        long batchSize = 2 * exec.getDl().getBalanceDownloadByteSize();
        int totalParts = (int) (exec.getFileSize() / batchSize);
        if (exec.getFileSize() % batchSize > 0) {
            totalParts++;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(totalParts);

        for (int part = 0; part < totalParts; part++) {
            start = part * batchSize;

            if (part == totalParts - 1) {
                end = exec.getFileSize();
            } else {
                end = start + batchSize;
            }

            end--;

            final long from = start;
            final long to = end;
            final int pos = part;
            executorService.execute(() -> {
                long threadId = Thread.currentThread().getId();
                try {
                    long startTime = System.currentTimeMillis();
                    downloadOneRangedPartWithRetry(from, to, exec);
                    LOGGER.debug(
                            "download file range part success. file {}, part {}, from {}, to {}, cost{}, threadId {}.",
                            Paths.get(
                                    exec.getFileMeta().getConfigItemSpec().getPath(),
                                    exec.getFileMeta().getConfigItemSpec().getName()),
                            pos,
                            from,
                            to,
                            System.currentTimeMillis() - startTime,
                            threadId);
                } catch (IOException e) {
                    LOGGER.error(
                            "download file part failed. file {}, part {}, start {}, error {}, threadId {}.",
                            Paths.get(
                                    exec.getFileMeta().getConfigItemSpec().getPath(),
                                    exec.getFileMeta().getConfigItemSpec().getName()),
                            pos,
                            from,
                            e,
                            threadId);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        LOGGER.debug(
                "download full file success. file {}.",
                Paths.get(
                        exec.getFileMeta().getConfigItemSpec().getPath(),
                        exec.getFileMeta().getConfigItemSpec().getName()));
    }

    private void downloadOneRangedPartWithRetry(
            long start,
            long end,
            ExecDownload exec) throws IOException, InterruptedException {
        RetryPolicyService retry = new RetryPolicyService(1, new int[] { 500, 10000 });
        int maxRetryCount = 5;

        while (retry.shouldContinue()) {
            if (retry.getRetryCount() >= maxRetryCount) {
                throw new IOException("Download file part failed, retry count: " + maxRetryCount);
            }

            try {
                downloadOneRangedPart(start, end, exec);
                break;
            } catch (IOException e) {
                LOGGER.error("Download file part failed. error {}, retry_count {}.", e, retry.getRetryCount());
            }
        }
    }

    private void downloadOneRangedPart(long start, long end, ExecDownload exec) throws IOException {
        if (start > end) {
            throw new IOException("Invalid start or end to do range download");
        }

        if (!exec.getDl().getSem().tryAcquire(1)) {
            throw new IOException("Acquire semaphore failed");
        }

        try {
            Map<String, String> header = new HashMap<>(exec.getHeader());
            if (start == end) {
                header.put("Range", "bytes=" + start + "-");
            } else {
                header.put("Range", "bytes=" + start + "-" + end);
            }

            InputStream body = doGet(header, 6 * Helper.REQUEST_AWAIT_RESPONSE_TIMEOUT_SECONDS, exec);
            try {
                write(body, end - start + 1, start, exec);
            } finally {
                body.close();
            }
        } finally {
            exec.getDl().getSem().release(1);
        }
    }

    private Map.Entry<Long, Boolean> isProviderSupportRangeDownload(ExecDownload exec) throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(exec.getDownloadUri())
                .head();

        for (Map.Entry<String, String> entry : exec.getHeader().entrySet()) {
            requestBuilder.header(entry.getKey(), entry.getValue());
        }

        requestBuilder.header("Request-Timeout", "15");

        Response response = exec.getClient().newCall(requestBuilder.build()).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Request to provider failed, HTTP code: " + response.code());
        }

        String acceptRanges = response.header("Accept-Ranges");
        if (acceptRanges == null || !acceptRanges.equals("bytes")) {
            return new AbstractMap.SimpleEntry<>(0L, false);
        }

        String contentLength = response.header("Content-Length");
        if (contentLength == null) {
            throw new IOException("Cannot get the content length from header");
        }

        long size = Long.parseLong(contentLength);

        return new AbstractMap.SimpleEntry<>(size, true);
    }

    private void downloadDirectlyWithRetry(ExecDownload exec) throws IOException, InterruptedException {
        RetryPolicyService retry = new RetryPolicyService(1, new int[] { 500, 10000 });
        int maxRetryCount = 5;

        while (retry.shouldContinue()) {
            if (retry.getRetryCount() >= maxRetryCount) {
                LOGGER.warn("Exec do download failed, retry count: " + maxRetryCount);
                throw new IOException("Exec do download failed, retry count: " + maxRetryCount);
            }

            try {
                downloadDirectly(Helper.REQUEST_AWAIT_RESPONSE_TIMEOUT_SECONDS, exec);
                break;
            } catch (IOException e) {
                LOGGER.error("Exec do download failed. fail message {}, retry_count {}.", e, retry.getRetryCount());
            }
        }
    }

    private void downloadDirectly(int timeoutSeconds, ExecDownload exec) throws IOException {
        try {
            exec.getDl().getSem().acquire(1);
        } catch (InterruptedException e) {
            throw new IOException("Acquire semaphore failed", e);
        }

        try {
            Instant start = Instant.now();
            Map<String, String> header = exec.getHeader();
            InputStream body = doGet(header, timeoutSeconds, exec);
            try {
                write(body, exec.getFileSize(), 0, exec);
            } finally {
                body.close();
            }

            LOGGER.debug(
                    "Download directly success. file {}, cost {}",
                    Paths.get(
                            exec.getFileMeta().getConfigItemSpec().getPath(),
                            exec.getFileMeta().getConfigItemSpec().getName()),
                    Duration.between(start, Instant.now()));
        } finally {
            exec.getDl().getSem().release(1);
        }
    }

    private void write(InputStream body, long expectSize, long start, ExecDownload exec) throws IOException {
        long totalSize = 0;
        byte[] swap = new byte[8192];

        while (true) {
            int picked = body.read(swap);
            if (picked > 0) {
                switch (exec.getTo()) {
                    case DownloadToBytes:
                        for (int i = 0; i < picked; i++) {
                            exec.getBytes()[(int) (totalSize + i)] = swap[i];
                        }
                        break;

                    case DownloadToFile:
                        try (OutputStream outputStream = Files.newOutputStream(
                                exec.getFile().toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
                            outputStream.write(swap, 0, picked);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid download destination");
                }

                totalSize += picked;
            }

            if (picked == -1) {
                break;
            }
        }

        if (totalSize != expectSize) {
            throw new IOException(
                    "The downloaded file's total size " + totalSize + " is not what we expected " + expectSize);
        }
    }

    private InputStream doGet(Map<String, String> header, int timeoutSeconds, ExecDownload exec) throws IOException {
        return doRequest("GET", header, null, timeoutSeconds, exec);
    }

    private InputStream doRequest(
            String method,
            Map<String, String> header,
            RequestBody body,
            int timeoutSeconds,
            ExecDownload exec) throws IOException {

        Request.Builder requestBuilder = new Request.Builder()
                .url(exec.getDownloadUri())
                .method(method, body);

        for (Map.Entry<String, String> entry : header.entrySet()) {
            requestBuilder.header(entry.getKey(), entry.getValue());
        }

        if (timeoutSeconds > 0) {
            requestBuilder.header("Request-Timeout", String.valueOf(timeoutSeconds));
        }

        Request request = requestBuilder.build();

        Response response = exec.getClient().newCall(request).execute();
        if (response.code() != 200 && response.code() != 206) {
            throw new IOException("Request to provider, but returned with HTTP code: " + response.code());
        }

        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            throw new IOException("Response body is null");
        }

        return responseBody.byteStream();
    }

    private SSLFactory tlsConfigFromTLSBytes(TLSBytes tlsBytes) {
        return TLSHelper.buildSSLFactory(tlsBytes);
    }

    private int setupMaxHttpDownloadGoroutines() {
        String weightEnv = System.getenv(Helper.ENV_MAX_HTTP_DOWNLOAD_GOROUTINES);
        if (weightEnv == null || weightEnv.isEmpty()) {
            return Helper.DEFAULT_DOWNLOAD_GROUTINES;
        }

        int weight;
        try {
            weight = Integer.parseInt(weightEnv);
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid max http download groutines, set to default for now: "
                    + "groutines=" + weightEnv + ", default=" + Helper.DEFAULT_DOWNLOAD_GROUTINES);
            return Helper.DEFAULT_DOWNLOAD_GROUTINES;
        }

        if (weight < 1) {
            LOGGER.warn("Invalid max http download groutines, should be >= 1, set to 1 for now: "
                    + "groutines=" + weight);
            return 1;
        }

        if (weight > 15) {
            LOGGER.warn("Invalid max http download groutines, should be <= 15, set to 1 for now: "
                    + "groutines=" + weight);
        }

        LOGGER.info("Max http download groutines: groutines=" + weight);

        return weight;
    }

    // Add getters and setters for the private fields if needed
}
