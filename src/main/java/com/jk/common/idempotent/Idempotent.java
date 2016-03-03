package com.jk.common.idempotent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 幂等注解， 用于修饰需要幂等控制的方法
 *
 * @see
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

}
