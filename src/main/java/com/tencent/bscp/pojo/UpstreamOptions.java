package com.tencent.bscp.pojo;

import java.util.List;
import java.util.Map;

public class UpstreamOptions {
    private int bizID;
    private String app;
    private String key;
    private Map<String, String> labels;
    private String uid;
    private List<String> feedAddrs;
    private long dialTimeoutMS;

    private Vas vas;

    public UpstreamOptions() {
    }

    public UpstreamOptions(int bizID, String app, String key, Map<String, String> labels, String uid,
            List<String> feedAddrs, long dialTimeoutMS) {
        this.bizID = bizID;
        this.app = app;
        this.key = key;
        this.labels = labels;
        this.uid = uid;
        this.feedAddrs = feedAddrs;
        this.dialTimeoutMS = dialTimeoutMS;
    }

    public int getBizID() {
        return bizID;
    }

    public String getApp() {
        return app;
    }

    public String getKey() {
        return key;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public String getUid() {
        return uid;
    }

    public List<String> getFeedAddrs() {
        return feedAddrs;
    }

    public long getDialTimeoutMS() {
        return dialTimeoutMS;
    }

    public Vas getVas() {
        return vas;
    }

    public void setBizID(int bizID) {
        this.bizID = bizID;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setFeedAddrs(List<String> feedAddrs) {
        this.feedAddrs = feedAddrs;
    }

    public void setDialTimeoutMS(long dialTimeoutMS) {
        this.dialTimeoutMS = dialTimeoutMS;
    }

    public void setVas(Vas vas) {
        this.vas = vas;
    }
}
