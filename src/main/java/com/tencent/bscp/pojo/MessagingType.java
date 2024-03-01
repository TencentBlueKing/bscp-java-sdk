package com.tencent.bscp.pojo;

/**
 * SidecarOffline means the sidecar is shutting down or something happens, to tell feed server this sidecar is offline.
 * Heartbeat means the sidecar is online, to tell feed server this sidecar is live.
 */
public enum MessagingType {
    SidecarOffline,
    Heartbeat;

    public void validate() throws Exception {
        switch (this) {
            case SidecarOffline:
            case Heartbeat:
                break;
            default:
                throw new Exception("Unknown " + this.name() + " sidecar message type");
        }
    }

    public int toInt() throws Exception {
        switch (this) {
            case SidecarOffline:
                return 1;
            case Heartbeat:
                return 2;
            default:
                throw new Exception("Unknown " + this.name() + " sidecar message type");
        }
    }
}
