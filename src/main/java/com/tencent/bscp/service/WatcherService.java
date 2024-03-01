package com.tencent.bscp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tencent.bscp.api.UpstreamApi;
import com.tencent.bscp.helper.CacheHelper;
import com.tencent.bscp.helper.Helper;
import com.tencent.bscp.helper.JsonHelper;
import com.tencent.bscp.helper.MetricsHelper;
import com.tencent.bscp.pbfs.FeedWatchMessage;
import com.tencent.bscp.pojo.AppOption;
import com.tencent.bscp.pojo.AppOptions;
import com.tencent.bscp.pojo.Callback;
import com.tencent.bscp.pojo.ConfigItemFile;
import com.tencent.bscp.pojo.ConfigItemMetaV1;
import com.tencent.bscp.pojo.FeedMessageType;
import com.tencent.bscp.pojo.HeartbeatPayload;
import com.tencent.bscp.pojo.MessagingType;
import com.tencent.bscp.pojo.Options;
import com.tencent.bscp.pojo.Release;
import com.tencent.bscp.pojo.ReleaseChangeEvent;
import com.tencent.bscp.pojo.ReleaseChangePayload;
import com.tencent.bscp.pojo.SideAppMeta;
import com.tencent.bscp.pojo.SideWatchPayload;
import com.tencent.bscp.pojo.SidecarMetaHeader;
import com.tencent.bscp.pojo.Subscriber;
import com.tencent.bscp.pojo.Vas;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.tencent.bscp.pojo.Vas.SemaphoreSize;

public class WatcherService {

    private List<Subscriber> subscribers;
    private Vas vas;
    private Options opts;
    private String metaHeaderValue;
    private UpstreamApi upstream;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Timer timer;

    private StreamObserver<FeedWatchMessage> watchStream;

    // Constructor
    public WatcherService() {
        // Initialize member variables
    }

    public WatcherService(UpstreamApi u, Options opts) throws JsonProcessingException {
        this.upstream = u;
        this.opts = opts;
        SidecarMetaHeader mh = new SidecarMetaHeader(opts.getBizID(), opts.getFingerprint());
        this.metaHeaderValue = JsonHelper.serialize(mh);
        this.subscribers = new ArrayList<>();
    }

    public Vas buildVas() {
        Map<String, String> pairs = new HashMap<>();
        // add finger printer
        pairs.put(Helper.SIDECAR_META_KEY, metaHeaderValue);
        return Helper.buildVas(pairs);
    }

    /**
     * Start the watch service
     * 
     * @throws Exception if the watch service cannot be started
     */
    public void startWatch() throws Exception {
        vas = buildVas();

        List<SideAppMeta> apps = new ArrayList<>();
        for (Subscriber subscriber : subscribers) {
            SideAppMeta appMeta = new SideAppMeta(
                    subscriber.getApp(), subscriber.getUid(), subscriber.getLabels(), subscriber.getCurrentReleaseID());
            appMeta.setCurrentCursorID(0);
            apps.add(appMeta);
        }

        SideWatchPayload payload = new SideWatchPayload();
        payload.setBizID(opts.getBizID());
        payload.setApplications(apps);

        String payloadJson = JsonHelper.serialize(payload);

        byte[] bytes = payloadJson.getBytes(StandardCharsets.UTF_8);

        watchStream = new StreamObserver<FeedWatchMessage>() {
            @Override
            public void onNext(FeedWatchMessage value) {
                try {
                    vas.wgAdd(1);
                    doMessage(value);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    vas.wgDone(1);
                }
            }

            @Override
            public void onError(Throwable t) {
                try {
                    doError(Status.fromThrowable(t));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onCompleted() {

            }
        };

        upstream.watch(vas, bytes, watchStream);
        // waitForReconnectSignal();

        loopHeartbeat();
    }

    public Subscriber subscribe(Callback callback, String app, AppOption... opts) {
        AppOptions options = new AppOptions();
        for (AppOption opt : opts) {
            opt.apply(options);
        }
        if (options.getUid() == null || options.getUid().isEmpty()) {
            options.setUid(this.opts.getFingerprint());
        }
        Map<String, String> mergedLabels = Helper.mergeLabels(this.opts.getLabels(), options.getLabels());
        Subscriber subscriber = new Subscriber(options, app, callback, 0, mergedLabels, options.getUid());
        subscribers.add(subscriber);
        return subscriber;
    }

    public List<Subscriber> getSubscribers() {
        return subscribers;
    }

    private void loopHeartbeat() {
        List<SideAppMeta> apps = new ArrayList<>();
        for (Subscriber subscriber : subscribers) {
            apps.add(new SideAppMeta(
                    subscriber.getApp(),
                    subscriber.getUid(),
                    subscriber.getLabels(),
                    subscriber.getCurrentReleaseID()));
        }

        HeartbeatPayload heartbeatPayload = new HeartbeatPayload(opts.getFingerprint(), apps);
        byte[] payload;
        try {
            payload = JsonHelper.serializeByte(heartbeatPayload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        LOGGER.info("stream start loop heartbeat");

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                LOGGER.info("stream will heartbeat");

                try {
                    heartbeatOnce(vas, MessagingType.Heartbeat, payload);
                } catch (Exception e) {
                    LOGGER.warn("stream heartbeat failed, notify reconnect upstream. err {}, rid {}.", e, vas.getRid());
                    reconnect("stream heartbeat failed");
                    timer.cancel();
                }
            }
        }, 0, Helper.DEFAULT_HEARTBEAT_INTERVAL);
    }

    private void heartbeatOnce(Vas vas, MessagingType msgType, byte[] payload) throws Exception {
        RetryPolicyService retry = new RetryPolicyService(Helper.MAX_HEARTBEAT_RETRY_COUNT, new int[] { 1000, 3000 });
        Exception lastErr = null;

        while (retry.shouldContinue()) {
            if (retry.getRetryCount() == Helper.MAX_HEARTBEAT_RETRY_COUNT) {
                assert lastErr != null;
                throw lastErr;
            }

            try {
                sendHeartbeatMessaging(vas, msgType, payload);
                return;
            } catch (Exception e) {
                LOGGER.error("send heartbeat message failed. retry_count {}, err {}, rid {}.",
                        retry.getRetryCount(), e, vas.getRid());
                lastErr = e;
            }
        }
    }

    private void sendHeartbeatMessaging(Vas vas, MessagingType msgType, byte[] payload) throws Exception {
        Vas timeoutVas = new Vas(vas.getRid(), vas.getCtx());
        upstream.messaging(timeoutVas, msgType, payload);
    }

    private void doMessage(FeedWatchMessage event) throws InterruptedException {
        LOGGER.info("received upstream event, apiVersion {}, payload {}, rid {}.", event.getApiVersion(),
                event.getPayload(), event.getRid());

        if (!Helper.checkAPIVersionMatch(event.getApiVersion())) {
            LOGGER.error("watch stream received incompatible event, version {}, rid {}.", event.getApiVersion(),
                    event.getRid());
            return;
        }

        switch (FeedMessageType.parse(event.getType())) {
            case Bounce:
                LOGGER.info(
                        "received upstream bounce request, need to reconnect upstream server, rid {}.", event.getRid());
                vas.wgDone(1);
                reconnect("received bounce request");
                vas.wgAdd(1);
                return;

            case PublishRelease:
                LOGGER.info("received upstream publish release event, rid <{}>.", event.getRid());
                ReleaseChangeEvent change = new ReleaseChangeEvent(
                        event.getRid(),
                        event.getApiVersion(),
                        event.getPayload().toByteArray());

                CacheHelper c = CacheHelper.INSTANCE;
                if (c != null) {
                    new Thread(() -> c.onReleaseChange(change)).start();
                }
                new Thread(() -> onReleaseChange(change)).start();
                break;
            default:
                LOGGER.error("watch stream received unsupported event, skip. type {}, rid {}.",
                        event.getType(), event.getRid());
        }
    }

    private void onReleaseChange(ReleaseChangeEvent event) {
        ReleaseChangePayload pl;
        try {
            pl = JsonHelper.deserialize(event.getPayload(), ReleaseChangePayload.class);
        } catch (IOException e) {
            LOGGER.error("decode release change event payload failed, skip the event. err {}, rid {}.",
                    e, event.getRid());
            return;
        }

        for (Subscriber subscriber : subscribers) {
            if (subscriber.getApp().equals(pl.getInstance().getApp())
                    && subscriber.getUid().equals(pl.getInstance().getUid())
                    && subscriber.getLabels().equals(pl.getInstance().getLabels())
                    && subscriber.getCurrentReleaseID() != pl.getReleaseMeta().getReleaseID()) {

                subscriber.setCurrentReleaseID(pl.getReleaseMeta().getReleaseID());

                resetConfigItems(subscriber, pl.getReleaseMeta().getCiMetas());

                List<ConfigItemFile> configItemFiles = new ArrayList<>();
                for (ConfigItemMetaV1 ci : pl.getReleaseMeta().getCiMetas()) {
                    ConfigItemFile configItemFile = new ConfigItemFile(
                            ci.getConfigItemSpec().getName(),
                            ci.getConfigItemSpec().getPath(),
                            ci.getConfigItemSpec().getPermission(),
                            ci);
                    configItemFiles.add(configItemFile);
                }

                Release release = new Release(
                        pl.getReleaseMeta().getReleaseID(),
                        configItemFiles,
                        pl.getReleaseMeta().getKvMetas(),
                        pl.getReleaseMeta().getPreHook(),
                        pl.getReleaseMeta().getPostHook());

                long start = System.currentTimeMillis();
                try {
                    subscriber.getCallback().apply(release);
                    MetricsHelper.reportReleaseChangeCallbackMetrics(subscriber, "success", start);
                } catch (Exception e) {
                    LOGGER.error("execute watch callback failed, app {}，err {}.", subscriber.getApp(), e);
                    MetricsHelper.reportReleaseChangeCallbackMetrics(subscriber, "failed", start);
                }
            }
        }
    }

    private void resetConfigItems(Subscriber subscriber, List<ConfigItemMetaV1> cis) {
        Map<String, Integer> m = new HashMap<>();
        for (ConfigItemMetaV1 ci : cis) {
            m.put(ci.getConfigItemSpec().getName(), ci.getCommitID());
        }
        subscriber.setCurrentConfigItems(m);
    }

    private void doError(Status status) throws InterruptedException {
        if (status.getCode() == Status.Code.UNAVAILABLE) {
            LOGGER.error("watch stream has been closed by remote upstream stream server, need to re-connect again");
            reconnect("connection is closed by remote upstream server");
        } else {
            LOGGER.error("watch stream is corrupted error {}", status);
            Thread.sleep(100);
            reconnect("watch stream corrupted");
        }
    }

    public void reconnect(String reason) {
        LOGGER.info("received reconnect signal <{}>, rid <{}>.", reason, vas.getRid());
        try {
            stopWatch();
            tryReconnect(vas.getRid());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopWatch() {
        long st = System.currentTimeMillis();
        try {
            vas.wgWait();
            watchStream.onCompleted();
            timer.cancel();
        } catch (Exception e) {
            LOGGER.warn("wg wait fail", e);
        } finally {
            vas.wgDone(SemaphoreSize);
        }
        LOGGER.info("stop watch done, rid {}, duration {}.", vas.getRid(), System.currentTimeMillis() - st);
    }

    private void tryReconnect(String rid) throws InterruptedException {
        long st = System.currentTimeMillis(); // NOCC:VariableDeclarationUsageDistance(设计如此)
        LOGGER.info("start to reconnect the upstream server, rid {}", vas.getRid());

        RetryPolicyService retry = new RetryPolicyService(5, new int[] { 500, 15000 });

        while (retry.shouldContinue()) {
            String subRid = rid + Long.toString(retry.getRetryCount());

            try {
                upstream.reconnectUpstreamServer();
                LOGGER.info("reconnect upstream server success, rid {}", subRid);
                break;
            } catch (Exception e) {
                LOGGER.error("reconnect upstream server failed, rid {}, error {}", subRid, e);
            }
        }

        while (retry.shouldContinue()) {
            String subRid = rid + Long.toString(retry.getRetryCount());

            try {
                startWatch();
                LOGGER.info("re-watch stream success, rid {}", subRid);
                break;
            } catch (Exception e) {
                LOGGER.error("re-watch stream failed, rid {}, error {}.", subRid, e);
            }
        }

        LOGGER.info("reconnect and re-watch the upstream server done. rid {}, duration {}.",
                rid, System.currentTimeMillis() - st);
    }
}
