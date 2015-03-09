package com.jk.common.gateway.http.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.ProxySelector;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientParamBean;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jk.common.gateway.http.CommunicationService;
import com.jk.common.gateway.http.HttpMethodEnum;
import com.jk.common.gateway.http.exception.ConnectionFailureException;
import com.jk.common.gateway.http.exception.UserInterruptedException;
import com.jk.common.gateway.http.handler.Callback;
import com.jk.common.gateway.http.handler.SimpleResponseHandler;


/**
 * This implementation of CommunicationService has a pool of threads that will handle concurrent http connections to the
 * server. When the maximum number of simultaneous connections has been reached (currently 5), the request functions
 * will block until a communication thread becomes available. Request functions will also block while awaiting a server
 * response, with a socket timeout of 5 minutes. This communication framework supports cookies as well as gzip
 * compressed communication, via the Accept-Encoding and Content-Encoding http headers.
 */
public class CommunicationServiceImpl implements CommunicationService {
    private static final Logger LOG = LoggerFactory.getLogger(CommunicationServiceImpl.class);
    private static final String UTF8_STRING = "UTF-8";
    private static final String SOCKET_TIMEOUT_STRING = "http.socket.timeout";
    private static final String HTTP_STRING = "http";
    private static final String HTTPS_STRING = "https";
    private static final String ACCEPT_ENCODING_STRING = "Accept-Encoding";
    private static final String GZIP_STRING = "gzip";
    private static final int SERVER_PORT = 8680;
    private static final int SERVER_SSL_PORT = 8643;

    private static ExecutorService postExecutorPool;

    private AbstractHttpClient httpClient;
    private Map<String,String> defaultHeaders;

    private final Map<ResponseHandler<?>, HttpUriRequest> handlerToReqMap = new HashMap<ResponseHandler<?>, HttpUriRequest>();

    private static SSLContext ctx = null;

    static {
        postExecutorPool = Executors.newCachedThreadPool();


        try {
            ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
        } catch (KeyManagementException e) {
            LOG.error("Could not init SSL KeyManager!", e);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Could not find SSL context for TLS!", e);
        }
        // Set the default SSL context to one that accepts all certificates.
        //SSLContext.setDefault(ctx);

    }

    public CommunicationServiceImpl() {
        defaultHeaders = new HashMap<String, String>();
    }

    public void init() {
        httpClient = setUpHttpClient();
    }

    private static class InnerDefaultConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {
        @Override
        public long getKeepAliveDuration(final HttpResponse httpResponse, final HttpContext httpContext) {
            return 5000;
        }
    }

    private AbstractHttpClient setUpHttpClient() {
        final ClientConnectionManager connectionManager = setUpConnectionManager();
        final HttpParams defaultClientParams = setUpDefaultClientParameters();
        final ProxySelectorRoutePlanner routePlanner = new ProxySelectorRoutePlanner(connectionManager.getSchemeRegistry(),
                ProxySelector.getDefault());

        final AbstractHttpClient innerHttpClient = new DefaultHttpClient(connectionManager, defaultClientParams);
        innerHttpClient.setRoutePlanner(routePlanner);

        innerHttpClient.setKeepAliveStrategy(new InnerDefaultConnectionKeepAliveStrategy());

        innerHttpClient.addResponseInterceptor(new ResponseContentEncoding());

        innerHttpClient.setHttpRequestRetryHandler(new CustomHttpRequestRetryHandler(4, true));

        return innerHttpClient;
    }

    /**
     * Returns an HttpClient for the application to use to download properties before the whole Spring Context is initialized
     * @return
     */
    public static HttpClient getSingleUseHttpClient(int port, String serverProtocol) {

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        if ("https".equals(serverProtocol)) {
            SSLContext sslContext = null;
            try {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
            } catch (KeyManagementException e) {
                LOG.error("Could not init SSL KeyManager!", e);
            } catch (NoSuchAlgorithmException e) {
                LOG.error("Could not find SSL context for TLS!", e);
            }

            schemeRegistry.register(new Scheme(HTTPS_STRING, port, new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)));
        } else {
            schemeRegistry.register(new Scheme(HTTP_STRING, port, PlainSocketFactory.getSocketFactory()));
        }
        ClientConnectionManager cm = new SingleClientConnManager(schemeRegistry);

        HttpClient client = new DefaultHttpClient(cm);
        return client;
    }

    private final class CustomHttpRequestRetryHandler extends DefaultHttpRequestRetryHandler {
        private final int retryCount;

        public CustomHttpRequestRetryHandler(int retryCount, boolean requestSentRetryEnabled) {
            super(retryCount, requestSentRetryEnabled);
            this.retryCount = retryCount;
        }
        //default API, the executionCount start with 1, then increase 1 every time
        public boolean retryRequest(final IOException exception, int executionCount, final HttpContext context) {
            if(exception != null) {
                if (executionCount <= this.retryCount) {
                    LOG.warn("the " + executionCount + " time request failure, ##" + exception.toString());
                    try {
                        Thread.sleep(executionCount * 1000);
                    } catch (InterruptedException e) {
                        LOG.error("Interrupted in HttpClient retry");
                    }
                    return true;
                }
            }
            return super.retryRequest(exception, executionCount, context);
        }
    }

    private ClientConnectionManager setUpConnectionManager() {
        final HttpParams params = new BasicHttpParams();
        ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(25));
        ConnManagerParams.setMaxTotalConnections(params,50);
        params.setParameter(SOCKET_TIMEOUT_STRING, 1000 * 60 * 5);
        SSLSocketFactory ssf = new SSLSocketFactory(ctx);
        ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        final SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme(HTTP_STRING, new PlainSocketFactory(), SERVER_PORT));
        registry.register(new Scheme(HTTPS_STRING, ssf, SERVER_SSL_PORT));

        return new ThreadSafeClientConnManager(params, registry);
    }

    private HttpParams setUpDefaultClientParameters() {
        final HttpParams params = new BasicHttpParams();

        HttpClientParams.setCookiePolicy(params, CookiePolicy.RFC_2109);
        final ClientParamBean paramBean = new ClientParamBean(params);
        final List<Header> headerList = new LinkedList<Header>();

        final Header gzipHeader = new BasicHeader(ACCEPT_ENCODING_STRING, GZIP_STRING);
        headerList.add(gzipHeader);

        for (Map.Entry<String, String> entry : defaultHeaders.entrySet()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Adding Default Header: " + entry.getKey() + ": " + entry.getValue());
            }

            Header header = new BasicHeader(entry.getKey(), entry.getValue());
            headerList.add(header);
        }
        paramBean.setDefaultHeaders(headerList);

        return params;
    }

    @Override
    public String executeRequest(final HttpMethodEnum method, final String url, final Map<String, String> params, final Map<String, String> headers) throws ClientProtocolException {
        return this.executeRequest(method, url, params, headers, new SimpleResponseHandler());
    }

    @Override
    public <T> T executeRequest(final HttpMethodEnum method, final String url, final Map<String, String> params, final Map<String, String> headers, final ResponseHandler<T> handler) throws ClientProtocolException {
        final FutureTask<T> runner = new FutureTask<T>(new FutureRunner<T>(method, url, params, headers, handler));
        postExecutorPool.execute(runner);

        try {
            return runner.get();
        } catch (final InterruptedException e) {
            LOG.debug("Communication task was interrupted: " + e.getMessage());

            // if the task has been interrupted, it is most likely because the user has canceled the file download
            // need to abort the connection to keep http client from reading to the end of the stream
            final HttpUriRequest req = handlerToReqMap.get(handler);

            if (req != null) {
                req.abort();
            }
            throw new UserInterruptedException(e.getMessage() , e);
        } catch (final ExecutionException e) {
            final HttpUriRequest req = handlerToReqMap.get(handler);

            if (req != null) {
                req.abort();
            }

            if (e.getCause() instanceof ConnectionFailureException) {
                throw (ConnectionFailureException) e.getCause();
            }

            if (e.getCause() instanceof HttpResponseException) {
                throw (HttpResponseException) e.getCause();
            }

            LOG.error("Exception executing communication task: " + e.getMessage());
            throw new ClientProtocolException(e.getMessage(), e);
        }
    }

    @Override
    public <T> void executeRequest(final HttpMethodEnum method, final String url, final Map<String, String> params, final Map<String, String> headers, final ResponseHandler<T> handler, Callback<T> callback) {
        final CallbackRunner<T> runner = new CallbackRunner<T>(method, url, params, headers, handler, callback);
        postExecutorPool.execute(runner);
    }

    @Override
    public <T> void executeRequestSynchronously(final HttpMethodEnum method, final String url, final Map<String, String> params, final Map<String, String> headers, final ResponseHandler<T> handler, Callback<T> callback) {
        try {
            callback.onDone(doRequest(method, url, params, headers, handler));
        } catch (ClientProtocolException e) {
            callback.onError(e);
        }
    }

    @Override
    public void executeRequest(final HttpMethodEnum method, final String url, final Map<String, String> params, final Map<String, String> headers, Callback<String> callback) {
        executeRequest(method, url, params, headers, new SimpleResponseHandler(), callback);
    }

    private final class FutureRunner<T> implements Callable<T> {
        private final String url;
        private final Map<String, String> parameterMap;
        private final Map<String, String> headerMap;
        private final HttpMethodEnum method;
        private final ResponseHandler<T> handler; //optional

        private FutureRunner(final HttpMethodEnum method, final String url, final Map<String, String> parameterMap, final Map<String, String> headerMap, final ResponseHandler<T> handler) {
            this.method = method;
            this.url = url;
            this.parameterMap = parameterMap;
            this.headerMap = headerMap;
            this.handler = handler;
        }

        @Override
        public T call() throws ClientProtocolException {
            return doRequest(method, url, parameterMap, headerMap, handler);
        }
    }

    private final class CallbackRunner<T> implements Runnable {
        private final String url;
        private final Map<String, String> parameterMap;
        private final Map<String, String> headerMap;
        private final HttpMethodEnum method;
        private final ResponseHandler<T> handler; //optional
        private final Callback<T> callback;

        private CallbackRunner(final HttpMethodEnum method, final String url, final Map<String, String> parameterMap, final Map<String, String> headerMap, final ResponseHandler<T> handler, final Callback<T> callback) {
            this.method = method;
            this.url = url;
            this.parameterMap = parameterMap;
            this.headerMap = headerMap;
            this.handler = handler;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                callback.onDone(doRequest(method, url, parameterMap, headerMap, handler));
            } catch (ClientProtocolException e) {
                callback.onError(e);
            }
        }
    }

    private HttpUriRequest getHttpMethod(final HttpMethodEnum method, final String url, final Map<String, String> params) {
        HttpUriRequest returnMethod;
        String paramString;

        switch (method) {
            case GET:
                paramString = getParameterString(params);
                if (paramString.length() > 0) {
                    returnMethod = new HttpGet(url + "?" + getParameterString(params));
                } else {
                    returnMethod = new HttpGet(url);
                }
                break;
            case POST:
                final HttpEntity postEntity = getMethodEntityParameters(params);
                returnMethod = new HttpPost(url);
                ((HttpPost) returnMethod).setEntity(postEntity);
                break;
            case PUT:
                final HttpEntity putEntity = getMethodEntityParameters(params);
                returnMethod = new HttpPut(url);
                ((HttpPut) returnMethod).setEntity(putEntity);
                break;
            case DELETE:
                paramString = getParameterString(params);
                if (paramString.length() > 0) {
                    returnMethod = new HttpDelete(url + "?" + getParameterString(params));
                } else {
                    returnMethod = new HttpDelete(url);
                }
                break;
            default:
                final HttpEntity defaultEntity = getMethodEntityParameters(params);
                returnMethod = new HttpPost(url);
                ((HttpPost) returnMethod).setEntity(defaultEntity);
                break;
        }

        return returnMethod;
    }

    private List<NameValuePair> getNameValuePair(final Map<String, String> params) {
        final List<NameValuePair> paramList = new LinkedList<NameValuePair>();
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        return paramList;
    }

    private UrlEncodedFormEntity getMethodEntityParameters(final Map<String, String> params) {
        final List<NameValuePair> paramList = getNameValuePair(params);

        UrlEncodedFormEntity postEntity = null;

        try {
            postEntity = new UrlEncodedFormEntity(paramList, UTF8_STRING);
        } catch (final UnsupportedEncodingException e) {
            LOG.error("Error encoding parameters using encoding " + UTF8_STRING);
        }

        return postEntity;
    }

    private String getParameterString(final Map<String, String> params) {
        final List<NameValuePair> paramList = getNameValuePair(params);
        return URLEncodedUtils.format(paramList, UTF8_STRING);
    }

    private static class NeedsRedirectException extends ClientProtocolException {
        private static final long serialVersionUID = -1124295271348026025L;
        private String uri;

        public NeedsRedirectException(String uri) {
            this.uri = uri;
        }

        public String getUri() {
            return uri;
        }
    }

    private static final class RedirectingResponseHandler<T> implements ResponseHandler<T> {
        private ResponseHandler<T> wrappedHandler;

        private RedirectingResponseHandler(ResponseHandler<T> wrappedHandler) {
            this.wrappedHandler = wrappedHandler;
        }

        @Override
        public T handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            int statusCode = response.getStatusLine().getStatusCode();
            switch (statusCode) {
                case HttpStatus.SC_MOVED_TEMPORARILY:
                case HttpStatus.SC_MOVED_PERMANENTLY:
                case HttpStatus.SC_TEMPORARY_REDIRECT:
                    throw new NeedsRedirectException(getUri(response));
                default:
                    return wrappedHandler.handleResponse(response);
            }
        }

        private String getUri(HttpResponse response) throws ClientProtocolException {
            Header locationHeader = response.getFirstHeader("location");
            if (locationHeader == null) {
                throw new ClientProtocolException("Received redirect response " + response.getStatusLine() + " but no location header");
            }
            String location = locationHeader.getValue();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Redirect requested to location '" + location + "'");
            }

            URI uri;
            try {
                uri = new URI(location);
            } catch (URISyntaxException ex) {
                throw new ClientProtocolException("Invalid redirect URI: " + location, ex);
            }
            if (!uri.isAbsolute()) {
                //Need to use the previous request to build the new uri using the relative redirect.
                //For now, relative redirects are not supported.
                throw new ClientProtocolException("Relative redirect location '" + uri + "' not allowed");
            }
            try {
                return uri.toURL().toString();
            } catch (MalformedURLException e) {
                throw new ClientProtocolException("Malformed URL in the redirect request: " + uri.toASCIIString() , e);
            }
        }
    }

    /**
     * Make a post request to the given url with the given parameters.
     *
     * @param method  The HTTP method to use
     * @param url     The url to post.
     * @param params  The parameters to add to the post method.
     * @param handler The handler for the response, should return type T
     * @return The response, or null if there was an error communicating with the server.
     */
    protected <T> T doRequest(final HttpMethodEnum method, final String url, final Map<String, String> params, final Map<String, String> headers,final ResponseHandler<T> handler) throws ClientProtocolException {
        HttpUriRequest request = getHttpMethod(method, url, params);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }
        ResponseHandler<T> wrappingHandler = new RedirectingResponseHandler<T>(handler);

        T response = null;
        try {
            // map handler to request so we can find it if the request needs to be aborted
            handlerToReqMap.put(wrappingHandler, request);
            response = httpClient.execute(request, wrappingHandler);

        } catch (NeedsRedirectException nde) {
            LOG.error("Processing redirect: " + nde.getUri());
            return doRequest(method, nde.getUri(), params, headers, handler);
        } catch (ClientProtocolException httpe) {
            LOG.error("Error Processing Response: " + httpe.getMessage());
            throw httpe;
        } catch (SocketException se) {
            // this will happen when the user cancels a file download; check to see if aborted before generating event
            if (!request.isAborted()) {
                if (se instanceof HttpHostConnectException
                        || se instanceof NoRouteToHostException) {
                    throw new ConnectionFailureException(se);
                } else {
                    LOG.error("Error Processing Response: " + se.getMessage());
                    throw new ClientProtocolException(se);
                }
            }
        } catch (IOException ioe) {
            if (!request.isAborted()) {
                if (ioe instanceof UnknownHostException) {
                    throw new ConnectionFailureException(new SocketException(ioe.getMessage()));
                }
                LOG.error("Error Processing Response: " + ioe.getMessage());
                throw new ClientProtocolException(ioe);
            }
        } finally {
            // cleanup handler to request mapping since it either completed successfully or was aborted
            handlerToReqMap.remove(wrappingHandler);
        }

        return response;
    }

    @Override
    public void setDefaultHeaders(Map<String, String> headers) {
        this.defaultHeaders.putAll(headers);
    }

    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }
}
