package com.tencent.bscp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KvAttachment {
    @JsonProperty("biz_id")
    private int bizId;

    @JsonProperty("app_id")
    private int appId;

    public KvAttachment() {
        // Default constructor
    }

    public KvAttachment(int bizId, int appId) {
        this.bizId = bizId;
        this.appId = appId;
    }

    public int getBizId() {
        return bizId;
    }

    public void setBizId(int bizId) {
        this.bizId = bizId;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }
}
