package com.tencent.bscp.pojo;

import java.util.HashMap;
import java.util.Map;

public class AppOptions {
    private String key;
    private Map<String, String> labels;
    private String uid;

    public AppOptions() {
        this.uid = "";
        this.labels = new HashMap<>();
        this.key = "";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
}
