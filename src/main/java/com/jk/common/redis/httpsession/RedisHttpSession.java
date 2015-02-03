package com.jk.common.redis.httpsession;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

@SuppressWarnings("deprecation")
/**
 * implements on HttpSession which stores in redis
 *
 */
public class RedisHttpSession implements HttpSession, Serializable {
    private static final long serialVersionUID = 1L;
    protected long creationTime = 0L;
    protected long lastAccessedTime = 0L;
    protected String id;
    protected int maxInactiveInterval;

    // if session expired flag, defined as transiend type that will not be serialized
    protected transient boolean expired;

    // if session is new flag,  defined as transiend type that will not be serialized
    protected transient boolean isNew;
    
    // if session is dirty flag, used to check if session is synchronized with redis, 
    // defined as transiend type that will not be serialized
    protected transient boolean isDirty;
    private transient SessionListener listener;

    // local session data
    private Map<String, Object> data;

    public RedisHttpSession() {
        this.expired = false;
        this.isNew = false;
        this.isDirty = false;
        this.data = new HashMap<String, Object>();
    }

    public void setListener(SessionListener listener) {
        this.listener = listener;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public String getId() {
        return this.id;
    }

    public long getLastAccessedTime() {
        return this.lastAccessedTime;
    }

    public void setLastAccessedTime(long lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public void setMaxInactiveInterval(int i) {
        this.maxInactiveInterval = i;
    }

    @Override
    public int getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getAttribute(String key) {
        return this.data.get(key);
    }

    @Override
    public Object getValue(String key) {
        return this.data.get(key);
    }

    @Override
    public String[] getValueNames() {
        String[] names = new String[this.data.size()];
        return ((String[]) this.data.keySet().toArray(names));
    }

    @Override
    public void setAttribute(String s, Object o) {
        this.data.put(s, o);
        this.isDirty = true;
    }

    @Override
    public void putValue(String s, Object o) {
        this.data.put(s, o);
        this.isDirty = true;
    }

    @Override
    public void removeAttribute(String s) {
        this.data.remove(s);
        this.isDirty = true;
    }

    @Override
    public void removeValue(String s) {
        this.data.remove(s);
        this.isDirty = true;
    }

    @Override
    public void invalidate() {
        this.expired = true;
        this.isDirty = true;
        if (this.listener != null) {
            this.listener.onInvalidated(this);
        }
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return new SessionEnumeration(this.data);
    }
}
