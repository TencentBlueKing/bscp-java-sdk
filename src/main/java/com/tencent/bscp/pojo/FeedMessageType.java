package com.tencent.bscp.pojo;

public enum FeedMessageType {
    Bounce(1),
    PublishRelease(2);

    private final int value;

    FeedMessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static FeedMessageType parse(int value) {
        for (FeedMessageType type : FeedMessageType.values()) {
            if (type.value == value) {
                return type;
            }
        }
        return Bounce;
    }
}
