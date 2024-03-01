package com.tencent.bscp.pojo;

public class HookSpec {
    private String name;
    private String type;
    private String tag;
    private String memo;
    private String content;

    public HookSpec(String name, String type, String tag, String memo, String content) {
        this.name = name;
        this.type = type;
        this.tag = tag;
        this.memo = memo;
        this.content = content;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
