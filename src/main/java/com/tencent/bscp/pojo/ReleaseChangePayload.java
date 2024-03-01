package com.tencent.bscp.pojo;

// ReleaseChangePayload defines the details when the sidecar's app instance's related
// release has been changed.
public class ReleaseChangePayload {
    private ReleaseEventMetaV1 releaseMeta;
    private InstanceSpec instance;
    private int cursorID;

    public ReleaseChangePayload() {
    }

    public ReleaseChangePayload(ReleaseEventMetaV1 releaseMeta, InstanceSpec instance, int cursorID) {
        this.releaseMeta = releaseMeta;
        this.instance = instance;
        this.cursorID = cursorID;
    }

    public ReleaseEventMetaV1 getReleaseMeta() {
        return releaseMeta;
    }

    public InstanceSpec getInstance() {
        return instance;
    }

    public int getCursorID() {
        return cursorID;
    }
}
