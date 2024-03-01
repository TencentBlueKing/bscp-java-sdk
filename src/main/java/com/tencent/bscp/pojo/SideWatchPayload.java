package com.tencent.bscp.pojo;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SideWatchPayload {
    @JsonProperty("bizID")
    private int bizID;
    @JsonProperty("apps")
    private List<SideAppMeta> applications;

    public int getBizID() {
        return bizID;
    }

    public void setBizID(int bizID) {
        this.bizID = bizID;
    }

    public List<SideAppMeta> getApplications() {
        return applications;
    }

    public void setApplications(List<SideAppMeta> applications) {
        this.applications = applications;
    }
}
