package com.tencent.bscp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigItemSpec {
    @JsonProperty("name")
    private String name;

    @JsonProperty("path")
    private String path;

    @JsonProperty("file_type")
    private String fileType;

    @JsonProperty("file_mode")
    private String fileMode;

    @JsonProperty("memo")
    private String memo;

    @JsonProperty("permission")
    private FilePermission permission;

    public ConfigItemSpec() {
        // Default constructor
    }

    public ConfigItemSpec(
            String name,
            String path,
            String fileType,
            String fileMode,
            String memo,
            FilePermission permission) {
        this.name = name;
        this.path = path;
        this.fileType = fileType;
        this.fileMode = fileMode;
        this.memo = memo;
        this.permission = permission;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileMode() {
        return fileMode;
    }

    public void setFileMode(String fileMode) {
        this.fileMode = fileMode;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public FilePermission getPermission() {
        return permission;
    }

    public void setPermission(FilePermission permission) {
        this.permission = permission;
    }

    @Override
    public String toString() {
        return "ConfigItemSpec{"
                + "name='" + name + '\''
                + ", path='" + path + '\''
                + ", fileType='" + fileType + '\''
                + ", fileMode='" + fileMode + '\''
                + ", memo='" + memo + '\''
                + ", permission=" + permission
                + '}';
    }
}
