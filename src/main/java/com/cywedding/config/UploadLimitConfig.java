package com.cywedding.config;

import java.util.concurrent.Semaphore;

import org.springframework.stereotype.Component;

@Component
public class UploadLimitConfig {
    private final Integer LIMIT = 10;
    private final Semaphore semaphore = new Semaphore(LIMIT);

    public boolean tryAcquire() {
        return semaphore.tryAcquire();
    }

    public void release() {
        semaphore.release();
    }

    public int getLimit() {
        return LIMIT;
    }
}