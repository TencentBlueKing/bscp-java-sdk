package com.tencent.bscp.sdk;

import com.tencent.bscp.api.ClientApi;
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
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatchKVTest extends BSCPTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    CountDownLatch latch = new CountDownLatch(1);


    public class Watcher implements Callback {
        private ClientApi bscp;
        private String app;

        public Watcher(ClientApi bscp, String app) {
            this.bscp = bscp;
            this.app = app;
        }

        @Override
        public void apply(Release release) {

            for (KvMetaV1 item : release.getKvItems()) {
                String value;
                try {
                    value = bscp.get(app, item.getKey());
                } catch (Exception e) {
                    LOGGER.error("get value failed. releaseId {}, key {}. error {}.", release.getReleaseID(),
                                 item.getId(), e
                    );
                    continue;
                }
                LOGGER.info("get value success. releaseId {}, key {}. value {}.", release.getReleaseID(),
                            item.getId(), value
                );
            }
//            latch.countDown(); // 释放阻塞, 结束程序
        }
    }

    /**
     * Test the watchAppKV method
     */
    @ParameterizedTest
    @ValueSource(
            ints = {0}
    )
    public void execute(int input) {
        LOGGER.info("start {}", input);
        // 注册终止信号处理器
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received termination signal");
            latch.countDown(); // 释放阻塞
        }));
        TestConfigItem configItem = config.getTest().getWatchKv().get(input);
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
        try {
            watchAppKV(bscp, configItem.getApp(), opts);
        } catch (Exception e) {
            LOGGER.error("watch", e);
            System.exit(1);
        }
    }

    public void watchAppKV(ClientApi bscp, String app, List<AppOption> opts) throws Exception {
        Watcher watcher = new Watcher(bscp, app);

        bscp.addWatcher(watcher, app, opts.toArray(new AppOption[0]));
        bscp.startWatch();
        LOGGER.info("watch start wait");
        latch.await(); // 阻塞等待终止信号
        bscp.stopWatch();
    }
}
