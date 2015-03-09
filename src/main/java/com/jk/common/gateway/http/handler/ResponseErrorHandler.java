package com.jk.common.gateway.http.handler;

import org.apache.http.HttpResponse;

/**
 * Interface for response handler to handle errors of a HTTP response
 *
 * @author Michael Hoglan
 */
public interface ResponseErrorHandler {
    /**
     * Determines if a HttpResponse has errors.
     * Responsible for error event notification and for handling
     * the error.
     * @param httpResponse HTTP response to handle
     * @return True if no errors or they were handled;  False otherwise.
     */
    boolean handleErrors(HttpResponse httpResponse);
}
