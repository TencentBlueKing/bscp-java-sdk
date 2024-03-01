package com.tencent.bscp.sdk.mock;

import com.google.protobuf.ByteString;
import com.tencent.bscp.pbbase.Base;
import com.tencent.bscp.pbci.ConfigItemOuterClass;
import com.tencent.bscp.pbcommit.CommitOuterClass;
import com.tencent.bscp.pbfs.FeedWatchMessage;
import com.tencent.bscp.pbfs.FileMeta;
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
import com.tencent.bscp.pbfs.RepositorySpec;
import com.tencent.bscp.pbfs.SideWatchMeta;
import com.tencent.bscp.pbfs.UpstreamGrpc;
import io.grpc.stub.StreamObserver;

public class MockFeedServiceImpl extends UpstreamGrpc.UpstreamImplBase {

    @Override
    public void handshake(HandshakeMessage request, StreamObserver<HandshakeResp> responseObserver) {
        HandshakeResp resp = HandshakeResp.newBuilder().setPayload(
                ByteString.copyFromUtf8(
                        "{\"serviceInfo\":{\"name\":\"GJBAvOaT\"},\"runtimeOption\":{\"bounceInterval\":48,\"repositoryTLS\":null,\"repository\":{\"root\":\"\",\"accessKeyId\":\"\",\"secretAccessKey\":\"\",\"url\":\"\"},\"reload\":null}}") // NOCC:LineLength(测试代码)
        ).setApiVersion(Base.Versioning.newBuilder().setMajor(1).build()).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void messaging(MessagingMeta request, StreamObserver<MessagingResp> responseObserver) {
        MessagingResp resp = MessagingResp.newBuilder().build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void watch(SideWatchMeta request, StreamObserver<FeedWatchMessage> responseObserver) {
        while (true) {
            /*ReleaseChangePayload kv example*/
            FeedWatchMessage resp = FeedWatchMessage.newBuilder().setApiVersion(
                            Base.Versioning.newBuilder().setMajor(1).build())
                    .setType(2)
                    .setPayload(
                            ByteString.copyFromUtf8(
                                    "eyJyZWxlYXNlTWV0YSI6IHsiYXBwSUQiOiA1MiwgImFwcCI6ICJkZW1vIiwgInJlbGVhc2VJRCI6IDI0MSwgImNpTWV0YXMiOiBudWxsLCAia3ZNZXRhcyI6IFt7ImlkIjogMjMyMiwgImtleSI6ICJzdHJpbmdfa2V5XzEiLCAia3ZfdHlwZSI6ICJzdHJpbmciLCAicmV2aXNpb24iOiB7ImNyZWF0b3IiOiAiZGVtbyIsICJjcmVhdGVfYXQiOiAiMjAyMy0xMi0xNFQxMDoxNjoxMFoiLCAidXBkYXRlX2F0IjogIjIwMjMtMTItMTRUMTA6MTY6MTBaIn0sICJrdl9hdHRhY2htZW50IjogeyJiaXpfaWQiOiAyLCAiYXBwX2lkIjogNTJ9fSwgeyJpZCI6IDIzMjMsICJrZXkiOiAibnVtYmVyX2tleV8xIiwgImt2X3R5cGUiOiAibnVtYmVyIiwgInJldmlzaW9uIjogeyJjcmVhdG9yIjogImRlbW8iLCAiY3JlYXRlX2F0IjogIjIwMjMtMTItMTRUMTA6MTY6MTBaIiwgInVwZGF0ZV9hdCI6ICIyMDIzLTEyLTE0VDEwOjE2OjEwWiJ9LCAia3ZfYXR0YWNobWVudCI6IHsiYml6X2lkIjogMiwgImFwcF9pZCI6IDUyfX0sIHsiaWQiOiAyMzI0LCAia2V5IjogInRleHRfa2V5XzEiLCAia3ZfdHlwZSI6ICJ0ZXh0IiwgInJldmlzaW9uIjogeyJjcmVhdG9yIjogImRlbW8iLCAiY3JlYXRlX2F0IjogIjIwMjMtMTItMTRUMTA6MTY6MTBaIiwgInVwZGF0ZV9hdCI6ICIyMDIzLTEyLTE0VDEwOjE2OjEwWiJ9LCAia3ZfYXR0YWNobWVudCI6IHsiYml6X2lkIjogMiwgImFwcF9pZCI6IDUyfX1dLCAicmVwb3NpdG9yeSI6IG51bGwsICJwcmVIb29rIjogbnVsbCwgInBvc3RIb29rIjogbnVsbH0sICJpbnN0YW5jZSI6IHsiYml6SUQiOiAyLCAiYXBwSUQiOiA1MiwgImFwcCI6ICJkZW1vIiwgInVpZCI6ICJjZGIwZmNiNGQ4MzcxNTQxMWU5YmEyZmY2NTNhODM1YyIsICJsYWJlbHMiOiB7fSwgImNvbmZpZ1R5cGUiOiAia3YifSwgImN1cnNvcklEIjogMH0=") // NOCC:LineLength(测试代码)
                    ).setRid("ridxxx")
                    .build();
            responseObserver.onNext(resp);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            /*ReleaseChangePayload file example*/
            resp = FeedWatchMessage.newBuilder().setApiVersion(
                            Base.Versioning.newBuilder().setMajor(1).build())
                    .setType(2)
                    .setPayload(
                            ByteString.copyFromUtf8(
                                    "eyJyZWxlYXNlTWV0YSI6IHsiYXBwSUQiOiAyOSwgImFwcCI6ICJkZW1vLWZpbGUiLCAicmVsZWFzZUlEIjogMTIwLCAiY2lNZXRhcyI6IFt7ImlkIjogNDAsICJjb21tZW50SUQiOiAyNywgImNvbnRlbnRTcGVjIjogeyJzaWduYXR1cmUiOiAiODEyNzFiOTQ0MTg4NDA0ZWIwOWM5YTJhMjRkMWNmNWViZmY5YzY0ZmVlNDkyY2U3ZjJlYTEzZjI4MzEyYjZkYiIsICJieXRlX3NpemUiOiAxNX0sICJjb25maWdJdGVtU3BlYyI6IHsibmFtZSI6ICJhLnR4dCIsICJwYXRoIjogIi9ldGMiLCAiZmlsZV90eXBlIjogInRleHQiLCAiZmlsZV9tb2RlIjogInVuaXgiLCAicGVybWlzc2lvbiI6IHsidXNlciI6ICJyb290IiwgInVzZXJfZ3JvdXAiOiAicm9vdCIsICJwcml2aWxlZ2UiOiAiNjQ0In19LCAiY29uZmlnSXRlbUF0dGFjaG1lbnQiOiB7ImJpel9pZCI6IDIsICJhcHBfaWQiOiAyOX0sICJyZXBvc2l0b3J5UGF0aCI6ICIvYnNjcC12MS1iaXotMi9maWxlLzgxMjcxYjk0NDE4ODQwNGViMDljOWEyYTI0ZDFjZjVlYmZmOWM2NGZlZTQ5MmNlN2YyZWExM2YyODMxMmI2ZGIifSwgeyJpZCI6IDQxLCAiY29tbWVudElEIjogMjMsICJjb250ZW50U3BlYyI6IHsic2lnbmF0dXJlIjogImZmYTBkYTVkODg1ZmJhMDlkOTAzYzc4MjcxM2I2YjA5OGM4Y2YyMWY1NmEzYTM1ZDlhYTkyMDYxMzIyMGQyZTEiLCAiYnl0ZV9zaXplIjogNX0sICJjb25maWdJdGVtU3BlYyI6IHsibmFtZSI6ICJiLnR4dCIsICJwYXRoIjogIi9ldGMiLCAiZmlsZV90eXBlIjogInRleHQiLCAiZmlsZV9tb2RlIjogInVuaXgiLCAicGVybWlzc2lvbiI6IHsidXNlciI6ICJyb290IiwgInVzZXJfZ3JvdXAiOiAicm9vdCIsICJwcml2aWxlZ2UiOiAiNjQ0In19LCAiY29uZmlnSXRlbUF0dGFjaG1lbnQiOiB7ImJpel9pZCI6IDIsICJhcHBfaWQiOiAyOX0sICJyZXBvc2l0b3J5UGF0aCI6ICIvYnNjcC12MS1iaXotMi9maWxlL2ZmYTBkYTVkODg1ZmJhMDlkOTAzYzc4MjcxM2I2YjA5OGM4Y2YyMWY1NmEzYTM1ZDlhYTkyMDYxMzIyMGQyZTEifSwgeyJpZCI6IDQyLCAiY29tbWVudElEIjogMjQsICJjb250ZW50U3BlYyI6IHsic2lnbmF0dXJlIjogIjRmZTAwNjE5NjQ3NGJmNDBiMDc4YjVlMjMwY2NmNTU4Zjc5MTEyOTgzNzg4NGNiYzc0ZGFmNzRlZjExNjQ0MjAiLCAiYnl0ZV9zaXplIjogNX0sICJjb25maWdJdGVtU3BlYyI6IHsibmFtZSI6ICJjLnR4dCIsICJwYXRoIjogIi9ldGMiLCAiZmlsZV90eXBlIjogInRleHQiLCAiZmlsZV9tb2RlIjogInVuaXgiLCAicGVybWlzc2lvbiI6IHsidXNlciI6ICJyb290IiwgInVzZXJfZ3JvdXAiOiAicm9vdCIsICJwcml2aWxlZ2UiOiAiNjQ0In19LCAiY29uZmlnSXRlbUF0dGFjaG1lbnQiOiB7ImJpel9pZCI6IDIsICJhcHBfaWQiOiAyOX0sICJyZXBvc2l0b3J5UGF0aCI6ICIvYnNjcC12MS1iaXotMi9maWxlLzRmZTAwNjE5NjQ3NGJmNDBiMDc4YjVlMjMwY2NmNTU4Zjc5MTEyOTgzNzg4NGNiYzc0ZGFmNzRlZjExNjQ0MjAifSwgeyJpZCI6IDQzLCAiY29tbWVudElEIjogMjYsICJjb250ZW50U3BlYyI6IHsic2lnbmF0dXJlIjogImE2NjVhNDU5MjA0MjJmOWQ0MTdlNDg2N2VmZGM0ZmI4YTA0YTFmM2ZmZjFmYTA3ZTk5OGU4NmY3ZjdhMjdhZTMiLCAiYnl0ZV9zaXplIjogM30sICJjb25maWdJdGVtU3BlYyI6IHsibmFtZSI6ICJkLnR4dCIsICJwYXRoIjogIi90bW8iLCAiZmlsZV90eXBlIjogInRleHQiLCAiZmlsZV9tb2RlIjogInVuaXgiLCAicGVybWlzc2lvbiI6IHsidXNlciI6ICJyb290IiwgInVzZXJfZ3JvdXAiOiAicm9vdCIsICJwcml2aWxlZ2UiOiAiNjQ0In19LCAiY29uZmlnSXRlbUF0dGFjaG1lbnQiOiB7ImJpel9pZCI6IDIsICJhcHBfaWQiOiAyOX0sICJyZXBvc2l0b3J5UGF0aCI6ICIvYnNjcC12MS1iaXotMi9maWxlL2E2NjVhNDU5MjA0MjJmOWQ0MTdlNDg2N2VmZGM0ZmI4YTA0YTFmM2ZmZjFmYTA3ZTk5OGU4NmY3ZjdhMjdhZTMifV0sICJrdk1ldGFzIjogbnVsbCwgInJlcG9zaXRvcnkiOiB7InJvb3QiOiAiIiwgImFjY2Vzc0tleUlkIjogIiIsICJzZWNyZXRBY2Nlc3NLZXkiOiAiIiwgInVybCI6ICIifSwgInByZUhvb2siOiBudWxsLCAicG9zdEhvb2siOiBudWxsfSwgImluc3RhbmNlIjogeyJiaXpJRCI6IDIsICJhcHBJRCI6IDI5LCAiYXBwIjogImRlbW8tZmlsZSIsICJ1aWQiOiAiY2RiMGZjYjRkODM3MTU0MTFlOWJhMmZmNjUzYTgzNWMiLCAibGFiZWxzIjogeyJhZGRyZXNzIjogIjEuMS4xLjEifSwgImNvbmZpZ1R5cGUiOiAiZmlsZSJ9LCAiY3Vyc29ySUQiOiAwfQ==") // NOCC:LineLength(测试代码)
                    ).setRid("ridxxx")
                    .build();
            responseObserver.onNext(resp);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void pullAppFileMeta(PullAppFileMetaReq request, StreamObserver<PullAppFileMetaResp> responseObserver) {
        PullAppFileMetaResp resp = PullAppFileMetaResp.newBuilder()
                .addFileMetas(
                        FileMeta.newBuilder().setId(1)
                                .setCommitId(1)
                                .setCommitSpec(CommitOuterClass.CommitSpec.newBuilder().build())
                                .setConfigItemAttachment(ConfigItemOuterClass.ConfigItemAttachment.newBuilder().build())
                                .setRepositorySpec(RepositorySpec.newBuilder().build())
                                .setConfigItemSpec(ConfigItemOuterClass.ConfigItemSpec.newBuilder().build())
                )
                .setReleaseId(666)
                .build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void getDownloadURL(GetDownloadURLReq request, StreamObserver<GetDownloadURLResp> responseObserver) {
        GetDownloadURLResp resp = GetDownloadURLResp.newBuilder().build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void pullKvMeta(PullKvMetaReq request, StreamObserver<PullKvMetaResp> responseObserver) {
        PullKvMetaResp resp = PullKvMetaResp.newBuilder().build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void getKvValue(GetKvValueReq request, StreamObserver<GetKvValueResp> responseObserver) {
        GetKvValueResp resp = GetKvValueResp.newBuilder()
                .setKvType("unkown")
                .setValue("unkown")
                .build();
        System.out.println("getKvValue"  + request.getKey());
        if (request.getKey().equals("string_key_1")) {
            resp = GetKvValueResp.newBuilder()
                    .setKvType("string")
                    .setValue("string_key_1_value")
                    .build();
        }
        if (request.getKey().equals("number_key_1")) {
            resp = GetKvValueResp.newBuilder()
                    .setKvType("number")
                    .setValue("number_key_1_value")
                    .build();
        }
        if (request.getKey().equals("text_key_1")) {
            resp = GetKvValueResp.newBuilder()
                    .setKvType("text")
                    .setValue("text_key_1_value")
                    .build();
        }
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void listApps(ListAppsReq request, StreamObserver<ListAppsResp> responseObserver) {
        ListAppsResp resp = ListAppsResp.newBuilder().build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}
