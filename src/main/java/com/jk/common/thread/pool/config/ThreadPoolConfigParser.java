package com.jk.common.thread.pool.config;

import java.io.InputStream;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;

/**
 * 
 */
public final class ThreadPoolConfigParser {

    private static final Logger LOG = Logger.getLogger(ThreadPoolConfigParser.class);
    private static final String THREAD_POOL_CONFIG = "sys-thread-pool.xml";
    private static ThreadPoolFactoryConfig config = null;

    private ThreadPoolConfigParser() {

    }

    public static ThreadPoolFactoryConfig getConfig() {
        if (config == null) {
            synchronized (THREAD_POOL_CONFIG) {
                if (config == null) {
                    try {
                        config = (ThreadPoolFactoryConfig) getDigester().parse(getInputStream());
                    } catch (Exception e) {
                        LOG.error("failed to parse thread pool configuration", e);
                    }
                }
            }
        }

        return config;
    }

    private static InputStream getInputStream() {
        return ThreadPoolConfigParser.class.getClassLoader().getResourceAsStream(THREAD_POOL_CONFIG);
    }

    private static Digester getDigester() {
        Digester digester = new Digester();
        digester.setValidating(false);

        // parse thread pool factory node
        digester.addObjectCreate("pools", ThreadPoolFactoryConfig.class);
        digester.addSetProperties("pools/monitors");
        digester.addFactoryCreate("pools/monitors/monitor", StateMonitorFactory.class);
        digester.addSetNext("pools/monitors/monitor", "addStateMonitor");

        // loop parse thread pool config node
        digester.addObjectCreate("*/pool", ThreadPoolConfig.class);
        digester.addSetProperties("*/pool");
        digester.addSetProperty("*/pool/attribute", "name", "value");
        digester.addSetNext("*/pool", "addThreadPoolConfig");
        
        return digester;
    }

}
