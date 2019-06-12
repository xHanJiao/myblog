package com.xhan.myblog.utils.counter;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MongoCounter {

    public MongoCounter(String kField, String kValue, Long value) {
        this.key = kField + Counter.separator + kValue;
        this.value = value;
    }

    public MongoCounter(String key, Long value) {
        this.key = key;
        this.value = value;
    }

    public MongoCounter(String kField, String kValue, Long value, Long expireTime) {
        this.key = kField + Counter.separator + kValue;
        this.value = value;
        this.expireTime = expireTime;
    }

    public MongoCounter(String key, Long value, Long expireTime) {
        this.key = key;
        this.value = value;
        this.expireTime = expireTime;
    }

    private String key;
    private Long value;
    private Long expireTime;
}
