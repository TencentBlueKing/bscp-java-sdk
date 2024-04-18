package com.tencent.bscp.helper;

import com.tencent.bscp.pojo.SidecarHandshakePayload;
import com.tencent.bscp.pojo.TLSBytes;
import nl.altindag.ssl.SSLFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class TLSHelper {

    /**
     * TLS OkHttpClient
     * 
     * @param tlsBytes TLS config
     * 
     * @return SSLFactory
     */
    public static SSLFactory buildSSLFactory(TLSBytes tlsBytes) {
        try {
            SSLFactory.Builder builder = SSLFactory.builder().withDefaultTrustMaterial();
            if (tlsBytes == null) {
                return builder.build();
            }
            if (!tlsBytes.isInsecureSkipVerify()) {
                // If certificate verification is not skipped, load the CA certificate
                byte[] caCertBytes = Base64.getDecoder().decode(tlsBytes.getCaFileBytes());
                CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                X509Certificate caCert = (X509Certificate) certificateFactory
                        .generateCertificate(new ByteArrayInputStream(caCertBytes));
                builder.withTrustMaterial(caCert);
            }
            if (tlsBytes.getCertFileBytes() != null && tlsBytes.getKeyFileBytes() != null) {
                // Load the client certificate and private key if they are provided
                byte[] certBytes = Base64.getDecoder().decode(tlsBytes.getCertFileBytes());
                byte[] keyBytes = Base64.getDecoder().decode(tlsBytes.getKeyFileBytes());
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(null, null);
                keyStore.setCertificateEntry("cert", getCertificate(certBytes));
                keyStore.setKeyEntry("key", getPrivateKey(keyBytes), null,
                        new Certificate[] { getCertificate(certBytes) });
                builder.withIdentityMaterial(keyStore, null);
            }
            return builder.build();
        } catch (NoSuchAlgorithmException | CertificateException | KeyStoreException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    // GET THE X509 CERTIFICATE
    private static X509Certificate getCertificate(byte[] certBytes) {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    // 获取私钥
    private static PrivateKey getPrivateKey(byte[] keyBytes) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
