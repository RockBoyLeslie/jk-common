/**
 * IdempotentTimeoutException.java
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
/**
 * ClassName:IdempotentTimeoutException
 * Function: TODO ADD FUNCTION
 * Reason:	 TODO ADD REASON
 *
 * @author   chao.li
 * @version  
 * @since    Ver 1.1
 * @Date	 2016年3月2日		下午8:04:48
 *
 * @see 	 
 */
@SuppressWarnings("serial")
public class IdempotentTimeoutException extends RuntimeException {
    
    public IdempotentTimeoutException(IdempotentRequestWrapper request) {
        super(String.format("Timed-out waiting for an idempotent request %s", request));
    }
}

