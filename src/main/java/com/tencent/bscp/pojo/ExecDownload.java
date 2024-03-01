package com.tencent.bscp.pojo;

import com.tencent.bscp.api.DownloaderApi;
import com.tencent.bscp.pbfs.FileMeta;
import okhttp3.OkHttpClient;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ExecDownload {
    private FileMeta fileMeta;
    private DownloaderApi dl;
    private DownloadTo to;
    private byte[] bytes;
    private File file;
    private OkHttpClient client;
    private Map<String, String> header;
    private String downloadUri;
    private long fileSize;

    public ExecDownload(FileMeta fileMeta, DownloaderApi dl, DownloadTo to, OkHttpClient client, String downloadUri,
            long fileSize) {
        this.fileMeta = fileMeta;
        this.dl = dl;
        this.to = to;
        this.client = client;
        this.header = new HashMap<>();
        this.downloadUri = downloadUri;
        this.fileSize = fileSize;
    }

    public FileMeta getFileMeta() {
        return fileMeta;
    }

    public DownloaderApi getDl() {
        return dl;
    }

    public DownloadTo getTo() {
        return to;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public File getFile() {
        return file;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public String getDownloadUri() {
        return downloadUri;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileMeta(FileMeta fileMeta) {
        this.fileMeta = fileMeta;
    }

    public void setDl(DownloaderApi dl) {
        this.dl = dl;
    }

    public void setTo(DownloadTo to) {
        this.to = to;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public void setDownloadUri(String downloadUri) {
        this.downloadUri = downloadUri;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
