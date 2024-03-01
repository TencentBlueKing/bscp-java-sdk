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

public class PullKVTest extends BSCPTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Test the pullAppKvs method
     */
    @ParameterizedTest
    @ValueSource(
            ints = {0}
    )
    public void execute(int input) {
        LOGGER.info("start {}", input);
        TestConfigItem configItem = config.getTest().getPullKv().get(input);
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

        pullAppKvs(bscp, configItem.getApp(), configItem.getKey(), opts);
    }

    public void pullAppKvs(ClientApi bscp, String app, String key, List<AppOption> opts) {
        try {
            String value = bscp.get(app, key, opts.toArray(new AppOption[0]));

            LOGGER.info("get value done. key {}, value {}.", key, value);
        } catch (Exception e) {
            LOGGER.error("Error pulling app files", e);
        }
    }
}
