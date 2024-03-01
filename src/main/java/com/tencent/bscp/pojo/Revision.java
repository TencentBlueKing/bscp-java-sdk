package com.tencent.bscp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Revision {
    @JsonProperty("creator")
    private String creator;

    @JsonProperty("reviser")
    private String reviser;

    @JsonProperty("create_at")
    private String createAt;

    @JsonProperty("update_at")
    private String updateAt;

    public Revision() {
        // Default constructor
    }

    public Revision(String creator, String reviser, String createAt, String updateAt) {
        this.creator = creator;
        this.reviser = reviser;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getReviser() {
        return reviser;
    }

    public void setReviser(String reviser) {
        this.reviser = reviser;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public String getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }
}
