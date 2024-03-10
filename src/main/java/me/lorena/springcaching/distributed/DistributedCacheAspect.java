package me.lorena.springcaching.distributed;

import me.lorena.springcaching.distributed.pubsub.RedisMessageListener;
import me.lorena.springcaching.distributed.pubsub.RedisMessagePublisher;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Aspect
@Order(1)
@Component
public class DistributedCacheAspect {
    private final static String ACCESS_KEY_CACHE = "ACCESS_CACHE";
    private final Logger log = LoggerFactory.getLogger(DistributedCacheAspect.class);
    private final ExpressionParser parser = new SpelExpressionParser();
    private final StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();

    private final CacheManager cacheManager;
    private final RedisMessageListenerContainer redisMessageListenerContainer;
    private final RedisMessagePublisher redisPublisher;
    private boolean lockedCache = false;


    public DistributedCacheAspect(
            CacheManager cacheManager,
            RedisMessageListenerContainer redisMessageListenerContainer,
            RedisMessagePublisher redisPublisher
    ) {
        this.cacheManager = cacheManager;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
        this.redisPublisher = redisPublisher;
    }

    public boolean isLocked() {
        return lockedCache;
    }

    public void setLocked(boolean locked) {
        this.lockedCache = locked;
    }

    @Around("@annotation(DistributedCaching)")
    public Object distributedCaching(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Entero laspeto");
        String accessKey = joinPoint
                .getSignature()
                .toLongString() + Arrays.toString(joinPoint.getArgs());

        if (checkKeyAlreadyExists(joinPoint)) {
            return joinPoint.proceed();
        }

        Topic topic = new ChannelTopic(accessKey);
        if (cacheManager.getCache(ACCESS_KEY_CACHE).get(accessKey) != null) {
            waitRequestedData(topic);
            return joinPoint.proceed();
        }

        log.info("Requesting data");
        cacheManager.getCache(ACCESS_KEY_CACHE).put(accessKey, true);
        Object proceed = joinPoint.proceed();

        cacheManager.getCache(ACCESS_KEY_CACHE).evict(accessKey);
        log.info("Requested data");
        redisPublisher.publish(topic, "DONE");
        return proceed;
    }

    private <T> T parseCacheKey(Method method, Object[] arguments, String spel) {
        String[] params = discoverer.getParameterNames(method);
        EvaluationContext context = new StandardEvaluationContext();

        if (params != null && params.length > 0) {
            for (int len = 0; len < params.length; len++) {
                context.setVariable(params[len], arguments[len]);
            }
        }

        Expression expression = parser.parseExpression(spel);
        return expression.getValue(context, (Class<T>) String.class);
    }

    private boolean checkKeyAlreadyExists(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Cacheable caching = method.getAnnotation(Cacheable.class);
        String key = parseCacheKey(method, joinPoint.getArgs(), caching.key());
        Optional<String> actualCachedValueCacheName = Arrays.stream(caching.value()).findFirst();
        if (actualCachedValueCacheName.isPresent() &&
                cacheManager.getCache(actualCachedValueCacheName.get()).get(key) != null) {
            log.info("Data present, carrying on");
            return true;
        }
        return false;
    }

    private void waitRequestedData(Topic topic) {
        log.info("Data was already requested, waiting");
        lockedCache = true;
        RedisMessageListener redisMessageListener =
                new RedisMessageListener(this);

        redisMessageListenerContainer
                .addMessageListener(redisMessageListener, topic);

        waitForSynchronization();

        redisMessageListenerContainer
                .removeMessageListener(redisMessageListener, topic);

        log.info("Data received, stop waiting");
    }

    private void waitForSynchronization() {
        try (ExecutorService executor = Executors.newSingleThreadExecutor();) {
            List<Future<Void>> result = executor.invokeAll(List.of(
                            new CacheManagerTask(this)),
                    10,
                    TimeUnit.SECONDS
            ); // Timeout of 10 seconds.

            result.getFirst().get();
        } catch (InterruptedException | ExecutionException e) {
            // Do something
            log.error("Something went wrong");
        }
    }
}
