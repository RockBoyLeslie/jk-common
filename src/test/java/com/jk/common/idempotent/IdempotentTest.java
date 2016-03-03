/**
 * IdempotentTest.java
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

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * ClassName:IdempotentTest
 * Function: TODO ADD FUNCTION
 * Reason:	 TODO ADD REASON
 *
 * @author   chao.li
 * @version  
 * @since    Ver 1.1
 * @Date	 2016年3月2日		下午8:55:59
 *
 * @see 	 
 */
public class IdempotentTest {
    
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext("IdempotentAspectTests-context.xml");
        
        IdempotentService idempotentService = (IdempotentService) application.getBean("idempotentService");
        
        System.out.println(idempotentService.test(new IdempotentDemoRequest("1")));
        Thread.sleep(10);
        System.out.println(idempotentService.test(new IdempotentDemoRequest("1")));
    }
    
}

