package com.jk.common.gateway.http.exception;

import java.net.SocketException;
import org.apache.http.client.ClientProtocolException;

/**
 * This exception is thrown whenever the HttpHostConnectException is seen.
 */
public class ConnectionFailureException extends ClientProtocolException {

    private static final long serialVersionUID = 1545479992766498603L;
    private SocketException exception;

    public ConnectionFailureException(SocketException exception) {
        super();
        this.exception = exception;
    }

    /**
     * Get the socket exception
     * 
     * @return exception - the socket exception
     */
    public SocketException getException() {
        return exception;
    }
}
