package com.tencent.bscp.sdk.mock;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.CountDownLatch;

public class MockFeedServiceRun {
    private Server server;
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void run(CountDownLatch latch)
            throws IOException, InterruptedException {
        final MockFeedServiceRun server = new MockFeedServiceRun();
        server.start();
        latch.countDown();
        server.blockUntilShutdown();
    }

    private void start()
            throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        LOGGER.info("Server begin {}", port);
        server = ServerBuilder.forPort(port)
//                .useTransportSecurity(
//                        MockFeedServiceRun.class.getResourceAsStream("/server.crt"),
//                        MockFeedServiceRun.class.getResourceAsStream("/server8.key")
//                )
                .addService(new MockFeedServiceImpl())
                .build()
                .start();
        LOGGER.info("Server started, listening on {}", port);
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                    LOGGER.info("*** shutting down gRPC server since JVM is shutting down");
                    MockFeedServiceRun.this.stop();
                    LOGGER.info("*** server shut down");
                }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown()
            throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
}
