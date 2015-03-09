package com.jk.common.gateway.http.handler;


import org.apache.http.client.ClientProtocolException;

/**
 * Clients that wish to receive asynchronous requests through the communicationService should implement this interface.
 * Upon completion of a request, either one of 2 things will happen:
 * 1) For a successful response, the onDone method will be called.
 * 2) In case of an unsuccessful response, or if there was an error handling the response,
 * the onError method will be called with the exception that was received when trying
 * to process the response.
 */
public interface Callback<T> {
    /**
     * This method will be called upon a successful response from the server.
     * @param result The result, which may be null.
     */
    void onDone(T result);

    /**
     * This method will be called for 2 reasons
     * 1) If there is an error communicating with the server. (For example, an IO Exception)
     * 2) If there is an error processing the response.
     * @param exception The exception that was caught when processing the response.
     */
    void onError(ClientProtocolException exception);
}
