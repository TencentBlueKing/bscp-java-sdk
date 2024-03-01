package com.tencent.bscp.pojo;

import com.tencent.bscp.pbci.ConfigItemOuterClass;

public class ConfigItemFile {
    private String name;
    private String path;
    private FilePermission permission;
    private ConfigItemMetaV1 fileMeta;

    public ConfigItemFile(
            String name, String path, ConfigItemOuterClass.FilePermission permission,
            ConfigItemMetaV1 fileMeta) {
        this.name = name;
        this.path = path;
        this.permission = new FilePermission(
                permission.getUser(), permission.getUserGroup(), permission.getPrivilege());
        this.fileMeta = fileMeta;
    }

    public ConfigItemFile(
            String name, String path, FilePermission permission,
            ConfigItemMetaV1 fileMeta) {
        this.name = name;
        this.path = path;
        this.permission = permission;
        this.fileMeta = fileMeta;
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

    public FilePermission getPermission() {
        return permission;
    }

    public void setPermission(FilePermission permission) {
        this.permission = permission;
    }

    public ConfigItemMetaV1 getFileMeta() {
        return fileMeta;
    }

    public void setFileMeta(ConfigItemMetaV1 fileMeta) {
        this.fileMeta = fileMeta;
    }

    @Override
    public String toString() {
        return "ConfigItemFile{"
                + "name='" + name + '\''
                + ", path='" + path + '\''
                + ", permission=" + permission
                + ", fileMeta=" + fileMeta
                + '}';
    }
}
