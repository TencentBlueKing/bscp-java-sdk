package com.tencent.bscp.sdk;

import com.tencent.bscp.api.ClientApi;
import com.tencent.bscp.api.UpstreamApi;
import com.tencent.bscp.helper.CacheHelper;
import com.tencent.bscp.helper.Helper;
import com.tencent.bscp.helper.JsonHelper;
import com.tencent.bscp.helper.OptionHelper;
import com.tencent.bscp.pbfs.App;
import com.tencent.bscp.pbfs.AppMeta;
import com.tencent.bscp.pbfs.FileMeta;
import com.tencent.bscp.pbfs.GetKvValueReq;
import com.tencent.bscp.pbfs.GetKvValueResp;
import com.tencent.bscp.pbfs.HandshakeMessage;
import com.tencent.bscp.pbfs.HandshakeResp;
import com.tencent.bscp.pbfs.KvMeta;
import com.tencent.bscp.pbfs.ListAppsReq;
import com.tencent.bscp.pbfs.ListAppsResp;
import com.tencent.bscp.pbfs.PullAppFileMetaReq;
import com.tencent.bscp.pbfs.PullAppFileMetaResp;
import com.tencent.bscp.pbfs.PullKvMetaReq;
import com.tencent.bscp.pbfs.PullKvMetaResp;
import com.tencent.bscp.pbfs.SidecarSpec;
import com.tencent.bscp.pojo.AppOption;
import com.tencent.bscp.pojo.AppOptions;
import com.tencent.bscp.pojo.Callback;
import com.tencent.bscp.pojo.ConfigItemFile;
import com.tencent.bscp.pojo.ConfigItemMetaV1;
import com.tencent.bscp.pojo.KvMetaV1;
import com.tencent.bscp.pojo.Option;
import com.tencent.bscp.pojo.Options;
import com.tencent.bscp.pojo.Release;
import com.tencent.bscp.pojo.SidecarHandshakePayload;
import com.tencent.bscp.pojo.SidecarMetaHeader;
import com.tencent.bscp.pojo.Subscriber;
import com.tencent.bscp.pojo.Vas;
import com.tencent.bscp.service.DownloaderService;
import com.tencent.bscp.service.WatcherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client implements ClientApi, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UpstreamApi upStreamApi;
    private final Options options;
    private final Map<String, String> pairs;

    private final DownloaderService downloaderService;

    private final WatcherService watcherService;

    /**
     * Create a new client instance.
     * 
     * @param opts the options
     * 
     * @throws Exception if the client instance cannot be created
     */
    public Client(Option... opts) throws Exception {
        Options clientOpt = new Options();
        String fp = Helper.getFingerPrint();
        if (fp == null) {
            throw new Exception("Generate instance fingerprint failed");
        }
        String md5fp = Helper.generateMD5(fp);
        LOGGER.info("Instance fingerprint: {}, md5: {}", fp, md5fp);
        clientOpt.setFingerprint(md5fp);
        clientOpt.setUid(clientOpt.getFingerprint());
        for (Option opt : opts) {
            if (opt != null) {
                opt.apply(clientOpt);
            }
        }
        // prepare pairs
        Map<String, String> pairs = new HashMap<>();

        // Add authorization header
        pairs.put(Helper.AUTHORIZATION_HEADER, Helper.BEARER_KEY + " " + clientOpt.getToken());

        // Add fingerprint
        SidecarMetaHeader mh = new SidecarMetaHeader(clientOpt.getBizID(), clientOpt.getFingerprint());
        String mhBytes = JsonHelper.serialize(mh);
        pairs.put(Helper.SIDECAR_META_KEY, mhBytes);

        // prepare upstream

        this.pairs = pairs;

        // handshake
        Vas vas = Helper.buildVas(pairs);
        UpStreamClient u = new UpStreamClient(
                OptionHelper.upstreamWithFeedAddrs(clientOpt.getFeedAddrs()),
                OptionHelper.upstreamWithDialTimeoutMS(clientOpt.getDialTimeoutMS()),
                OptionHelper.upstreamWithBizID(clientOpt.getBizID()),
                OptionHelper.upstreamWithVas(vas)

        );
        this.upStreamApi = u;
        this.options = clientOpt;
        HandshakeMessage msg = HandshakeMessage.newBuilder()
                .setApiVersion(Helper.CURRENT_API_VERSION)
                .setSpec(
                        SidecarSpec.newBuilder()
                                .setBizId(clientOpt.getBizID())
                                .setVersion(upStreamApi.version())
                                .build())
                .build();
        HandshakeResp resp = upStreamApi.handshake(vas, msg);
        if (resp == null) {
            throw new Exception("Handshake with upstream failed");
        }
        SidecarHandshakePayload pl = JsonHelper.deserialize(
                resp.getPayload().toByteArray(), SidecarHandshakePayload.class);
        if (pl == null) {
            throw new Exception("Decode handshake payload failed");
        }
        this.downloaderService = new DownloaderService(
                vas, u, clientOpt.getBizID(), clientOpt.getToken(), pl.getRuntimeOption().getRepositoryTLS());
        if (clientOpt.isUseFileCache()) {
            CacheHelper.init();
        }
        try {
            this.watcherService = new WatcherService(u, clientOpt);
        } catch (Exception e) {
            LOGGER.warn("Init watcher failed");
            throw new Exception("Init watcher failed");
        }
    }

    @Override
    public List<App> listApp(List<String> match) throws Exception {
        Vas vas = Helper.buildVas(pairs);
        ListAppsReq req = ListAppsReq.newBuilder().setBizId(options.getBizID()).addAllMatch(match).build();
        ListAppsResp resp = upStreamApi.listApps(vas, req);
        return resp.getAppsList();
    }

    @Override
    public Release pullFiles(String app, AppOption... opts) throws Exception {
        AppOptions option = new AppOptions();
        for (AppOption opt : opts) {
            opt.apply(option);
        }
        Vas vas = Helper.buildVas(pairs);
        PullAppFileMetaReq req = PullAppFileMetaReq.newBuilder().setApiVersion(Helper.CURRENT_API_VERSION)
                .setBizId(options.getBizID())
                .setAppMeta(AppMeta.newBuilder().setApp(app)
                        .putAllLabels(Helper.mergeLabels(options.getLabels(), option.getLabels()))
                        .setUid(!option.getUid().isEmpty() ? option.getUid() : options.getUid()).build())
                .setToken(options.getToken())
                .setKey(option.getKey()).build();
        PullAppFileMetaResp resp = upStreamApi.pullAppFileMeta(vas, req);
        List<ConfigItemFile> files = new ArrayList<>();
        for (FileMeta meta : resp.getFileMetasList()) {
            ConfigItemFile file = new ConfigItemFile(
                    meta.getConfigItemSpec().getName(),
                    meta.getConfigItemSpec().getPath(),
                    meta.getConfigItemSpec().getPermission(),
                    new ConfigItemMetaV1(
                            meta.getId(), meta.getCommitId(),
                            meta.getCommitSpec().getContent(),
                            meta.getConfigItemSpec(), meta.getConfigItemAttachment(),
                            meta.getRepositorySpec().getPath()));
            files.add(file);
        }
        return new Release(resp.getReleaseId(), files, resp.getPreHook(), resp.getPostHook());
    }

    @Override
    public Release pullKvs(String app, List<String> match, AppOption... opts) throws Exception {
        AppOptions option = new AppOptions();
        for (AppOption opt : opts) {
            opt.apply(option);
        }
        Vas vas = Helper.buildVas(pairs);
        PullKvMetaReq req = PullKvMetaReq.newBuilder().setBizId(options.getBizID()).addAllMatch(match)
                .setAppMeta(AppMeta.newBuilder().setApp(app)
                        .putAllLabels(Helper.mergeLabels(options.getLabels(), option.getLabels()))
                        .setUid(!option.getUid().isEmpty() ? option.getUid() : options.getUid()).build())
                .build();
        PullKvMetaResp resp = upStreamApi.pullKvMeta(vas, req);
        List<KvMetaV1> kvs = new ArrayList<>();
        for (KvMeta meta : resp.getKvMetasList()) {
            KvMetaV1 kv = new KvMetaV1(meta.getKey(), meta.getKvType(), meta.getRevision(), meta.getKvAttachment());
            kvs.add(kv);
        }
        return new Release(resp.getReleaseId(), new ArrayList<>(), kvs, null, null);
    }

    @Override
    public String get(String app, String key, AppOption... opts) throws Exception {
        AppOptions option = new AppOptions();
        for (AppOption opt : opts) {
            opt.apply(option);
        }
        Vas vas = Helper.buildVas(pairs);
        GetKvValueReq req = GetKvValueReq.newBuilder().setBizId(options.getBizID())
                .setAppMeta(AppMeta.newBuilder().setApp(app)
                        .putAllLabels(Helper.mergeLabels(options.getLabels(), option.getLabels()))
                        .setUid(!option.getUid().isEmpty() ? option.getUid() : options.getUid()).build())
                .setKey(key)
                .build();
        GetKvValueResp resp = upStreamApi.getKvValue(vas, req);
        return resp.getValue();
    }

    @Override
    public void addWatcher(Callback callback, String app, AppOption... opts) throws Exception {
        this.watcherService.subscribe(callback, app, opts);
    }

    @Override
    public void startWatch() throws Exception {
        this.watcherService.startWatch();
    }

    @Override
    public void stopWatch() {
        this.watcherService.stopWatch();
    }

    @Override
    public void resetLabels(Map<String, String> labels) {
        this.options.setLabels(labels);
        for (Subscriber subscriber : this.watcherService.getSubscribers()) {
            subscriber.setLabels(Helper.mergeLabels(labels, subscriber.getOpts().getLabels()));
        }

        this.watcherService.reconnect("reset labels");
    }

    @Override
    public void close() throws IOException {
        stopWatch();
    }
}
