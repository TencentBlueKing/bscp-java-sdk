package com.tencent.bscp.helper;

import com.tencent.bscp.pojo.Subscriber;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Histogram;

public class MetricsHelper {
    private static final String NAMESPACE = "bscp_java";

    public static final Counter RELEASE_CHANGE_CALLBACK_COUNTER = Counter.builder()
            .name("total_release_change_callback_count")
            .help("the total release change count to callback the release change event")
            .labelNames("app", "status", "release")
            .register();

    public static final Histogram RELEASE_CHANGE_CALLBACK_HANDING_SECOND = Histogram.builder()
            .name("release_change_callback_handing_second")
            .help("the handing time(seconds) of release change callback")
            .classicUpperBounds(1, 2, 5, 10, 30, 60, 120, 300, 600, 1800, 3600)
            .labelNames("app", "status", "release")
            .register();

    public static void reportReleaseChangeCallbackMetrics(Subscriber subscriber, String status, long start) {
        String releaseID = String.valueOf(subscriber.getCurrentReleaseID());
        RELEASE_CHANGE_CALLBACK_COUNTER.labelValues(subscriber.getApp(), status, releaseID).inc();
        double seconds = (System.currentTimeMillis() - start) / 1000.0;
        RELEASE_CHANGE_CALLBACK_HANDING_SECOND.labelValues(subscriber.getApp(), status, releaseID).observe(seconds);
    }
}
