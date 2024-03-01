package com.tencent.bscp.helper;

import com.tencent.bscp.pojo.AppOption;
import com.tencent.bscp.pojo.Option;
import com.tencent.bscp.pojo.UpstreamOption;
import com.tencent.bscp.pojo.Vas;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OptionHelper {
    public static Option withFeedAddrs(List<String> addrs) {
        return options -> options.setFeedAddrs(addrs);
    }

    public static Option withFeedAddr(String addr) {
        return options -> options.setFeedAddrs(Collections.singletonList(addr));
    }

    public static Option withBizID(int id) {
        return options -> options.setBizID(id);
    }

    public static Option withLabels(Map<String, String> labels) {
        return options -> options.setLabels(labels);
    }

    public static Option withUID(String uid) {
        return options -> options.setUid(uid);
    }

    public static Option withToken(String token) {
        return options -> options.setToken(token);
    }

    public static AppOption withAppKey(String key) {
        return options -> options.setKey(key);
    }

    public static AppOption withAppLabels(Map<String, String> labels) {
        return options -> options.setLabels(labels);
    }

    public static AppOption withAppUID(String uid) {
        return options -> options.setUid(uid);
    }

    public static UpstreamOption upstreamWithApp(String app) {
        return o -> o.setApp(app);
    }

    public static UpstreamOption upstreamWithKey(String key) {
        return o -> o.setKey(key);
    }

    public static UpstreamOption upstreamWithLabels(Map<String, String> labels) {
        return o -> o.setLabels(labels);
    }

    public static UpstreamOption upstreamWithUID(String uid) {
        return o -> o.setUid(uid);
    }

    public static UpstreamOption upstreamWithFeedAddrs(List<String> addrs) {
        return o -> o.setFeedAddrs(addrs);
    }

    public static UpstreamOption upstreamWithDialTimeoutMS(long timeout) {
        return o -> o.setDialTimeoutMS(timeout);
    }

    public static UpstreamOption upstreamWithVas(Vas vas) {
        return o -> o.setVas(vas);
    }

    public static UpstreamOption upstreamWithBizID(int id) {
        return o -> o.setBizID(id);
    }
}
