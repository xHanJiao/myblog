package com.xhan.myblog.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenBucketManagerTest {

    private static final int THREAD_POOL_REST_LIFE = 5;
    private ExecutorService es;
    private String TEST_KEY = "test";
    private int tokenNum = 10;

    /**
     * 在每个TEST 函数运行前首先打开一个CachedThreadPool
     */
    @Before
    public void setUp() {
        es = Executors.newCachedThreadPool();
    }

    /**
     * 在每个TEST 函数运行结束后关闭线程池，并等待THREAD_POOL_REST_LIFE 秒钟
     *
     * @throws InterruptedException 关闭线程池的时候可能抛出的异常
     */
    @After
    public void tearDown() throws InterruptedException {
        es.awaitTermination(THREAD_POOL_REST_LIFE, TimeUnit.SECONDS);
    }

    /**
     * 测试能否获得小于当前桶内令牌数的令牌
     */
    @Test
    public void acquireWithInTokenNum() {
        TokenBucketManager manager = new TokenBucketManager(1);
        manager.addRateLimiter(TEST_KEY, tokenNum, THREAD_POOL_REST_LIFE + 1);
        for (int i = 0; i < tokenNum - 1; i++) {
            es.submit(() -> assertTrue(manager.acquire(TEST_KEY)));
        }
    }

    /**
     * 测试获得超出桶内令牌数的令牌，并且验证超出的部分会获取失败
     */
    @Test
    public void acquireExceedTokenNum() {
        TokenBucketManager manager = new TokenBucketManager(1);
        manager.addRateLimiter(TEST_KEY, tokenNum, THREAD_POOL_REST_LIFE + 1);
        CountDownLatch latch = new CountDownLatch(tokenNum);

        getAllTokenAndCountDown(manager, latch);

        for (int i = 0; i < tokenNum; i++) {
            es.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                assertFalse(manager.acquire(TEST_KEY));
            });
        }
    }

    /**
     * 验证尝试获得超出桶内令牌的请求会被阻塞并在桶中又被放入令牌时成功
     */
    @Test
    public void tryAcquire() {
        TokenBucketManager manager = new TokenBucketManager(1);
        manager.addRateLimiter(TEST_KEY, tokenNum, THREAD_POOL_REST_LIFE / 2);
        CountDownLatch latch = new CountDownLatch(tokenNum);

        getAllTokenAndCountDown(manager, latch);

        for (int i = 0; i < tokenNum / 3; i++) {
            es.submit(() -> {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                assertTrue(manager.tryAcquire(TEST_KEY, THREAD_POOL_REST_LIFE / 2 + 1));
            });
        }
    }

    private void getAllTokenAndCountDown(TokenBucketManager manager, CountDownLatch latch) {
        for (int i = 0; i < tokenNum; i++) {
            es.submit(() -> {
                assertTrue(manager.acquire(TEST_KEY));
                latch.countDown();
            });
        }
    }
}