package com.tencent.bscp.helper;

import com.tencent.bscp.pbbase.Base;
import com.tencent.bscp.pojo.Vas;
import io.grpc.Metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Helper {

    // defaultHeartbeatIntervalSec defines heartbeat default interval.
    public static final Long DEFAULT_HEARTBEAT_INTERVAL = Duration.ofSeconds(15).toMillis();

    // defaultHeartbeatTimeout defines default heartbeat request timeout.
    public static final Long DEFAULT_HEARTBEAT_TIMEOUT = Duration.ofSeconds(5).toMillis();

    // maxHeartbeatRetryCount defines heartbeat max retry count.
    public static final int MAX_HEARTBEAT_RETRY_COUNT = 3;

    // SidecarMetaKey defines the key to store the sidecar's metadata info.
    public static final String SIDECAR_META_KEY = "sidecar-meta";
    // SideRidKey defines the incoming request id between sidecar and feed server.
    public static final String SIDE_RID_KEY = "side-rid";
    // SideWorkspaceDir sidecar workspace dir name.
    public static final String SIDE_WORKSPACE_DIR = "bk-bscp";

    // AuthLoginProviderKey is auth login provider
    public static final String AUTH_LOGIN_PROVIDER_KEY = "auth-login-provider";
    // AuthLoginUID is auth login uid
    public static final String AUTH_LOGIN_UID = "auth-login-uid";
    // AuthLoginToken is auth login token
    public static final String AUTH_LOGIN_TOKEN = "auth-login-token";

    public static final String AUTHORIZATION_HEADER = "authorization";
    public static final String BEARER_KEY = "bearer";

    public static final int DEFAULT_SWAP_BUFFER_SIZE = 2 * 1024 * 1024;
    public static final long DEFAULT_RANGE_DOWNLOAD_BYTE_SIZE = 5 * DEFAULT_SWAP_BUFFER_SIZE;
    public static final int REQUEST_AWAIT_RESPONSE_TIMEOUT_SECONDS = 10;
    public static final int DEFAULT_DOWNLOAD_GROUTINES = 10;
    public static final String ENV_MAX_HTTP_DOWNLOAD_GOROUTINES = "BK_BSCP_MAX_HTTP_DOWNLOAD_GOROUTINES";

    public static final Base.Versioning CURRENT_API_VERSION = Base.Versioning.newBuilder().setMajor(1).setMinor(0)
            .setPatch(0).build();
    public static final Base.Versioning LEAST_SIDE_CAR_API_VERSION = Base.Versioning.newBuilder().setMajor(1)
            .setMinor(0).setPatch(0).build();

    public static Vas buildVas(Map<String, String> pairs) {
        String rid = UUID.randomUUID().toString();
        Metadata metadata = new Metadata();
        metadata.put(Metadata.Key.of(SIDE_RID_KEY, Metadata.ASCII_STRING_MARSHALLER), rid);
        for (Map.Entry<String, String> entry : pairs.entrySet()) {
            String value = entry.getValue();
            Metadata.Key<String> key = Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER);
            if (!metadata.containsKey(key)) {
                metadata.put(key, value);
            }
        }
        return new Vas(rid, metadata);
    }

    // GetFingerPrint get current sidecar runtime fingerprint.
    public static String getFingerPrint() {
        File cpusetFile = new File("/proc/1/cpuset");
        if (cpusetFile.exists() && cpusetFile.length() > 32) {
            return getContainerFingerPrint(cpusetFile);
        } else {
            return getHostFingerPrint();
        }
    }

    public static String generateMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return truncateString(input, 32);
        }
    }

    public static String truncateString(String input, int maxLength) {
        if (input.length() <= maxLength) {
            return input;
        } else {
            return input.substring(0, maxLength);
        }
    }

    /**
     * checkAPIVersionMatch checks if the given version is compatible with the
     * current sidecar API version.
     *
     * @param ver the version to check
     * @return true if the version is compatible, otherwise false
     */
    public static boolean checkAPIVersionMatch(Base.Versioning ver) {
        if (ver == null) {
            return false;
        }

        if (ver.getMajor() < LEAST_SIDE_CAR_API_VERSION.getMajor()) {
            return false;
        }

        if (ver.getMajor() == LEAST_SIDE_CAR_API_VERSION.getMajor()) {
            if (ver.getMinor() < LEAST_SIDE_CAR_API_VERSION.getMinor()) {
                return false;
            }

            if (ver.getMinor() == LEAST_SIDE_CAR_API_VERSION.getMinor()) {
                if (ver.getPatch() < LEAST_SIDE_CAR_API_VERSION.getPatch()) {
                    return false;
                }
            }

            return true;
        }

        return true;
    }

    public static Map<String, String> mergeLabels(Map<String, String>... labelsGroup) {
        Map<String, String> result = new HashMap<>();
        for (Map<String, String> labels : labelsGroup) {
            if (labels == null) {
                continue;
            }
            result.putAll(labels);
        }
        return result;
    }

    private static String getContainerFingerPrint(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            String[] elements = line.split("/");
            String containerID = elements[elements.length - 1].trim();
            if (containerID.length() < 32) {
                throw new Exception("container id length < 32, containerID: " + containerID);
            }
            if (containerID.contains("-")) {
                containerID = containerID.split("-")[1];
            }
            if (containerID.length() > 12) {
                containerID = containerID.substring(0, 12);
            }

            String ip = System.getenv("HOST_IP");
            if (ip == null || ip.isEmpty()) {
                ip = getIPFromNetworkInterface();
            }

            if (ip != null && InetAddress.getByName(ip) != null) {
                return ip + "-" + containerID;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private static String getHostFingerPrint() {
        try {
            String ip = getIPFromNetworkInterface();
            if (ip != null && InetAddress.getByName(ip) != null) {
                return ip;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String getIPFromNetworkInterface() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()
                            && !address.isMulticastAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;
    }
}
