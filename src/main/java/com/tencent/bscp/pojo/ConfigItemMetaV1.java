package com.tencent.bscp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tencent.bscp.pbci.ConfigItemOuterClass;
import com.tencent.bscp.pbcommit.CommitOuterClass;
import com.tencent.bscp.pbcontent.ContentOuterClass;
import com.tencent.bscp.pbfs.FileMeta;

public class ConfigItemMetaV1 {
    private int id;
    @JsonProperty("commentID")
    private int commitID;
    private ContentSpec contentSpec;
    private ConfigItemSpec configItemSpec;
    private ConfigItemAttachment configItemAttachment;
    private String repositoryPath;

    /**
     * Constructor for ConfigItemMetaV1
     * @param id The ID of the config item
     * @param commitID The commit ID of the config item
     * @param contentSpec The content spec of the config item
     * @param configItemSpec The config item spec of the config item
     * @param configItemAttachment The config item attachment of the config item
     * @param repositoryPath The repository path of the config item content
     */
    public ConfigItemMetaV1(
            int id,
            int commitID,
            ContentOuterClass.ContentSpec contentSpec,
            ConfigItemOuterClass.ConfigItemSpec configItemSpec,
            ConfigItemOuterClass.ConfigItemAttachment configItemAttachment,
            String repositoryPath
    ) {
        this.id = id;
        this.commitID = commitID;
        this.contentSpec = new ContentSpec(contentSpec.getSignature(), contentSpec.getByteSize());

        FilePermission permission = new FilePermission(
                configItemSpec.getPermission().getUser(), configItemSpec.getPermission().getUserGroup(),
                configItemSpec.getPermission().getPrivilege()
        );
        this.configItemSpec = new ConfigItemSpec(configItemSpec.getName(), configItemSpec.getPath(),
                                                 configItemSpec.getFileType(), configItemSpec.getFileMode(),
                                                 configItemSpec.getMemo(), permission
        );
        this.configItemAttachment = new ConfigItemAttachment(
                configItemAttachment.getBizId(),
                configItemAttachment.getAppId()
        );
        this.repositoryPath = repositoryPath;
    }

    public ConfigItemMetaV1() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCommitID() {
        return commitID;
    }

    public void setCommitID(int commitID) {
        this.commitID = commitID;
    }

    public ContentSpec getContentSpec() {
        return contentSpec;
    }

    public void setContentSpec(ContentSpec contentSpec) {
        this.contentSpec = contentSpec;
    }

    public ConfigItemSpec getConfigItemSpec() {
        return configItemSpec;
    }

    public void setConfigItemSpec(ConfigItemSpec configItemSpec) {
        this.configItemSpec = configItemSpec;
    }

    public ConfigItemAttachment getConfigItemAttachment() {
        return configItemAttachment;
    }

    public void setConfigItemAttachment(ConfigItemAttachment configItemAttachment) {
        this.configItemAttachment = configItemAttachment;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    /**
     * Get the FileMeta of the ConfigItemMetaV1
     * @return The FileMeta of the ConfigItemMetaV1
     */
    public FileMeta getFileMeta() {
        return FileMeta.newBuilder().setId(id).setCommitId(commitID).setCommitSpec(
                        CommitOuterClass.CommitSpec.newBuilder().setContent(
                                ContentOuterClass.ContentSpec.newBuilder().setSignature(contentSpec.getSignature())
                                        .setByteSize(contentSpec.getByteSize())
                                        .build()
                        ).build()
                ).setConfigItemSpec(ConfigItemOuterClass.ConfigItemSpec.newBuilder()
                                            .setName(configItemSpec.getName())
                                            .setPath(configItemSpec.getPath())
                                            .setFileType(configItemSpec.getFileType())
                                            .setFileMode(configItemSpec.getFileMode())
                                            .setMemo(configItemSpec.getMemo())
                                            .setPermission(
                                                    ConfigItemOuterClass.FilePermission.newBuilder()
                                                            .setUser(configItemSpec.getPermission().getUser())
                                                            .setUserGroup(configItemSpec.getPermission().getUserGroup())
                                                            .setPrivilege(configItemSpec.getPermission().getPrivilege())
                                                            .build()
                                            ).build()
                )
                .setConfigItemAttachment(
                        ConfigItemOuterClass.ConfigItemAttachment.newBuilder()
                                .setBizId(configItemAttachment.getBizId())
                                .setAppId(configItemAttachment.getAppId()).build()
                ).build();
    }

    @Override
    public String toString() {
        return "ConfigItemMetaV1{"
        + "ID=" + id
        + ", commitID=" + commitID
        + ", contentSpec=" + contentSpec
        + ", configItemSpec=" + configItemSpec
        + ", configItemAttachment=" + configItemAttachment
        + ", repositoryPath='" + repositoryPath
        + '\'' + '}';
    }
}
