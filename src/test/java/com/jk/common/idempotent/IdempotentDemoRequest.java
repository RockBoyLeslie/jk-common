/**
 * IdempotentRequest.java
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

/**
 * ClassName:IdempotentRequest
 * Function: TODO ADD FUNCTION
 * Reason:	 TODO ADD REASON
 *
 * @author   chao.li
 * @version  
 * @since    Ver 1.1
 * @Date	 2016年3月2日		下午8:53:22
 *
 * @see 	 
 */
@SuppressWarnings("serial")
public class IdempotentDemoRequest implements Serializable {
    
    private String id;
    
    public IdempotentDemoRequest(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof IdempotentDemoRequest)) {
            return false;
        }
        IdempotentDemoRequest other = (IdempotentDemoRequest) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    
    @Override
    public String toString() {
        return String.format("Request [id=%s]", id);
    }
}

