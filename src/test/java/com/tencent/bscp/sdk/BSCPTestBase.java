package com.tencent.bscp.sdk;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tencent.bscp.sdk.mock.MockFeedServiceRun;
import org.junit.jupiter.api.BeforeAll;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class BSCPTestBase {
    private static Thread seedServer;

    protected static Config config;

    @BeforeAll
    public static void before() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        seedServer = new Thread(() -> {
            try {
                MockFeedServiceRun.run(latch);
            } catch (IOException | InterruptedException e) {
                System.out.println("MockFeedServiceRun run fail" + e);
                throw new RuntimeException(e);
            }
        });
        seedServer.start();
        latch.await();
        config = loadConfig();
    }

    public static Config loadConfig() {
        InputStream inputStream = BSCPTestBase.class.getResourceAsStream("/config.yml");
        // 创建 YAML 解析器
        Yaml yaml = new Yaml();
        return yaml.loadAs(inputStream, Config.class);
    }

    public static class Config {
        private LinkedList<String> feedAddrs;
        private int biz;
        private String token;
        private ArrayList<LinkedHashMap<String, String>> labels;
        private String tempDir;
        private TestConfig test;

        public Config() {
        }

        public LinkedList<String> getFeedAddrs() {
            return feedAddrs;
        }

        public void setFeedAddrs(LinkedList<String> feedAddrs) {
            this.feedAddrs = feedAddrs;
        }

        public int getBiz() {
            return biz;
        }

        public void setBiz(int biz) {
            this.biz = biz;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public ArrayList<LinkedHashMap<String, String>> getLabels() {
            return labels;
        }

        public void setLabels(ArrayList<LinkedHashMap<String, String>> labels) {
            this.labels = labels;
        }

        public String getTempDir() {
            return tempDir;
        }

        public void setTempDir(String tempDir) {
            this.tempDir = tempDir;
        }

        public TestConfig getTest() {
            return test;
        }

        public void setTest(TestConfig test) {
            this.test = test;
        }
    }

    public static class TestConfig {
        private LinkedList<TestConfigItem> kvCtl;
        private LinkedList<TestConfigItem> pullFile;
        private LinkedList<TestConfigItem> pullKv;
        private LinkedList<TestConfigItem> watchFile;
        private LinkedList<TestConfigItem> watchKv;

        public TestConfig() {
        }

        public LinkedList<TestConfigItem> getKvCtl() {
            return kvCtl;
        }

        public void setKvCtl(LinkedList<TestConfigItem> kvCtl) {
            this.kvCtl = kvCtl;
        }

        public LinkedList<TestConfigItem> getPullFile() {
            return pullFile;
        }

        public void setPullFile(LinkedList<TestConfigItem> pullFile) {
            this.pullFile = pullFile;
        }

        public LinkedList<TestConfigItem> getPullKv() {
            return pullKv;
        }

        public void setPullKv(LinkedList<TestConfigItem> pullKv) {
            this.pullKv = pullKv;
        }

        public LinkedList<TestConfigItem> getWatchFile() {
            return watchFile;
        }

        public void setWatchFile(LinkedList<TestConfigItem> watchFile) {
            this.watchFile = watchFile;
        }

        public LinkedList<TestConfigItem> getWatchKv() {
            return watchKv;
        }

        public void setWatchKv(LinkedList<TestConfigItem> watchKv) {
            this.watchKv = watchKv;
        }
    }

    public static class TestConfigItem {
        private Boolean watchMode;
        private String keys;
        private String key;
        private String uid;
        private String app;
        private LinkedHashMap<String, String> labels;

        public TestConfigItem() {
        }

        public Boolean getWatchMode() {
            return watchMode;
        }

        public void setWatchMode(Boolean watchMode) {
            this.watchMode = watchMode;
        }

        public String getKeys() {
            return keys;
        }

        public void setKeys(String keys) {
            this.keys = keys;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getApp() {
            return app;
        }

        public void setApp(String app) {
            this.app = app;
        }

        public LinkedHashMap<String, String> getLabels() {
            return labels;
        }

        public void setLabels(LinkedHashMap<String, String> labels) {
            this.labels = labels;
        }
    }
}
