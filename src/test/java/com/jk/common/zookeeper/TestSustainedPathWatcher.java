package com.jk.common.zookeeper;

import com.jk.common.zookeeper.util.ConnectionHelper;
import com.jk.common.zookeeper.watcher.AbstractSustainedPathWatcher;

import java.util.List;

import org.apache.zookeeper.ZooKeeper;

public class TestSustainedPathWatcher extends AbstractSustainedPathWatcher {

    public TestSustainedPathWatcher(ZooKeeper zk) {
        super(zk);
    }

    @Override
    public void childrenChanged(List<String> children) {
        System.out.println(children);
    }

    public static void main(String[] args) throws Exception {
        ZooKeeper zk = ConnectionHelper.connect("localhost:2181,localhost:2182,localhost:2183");
        TestSustainedPathWatcher watcher = new TestSustainedPathWatcher(zk);
        watcher.sustainedWatch("/");
    }

}
