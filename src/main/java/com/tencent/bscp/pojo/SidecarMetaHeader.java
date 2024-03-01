package com.tencent.bscp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SidecarMetaHeader {
    @JsonProperty("bid")
    private int bizID;
    @JsonProperty("fpt")
    private String fingerprint;

    public SidecarMetaHeader(int bizID, String fingerprint) {
        this.bizID = bizID;
        this.fingerprint = fingerprint;
    }

    public int getBizID() {
        return bizID;
    }

    public void setBizID(int bizID) {
        this.bizID = bizID;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }
}
