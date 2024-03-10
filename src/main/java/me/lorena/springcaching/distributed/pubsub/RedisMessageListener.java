package me.lorena.springcaching.distributed.pubsub;

import me.lorena.springcaching.distributed.DistributedCacheAspect;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

public class RedisMessageListener implements MessageListener {
    private final DistributedCacheAspect distributedCacheAspect;

    public RedisMessageListener(DistributedCacheAspect distributedCacheAspect) {
        this.distributedCacheAspect = distributedCacheAspect;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        //unlock the thread!
        distributedCacheAspect.setLocked(false);
    }
}