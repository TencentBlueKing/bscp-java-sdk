package com.tencent.bscp.pojo;

import java.util.Map;

public class InstanceSpec {
    private int bizID;
    private int appID;
    private String app;
    private String uid;
    private Map<String, String> labels;
    private ConfigType configType;

    public InstanceSpec(int bizID, int appID, String app, String uid, Map<String, String> labels,
            ConfigType configType) {
        this.bizID = bizID;
        this.appID = appID;
        this.app = app;
        this.uid = uid;
        this.labels = labels;
        this.configType = configType;
    }

    public InstanceSpec() {
    }

    public int getBizID() {
        return bizID;
    }

    public int getAppID() {
        return appID;
    }

    public String getApp() {
        return app;
    }

    public String getUid() {
        return uid;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public ConfigType getConfigType() {
        return configType;
    }
}
