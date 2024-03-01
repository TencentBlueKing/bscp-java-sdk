package com.tencent.bscp.pojo;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Blocker {
    private Lock lock;
    private AtomicBoolean state;
    private Semaphore semaphore;

    public Blocker() {
        this.lock = new ReentrantLock();
        this.state = new AtomicBoolean(false);
        this.semaphore = new Semaphore(1);
    }

    /**
     * Try to block the blocker. If the blocker is already blocked, return false. If
     * the blocker is not blocked, set the blocker state to be blocked and return
     * true.
     * 
     * @return true if the blocker is not blocked and the blocker state is set to be
     *         blocked, otherwise return false.
     */
    public boolean tryBlock() {
        lock.lock();
        try {
            if (state.get()) {
                // The blocker is already blocked, no need to block again.
                return false;
            }

            // Set the blocker state to be blocked.
            state.set(true);

            // Initialize the signal object, which is used to broadcast unblock messages.
            semaphore = new Semaphore(1);

            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Unblock the blocker. If the blocker is already unblocked, no need to unblock
     * again.
     */
    public void unBlock() {
        lock.lock();
        try {
            if (!state.get()) {
                // The blocker is already blocked, no need to block again.
                return;
            }

            // Set the blocker state to be blocked.
            state.set(false);

            // Initialize the signal object, which is used to broadcast unblock messages.
            semaphore.release();

        } finally {
            lock.unlock();
        }
    }

    public void waitMS(long timeoutMS) throws InterruptedException {
        if (!state.get()) {
            // The blocker is already blocked, no need to block again.
            return;
        }
        if (!semaphore.tryAcquire(timeoutMS, TimeUnit.MILLISECONDS)) {
            throw new RuntimeException("Wait for blocker unblock, but timeout after " + timeoutMS + " milliseconds");
        }
    }

    public void waiting() throws InterruptedException {
        if (!state.get()) {
            // The blocker is already blocked, no need to block again.
            return;
        }
        semaphore.acquire();
    }
}
