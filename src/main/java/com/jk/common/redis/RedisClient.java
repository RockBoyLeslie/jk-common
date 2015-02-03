package com.jk.common.redis;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Common redis client
 * 
 * @author leslie
 *
 */
public class RedisClient implements InitializingBean, DisposableBean {

    // redis executor pool
    private JedisPool pool;

    // redis server host
    private String host;
    // redis server port
    private int port;

    private int maxTotal = 1024;
    private int maxIdle = 200;
    private long maxWaitMillis = 1000;
    private boolean testOnBorrow = true;
    private boolean testOnReturn = true;

    // this is used as SoTimeOut configuration for socket with redis server
    // default sotimeout is 30 s
    private int timeout = 30 * 1000;

    public RedisClient() {
    }

    public Jedis getRedis() {
        Jedis jedis = pool.getResource();
        jedis.select(0);

        return jedis;
    }

    public Jedis getRedis(int index) {
        Jedis jedis = pool.getResource();
        jedis.select(index);
        return jedis;
    }

    public void returnRedis(Jedis jedis) {
        pool.returnResource(jedis);
    }

    public void returnBrokeRedis(Jedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    @Override
    public void destroy() {
        pool.destroy();
    }

    @Override
    public void afterPropertiesSet() {
        JedisPoolConfig poolconfig = new JedisPoolConfig();
        poolconfig.setMaxTotal(maxTotal);
        poolconfig.setMaxIdle(maxIdle);
        poolconfig.setMaxWaitMillis(maxWaitMillis);
        poolconfig.setTestOnBorrow(testOnBorrow);
        poolconfig.setTestOnReturn(testOnReturn);

        pool = new JedisPool(poolconfig, host, port, timeout);
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setPool(JedisPool pool) {
        this.pool = pool;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public void setMaxWaitMillis(long maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

}
