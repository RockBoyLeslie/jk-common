package com.jk.common.idempotent.aspect;

import org.aspectj.lang.annotation.Around;

import com.jk.common.idempotent.IdempotentRequest;
import com.jk.common.idempotent.IdempotentRequestKey;
import com.jk.common.idempotent.IdempotentRequestStore;
import com.jk.common.idempotent.IdempotentRequestWrapper;
import com.jk.common.idempotent.IdempotentResponseWrapper;
import com.jk.common.idempotent.IdempotentTimeoutException;
import java.lang.annotation.Annotation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 幂等性控制拦截切面，  拦截幂等控制方法 ， 判断缓存中是否存在幂等因子， 存在则做polling操作。
 * 不存在则新增（key-null）到缓存中， 处理业务线程， 并将最后的结果set到缓存中
 *
 * @see
 */
@Aspect
public class IdempotentAspect {

    private final static Logger logger = LoggerFactory.getLogger(IdempotentAspect.class);

    private long maxWait = 60 * 1000;

    private long sleepTime = 1000;

    private IdempotentRequestStore idempotentRequestStore;

    @Around("@annotation(com.jk.common.idempotent.Idempotent)")
    public Object executeIdempotentCall(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.debug("Starting idempotent checks ...");

        IdempotentRequestWrapper request = findIdempotentRequestArg(joinPoint);
        if (request == null) {
            throw new Throwable("No idempotent request setted");
        }

        IdempotentRequestKey key = new IdempotentRequestKey(joinPoint.getSignature(), request);
        if (idempotentRequestStore.contains(key)) {
            return retrieveResponse(key);
        } else {
            logger.debug("Storing the first request {}", key);
            idempotentRequestStore.store(key);
        }

        Object result = joinPoint.proceed();
        if (result != null) {
            logger.debug("Cache response for request {}", request);
            idempotentRequestStore.setResponse(key, new IdempotentResponseWrapper(result));
        }

        return result;
    }

    private Object retrieveResponse(IdempotentRequestKey key) {
        long start = System.currentTimeMillis();

        while (true) {
            IdempotentResponseWrapper response = idempotentRequestStore.getResponse(key);
            if (response != null && response.getResponse() != null) {
                logger.debug("Return a cached response for request {}", key.getRequest());
                return response.getResponse();
            }

            if (System.currentTimeMillis() - start >= maxWait) {
                throw new IdempotentTimeoutException(key.getRequest());
            }

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                return null;
            }
        }
    }

    private IdempotentRequestWrapper findIdempotentRequestArg(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        if (args.length == 0) {
            return new IdempotentRequestWrapper(null);
        } else {
            try {
                MethodSignature signature = (MethodSignature) pjp.getSignature();
                String methodName = signature.getMethod().getName();
                Class<?>[] parameterTypes = signature.getMethod().getParameterTypes();
                Annotation[][] annotations = pjp.getTarget().getClass().getMethod(methodName, parameterTypes).getParameterAnnotations();
                for (int i = 0; i < args.length; i++) {
                    for (Annotation annotation : annotations[i]) {
                        if (annotation instanceof IdempotentRequest) {
                            return new IdempotentRequestWrapper(args[i]);
                        }
                    }
                }
            } catch (NoSuchMethodException | SecurityException e) {
                throw new IllegalStateException("Idempotent method not found", e);
            }
        }
        return null;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void setIdempotentRequestStore(IdempotentRequestStore idempotentRequestStore) {
        this.idempotentRequestStore = idempotentRequestStore;
    }

}
