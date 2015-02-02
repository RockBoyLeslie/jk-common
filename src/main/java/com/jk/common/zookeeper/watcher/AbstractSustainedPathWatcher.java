package com.jk.common.zookeeper.watcher;

import com.jk.common.zookeeper.util.ConnectionHelper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public abstract class AbstractSustainedPathWatcher {

    protected ZooKeeper zk;
    private Semaphore semaphore = new Semaphore(1);

    public AbstractSustainedPathWatcher(ZooKeeper zk) {
        this.zk = zk;
    }

    public void sustainedWatch(String path) throws KeeperException, InterruptedException {
        semaphore.acquire();

        while (true) {
            List<String> children = zk.getChildren(path, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged || event.getType() == Event.EventType.NodeDataChanged) {
                        semaphore.release();
                    }
                }
            });

            Collections.sort(children);
            childrenChanged(children);

            semaphore.acquire();
        }
    }

    public abstract void childrenChanged(List<String> children);

}
