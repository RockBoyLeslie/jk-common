package com.jk.common.zookeeper.util;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class ConnectionWatcher implements Watcher {

    private static final Logger LOG = Logger.getLogger(ConnectionWatcher.class);
    private static final int SESSION_TIME_OUT = 5000;

    protected ZooKeeper zk;
    private CountDownLatch connectSignal = new CountDownLatch(1);

    public void connect(String hosts) throws IOException {
        connect(hosts, SESSION_TIME_OUT);
    }

    public void connect(String hosts, int sessionTimeout) throws IOException {
        zk = new ZooKeeper(hosts, sessionTimeout, this);
        try {
            connectSignal.await();
        } catch (InterruptedException e) {
            LOG.error("zookeeper connection signal thread interrupted", e);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
            connectSignal.countDown();
        }
    }

    public void close() {
        ConnectionHelper.close(zk);
    }

}
