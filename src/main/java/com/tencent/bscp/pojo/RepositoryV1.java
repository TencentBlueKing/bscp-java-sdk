package com.tencent.bscp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

// RepositoryV1 defines repository related metas.
public class RepositoryV1 {
    // Root is the root path to download the configuration items from repository.
    @JsonProperty("root")
    private String root;

    @JsonProperty("tls")
    private TLSBytes tls;

    @JsonProperty("accessKeyId")
    private String accessKeyID;

    @JsonProperty("secretAccessKey")
    private String secretAccessKey;

    @JsonProperty("url")
    private String url;

    public RepositoryV1(String root, TLSBytes tls, String accessKeyID, String secretAccessKey, String url) {
        this.root = root;
        this.tls = tls;
        this.accessKeyID = accessKeyID;
        this.secretAccessKey = secretAccessKey;
        this.url = url;
    }

    public RepositoryV1() {
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public TLSBytes getTls() {
        return tls;
    }

    public void setTls(TLSBytes tls) {
        this.tls = tls;
    }

    public String getAccessKeyID() {
        return accessKeyID;
    }

    public void setAccessKeyID(String accessKeyID) {
        this.accessKeyID = accessKeyID;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
