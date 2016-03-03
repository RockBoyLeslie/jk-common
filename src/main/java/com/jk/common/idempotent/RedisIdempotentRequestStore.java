package com.jk.common.idempotent;

import com.jk.common.redis.RedisTemplate;
import org.springframework.util.SerializationUtils;

/**
 *
 *
 * @see
 */
public class RedisIdempotentRequestStore implements IdempotentRequestStore {

    private final static String DEFAULT_KEY = "idempotentRequestStore";

    private final static byte[] DEFAULT_KEY_SERIAL = SerializationUtils.serialize(DEFAULT_KEY);

    private RedisTemplate redisTemplate;

    @Override
    public boolean contains(IdempotentRequestKey key) {
        return redisTemplate.hexists(DEFAULT_KEY_SERIAL, SerializationUtils.serialize(key.toString()));
    }

    @Override
    public void store(IdempotentRequestKey key) {
        byte[] value = SerializationUtils.serialize(new IdempotentResponseWrapper(null));
        redisTemplate.hset(DEFAULT_KEY_SERIAL, SerializationUtils.serialize(key.toString()), value);
    }

    @Override
    public void setResponse(IdempotentRequestKey key, IdempotentResponseWrapper response) {
        redisTemplate.hset(DEFAULT_KEY_SERIAL, SerializationUtils.serialize(key.toString()), SerializationUtils.serialize(response));
    }

    @Override
    public IdempotentResponseWrapper getResponse(IdempotentRequestKey key) {
        byte[] response = redisTemplate.hget(DEFAULT_KEY_SERIAL, SerializationUtils.serialize(key.toString()));
        return response == null ? null : (IdempotentResponseWrapper) SerializationUtils.deserialize(response);
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
}
