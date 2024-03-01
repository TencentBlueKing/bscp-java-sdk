package com.tencent.bscp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

// TLSBytes defines the repository's TLS file's body bytes.
// Note: each file's content byte is encoded with BASE64 when it is marshaled
// with json and decoded it from BASE64 when unmarshal it from json.
public class TLSBytes {
    @JsonProperty("insecure")
    private boolean insecureSkipVerify;

    @JsonProperty("ca")
    private String caFileBytes;

    @JsonProperty("cert")
    private String certFileBytes;

    @JsonProperty("key")
    private String keyFileBytes;

    public boolean isInsecureSkipVerify() {
        return insecureSkipVerify;
    }

    public void setInsecureSkipVerify(boolean insecureSkipVerify) {
        this.insecureSkipVerify = insecureSkipVerify;
    }

    public String getCaFileBytes() {
        return caFileBytes;
    }

    public void setCaFileBytes(String caFileBytes) {
        this.caFileBytes = caFileBytes;
    }

    public String getCertFileBytes() {
        return certFileBytes;
    }

    public void setCertFileBytes(String certFileBytes) {
        this.certFileBytes = certFileBytes;
    }

    public String getKeyFileBytes() {
        return keyFileBytes;
    }

    public void setKeyFileBytes(String keyFileBytes) {
        this.keyFileBytes = keyFileBytes;
    }
}
