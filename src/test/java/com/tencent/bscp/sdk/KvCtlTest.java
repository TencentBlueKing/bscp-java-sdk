package com.tencent.bscp.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tencent.bscp.api.ClientApi;
import com.tencent.bscp.helper.JsonHelper;
import com.tencent.bscp.helper.OptionHelper;
import com.tencent.bscp.pojo.AppOption;
import com.tencent.bscp.pojo.Callback;
import com.tencent.bscp.pojo.KvMetaV1;
import com.tencent.bscp.pojo.Release;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class KvCtlTest extends BSCPTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    CountDownLatch latch = new CountDownLatch(1);

    public class Watcher implements Callback {
        private ClientApi bscp;
        private String app;
        private Map<String, String> keyMap;

        public Watcher(ClientApi bscp, String app, Map<String, String> keyMap) {
            this.bscp = bscp;
            this.app = app;
            this.keyMap = keyMap;
        }

        @Override
        public void apply(Release release) {
            Map<String, String> result = new HashMap<>();
            List<String> errKeys = new ArrayList<>();

            for (KvMetaV1 item : release.getKvItems()) {
                if (!keyMap.containsKey(item.getKey()) && keyMap.size() != 0) {
                    continue;
                }

                String value;
                try {
                    value = bscp.get(app, item.getKey());
                } catch (Exception e) {
                    errKeys.add(item.getKey());
                    continue;
                }

                if (keyMap.containsKey(item.getKey()) || keyMap.size() == 0) {
                    result.put(item.getKey(), value);
                }
            }

            if (errKeys.size() > 0) {
                LOGGER.warn("get key failed {}", errKeys);
            }

            // JSON serialization
            try {
                LOGGER.warn("KvCtlTest success!!!!! result {}", JsonHelper.serialize(result));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
//            latch.countDown(); // 释放阻塞
        }
    }

    /**
     * Test method for 'com.tencent.bscp.sdk.KvCtl.execute(int)'
     */
    @ParameterizedTest
    @ValueSource(
            ints = {0}
    )
    public void execute(int input) throws JsonProcessingException {
        LOGGER.info("start {}", input);
        // 注册终止信号处理器
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received termination signal");
            latch.countDown(); // 释放阻塞
        }));
        TestConfigItem configItem = config.getTest().getKvCtl().get(input);
        LOGGER.isDebugEnabled();

        ClientApi bscp;
        try {
            bscp = new Client(
                    OptionHelper.withFeedAddrs(config.getFeedAddrs()),
                    OptionHelper.withBizID(config.getBiz()),
                    OptionHelper.withToken(config.getToken()),
                    OptionHelper.withLabels(configItem.getLabels())
            );
        } catch (Exception e) {
            LOGGER.error("init client failed", e);
            System.exit(1);
            return;
        }
        List<AppOption> opts = new ArrayList<>();

        List<String> keyList = new ArrayList<>();
        if (configItem.getKeys() != null && !configItem.getKeys().isEmpty()) {
            keyList = Arrays.asList(configItem.getKeys().split(","));
        }

        if (configItem.getWatchMode()) {
            try {
                watchAppKV(bscp, configItem.getApp(), keyList, opts);
            } catch (Exception e) {
                LOGGER.error("watch", e);
                System.exit(1);
            }
        } else {
            Map<String, String> result = new HashMap<>();
            if (keyList.isEmpty()) {
                try {
                    Release release = bscp.pullKvs(configItem.getApp(), keyList,
                                                   opts.toArray(new AppOption[0])
                    );
                    if (release.getKvItems().isEmpty()) {
                        LOGGER.error("kv release is empty");
                        System.exit(1);
                    }
                    for (KvMetaV1 kv : release.getKvItems()) {
                        keyList.add(kv.getKey());
                    }
                } catch (Exception e) {
                    LOGGER.error("pull kv failed", e);
                    System.exit(1);
                }
            }

            List<String> errKeys = new ArrayList<>();
            for (String key : keyList) {
                try {
                    String value = bscp.get(configItem.getApp(), key, opts.toArray(new AppOption[0]));
                    result.put(key, value);
                } catch (Exception e) {
                    errKeys.add(key);
                }
            }
            if (!errKeys.isEmpty()) {
                LOGGER.warn("get key failed, keys {}", errKeys);
            }

            System.out.println(JsonHelper.serialize(result));
        }
    }

    public void watchAppKV(ClientApi bscp, String app, List<String> keys, List<AppOption> opts) throws Exception {
        Map<String, String> keyMap = new HashMap<>();
        for (String key : keys) {
            keyMap.put(key, key);
        }

        Watcher watcher = new Watcher(bscp, app, keyMap);

        bscp.addWatcher(watcher, app, opts.toArray(new AppOption[0]));
        bscp.startWatch();
        LOGGER.info("watch start wait");
        latch.await(); // 阻塞等待终止信号
        bscp.stopWatch();
    }
}
