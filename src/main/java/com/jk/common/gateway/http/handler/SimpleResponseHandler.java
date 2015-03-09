package com.jk.common.gateway.http.handler;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple response handler which will return back a String of the response.
 * Buffers the response to a string and returns it.  Empty string if no response.
 */
public class SimpleResponseHandler implements ResponseHandler<String> {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleResponseHandler.class);

    public SimpleResponseHandler() {
        super();
    }

    @Override
    public String handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
        String response = "";

        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            response = EntityUtils.toString(httpResponse.getEntity());
        } else if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_GONE) {
            // Element is gone
            String error = "Element gone "+  httpResponse.getStatusLine().getStatusCode() + "-" + httpResponse.getStatusLine().getReasonPhrase();
            LOG.error(error);

        } else {
            String error = "Error communicating with server " + httpResponse.getStatusLine().getStatusCode() + "-" + httpResponse.getStatusLine().getReasonPhrase();
            LOG.error(error);
            if (httpResponse.getStatusLine().getStatusCode() != HttpStatus.SC_BAD_REQUEST) {
                throw new ClientProtocolException(error);
            }
        }

        return response;
    }
}
