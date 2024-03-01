package com.tencent.bscp.pojo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Options {
    private List<String> feedAddrs;
    private int bizID;
    private Map<String, String> labels;
    private String fingerprint;
    private String uid;
    private boolean useFileCache;
    private String fileCacheDir;
    private long dialTimeoutMS;
    private String token;

    public List<String> getFeedAddrs() {
        return feedAddrs;
    }

    public void setFeedAddrs(List<String> feedAddrs) {
        this.feedAddrs = feedAddrs;
    }

    public int getBizID() {
        return bizID;
    }

    public void setBizID(int bizID) {
        this.bizID = bizID;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isUseFileCache() {
        return useFileCache;
    }

    public void setUseFileCache(boolean useFileCache) {
        this.useFileCache = useFileCache;
    }

    public String getFileCacheDir() {
        return fileCacheDir;
    }

    public void setFileCacheDir(String fileCacheDir) {
        this.fileCacheDir = fileCacheDir;
    }

    public long getDialTimeoutMS() {
        return dialTimeoutMS;
    }

    public void setDialTimeoutMS(long dialTimeoutMS) {
        this.dialTimeoutMS = dialTimeoutMS;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
