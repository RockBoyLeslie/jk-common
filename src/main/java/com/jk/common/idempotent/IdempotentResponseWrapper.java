package com.jk.common.idempotent;

import java.io.Serializable;

/**
 * ClassName:IdempotentResponseWrapper Function: TODO ADD FUNCTION Reason: TODO
 * ADD REASON
 *
 * @author chao.li
 * @version
 * @since Ver 1.1
 * @Date 2016年3月2日 下午3:32:56
 *
 * @see
 */
@SuppressWarnings("serial")
public class IdempotentResponseWrapper implements Serializable {

    private final Object response;

    public IdempotentResponseWrapper(Object response) {
        this.response = response;
    }

    public Object getResponse() {
        return response;
    }

}
