package com.tencent.bscp.pojo;

import io.grpc.Metadata;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Vas {
    private String rid;
    private Metadata ctx;
    private Semaphore wg;

    public static int SemaphoreSize = 10;

    public Vas(String rid, Metadata ctx) {
        this.rid = rid;
        this.ctx = ctx;
        this.wg = new Semaphore(SemaphoreSize);
    }

    public String getRid() {
        return rid;
    }

    public Metadata getCtx() {
        return ctx;
    }

    public void wgDone(int p) {
        wg.release(p);
    }

    public void wgAdd(int p) throws InterruptedException {
        wg.acquire(p);
    }

    public void wgWait() throws InterruptedException {
        wg.acquire(SemaphoreSize);
    }

    public boolean wgWait(long timeout, TimeUnit unit) throws InterruptedException {
        return wg.tryAcquire(timeout, unit);
    }
}
