package com.xhan.myblog.utils.counter;

import com.mongodb.client.result.UpdateResult;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static org.springframework.data.mongodb.core.query.Query.query;

@Component(value = "mongoCounter")
public class MongoCounterHandler implements Counter {

    @Resource
    private MongoTemplate mongoTemplate;

    private static final String COLLECTION_NAME = "counter";


    @Override
    public Long getCounter(String key) {
        checkKey(key);
        MongoCounter counter = mongoTemplate.findOne(query(Criteria.where("key").is(key)),
                MongoCounter.class, COLLECTION_NAME);
        return counter == null ? 0 : counter.getValue();
    }

    @Override
    public Long getCounter(String kField, String kValue) {
        checkKey(kField + Counter.separator + kValue);
        MongoCounter counter =
                mongoTemplate.findOne(query(Criteria.where("key").is(kField + Counter.separator + kValue)),
                        MongoCounter.class, COLLECTION_NAME);
        return counter == null ? 0 : counter.getValue();
    }

    @Override
    public boolean setCounter(String key) {

        return upsertKeyValue(key, 1);
    }

    @Override
    public boolean setCounter(String key, int value) {
        return upsertKeyValue(key, value);
    }

    @Override
    public boolean setCounter(String kField, String kValue) {
        String key = kField + Counter.separator + kValue;

        return upsertKeyValue(key, 1);
    }

    @Override
    public boolean setCounter(String kField, String kValue, int value) {
        String key = kField + Counter.separator + kValue;
        return upsertKeyValue(key, value);
    }

    private boolean upsertKeyValue(String key, int value) {
        checkKey(key);
        Query query = query(Criteria.where("key").is(key));
        UpdateResult result = mongoTemplate.upsert(query, new Update().set("value", value), MongoCounter.class, COLLECTION_NAME);
        return result.getModifiedCount() == 1;
    }

    @Override
    public boolean decr(String key) {
        return modifyValue(key, -1);
    }

    @Override
    public boolean decr(String kField, String kValue) {
        String key = kField + Counter.separator + kValue;
        return modifyValue(key, -1);
    }

    @Override
    public boolean incr(String key) {

        return modifyValue(key, 1);
    }

    @Override
    public boolean incr(String kField, String kValue) {
        String key = kField + Counter.separator + kValue;

        return modifyValue(key, 1);
    }

    @Override
    public boolean incrBy(String key, int value) {
        return modifyValue(key, value);
    }

    private boolean modifyValue(String key, long l) {
        checkKey(key);
        UpdateResult result;
        Query kq = query(Criteria.where("key").is(key));
        try {
            result = mongoTemplate.upsert(kq, new Update().inc("value", l),
                    MongoCounter.class, COLLECTION_NAME);
        } catch (DuplicateKeyException e) {
            result = mongoTemplate.updateFirst(kq, new Update().inc("value", l), COLLECTION_NAME);
        }
        return result.wasAcknowledged() && result.getModifiedCount() == 1;
    }

    @Override
    public boolean incrBy(String kField, String kValue, int value) {
        String key = kField + Counter.separator + kValue;
        return modifyValue(key, value);
    }

    @Override
    public boolean incrBy(String kField, String kValue, long value) {
        String key = kField + Counter.separator + kValue;
        return modifyValue(key, value);
    }

    @Override
    public boolean decrBy(String key, int value) {
        return value < 0 ? modifyValue(key, value) : modifyValue(key, value * -1);
    }

    @Override
    public boolean decrBy(String kField, String kValue, int value) {
        String key = kField + Counter.separator + kValue;
        return value < 0 ? modifyValue(key, value) : modifyValue(key, value * -1);
    }

    @Override
    public boolean delCounter(String key) {
        checkKey(key);

        return mongoTemplate.remove(query(Criteria.where("key").is(key)), MongoCounter.class, COLLECTION_NAME).wasAcknowledged();
    }

    @Override
    public boolean decrBy(String kField, String kValue, long value) {
        String key = kField + Counter.separator + kValue;
        return value < 0 ? modifyValue(key, value) : modifyValue(key, value * -1);
    }
}
