package com.tencent.bscp.sdk;

import com.tencent.bscp.api.ClientApi;
import com.tencent.bscp.helper.OptionHelper;
import com.tencent.bscp.pojo.AppOption;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WatchFileTest extends BSCPTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    CountDownLatch latch = new CountDownLatch(1);

    /**
     * Test the watchAppRelease method
     */
    @ParameterizedTest
    @ValueSource(
            ints = {0}
    )
    public void execute(int input) throws Exception {
        LOGGER.info("start {}", input);
        // 注册终止信号处理器
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Received termination signal");
            latch.countDown(); // 释放阻塞
        }));
        TestConfigItem configItem = config.getTest().getWatchFile().get(input);
        LOGGER.isDebugEnabled();

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

        watchAppRelease(bscp, configItem.getApp(), opts);
    }

    public void watchAppRelease(ClientApi bscp, String app, List<AppOption> opts) throws Exception {
        bscp.addWatcher(release -> {
            // 文件列表, 可以自定义操作，如查看content, 写入文件等
            LOGGER.info("get event done. releaseID {}, items {}.", release.getReleaseID(), release.getFileItems());

//            latch.countDown(); // 释放阻塞, 程序结束
        }, app, opts.toArray(new AppOption[0]));
        bscp.startWatch();
        LOGGER.info("watch start wait");
        latch.await(); // 阻塞等待终止信号
        bscp.stopWatch();
    }
}
