package com.tencent.bscp.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

// SidecarHandshakePayload defines the options which is returned by feed server
public class SidecarHandshakePayload {
    @JsonProperty("serviceInfo")
    private ServiceInfo serviceInfo;

    @JsonProperty("runtimeOption")
    private SidecarRuntimeOption runtimeOption;

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public SidecarRuntimeOption getRuntimeOption() {
        return runtimeOption;
    }

    public void setRuntimeOption(SidecarRuntimeOption runtimeOption) {
        this.runtimeOption = runtimeOption;
    }

    // ServiceInfo defines the sidecar's need info from the upstream server with handshake.
    public static class ServiceInfo {
        // Name feed server instance name, it is used to determine which service instance sidecar is connected to.
        @JsonProperty("name")
        private String name;
    }

    // SidecarRuntimeOption defines the sidecar's runtime options delivered from the
    // upstream server with handshake.
    public static class SidecarRuntimeOption {
        // BounceIntervalHour sidecar connect bounce interval, if reach this bounce interval, sidecar will
        // reconnect stream server instance.
        @JsonProperty("bounceInterval")
        private int bounceIntervalHour;

        @JsonProperty("repositoryTLS")
        private TLSBytes repositoryTLS;

        @JsonProperty("repository")
        private RepositoryV1 repository;

        @JsonProperty("reload")
        private Map<Integer, Reload> appReloads;

        public int getBounceIntervalHour() {
            return bounceIntervalHour;
        }

        public void setBounceIntervalHour(int bounceIntervalHour) {
            this.bounceIntervalHour = bounceIntervalHour;
        }

        public TLSBytes getRepositoryTLS() {
            return repositoryTLS;
        }

        public void setRepositoryTLS(TLSBytes repositoryTLS) {
            this.repositoryTLS = repositoryTLS;
        }

        public RepositoryV1 getRepository() {
            return repository;
        }

        public void setRepository(RepositoryV1 repository) {
            this.repository = repository;
        }

        public Map<Integer, Reload> getAppReloads() {
            return appReloads;
        }

        public void setAppReloads(Map<Integer, Reload> appReloads) {
            this.appReloads = appReloads;
        }
    }

    // Reload defines the sidecar's notify app to reload config file options delivered from the
    // upstream server with handshake.
    public static class Reload {
        @JsonProperty("reload_type")
        private AppReloadType reloadType;

        @JsonProperty("file_reload_spec")
        private FileReloadSpec fileReloadSpec;
    }

    // AppReloadType is the app's sidecar instance to notify application reload config files way.
    public enum AppReloadType {
        // Define your enum values here
    }

    // FileReloadSpec defines sidecar file reload need info.
    public static class FileReloadSpec {
        @JsonProperty("reload_file_path")
        private String reloadFilePath;
    }
}
