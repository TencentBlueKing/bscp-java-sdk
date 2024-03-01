package com.tencent.bscp.sdk;

import com.tencent.bscp.api.ClientApi;
import com.tencent.bscp.helper.OptionHelper;
import com.tencent.bscp.pojo.AppOption;
import com.tencent.bscp.pojo.ConfigItemFile;
import com.tencent.bscp.pojo.Release;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class PullFileTest extends BSCPTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Test the pullFiles method
     */
    @ParameterizedTest
    @ValueSource(
            ints = {0}
    )
    public void execute(int input) {
        LOGGER.info("start {}", input);
        LOGGER.isDebugEnabled();

        TestConfigItem configItem = config.getTest().getPullFile().get(input);
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
}
