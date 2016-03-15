package com.jk.common.idempotent;

import com.jk.common.redis.RedisTemplate;
import org.springframework.util.SerializationUtils;

/**
 *
 *
 * @see
 */
public class RedisIdempotentRequestStore implements IdempotentRequestStore {

    private final static String DEFAULT_KEY_PREFIX = "idempotent_";

    private RedisTemplate redisTemplate;

    @Override
    public boolean contains(IdempotentRequestKey key) {
        return redisTemplate.exists(SerializationUtils.serialize(getKey(key)));
    }

    @Override
    public boolean storenx(IdempotentRequestKey key) {
        byte[] value = SerializationUtils.serialize(new IdempotentResponseWrapper(null));
        return redisTemplate.setnx(SerializationUtils.serialize(getKey(key)), value);
    }

    @Override
    public void setResponse(IdempotentRequestKey key, IdempotentResponseWrapper response) {
        redisTemplate.set(SerializationUtils.serialize(getKey(key)), SerializationUtils.serialize(response));
    }

    @Override
    public IdempotentResponseWrapper getResponse(IdempotentRequestKey key) {
        byte[] response = redisTemplate.getByte(SerializationUtils.serialize(getKey(key)));
        return response == null ? null : (IdempotentResponseWrapper) SerializationUtils.deserialize(response);
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    private String getKey(IdempotentRequestKey key) {
        return DEFAULT_KEY_PREFIX + key.toString();
    }
    
}
