package com.jk.common.gateway.http;

import com.jk.common.gateway.http.handler.Callback;
import java.util.Map;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

/**
 * This class has a pool of threads that will handle concurrent http connections to the server. To make a connection, a
 * class must call executePost and it will return the server's response. When the maximum number of simultaneous
 * connections has been reached, the request functions will block until a communication thread becomes available.
 * Request functions will also block while awaiting a server response.
 */
public interface CommunicationService {

    /**
     * This can be used to set default header values which will be used in every request to the server.
     * @param headers The default headers to use.
     */
    void setDefaultHeaders(Map<String, String> headers);

    /**
     * This function will execute the request on a separate thread and, when finished, will call onDone
     * on the given callback method.  The type of the onDone will be determined by how the responseHandler handles
     * the response.
     * @param method The HTTP method to use.
     * @param url The url to request
     * @param params The parameters to send to the server, un-encoded.
     * @param headers The headers to add to the request
     * @param handler The specific response handler to use.
     * @param callback The callback method that will be called when execution is complete.  If there was an
     *        error processing, the parameter passed to the callback's onDone method will be null.
     * @param <T> The type of object returned by the callback and response handler.
     */
    <T> void executeRequest(final HttpMethodEnum method, final String url, final Map<String, String> params, Map<String, String> headers, final ResponseHandler<T> handler, Callback<T> callback);

    /**
     * This function will execute the request on the Callers thread and, when finished, will call onDone
     * on the given callback method.  The type of the onDone will be determined by how the responseHandler handles
     * the response.
     * @param method The HTTP method to use.
     * @param url The url to request
     * @param params The parameters to send to the server, un-encoded.
     * @param headers The headers to add to the request
     * @param handler The specific response handler to use.
     * @param callback The callback method that will be called when execution is complete.  If there was an
     *        error processing, the parameter passed to the callback's onDone method will be null.
     * @param <T> The type of object returned by the callback and response handler.
     */
    <T> void executeRequestSynchronously(final HttpMethodEnum method, final String url, final Map<String, String> params, Map<String, String> headers, final ResponseHandler<T> handler, Callback<T> callback);

    /**
     * This function executes a post request to the given server with the given parameters. It returns a response based
     * on the response handler passed from the caller. This function blocks until it receives a response.
     *
     * @param method The HTTP method to use
     * @param url    The url to request.
     * @param params The parameters to send to the server, un-encoded.
     * @param headers The headers to add to the request
     * @param handler The specific response handler to use.
     * @return The server's response as a string.
     * @throws org.apache.http.client.ClientProtocolException If there was an error handling the response, a ClientProtocolException will be thrown.
     */
    <T> T executeRequest(HttpMethodEnum method, String url, Map<String, String> params, Map<String, String> headers, ResponseHandler<T> handler) throws ClientProtocolException;
    
    

    /**
     * This function will execute the request on a separate thread and, when finished, will call onDone
     * on the given callback method.  It is a convenience method that provides a string to the user as part of the callback.
     * @param method The HTTP method to use.
     * @param url The url to request
     * @param params The parameters to send to the server, un-encoded.
     * @param headers The headers to add to the request
     * @param callback The callback method that will be called when execution is complete.  If there was an
     *        error processing, the parameter passed to the callback's onDone method will be null.
     */
    void executeRequest(final HttpMethodEnum method, final String url, final Map<String, String> params, Map<String, String> headers, Callback<String> callback);

    /**
     * Default executeRequest method using a default response handler.  This is a convenience method for obtaining a
     * string representation of the server's response.
     * @param method The HTTP method to use.
     * @param url The url to request
     * @param params The parameters to send to the server, un-encoded.
     * @param headers The headers to add to the request
     * @return The http response as a string.
     * @throws org.apache.http.client.ClientProtocolException If there was an error handling the response, a ClientProtocolException will be thrown.
     */
    String executeRequest(HttpMethodEnum method, String url, Map<String, String> params, Map<String, String> headers) throws ClientProtocolException;
}
