package com.jk.common.param.config;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.jk.common.param.ParamContextException;
import com.jk.common.param.handler.AbstractParamHandler;

public class ParamGroupNode extends AbstractParamHandler {
    private List<AbstractParamHandler> handlers;

    public ParamGroupNode() {
        this.handlers = new ArrayList<AbstractParamHandler>();
    }

    public void addHandler(AbstractParamHandler handler) {
        handlers.add(handler);
    }

    public int size() {
        return handlers.size();
    }

    public AbstractParamHandler getHandler(int index) {
        return handlers.get(index);
    }

    @Override
    public void active() {
        try {
            lock.writeLock().lockInterruptibly();
        } catch (InterruptedException e) {
            return;
        }

        for (int i = 0; i < handlers.size(); ++i) {
            ((AbstractParamHandler) handlers.get(i)).doActive();
        }

        lock.writeLock().unlock();
    }

    @Override
    public void load() throws ParamContextException {
        for (int i = 0; i < handlers.size(); ++i) {
            ((AbstractParamHandler) handlers.get(i)).load();
        }
    }

    public void setParamCtx(JSONObject paramCtx) {
        this.paramCtx = paramCtx;
        for (int i = 0; i < handlers.size(); ++i) {
            ((AbstractParamHandler) handlers.get(i)).setParamCtx(paramCtx);
        }
    }
}
