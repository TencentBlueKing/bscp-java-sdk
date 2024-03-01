package com.tencent.bscp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ConfigType {
    @JsonProperty("kv")
    KV("kv"),
    @JsonProperty("file")
    File("file"),
    @JsonProperty("table")
    Table("table");

    private final String value;

    ConfigType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
