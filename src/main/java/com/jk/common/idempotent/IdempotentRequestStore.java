package com.jk.common.idempotent;

/**
 * 定义幂等请求分布式环境下的存储接口
 *
 * @see
 */
public interface IdempotentRequestStore {

    boolean contains(IdempotentRequestKey key);

    boolean storenx(IdempotentRequestKey key);

    void setResponse(IdempotentRequestKey key, IdempotentResponseWrapper response);

    IdempotentResponseWrapper getResponse(IdempotentRequestKey key);

}
