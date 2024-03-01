package com.tencent.bscp.pojo;

/**
 * SidecarOffline means the sidecar is shutting down or something happens, to tell feed server this sidecar is offline.
 * Heartbeat means the sidecar is online, to tell feed server this sidecar is live.
 */
public enum DownloadTo {

    // DownloadToBytes download file content to bytes.
    DownloadToBytes,
    // DownloadToFile download file content to file.
    DownloadToFile;
}
