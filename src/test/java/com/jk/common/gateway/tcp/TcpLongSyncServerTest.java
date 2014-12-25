package com.jk.common.gateway.tcp;


import org.apache.mina.core.service.IoHandlerAdapter;

public class TcpLongSyncServerTest {

    public static void main(String[] args) throws Exception {
        TcpLongSyncServer server = new TcpLongSyncServer(2000, new IoHandlerAdapter());
        server.start();
        Thread.sleep(2000);
        server.stop();
    }
}
