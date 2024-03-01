package com.tencent.bscp.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tencent.bscp.pojo.ConfigItemMetaV1;
import com.tencent.bscp.pojo.DownloadTo;
import com.tencent.bscp.pojo.ReleaseChangeEvent;
import com.tencent.bscp.pojo.ReleaseChangePayload;
import com.tencent.bscp.service.DownloaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CacheHelper {
    public static CacheHelper INSTANCE = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String cachePath;

    public CacheHelper() {
        this.cachePath = null;
    }

    public static void init() {
        INSTANCE = new CacheHelper();
    }

    /**
     * This method is used to handle the release change event.
     * 
     * @param event the release change event
     */
    public void onReleaseChange(ReleaseChangeEvent event) {
        ReleaseChangePayload pl;
        try {
            pl = JsonHelper.deserialize(event.getPayload(), ReleaseChangePayload.class);
        } catch (IOException e) {
            LOGGER.error("decode release change event payload failed, skip the event ,error {}, rid {}.",
                    e, event.getRid());
            return;
        }

        try {
            assert cachePath != null;
            Files.createDirectories(Paths.get(cachePath));
        } catch (IOException e) {
            LOGGER.error("mkdir cache path failed. path {}, error {}.", cachePath, "error", e);
            return;
        }

        for (ConfigItemMetaV1 ci : pl.getReleaseMeta().getCiMetas()) {
            boolean exists;
            try {
                exists = checkFileCacheExists(ci);
            } catch (IOException e) {
                LOGGER.error("check config item exists failed. error {}, rid {}.", e, event.getRid());
                continue;
            }
            if (exists) {
                continue;
            }
            String filePath = Paths.get(cachePath, ci.getContentSpec().getSignature()).toString();
            try {
                DownloaderService.INSTANCE.download(ci.getFileMeta(), ci.getRepositoryPath(),
                        ci.getContentSpec().getByteSize(),
                        DownloadTo.DownloadToFile, null, filePath);
            } catch (Exception e) {
                LOGGER.error("download file failed. error {}, rid {}.", e, event.getRid());
                return;
            }
        }
    }

    /**
     * This method is used to check the file cache exists.
     * 
     * @param ci the config item meta
     * 
     * @return true if the file cache exists, otherwise false
     * 
     * @throws IOException if an I/O error occurs
     */
    public boolean checkFileCacheExists(ConfigItemMetaV1 ci) throws IOException {
        assert cachePath != null;
        String filePath = Paths.get(cachePath, ci.getContentSpec().getSignature()).toString();
        File file = new File(filePath);
        if (!file.exists()) {
            // content is not exist
            LOGGER.error("cache file not exist");
            return false;
        }

        String sha;
        try {
            byte[] data = Files.readAllBytes(Paths.get(filePath));
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
            sha = new BigInteger(1, hash).toString(16);
        } catch (IOException e) {
            throw new IOException("check configuration item's SHA256 failed, err: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        if (!sha.equals(ci.getContentSpec().getSignature())) {
            return false;
        }

        return true;
    }
}
