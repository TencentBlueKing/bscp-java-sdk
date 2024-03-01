package com.tencent.bscp.api;

import com.tencent.bscp.pbbase.Base;
import com.tencent.bscp.pbfs.*;// NOCC:AvoidStarImport(设计如此)
import com.tencent.bscp.pojo.*;// NOCC:AvoidStarImport(设计如此)
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.Map;

public interface UpstreamApi {
    void reconnectUpstreamServer() throws Exception;

    HandshakeResp handshake(Vas vas, HandshakeMessage msg) throws Exception;

    void watch(Vas vas, byte[] payload, StreamObserver<FeedWatchMessage> resp) throws Exception;

    MessagingResp messaging(Vas vas, MessagingType type, byte[] payload) throws Exception;

    void enableBounce(int bounceIntervalHour);

    PullAppFileMetaResp pullAppFileMeta(Vas vas, PullAppFileMetaReq req) throws Exception;

    PullKvMetaResp pullKvMeta(Vas vas, PullKvMetaReq req) throws Exception;

    GetKvValueResp getKvValue(Vas vas, GetKvValueReq req) throws Exception;

    GetDownloadURLResp getDownloadURL(Vas vas, GetDownloadURLReq req) throws Exception;

    Base.Versioning version();

    ListAppsResp listApps(Vas vas, ListAppsReq req) throws Exception;
}
