package com.jk.common.param;

import com.jk.common.param.config.ParamsNode;
import com.jk.common.param.handler.AbstractParamHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 * 
 * Singleton param loader
 * 
 * @author LICHAO844
 * 
 */

public class ParamContextLoader {

    private static final Logger LOG = Logger.getLogger(ParamContextLoader.class);

    // 调度线程池核心线程数大小
    private static final int SCHEDULE_POOL_SIZE = 10;
    // 调度线程池执行延迟时间， 默认5秒
    private static final long INITIAL_DELAY = 5;
    // 任务调度线程池
    private static ScheduledExecutorService POOL = Executors.newScheduledThreadPool(SCHEDULE_POOL_SIZE);

    private ParamsNode paramsNode;

    public ParamContextLoader(ParamsNode paramsNode) {
        this.paramsNode = paramsNode;
    }

    public void startUp() {
        if (LOG.isInfoEnabled()) {
            LOG.info("load param nodes start");
        }
        
        for (int i = 0; i < paramsNode.size(); ++i) {
            AbstractParamHandler handler = paramsNode.getHandler(i);
            POOL.scheduleAtFixedRate(handler, INITIAL_DELAY, handler.getInterval(), TimeUnit.SECONDS);

            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("param [%s] load, interval [%d]", handler.getName(), handler.getInterval()));
            }
        }
    }

    public void shutDown() {
        POOL.shutdown();
        if (LOG.isInfoEnabled()) {
            LOG.info("param server shutdown");
        }
    }

    public void doForceRun() {
        for (int i = 0; i < paramsNode.size(); i++) {
            AbstractParamHandler handler = paramsNode.getHandler(i);
            POOL.execute(handler);
        }
    }
}
