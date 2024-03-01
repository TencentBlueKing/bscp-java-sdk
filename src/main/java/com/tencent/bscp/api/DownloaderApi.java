package com.tencent.bscp.api;

import com.tencent.bscp.pbfs.FileMeta;
import com.tencent.bscp.pojo.DownloadTo;
import com.tencent.bscp.pojo.Vas;
import nl.altindag.ssl.SSLFactory;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public interface DownloaderApi {

    Vas getVas();

    UpstreamApi getUpstream();

    int getBizID();

    String getToken();

    SSLFactory getTls();

    Semaphore getSem();

    // balanceDownloadByteSize determines when to download the file with range policy
    // if the configuration item's content size is larger than this, then it
    // will be downloaded with range policy, otherwise, it will be downloaded directly
    // without range policy.
    long getBalanceDownloadByteSize();

    void download(FileMeta fileMeta, String downloadUri, long fileSize, DownloadTo to, byte[] bytes, String toFile)
            throws Exception;
}
