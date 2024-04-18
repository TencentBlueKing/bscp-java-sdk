package com.tencent.bscp.helper;

import com.tencent.bscp.pojo.TLSBytes;
import nl.altindag.ssl.SSLFactory;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class OkhttpHelper {
    private static final Long connectTimeout = 5L;
    private static final Long readTimeout = 30L;
    private static final Long writeTimeout = 30L;
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            .hostnameVerifier((hostname, session) -> true)
            .build();

    public static OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public static OkHttpClient getDownloaderOkhttpClient(SSLFactory factory) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .sslSocketFactory(factory.getSslSocketFactory(), factory.getTrustManager().get())
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.MINUTES)
                .writeTimeout(0, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .connectionPool(new ConnectionPool(10, 5, TimeUnit.MINUTES));
        return clientBuilder.build();
    }

    public static OkHttpClient getTLSOkHttpClient(TLSBytes tlsBytes) {
        SSLFactory factory = TLSHelper.buildSSLFactory(tlsBytes);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.hostnameVerifier((hostname, session) -> true); // 不验证主机名
        builder.sslSocketFactory(factory.getSslSocketFactory(), factory.getTrustManager().get());
        return builder.build();
    }
}
