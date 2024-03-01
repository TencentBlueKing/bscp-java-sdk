package com.tencent.bscp.pojo;

import java.util.Map;

public class SideAppMeta {
    private int appID;
    private String app;
    private String namespace;
    private String uid;
    private Map<String, String> labels;
    private int currentReleaseID;
    private int currentCursorID;

    public SideAppMeta(String app, String uid, Map<String, String> labels, int currentReleaseID) {
        this.app = app;
        this.uid = uid;
        this.labels = labels;
        this.currentReleaseID = currentReleaseID;
    }

    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    public int getCurrentReleaseID() {
        return currentReleaseID;
    }

    public void setCurrentReleaseID(int currentReleaseID) {
        this.currentReleaseID = currentReleaseID;
    }

    public int getCurrentCursorID() {
        return currentCursorID;
    }

    public void setCurrentCursorID(int currentCursorID) {
        this.currentCursorID = currentCursorID;
    }

    // Getters and setters
}
