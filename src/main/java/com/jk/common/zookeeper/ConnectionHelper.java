package com.jk.common.zookeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public final class ConnectionHelper {

    private static final Logger LOG = Logger.getLogger(ConnectionHelper.class);
    private static final int SESSION_TIME_OUT = 5000;

    private ConnectionHelper() {
    }

    public static ZooKeeper connect(String host, int timeout) throws IOException {
        final CountDownLatch connectSignal = new CountDownLatch(1);

        ZooKeeper zk = new ZooKeeper(host, timeout, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    connectSignal.countDown();
                }
            }
        });

        try {
            connectSignal.await();
        } catch (InterruptedException e) {
            LOG.error("connection sign thread interrupted", e);
        }

        return zk;
    }

    public static ZooKeeper connect(String host) throws IOException {
        return connect(host, SESSION_TIME_OUT);
    }

}
