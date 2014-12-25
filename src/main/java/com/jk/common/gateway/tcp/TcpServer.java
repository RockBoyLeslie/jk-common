package com.jk.common.gateway.tcp;

import java.io.IOException;

public interface TcpServer {

    public void start() throws IOException;

    public void stop() throws IOException;
}
