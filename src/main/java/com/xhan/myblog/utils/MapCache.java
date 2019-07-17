package com.xhan.myblog.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * map缓存实现
 */
public class MapCache {

    /**
     * 默认存储1024个缓存
     */
    private static final int DEFAULT_CACHES = 1024;

    private volatile static MapCache INS;

    public static MapCache single() {
        if (INS == null) {
            synchronized (MapCache.class) {
                if (INS == null) {
                    INS = new MapCache();
                }
            }
        }
        return INS;
    }

    /**
     * 缓存容器
     */
    private Map<String, CacheObject> cachePool;

    public MapCache() {
        this(DEFAULT_CACHES);
    }

    public MapCache(int cacheCount) {
        cachePool = new ConcurrentHashMap<>(cacheCount);
    }

    /**
     * 读取一个缓存
     *
     * @param key 缓存key
     * @param <T>
     * @return
     */
    public <T> T get(String key) {
        CacheObject cacheObject = cachePool.get(key);
        AtomicLong l = new AtomicLong(0);

        if (null != cacheObject) {
            long cur = System.currentTimeMillis() / 1000;
            if (cacheObject.getExpired() <= 0 || cacheObject.getExpired() > cur) {
                Object result = cacheObject.getValue();
                return (T) result;
            } else {
                cachePool.remove(key, cacheObject);
            }
        }
        return null;
    }

    /**
     * 读取一个hash类型缓存
     *
     * @param key   缓存key
     * @param field 缓存field
     * @param <T>
     * @return
     */
    public <T> T hget(String key, String field) {
        key = key + ":" + field;
        return this.get(key);
    }

    /**
     * 设置一个缓存
     *
     * @param key   缓存key
     * @param value 缓存value
     */
    public void set(String key, Object value) {
        this.set(key, value, -1);
    }

    /**
     * 设置一个缓存并带过期时间
     *
     * @param key     缓存key
     * @param value   缓存value
     * @param expired 过期时间，单位为秒
     */
    public void set(String key, Object value, long expired) {
        put(key, value, expired);
    }

    private void put(String key, Object value, long expired) {
        expired = expired > 0 ? System.currentTimeMillis() / 1000 + expired : expired;
        CacheObject cacheObject = new CacheObject(key, value, expired);
        cachePool.put(key, cacheObject);
    }

    /**
     * 当键不存在或者过期的时候设置键，这里重要的是对过期的判断，如果过期就
     * 清除它，然后用putIfAbsent设置，如果没过期就返回，如果不存在，也用
     * putIfAbsent设置。如果设置成功，就会把设置的值返回，否则就把原来的值
     * 返回
     *
     * @param key
     * @param value
     * @param expired
     * @return Object 当过期或不存在时，就返回传入的value，当值依旧可用时，就返回值
     */
    public Object setnx(String key, Object value, long expired) {
        expired = expired > 0 ? System.currentTimeMillis() / 1000 + expired : expired;
        CacheObject cacheObject = new CacheObject(key, value, expired);
        Object returnedObject = get(key);
        // 这里一个先判断后执行，可能出现这个线程将值删除（在get里）但是其他线程又将值设置的情况
        // 如果get(key) != null，然后返回了要设置的值

        // 如果超时或者不存在，就用putIfAbsent将键值对置入，如果存在，就将旧值返回
        if (returnedObject == null)
            cacheObject = cachePool.putIfAbsent(key, cacheObject);
        else
            return returnedObject;

        // 如果置入成功，cacheObject则为null，就返回置入的值，
        // 如果置入失败，则cacheObject不为null，则返回cacheObject.val
        return cacheObject == null ? value : cacheObject.value;
    }

    /**
     * 设置一个hash缓存
     *
     * @param key   缓存key
     * @param field 缓存field
     * @param value 缓存value
     */
    public void hset(String key, String field, Object value) {
        this.hset(key, field, value, -1);
    }

    /**
     * 设置一个hash缓存并带过期时间
     *
     * @param key     缓存key
     * @param field   缓存field
     * @param value   缓存value
     * @param expired 过期时间，单位为秒
     */
    public void hset(String key, String field, Object value, long expired) {
        key = key + ":" + field;
        put(key, value, expired);
    }

    /**
     * 根据key删除缓存
     *
     * @param key 缓存key
     */
    public void del(String key) {
        cachePool.remove(key);
    }

    /**
     * 根据key和field删除缓存
     *
     * @param key   缓存key
     * @param field 缓存field
     */
    public void hdel(String key, String field) {
        key = key + ":" + field;
        this.del(key);
    }

    /**
     * 清空缓存
     */
    public void clean() {
        cachePool.clear();
    }

    static class CacheObject {
        private String key;
        private Object value;
        private final long expired;

        public CacheObject(String key, Object value, long expired) {
            this.key = key;
            this.value = value;
            this.expired = expired;
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public long getExpired() {
            return expired;
        }
    }
}