package com.jk.common.gateway.http.exception;

import org.apache.http.client.ClientProtocolException;

/**
 * Exception thrown when user interrupted an action
 */
public class UserInterruptedException extends ClientProtocolException {

    private static final long serialVersionUID = -3509901945497160599L;

    public UserInterruptedException() {
        super();
    }

    public UserInterruptedException(String s) {
        super(s);
    }

    public UserInterruptedException(String s, Throwable cause) {
        super(s, cause);
    }
}
