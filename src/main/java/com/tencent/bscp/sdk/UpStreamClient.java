package com.tencent.bscp.sdk;

import com.google.protobuf.ByteString;
import com.tencent.bscp.api.UpstreamApi;
import com.tencent.bscp.helper.Helper;
import com.tencent.bscp.pbbase.Base;
import com.tencent.bscp.pbfs.FeedWatchMessage;
import com.tencent.bscp.pbfs.GetDownloadURLReq;
import com.tencent.bscp.pbfs.GetDownloadURLResp;
import com.tencent.bscp.pbfs.GetKvValueReq;
import com.tencent.bscp.pbfs.GetKvValueResp;
import com.tencent.bscp.pbfs.HandshakeMessage;
import com.tencent.bscp.pbfs.HandshakeResp;
import com.tencent.bscp.pbfs.ListAppsReq;
import com.tencent.bscp.pbfs.ListAppsResp;
import com.tencent.bscp.pbfs.MessagingMeta;
import com.tencent.bscp.pbfs.MessagingResp;
import com.tencent.bscp.pbfs.PullAppFileMetaReq;
import com.tencent.bscp.pbfs.PullAppFileMetaResp;
import com.tencent.bscp.pbfs.PullKvMetaReq;
import com.tencent.bscp.pbfs.PullKvMetaResp;
import com.tencent.bscp.pbfs.SideWatchMeta;
import com.tencent.bscp.pbfs.UpstreamGrpc;
import com.tencent.bscp.pojo.Balancer;
import com.tencent.bscp.pojo.Blocker;
import com.tencent.bscp.pojo.MessagingType;
import com.tencent.bscp.pojo.UpstreamOption;
import com.tencent.bscp.pojo.UpstreamOptions;
import com.tencent.bscp.pojo.Vas;
import com.tencent.bscp.service.BounceService;
import io.grpc.CallCredentials;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * upstreamClient is an implementation of the upstream server's client, it sends to and receive messages from
 * the upstream feed server.
 * Note:
 * 1. it also hijacked the connections to upstream server so that it can
 * do reconnection, bounce work and so on.
 * 2. it blocks the request until the connections to the upstream server go back to normal when the connection
 * is unavailable.
 */
public class UpStreamClient implements UpstreamApi, Closeable {
    public static int DefaultDialTimeoutMS = 2000;
    private final UpstreamOptions options;
    private final Base.Versioning sidecarVer;
    private final Blocker wait;
    private final Balancer lb;
    private final BounceService bounceService;
    private ManagedChannel channel;
    private UpstreamGrpc.UpstreamBlockingStub blockingStub;

    private UpstreamGrpc.UpstreamStub asyncStub;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * UpStreamClient creates a new instance of the upstream client.
     * 
     * @param options the options to create the upstream client.
     * 
     * @throws Exception if the upstream client cannot be created.
     */
    public UpStreamClient(UpstreamOption... options) throws Exception {
        this.options = new UpstreamOptions();
        for (UpstreamOption opt : options) {
            opt.apply(this.options);
        }
        if (this.options.getDialTimeoutMS() <= 0) {
            this.options.setDialTimeoutMS(DefaultDialTimeoutMS);
        }
        this.lb = new Balancer(this.options.getFeedAddrs());
        this.wait = new Blocker();
        this.sidecarVer = Base.Versioning.newBuilder()
                .setMajor(Helper.CURRENT_API_VERSION.getMajor())
                .setMinor(Helper.CURRENT_API_VERSION.getMinor())
                .setPatch(Helper.CURRENT_API_VERSION.getPatch())
                .build();

        this.bounceService = new BounceService(() -> {
            try {
                reconnectUpstreamServer();
            } catch (Exception e) {
                LOGGER.warn("reconnectUpstreamServer failed.");
            }
        });

        try {
            dial();
        } catch (Exception e) {
            LOGGER.warn("dial failed.");
            throw e;
        }
        Thread thread = new Thread(this::waitForStateChange);
        thread.start();
    }

    private void dial() throws Exception {
        if (channel != null) {
            channel.shutdownNow();
            if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                throw new Exception("Failed to close the previous connection");
            }
        }

        long timeout = options.getDialTimeoutMS();

        String endpoint = lb.pickOne();
        ManagedChannel channel = NettyChannelBuilder.forTarget(endpoint)
                .userAgent("bscp-sdk-java")
                .usePlaintext()
                .build();

        this.channel = channel;
        CallCredentials credentials = new CallCredentials() {
            @Override
            public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
                applier.apply(options.getVas().getCtx());
            }
        };
        this.blockingStub = UpstreamGrpc.newBlockingStub(channel).withCallCredentials(credentials);
        this.asyncStub = UpstreamGrpc.newStub(channel).withCallCredentials(credentials);

        LOGGER.info("Dial upstream server success, upstream:" + endpoint);
    }

    private void waitForStateChange() {
        while (true) {
            ConnectivityState currentState = channel.getState(true);
            if (currentState == ConnectivityState.READY) {
                // TODO: Implement the logic to handle the ready state
                // You can loop and wait here, and then determine whether you need to create a
                // new connection
            }
        }
    }

    @Override
    public void reconnectUpstreamServer() throws Exception {
        if (!wait.tryBlock()) {
            LOGGER.warn(
                    "Received reconnect to upstream server request, but another reconnect is processing, ignore this");
            return;
        }

        try {
            dial();
        } catch (Exception e) {
            LOGGER.error("Reconnect upstream server failed because of " + e.getMessage(), e);
            throw e;
        } finally {
            wait.unBlock();
        }
    }

    // Handshake to the upstream server
    @Override
    public HandshakeResp handshake(Vas vas, HandshakeMessage msg) throws Exception {
        wait.waiting();
        return blockingStub.handshake(msg);
    }

    // Watch release related messages from upstream feed server.
    @Override
    public void watch(Vas vas, byte[] payload, StreamObserver<FeedWatchMessage> resp) throws Exception {
        wait.waiting();
        asyncStub.watch(
                SideWatchMeta.newBuilder()
                        .setApiVersion(Helper.CURRENT_API_VERSION)
                        .setPayload(ByteString.copyFrom(payload))
                        .build(),
                resp);
    }

    // Messaging is a message pipeline to send message to the upstream feed server.
    @Override
    public MessagingResp messaging(Vas vas, MessagingType type, byte[] payload) throws Exception {
        wait.waiting();
        type.validate();
        return blockingStub.messaging(MessagingMeta.newBuilder()
                .setApiVersion(Helper.CURRENT_API_VERSION)
                .setRid(vas.getRid())
                .setType(type.toInt())
                .setPayload(ByteString.copyFrom(payload))
                .build());
    }

    // EnableBounce set conn reconnect interval, and start loop wait connect bounce.
    // call multiple times,
    // you need to wait for the last bounce interval to arrive, the bounce interval
    // of set this time
    // will take effect.
    @Override
    public void enableBounce(int bounceIntervalHour) {
        bounceService.updateInterval(bounceIntervalHour);

        if (!bounceService.getState()) {
            new Thread(() -> {
                try {
                    bounceService.enableBounce();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }

    // PullAppFileMeta pulls the app file meta from upstream feed server.
    @Override
    public PullAppFileMetaResp pullAppFileMeta(Vas vas, PullAppFileMetaReq req) throws Exception {
        wait.waiting();
        return blockingStub.pullAppFileMeta(req);
    }

    // PullKVMeta pulls the app kv meta from upstream feed server.
    @Override
    public PullKvMetaResp pullKvMeta(Vas vas, PullKvMetaReq req) throws Exception {
        wait.waiting();
        return blockingStub.pullKvMeta(req);
    }

    // GetKvValue get the kvs value from upstream feed server.
    @Override
    public GetKvValueResp getKvValue(Vas vas, GetKvValueReq req) throws Exception {
        wait.waiting();
        return blockingStub.getKvValue(req);
    }

    // GetDownloadURL gets the file temp download url from upstream feed server.
    @Override
    public GetDownloadURLResp getDownloadURL(Vas vas, GetDownloadURLReq req) throws Exception {
        wait.waiting();
        return blockingStub.getDownloadURL(req);
    }

    // Version returns the version of the sdk.
    @Override
    public Base.Versioning version() {
        return sidecarVer;
    }

    // ListApps list the apps value from upstream feed server.
    @Override
    public ListAppsResp listApps(Vas vas, ListAppsReq req) throws Exception {
        wait.waiting();
        return blockingStub.listApps(req);
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.shutdownNow();
            try {
                if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                    throw new Exception("Failed to close the previous connection");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
