package com.tencent.bscp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tencent.bscp.pbbase.Base;
import com.tencent.bscp.pbkv.Kvs;

public class KvMetaV1 {
    @JsonProperty("id")
    private int id;
    private String key;
    @JsonProperty("kv_type")
    private String kvType;
    private Revision revision;
    @JsonProperty("kv_attachment")
    private KvAttachment kvAttachment;

    public KvMetaV1(String key, String kvType, Base.Revision revision, Kvs.KvAttachment kvAttachment) {
        this.key = key;
        this.kvType = kvType;
        this.revision = new Revision(revision.getCreator(), revision.getReviser(), revision.getCreateAt(),
                                     revision.getUpdateAt()
        );
        this.kvAttachment = new KvAttachment(kvAttachment.getBizId(), kvAttachment.getAppId());
    }

    public KvMetaV1() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKvType() {
        return kvType;
    }

    public void setKvType(String kvType) {
        this.kvType = kvType;
    }

    public Revision getRevision() {
        return revision;
    }

    public void setRevision(Revision revision) {
        this.revision = revision;
    }

    public KvAttachment getKvAttachment() {
        return kvAttachment;
    }

    public void setKvAttachment(KvAttachment kvAttachment) {
        this.kvAttachment = kvAttachment;
    }
}
