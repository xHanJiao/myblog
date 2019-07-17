package com.xhan.myblog.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.valueOf;

@Slf4j
public class TokenBucketManager {

    private static final int SCHEDULED_POOL_SIZE = 3;
    private static final int DEFAULT_TOKEN_NUM = 5;
    private static final int MAX_WAIT_SECOND = 10;
    private static final int DEFAULT_INTERVAL = 10;

    private final ConcurrentHashMap<String, TokenBucket> bucketHolder;
    private final ScheduledExecutorService scheduledExecutorService;

    public TokenBucketManager() {
        bucketHolder = new ConcurrentHashMap<>();
        scheduledExecutorService = new ScheduledThreadPoolExecutor(SCHEDULED_POOL_SIZE);
    }

    @PreDestroy
    public void destroy() {
        scheduledExecutorService.shutdown();
    }

    public void addRateLimiter(String key) {
        addRateLimiterAndSetTokenNum(key, DEFAULT_TOKEN_NUM, DEFAULT_INTERVAL);
    }

    public void addRateLimiter(String key, int tokenNum, int interval) {
        addRateLimiterAndSetTokenNum(key, tokenNum, interval);
    }

    private void addRateLimiterAndSetTokenNum(String key, int tokenNum, int interval) {
        TokenBucket bucket = new TokenBucket(tokenNum);
        TokenBucket sig = bucketHolder.putIfAbsent(key, bucket);
        log.debug(String.format("add RateLimiter for [key: %s] --- [tokenNum: %d] --- [interval: %d]",
                key, tokenNum, interval));
        if (sig == null) {
            scheduledExecutorService.scheduleAtFixedRate(new ReplaceBucketWorker(key, tokenNum), interval, interval, TimeUnit.SECONDS);
        }
    }

    public boolean acquire(String key) {
        TokenBucket bucket = bucketHolder.get(key);
        Assert.notNull(bucket, "tokenBucket cannot be null there");
        log.debug(String.format("try to acquire token for [key: %s]", key));
        return bucket.acquire();
    }

    public boolean acquireWithExpire(String key, int waitSecond) {
        TokenBucket bucket = bucketHolder.get(key);
        Assert.notNull(bucket, "tokenBucket cannot be null there");
        log.debug(String.format("try to acquire token for [key: %s] and [waitSecond: %s]", key, valueOf(waitSecond)));
        return bucket.acquireWithExpire(waitSecond);
    }

    /**
     * 这个类是一个令牌桶对象，在里面存放了代表令牌数的AtomicLong
     */
    private static class TokenBucket {
        private final Object waitLock = new Object();
        private AtomicLong tokenHolder;

        private TokenBucket(int tokenNum) {
            this.tokenHolder = new AtomicLong(tokenNum);
        }

        /**
         * 替换一个新桶，在执行时，<b>会将一个tokenHolder换成一个新的AtomicLong对象，</b>
         * 并且会通知所有waitLock等待集上的线程恢复运行。
         *
         * @param tokenNum 新放进去桶中的令牌数
         */
        private void replaceNewBucket(int tokenNum) {
            log.debug(String.format("replace new bucket for [tokenNum: %d], elder bucket has %d remain",
                    tokenNum, this.tokenHolder.get()));
            this.tokenHolder = new AtomicLong(tokenNum);
            synchronized (waitLock) {
                waitLock.notifyAll();
            }
        }

        /**
         * 非阻塞的获取令牌，如果AtomicLong自减1 之后的值仍大于等于0, 则说明获取成功，返回true
         * 否则说明令牌已经不足了，就返回false, 令牌不足时，不会再将令牌放回去
         *
         * @return boolean, true 是获取成功，false 是获取失败
         */
        private boolean acquire() {
            long afterTaken = tokenHolder.decrementAndGet();
            log.debug(String.format("after taken the token remains for %d", afterTaken));
            return afterTaken >= 0;
        }

        /**
         * 尝试获取令牌，如果获取成功，则会返回true，如果当前令牌桶中数目不足，则会wait()
         * 直到新桶到来，并再次尝试获取令牌。有一个等待的时间，如果超过等待时间还没有获得
         * 令牌，就返回false
         *
         * @param waitSecond 等待的时间，不能小于0，不能大于MAX_WAIT_SECOND
         * @return boolean
         */
        private boolean acquireWithExpire(int waitSecond) {
            if (waitSecond <= 0) throw new IllegalArgumentException("等待时间必须大于0");
            waitSecond = waitSecond > MAX_WAIT_SECOND ? MAX_WAIT_SECOND : waitSecond;
            long waitUntil = System.currentTimeMillis() / 1000 + waitSecond;
            while (System.currentTimeMillis() / 1000 < waitUntil) {
                long afterTaken = tokenHolder.decrementAndGet();
                boolean isSuccess = afterTaken >= 0;
                log.debug(String.format("after taken the token remains for %d", afterTaken));
                if (isSuccess) {
                    return true;
                } else {
                    synchronized (waitLock) {
                        try {
                            log.debug(String.format("wait for %s seconds",
                                    valueOf(waitUntil - System.currentTimeMillis() / 1000)));
                            waitLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                }
            }
            return false;
        }
    }

    private class ReplaceBucketWorker implements Runnable {

        private String key;
        private int tokenNum;

        private ReplaceBucketWorker(String key, int tokenNum) {
            Assert.isTrue(tokenNum > 0, "令牌数必须大于0");
            this.key = key;
            this.tokenNum = tokenNum;
        }

        /**
         * 从bucketHolder 中取出key 对应的bucket，并且将它的桶中令牌数
         * 重置为tokenNum
         */
        @Override
        public void run() {
            TokenBucket bucket = bucketHolder.get(key);
            Assert.notNull(bucket, "tokenBucket cannot be null there");
            bucket.replaceNewBucket(tokenNum);
        }
    }
}
