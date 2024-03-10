package me.lorena.springcaching.distributed.pubsub;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.Topic;

public class RedisMessagePublisher {
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisMessagePublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(Topic topic, String message) {
        redisTemplate.convertAndSend(topic.getTopic(), message);
    }
}
