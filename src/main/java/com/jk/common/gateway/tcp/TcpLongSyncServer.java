package com.jk.common.gateway.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class TcpLongSyncServer implements TcpServer {

    private static final Logger LOG = Logger.getLogger(TcpLongSyncServer.class);

    // listened port
    private int port = 0;
    // idle time for both sides, when idleTime reached, session idle event will
    // be thrown, default idle time is 30s
    private int idleTime = 30;

    private IoHandler handler;
    private ProtocolCodecFactory codecFactory;
    private ExecutorService threadPool;
    private IoAcceptor acceptor;

    public TcpLongSyncServer(int port, IoHandler handler) {
        this(port, handler, null);
    }

    public TcpLongSyncServer(int port, IoHandler handler, ProtocolCodecFactory codecFactory) {
        this.port = port;
        this.handler = handler;
        this.codecFactory = codecFactory;
        init();
    }

    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }

    @Override
    public void start() throws IOException {
        if (port <= 0) {
            LOG.error(String.format("tcp server failed to start, port [%d] is invalid", port));
        }

        if (idleTime >= 0) {
            acceptor.getSessionConfig().setBothIdleTime(idleTime);
        }

        try {
            acceptor.bind(new InetSocketAddress(port));
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("tcp server listen on port [%d] started", port));
            }
        } catch (IOException e) {
            LOG.error("tcp server failed to start", e);
            throw e;
        }
    }

    @Override
    public void stop() {
        acceptor.unbind();
        threadPool.shutdown();

        if (LOG.isInfoEnabled()) {
            LOG.info(String.format("tcp server listen on port [%d] stoped", port));
        }
    }

    private void init() {

        this.acceptor = new NioSocketAcceptor();
        // set i/o adapter for acceptor
        acceptor.setHandler(handler);
        acceptor.getFilterChain().addLast("logger", new LoggingFilter());

        // set codec factory to filter chain, default is text line codec factory
        if (codecFactory == null) {
            codecFactory = new TextLineCodecFactory(Charset.forName("UTF-8"));
        }
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(codecFactory));

        // set single thread pool for i/o handler
        threadPool = Executors.newCachedThreadPool();
        acceptor.getFilterChain().addLast("threadPool", new ExecutorFilter(threadPool));
    }
}
