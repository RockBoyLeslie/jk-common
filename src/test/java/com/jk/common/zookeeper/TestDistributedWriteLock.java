package com.jk.common.zookeeper;

import com.jk.common.zookeeper.lock.DistributedWriteLock;
import com.jk.common.zookeeper.util.ConnectionHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

public class TestDistributedWriteLock {

    public static void main(String[] args) throws Exception {
        final ZooKeeper zk = ConnectionHelper.connect("localhost:2181");

        ExecutorService pool = Executors.newCachedThreadPool();

        for (int i = 0; i < 5; i++) {
            pool.execute(new Runnable() {

                @Override
                public void run() {
                    DistributedWriteLock lock = new DistributedWriteLock("lock", zk, "/distribute_lock");
                    try {
                        
                        lock.lock();
                        System.out.println(Thread.currentThread().getName() + " get lock");
                        Thread.sleep(2000);
                    } catch (KeeperException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    lock.unlock();
                    System.out.println(Thread.currentThread().getName() + " release lock");
                }

            });
        }

    }
}
