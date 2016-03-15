package com.jk.common.idempotent;

import java.io.Serializable;

/**
 * 幂等请求包装适配， 实现序列化接口， 方便store到redis或者hazelcast等分布式缓存中
 * 
 * @see
 */

@SuppressWarnings("serial")
public class IdempotentRequestWrapper implements Serializable {

    private final Object request;

    public IdempotentRequestWrapper(Object request) {
        this.request = request;
    }

    public Object getRequest() {
        return request;
    }

    @Override
    public int hashCode() {
        return request == null ? 0 : request.hashCode();
    }

    @Override
    public boolean equals(Object that) {
        return request == null ? false : request.equals(that);
    }

    @Override
    public String toString() {
        return String.format("IdempotentRequestWrapper[request=%s]", request);
    }
}
