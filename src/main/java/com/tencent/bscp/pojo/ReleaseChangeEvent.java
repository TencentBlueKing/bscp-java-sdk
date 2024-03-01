package com.tencent.bscp.pojo;

import com.tencent.bscp.pbbase.Base;

public class ReleaseChangeEvent {
    private String rid;
    private Base.Versioning apiVersion;
    private byte[] payload;

    public ReleaseChangeEvent(String rid, Base.Versioning apiVersion, byte[] payload) {
        this.rid = rid;
        this.apiVersion = apiVersion;
        this.payload = payload;
    }

    public String getRid() {
        return rid;
    }

    public Base.Versioning getApiVersion() {
        return apiVersion;
    }

    public byte[] getPayload() {
        return payload;
    }
}
