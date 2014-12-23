package com.jk.common.param.handler;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;

import com.jk.common.param.ParamContextException;

public abstract class AbstractParamHandler implements Runnable {

    private static final Logger LOG = Logger.getLogger(AbstractParamHandler.class);
    protected BeanFactory beanFactory;
    protected ReentrantReadWriteLock lock;
    protected JSONObject paramCtx;
    protected JSONObject valueObj;
    protected JSONObject oldValueObj;

    protected int interval;
    protected String name;
    protected long lastModified;

    public AbstractParamHandler() {
        this.lock = null;
        this.paramCtx = null;
        this.valueObj = new JSONObject();
        this.oldValueObj = null;
    }

    @Override
    public void run() {
        long l = System.currentTimeMillis();
        try {
            load();
            active();
            lastModified = System.currentTimeMillis();
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("param [%s] load succeed, param cache [%s]", name, paramCtx.get(name)));
            }
        } catch (Exception e) {
            LOG.error(String.format("param [%s] load failed", name), e);
        } finally {
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("process:[%s], cost time:[%d]ms", name, lastModified - l));
            }
        }
    }

    public abstract void load() throws ParamContextException;

    public void active() {
        try {
            lock.writeLock().lockInterruptibly();
            doActive();
        } catch (InterruptedException e) {
            LOG.error("active lock interrupted", e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void doActive() {
        paramCtx.put(name, valueObj);
        oldValueObj = valueObj;
        valueObj = new JSONObject();
    }

    public ReentrantReadWriteLock getLock() {
        return lock;
    }

    public void setLock(ReentrantReadWriteLock lock) {
        this.lock = lock;
    }

    public JSONObject getParamCtx() {
        return paramCtx;
    }

    public void setParamCtx(JSONObject paramCtx) {
        this.paramCtx = paramCtx;
    }

    public int getInterval() {
        return this.interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }
}
