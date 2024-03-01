package com.tencent.bscp.pojo;

import java.util.ArrayList;
import java.util.List;

public class ReleaseEventMetaV1 {
    private int appID;
    private String app;
    private int releaseID;
    private List<ConfigItemMetaV1> ciMetas;
    private List<KvMetaV1> kvMetas;
    private RepositoryV1 repository;
    private HookSpec preHook;
    private HookSpec postHook;

    public ReleaseEventMetaV1(
            int appID, String app, int releaseID, List<ConfigItemMetaV1> ciMetas,
            List<KvMetaV1> kvMetas, RepositoryV1 repository, HookSpec preHook,
            HookSpec postHook
    ) {
        this.appID = appID;
        this.app = app;
        this.releaseID = releaseID;
        this.ciMetas = ciMetas;
        this.kvMetas = kvMetas;
        this.repository = repository;
        this.preHook = preHook;
        this.postHook = postHook;
    }

    public ReleaseEventMetaV1() {
    }

    public int getAppID() {
        return appID;
    }

    public String getApp() {
        return app;
    }

    public int getReleaseID() {
        return releaseID;
    }

    public List<ConfigItemMetaV1> getCiMetas() {
        return ciMetas == null ? new ArrayList<>() : ciMetas;
    }

    public List<KvMetaV1> getKvMetas() {
        return kvMetas;
    }

    public RepositoryV1 getRepository() {
        return repository;
    }

    public HookSpec getPreHook() {
        return preHook;
    }

    public HookSpec getPostHook() {
        return postHook;
    }
}
