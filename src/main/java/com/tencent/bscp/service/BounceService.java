package com.tencent.bscp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BounceService {
    private Runnable reconnectFunc;
    private AtomicInteger intervalHour;
    private AtomicBoolean state;
    public static int defaultBounceIntervalHour = 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public BounceService(Runnable reconnectFunc) {
        this.reconnectFunc = reconnectFunc;
        this.intervalHour = new AtomicInteger(defaultBounceIntervalHour);
        this.state = new AtomicBoolean(false);
    }

    public boolean getState() {
        return state.get();
    }

    public void updateInterval(int intervalHour) {
        this.intervalHour.set(intervalHour);
    }

    /**
     * Enables the bounce effect.
     * 
     * @throws InterruptedException if the current thread is interrupted while
     */
    public void enableBounce() throws InterruptedException {
        if (state.get()) {
            LOGGER.info("Bounce is already enabled. Unable to enable bounce again.");
            return;
        }

        state.set(true);

        while (true) {
            int intervalHour = this.intervalHour.get();

            LOGGER.info("Start waiting for connect bounce, bounce interval: " + intervalHour + " hour(s)");

            try {
                TimeUnit.HOURS.sleep(intervalHour);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            LOGGER.info("Reached the bounce time and start reconnecting to upstream server");

            RetryPolicyService retry = new RetryPolicyService(5, new int[] { 500, 15000 });
            while (retry.shouldContinue()) {
                try {
                    reconnectFunc.run();
                    LOGGER.info("Reconnect to upstream server success.");
                    break;
                } catch (Exception e) {
                    LOGGER.info("Reconnect to upstream server failed: " + e.getMessage());
                }
            }
        }
    }
}
