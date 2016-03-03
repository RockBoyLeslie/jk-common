/**
 * IdempotentService.java
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
 * ClassName:IdempotentService Function: TODO ADD FUNCTION Reason: TODO ADD
 * REASON
 *
 * @author chao.li
 * @version
 * @since Ver 1.1
 * @Date 2016年3月2日 下午8:52:56
 *
 * @see
 */
public class IdempotentService {

    @Idempotent
    public long test(@IdempotentRequest IdempotentDemoRequest request) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {

            // TODO Auto-generated catch block
            e.printStackTrace();

        }

        return System.currentTimeMillis();
    }

}
