package com.tencent.bscp.service;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

// NewRetryPolicy create a new retry policy.
// Note:
//  1. immuneCount is the count which the sleep time is constant at: retryCount * sleepRangeMS[0] * time.Millisecond.
//  2. if the retry times > immuneCount, then the sleep time will be the value calculated bellow in milliseconds.
//     sleepTime = sleepRangeMS[0] + randomValueBetween(sleepRangeMS[0], sleepRangeMS[1])
//  3. both immuneCount and sleepRangeMS value should all be > 0, if not, the default value will be used.
public class RetryPolicyService {
    // default retry sleep random milliseconds range.
    private static final int[] defaultRangeMillSeconds = { 1000, 15000 };
    // default immune count for retry policy
    private static final int defaultImmuneCount = 5;

    // when retry count less than this, retry with a immune policy.
    // default value is defaultImmuneCount
    private int immuneCount;
    // to generate a random time between this range in million seconds
    // default value is defaultRangeMillSeconds
    private int[] rangeMillSeconds;
    private AtomicInteger retryCount;

    public RetryPolicyService(int immuneCount, int[] rangeMillSeconds) {
        this.immuneCount = immuneCount > 0 ? immuneCount : defaultImmuneCount;
        this.rangeMillSeconds = (rangeMillSeconds[0] > 0 || rangeMillSeconds[1] > 0) ? rangeMillSeconds
                : defaultRangeMillSeconds;
        this.retryCount = new AtomicInteger(0);
    }

    private void sleep() throws InterruptedException {
        int currentRetryCount = retryCount.getAndIncrement();

        if (currentRetryCount == 0) {
            return;
        }

        if (currentRetryCount <= immuneCount) {
            int duration = currentRetryCount * rangeMillSeconds[0];
            Thread.sleep(duration);
            return;
        }

        Random rand = new Random(System.nanoTime());
        int randTime = rand.nextInt(rangeMillSeconds[1] - rangeMillSeconds[0]) + rangeMillSeconds[0];
        int duration = rangeMillSeconds[0] + randTime;
        Thread.sleep(duration);
    }

    public boolean shouldContinue() throws InterruptedException {
        sleep();
        return true;
    }

    public int getRetryCount() {
        return retryCount.get();
    }

    public void reset() {
        retryCount.set(0);
    }
}
