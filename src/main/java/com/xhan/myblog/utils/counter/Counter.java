package com.xhan.myblog.utils.counter;

import static org.springframework.util.StringUtils.hasText;

public interface Counter {

    String USED_SPACE = "USED_SPACE";

    String REGISTER = "register";

    String separator = ":";

    Long getCounter(String key);

    Long getCounter(String kField, String kValue);

    boolean setCounter(String key);

    boolean setCounter(String key, int value);

    boolean setCounter(String kField, String kValue);

    boolean setCounter(String kField, String kValue, int value);

    boolean decr(String key);

    boolean decr(String kField, String kValue);

    boolean incr(String key);

    boolean incr(String kField, String kValue);

    boolean incrBy(String key, int value);

    boolean incrBy(String kField, String kValue, int value);

    boolean incrBy(String kField, String kValue, long value);

    boolean decrBy(String key, int value);

    boolean decrBy(String kField, String kValue, int value);

    boolean delCounter(String key);

    default boolean isKeyValid(String key) {
        return hasText(key);
    }

    default void checkKey(String key) {
        if (!isKeyValid(key))
            throw new IllegalArgumentException(key + " is not legal");
    }

    boolean decrBy(String kField, String kValue, long length);
}
