package com.jk.common.zookeeper.lock;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.recipes.lock.LockListener;
import org.apache.zookeeper.recipes.lock.WriteLock;

public class DistributedWriteLock {

    private static final Logger LOG = Logger.getLogger(DistributedWriteLock.class);
    private static final List<ACL> DEFAULT_ACL = ZooDefs.Ids.OPEN_ACL_UNSAFE;

    private String name;
    private String path;
    private WriteLock writeLock;
    private CountDownLatch lockSignal = new CountDownLatch(1);

    public DistributedWriteLock(String name, ZooKeeper zk, String path) {
        this(name, zk, path, DEFAULT_ACL);
    }

    public DistributedWriteLock(String name, ZooKeeper zk, String path, List<ACL> acl) {
        this.name = name;
        this.path = path;
        this.writeLock = new WriteLock(zk, path, acl, new SyncLockListener());
    }

    public void lock() throws KeeperException, InterruptedException {
        writeLock.lock();
        lockSignal.await();
    }

    public boolean lock(long timeout, TimeUnit timeUnit) throws KeeperException, InterruptedException {
        boolean locked = writeLock.lock();
        return lockSignal.await(timeout, timeUnit) && locked;
    }

    public boolean tryLock() throws KeeperException, InterruptedException {
        return lock(1, TimeUnit.SECONDS);
    }

    public void unlock() {
        writeLock.unlock();
    }

    private class SyncLockListener implements LockListener {

        @Override
        public void lockAcquired() {
            lockSignal.countDown();

            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("Lock acquired by %s on %s\n", name, path));
            }
        }

        @Override
        public void lockReleased() {

        }
    }
}
