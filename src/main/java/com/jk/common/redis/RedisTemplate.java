package com.jk.common.redis;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisTemplate {

    private static final Logger LOG = Logger.getLogger(RedisTemplate.class);

    private RedisClient redisClient;

    public RedisTemplate() {

    }

    public void select(int index) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                int index = Integer.parseInt(((Object[]) parms)[0].toString());
                return jedis.select(index);
            }
        }, index);
    }

    public boolean hexists(String key, String field) {
        return execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean call(Jedis jedis, Object params) {
                String key = ((Object[]) params)[0].toString();
                String field = ((Object[]) params)[1].toString();
                return jedis.hexists(key, field);
            }
        });
    }
    
    public boolean hexists(byte[] key, byte[] field) {
        return execute(new RedisCallback<Boolean>() {

            @Override
            public Boolean call(Jedis jedis, Object params) {
                byte[] key = (byte[]) ((Object[]) params)[0];
                byte[] field = (byte[]) ((Object[]) params)[1];
                return jedis.hexists(key, field);
            }
        }, key, field);
    }
    
    public String hget(String key, String field) {
        return execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[0].toString();
                String field = ((Object[]) parms)[1].toString();
                return jedis.hget(key, field);
            }
        }, key, field);
    }
    
    public byte[] hget(byte[] key, byte[] field) {
        return execute(new RedisCallback<byte[]>() {
            public byte[] call(Jedis jedis, Object parms) {
                byte[] key = (byte[]) ((Object[]) parms)[0];
                byte[] field = (byte[]) ((Object[]) parms)[1];
                return jedis.hget(key, field);
            }
        }, key, field);
    }

    public void hset(String key, String field, String value) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[0].toString();
                String field = ((Object[]) parms)[1].toString();
                String value = ((Object[]) parms)[2].toString();
                jedis.hset(key, field, value);
                return null;
            }
        }, key, field, value);
    }
    
    public void hset(byte[] key, byte[] field, byte[] value) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                byte[] key = (byte[]) ((Object[]) parms)[0];
                byte[] field = (byte[]) ((Object[]) parms)[1];
                byte[] value = (byte[]) ((Object[]) parms)[2];
                jedis.hset(key, field, value);
                return null;
            }
        }, key, field, value);
    }

    public String get(String key) {
        return execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[0].toString();
                return jedis.get(key);
            }
        }, key);
    }

    public byte[] getByte(String key) {
        return execute(new RedisCallback<byte[]>() {
            public byte[] call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[0].toString();
                try {
                    return jedis.get(key.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    LOG.error(String.format("unsupported encoding %s", "UTF-8"), e);
                }
                return null;
            }
        }, key);
    }

    public void set(String key, String value) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[0].toString();
                String value = ((Object[]) parms)[1].toString();
                jedis.set(key, value);
                return null;
            }
        }, key, value);
    }

    public void set(String key, byte[] value) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[0].toString();
                byte[] value = (byte[]) ((Object[]) parms)[1];
                try {
                    jedis.set(key.getBytes("UTF-8"), value);
                } catch (UnsupportedEncodingException e) {
                    LOG.error(e);
                }
                return null;
            }
        }, key, value);
    }

    // 批量Set
    public void setPipeLine(Map<String, String> map) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                Pipeline p = jedis.pipelined();
                @SuppressWarnings("unchecked")
                Map<String, String> values = (Map<String, String>) ((Object[]) parms)[0];
                for (Entry<String, String> entry : values.entrySet()) {
                    p.set(entry.getKey(), entry.getValue());
                }
                p.sync();
                return null;
            }
        }, map);
    }

    // seconds:过期时间（单位：秒）
    public void set(String key, String value, int seconds) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[0].toString();
                String value = ((Object[]) parms)[1].toString();
                String seconds = ((Object[]) parms)[2].toString();
                jedis.setex(key, Integer.parseInt(seconds), value);
                return null;
            }
        }, key, value, seconds);
    }

    public void set(String key, byte[] value, int seconds) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[0].toString();
                byte[] value = (byte[]) ((Object[]) parms)[1];
                String seconds = ((Object[]) parms)[2].toString();
                try {
                    jedis.setex(key.getBytes("UTF-8"), Integer.parseInt(seconds), value);
                } catch (UnsupportedEncodingException e) {
                    LOG.error(e);
                }
                return null;
            }
        }, key, value, seconds);
    }

    public void del(String key) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[0].toString();
                jedis.del(key);
                return null;
            }
        }, key);
    }

    public String llen(String key) {
        return execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[0].toString();
                return jedis.llen(key) + "";
            }
        }, key);
    }

    public void lpush(String key, String value) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[0].toString();
                String value = ((Object[]) parms)[1].toString();
                jedis.lpush(key, value);
                return null;
            }
        }, key, value);
    }

    public void lpushPipeLine(String key, List<String> values) {
        execute(new RedisCallback<String>() {
            @SuppressWarnings("unchecked")
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[0].toString();
                List<String> values = (List<String>) ((Object[]) parms)[1];
                Pipeline p = jedis.pipelined();
                for (String value : values) {
                    p.lpush(key, value);
                }
                p.sync();
                return null;
            }
        }, key, values);
    }

    public List<String> lrange(String key, long start, long end) {
        return execute(new RedisCallback<List<String>>() {
            public List<String> call(Jedis jedis, Object parms) {
                Object[] ps = ((Object[]) parms);
                String key = ps[0].toString();
                long start = Long.parseLong(ps[1].toString());
                long end = Long.parseLong(ps[2].toString());
                return jedis.lrange(key, start, end);
            }
        }, key, start, end);
    }

    public void incr(String key) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[0].toString();
                jedis.incr(key);
                return null;
            }
        }, key);
    }

    public void sadd(String key, String value) {
        execute(new RedisCallback<String>() {
            public String call(Jedis jedis, Object parms) {
                String key = ((Object[]) parms)[0].toString();
                String value = ((Object[]) parms)[1].toString();
                jedis.sadd(key, value);
                return null;
            }
        }, key, value);
    }

    public Set<String> smembers(String key) {
        return execute(new RedisCallback<Set<String>>() {
            public Set<String> call(Jedis jedis, Object parms) {
                Object[] ps = ((Object[]) parms);
                String key = ps[0].toString();
                return jedis.smembers(key);
            }
        }, key);
    }

    public List<String> brpop(String key) {
        return execute(new RedisCallback<List<String>>() {
            public List<String> call(Jedis jedis, Object parms) {
                Object[] ps = ((Object[]) parms);
                String key = ps[0].toString();
                return jedis.brpop(0, key);
            }
        }, key);
    }

    protected <T> T execute(RedisCallback<T> callback, Object... args) {
        Jedis jedis = null;
        try {
            jedis = redisClient.getRedis();
            return callback.call(jedis, args);
        } catch (JedisConnectionException e) {
            LOG.error("redis connect failed", e);

            if (jedis != null) {
                redisClient.returnBrokeRedis(jedis);
            }
            jedis = redisClient.getRedis();
        } catch (Exception e) {
            LOG.error("Exception happens when get redis connection", e);
        } finally {
            if (jedis != null) {
                redisClient.returnRedis(jedis);
            }
        }
        return null;
    }
    
    public void setRedisClient(RedisClient redisClient) {
        this.redisClient = redisClient;
    }
}
