package com.tencent.bscp.pojo;

import java.util.Map;

public class Subscriber {
    private AppOptions opts;
    private String app;
    private Callback callback;
    private int currentReleaseID;
    private Map<String, String> labels;
    private String uid;
    private Map<String, Integer> currentConfigItems;

    // Constructor
    public Subscriber() {
    }

    public Subscriber(AppOptions opts, String app, Callback callback, int currentReleaseID, Map<String, String> labels,
            String uid) {
        this.opts = opts;
        this.app = app;
        this.callback = callback;
        this.currentReleaseID = currentReleaseID;
        this.labels = labels;
        this.uid = uid;
    }

    public AppOptions getOpts() {
        return opts;
    }

    public void setOpts(AppOptions opts) {
        this.opts = opts;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public int getCurrentReleaseID() {
        return currentReleaseID;
    }

    public void setCurrentReleaseID(int currentReleaseID) {
        this.currentReleaseID = currentReleaseID;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Map<String, Integer> getCurrentConfigItems() {
        return currentConfigItems;
    }

    public void setCurrentConfigItems(Map<String, Integer> currentConfigItems) {
        this.currentConfigItems = currentConfigItems;
    }
}
