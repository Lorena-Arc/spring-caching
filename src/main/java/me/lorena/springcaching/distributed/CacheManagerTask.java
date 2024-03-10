package me.lorena.springcaching.distributed;

import java.util.concurrent.Callable;

public class CacheManagerTask implements Callable<Void> {
    private final DistributedCacheAspect distributedCacheAspect;
    public CacheManagerTask(DistributedCacheAspect distributedCacheAspect) {
        this.distributedCacheAspect = distributedCacheAspect;
    }

    @Override
    public Void call() throws InterruptedException {
        while (distributedCacheAspect.isLocked()) {
            Thread.sleep(10);
        }
        return null;
    }
}