import com.tencent.bscp.api.ClientApi;
import com.tencent.bscp.helper.OptionHelper;
import com.tencent.bscp.pojo.AppOption;
import com.tencent.bscp.pojo.ConfigItemFile;
import com.tencent.bscp.pojo.Release;
import com.tencent.bscp.sdk.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class PullFileTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public void execute() {
        LOGGER.isDebugEnabled();
        Config config = loadConfig();

        TestConfigItem configItem = config.getConfig();
        ClientApi bscp;
        try {
            bscp = new Client(
                    OptionHelper.withFeedAddrs(config.getFeedAddrs()),
                    OptionHelper.withBizID(config.getBiz()),
                    OptionHelper.withToken(config.getToken())
            );
        } catch (Exception e) {
            LOGGER.error("init client failed", e);
            System.exit(1);
            return;
        }
        List<AppOption> opts = new ArrayList<>();

        pullAppFiles(bscp, configItem.getApp(), opts);
    }

    public void pullAppFiles(ClientApi bscp, String app, List<AppOption> opts) {
        try {
            Release release = bscp.pullFiles(app, opts.toArray(new AppOption[0]));

            // 文件列表, 可以自定义操作，如查看content, 写入文件等
            for (ConfigItemFile f : release.getFileItems()) {
                LOGGER.info("get event done. release id {}, item {}.", release.getReleaseID(), f);
            }
        } catch (Exception e) {
            LOGGER.error("Error pulling app files", e);
        }
    }

    public static Config loadConfig() {
        InputStream inputStream = PullFileTest.class.getResourceAsStream("/config.yml");
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
        private TestConfigItem config;

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

        public TestConfigItem getConfig() {
            return config;
        }

        public void setConfig(TestConfigItem config) {
            this.config = config;
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
