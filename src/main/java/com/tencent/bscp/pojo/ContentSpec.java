package com.tencent.bscp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContentSpec {
    @JsonProperty("signature")
    private String signature;

    @JsonProperty("byte_size")
    private long byteSize;

    public ContentSpec() {
        // Default constructor
    }

    public ContentSpec(String signature, long byteSize) {
        this.signature = signature;
        this.byteSize = byteSize;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public long getByteSize() {
        return byteSize;
    }

    public void setByteSize(long byteSize) {
        this.byteSize = byteSize;
    }

    @Override
    public String toString() {
        return "ContentSpec{"
        + "signature='" + signature + '\''
        + ", byteSize=" + byteSize
        + '}';
    }
}
