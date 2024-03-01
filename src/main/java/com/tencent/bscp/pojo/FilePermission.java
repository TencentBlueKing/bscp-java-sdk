package com.tencent.bscp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FilePermission {
    @JsonProperty("user")
    private String user;

    @JsonProperty("user_group")
    private String userGroup;

    @JsonProperty("privilege")
    private String privilege;

    public FilePermission() {
        // Default constructor
    }

    public FilePermission(String user, String userGroup, String privilege) {
        this.user = user;
        this.userGroup = userGroup;
        this.privilege = privilege;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }

    public String getPrivilege() {
        return privilege;
    }

    public void setPrivilege(String privilege) {
        this.privilege = privilege;
    }

    @Override
    public String toString() {
        return "FilePermission{"
                + "user='" + user + '\''
                + ", userGroup='" + userGroup + '\''
                + ", privilege='" + privilege + '\''
                + '}';
    }
}
