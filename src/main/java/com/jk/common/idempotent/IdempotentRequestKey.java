/**
 * IdempotentRequestKey.java
 * com.jk.common.idempotent
 *
 * Function： TODO 
 *
 *   ver     date      		author
 * ──────────────────────────────────
 *   		 2016年3月2日 		chao.li
 *
 * Copyright (c) 2016, Howbuy Rights Reserved.
 */

package com.jk.common.idempotent;

import java.io.Serializable;
import org.aspectj.lang.Signature;

/**
 * ClassName:IdempotentRequestKey Function: TODO ADD FUNCTION Reason: TODO ADD
 * REASON
 *
 * @author chao.li
 * @version
 * @since Ver 1.1
 * @Date 2016年3月2日 下午4:14:39
 *
 * @see
 */
@SuppressWarnings("serial")
public class IdempotentRequestKey implements Serializable {

    private final Signature signature;

    private final IdempotentRequestWrapper request;

    public IdempotentRequestKey(Signature signature, IdempotentRequestWrapper request) {
        this.signature = signature;
        this.request = request;
    }

    @Override
    public String toString() {
        return String.format("Key [signature=%s, request=%s]", signature.toLongString(), request);
    }
    
    public IdempotentRequestWrapper getRequest() {
        return request;
    }
}
