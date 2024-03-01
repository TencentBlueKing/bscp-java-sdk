package com.tencent.bscp.pojo;

import com.tencent.bscp.pbhook.HookOuterClass;

import java.util.List;

public class Release {
    private int releaseID;
    private List<ConfigItemFile> fileItems;
    private List<KvMetaV1> kvItems;
    private HookSpec preHook;
    private HookSpec postHook;

    public Release(
            int releaseID,
            List<ConfigItemFile> fileItems,
            HookOuterClass.HookSpec preHook,
            HookOuterClass.HookSpec postHook
    ) {
        this.releaseID = releaseID;
        this.fileItems = fileItems;
        this.preHook = new HookSpec(preHook.getName(), preHook.getType(), preHook.getTag(), preHook.getMemo(),
                                    preHook.getContent()
        );
        this.postHook = new HookSpec(postHook.getName(), postHook.getType(), postHook.getTag(), postHook.getMemo(),
                                     postHook.getContent()
        );
    }

    public Release(
            int releaseID,
            List<ConfigItemFile> fileItems,
            List<KvMetaV1> kvItems,
            HookSpec preHook,
            HookSpec postHook
    ) {
        this.releaseID = releaseID;
        this.fileItems = fileItems;
        this.kvItems = kvItems;
        this.preHook = preHook;
        this.postHook = postHook;
    }

    public int getReleaseID() {
        return releaseID;
    }

    public void setReleaseID(int releaseID) {
        this.releaseID = releaseID;
    }

    public List<ConfigItemFile> getFileItems() {
        return fileItems;
    }

    public void setFileItems(List<ConfigItemFile> fileItems) {
        this.fileItems = fileItems;
    }

    public List<KvMetaV1> getKvItems() {
        return kvItems;
    }

    public void setKvItems(List<KvMetaV1> kvItems) {
        this.kvItems = kvItems;
    }

    public HookSpec getPreHook() {
        return preHook;
    }

    public void setPreHook(HookSpec preHook) {
        this.preHook = preHook;
    }

    public HookSpec getPostHook() {
        return postHook;
    }

    public void setPostHook(HookSpec postHook) {
        this.postHook = postHook;
    }
}
