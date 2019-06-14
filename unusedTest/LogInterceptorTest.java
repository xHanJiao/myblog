package com.xhan.myblog;

import com.xhan.myblog.utils.MapCache;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class LogInterceptorTest {

    @Test
    public void testAtomic() throws InterruptedException {
        AtomicLong atl = new AtomicLong(0);
        ExecutorService es = Executors.newFixedThreadPool(10);
        IntStream.range(0, 10)
                .forEach(i -> es.submit(atl::incrementAndGet));

        es.awaitTermination(20, TimeUnit.SECONDS);
        System.out.println("value : " + atl.get());
    }

    @Test
    public void testMapCache() {
        MapCache cache = MapCache.single();
        ExecutorService es = Executors.newCachedThreadPool();
        String key = "counter";
        IntStream.range(0, 10)
                .forEach(i -> {
                    cache.setnx(key, new AtomicInteger(0), -1);
                    AtomicInteger integer = cache.get(key);
                    integer.incrementAndGet();
                });
        AtomicInteger integer = cache.get(key);
        System.out.println("value : " + integer.get());
    }
}