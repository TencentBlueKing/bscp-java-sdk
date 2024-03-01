package com.tencent.bscp.pojo;

import java.util.List;

public class HeartbeatPayload {
    private String fingerprint;
    private List<SideAppMeta> applications;

    public HeartbeatPayload(String fingerprint, List<SideAppMeta> applications) {
        this.fingerprint = fingerprint;
        this.applications = applications;
    }

    public HeartbeatPayload() {
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public List<SideAppMeta> getApplications() {
        return applications;
    }

    public void setApplications(List<SideAppMeta> applications) {
        this.applications = applications;
    }
}
