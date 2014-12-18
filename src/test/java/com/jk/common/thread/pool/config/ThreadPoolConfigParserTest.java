package com.jk.common.thread.pool.config;

import com.jk.common.thread.pool.config.ThreadPoolConfig;
import com.jk.common.thread.pool.config.ThreadPoolConfigParser;
import com.jk.common.thread.pool.config.ThreadPoolFactoryConfig;
import com.jk.common.thread.pool.state.AbstractStateMonitor;

public class ThreadPoolConfigParserTest {

    public static void main(String[] args) {
        ThreadPoolFactoryConfig config = ThreadPoolConfigParser.getConfig();
        
        System.out.println(String.format("monitor start initial delay %d", config.getInitialDelay()));
        System.out.println(String.format("monitor start period %d", config.getPeriod()));
        for (AbstractStateMonitor monitor : config.getStateMonitorMap().values()) {
            System.out.println(String.format("monitor name %s", monitor.getName()));
        }

        for (ThreadPoolConfig poolConfig : config.getPoolConfigMap().values()) {
            System.out.println(String.format("pool name %s", poolConfig.getName()));
        }
    }
}
