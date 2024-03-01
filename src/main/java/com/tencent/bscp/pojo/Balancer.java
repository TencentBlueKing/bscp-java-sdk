package com.tencent.bscp.pojo;

import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Balancer {
    private Lock lock;
    private int index;
    private int max;
    private List<String> endpoints;

    public Balancer(List<String> endpoints) throws Exception {
        if (endpoints.isEmpty()) {
            throw new Exception("No endpoints is set, cannot initialize the client round-robin balancer");
        }

        Random rand = new Random(System.nanoTime());
        int index = rand.nextInt(endpoints.size());
        this.lock = new ReentrantLock();
        this.index = index;
        this.max = endpoints.size() - 1;
        this.endpoints = endpoints;
    }


    public Balancer(List<String> endpoints, int index) {
        this.lock = new ReentrantLock();
        this.index = index;
        this.max = endpoints.size() - 1;
        this.endpoints = endpoints;
    }

    public String pickOne() {
        lock.lock();
        try {
            if (index == max) {
                index = 0;
                return endpoints.get(max);
            }

            index++;

            return endpoints.get(index - 1);
        } finally {
            lock.unlock();
        }
    }
}
