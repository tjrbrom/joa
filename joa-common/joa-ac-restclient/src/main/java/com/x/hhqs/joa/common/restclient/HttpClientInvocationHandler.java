package com.x.hhqs.joa.common.restclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.x.hhqs.joa.common.restclient.annotation.*;
import com.x.hhqs.joa.common.restclient.annotation.RestClient;
import com.x.hhqs.joa.common.restclient.config.RestClientConfig;
import com.x.hhqs.joa.common.restclient.exception.AuthErrorCode;
import com.x.hhqs.joa.common.restclient.exception.AuthException;
import com.x.hhqs.joa.common.restclient.exception.HttpClientCircuitOpenException;
import com.x.hhqs.joa.common.restclient.processor.ConnectionErrorProcessor;
import com.x.hhqs.joa.common.restclient.processor.HttpErrorCodeProcessor;
import com.x.hhqs.joa.common.restclient.processor.HttpRequestBaseProcessor;
import com.x.hhqs.joa.common.restclient.provider.*;
import com.x.hhqs.joa.common.restclient.processor.RestClientResultProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HttpClientInvocationHandler extends AbstractCircuitBreaker implements InvocationHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientInvocationHandler.class);
    public static final int REFRESH_TIME_IN_MINUTES = 5;
    public static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 20;
    public static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 20;

    private static final String BEARER_TOKEN_TYPE = "Bearer";
    private static final String SPACE = " ";
    private static final String DEFAULT_RESTCONF_SOCKET_READ_TIMEOUT_MS = "300000"; //300 seconds
    private static final String DEFAULT_RESTCONF_CONNECT_TIMEOUT_MS = "15000"; //15 seconds
    public static final String RESTCONF_CONNECT_TIMEOUT_MS = "RESTCONF_CONNECT_TIMEOUT_MS";
    public static final String RESTCONF_SOCKET_READ_TIMEOUT_MS = "RESTCONF_SOCKET_READ_TIMEOUT_MS";
    public static final String USER_CONTEXT_HEADER = "X-usercontext";
    public static final String X_CONTEXT_HEADER = "X-context-";
    public static final String SESSION_ID_HEADER = "X-sessionid";
    public static final String APPLICATION_HEADER = "X-application";
    private static ObjectMapper MAPPER = new ObjectMapper();
    private TokenHolder m_tokenHolder;
    private RestClientMeta m_meta;
    private RestClientConfig m_clientConfig;
    private static final String REST_CB_CONNECTION_STATUS_METRIC = "rest.cb.connection.status";
    private static final String REST_CB_CONNECTION_LOSS_FAILURES_METRIC = "rest.cb.connection.loss.failures";
    private int m_connectTimeout;
    private int m_socketTimeout;
    private CloseableHttpClient m_client;
    private ServiceRegistration<EventHandler> m_eventHandlerReference;
    private HttpMonitor m_httpMonitor;

    public HttpClientInvocationHandler(Class clazz, RestClientConfig clientConfig, HttpMonitor httpMonitor) {
        super(clazz.getSimpleName() + "-RestClient");
        this.m_meta = extractRestClientMeta(clazz);
        this.m_clientConfig = clientConfig;
        this.m_httpMonitor = httpMonitor;
        int maxConnectionsPerRoute = clientConfig.getMaxConnectionsPerRoute() > 0 ? clientConfig.getMaxConnectionsPerRoute() : DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
        int maxTotalConnections = clientConfig.getMaxTotalConnections() > 0 ? clientConfig.getMaxTotalConnections() : DEFAULT_MAX_TOTAL_CONNECTIONS;

        HttpClientBuilder builder = HttpClients.custom().setMaxConnPerRoute(maxConnectionsPerRoute).setMaxConnTotal(maxTotalConnections);

        m_client = clientConfig.isHttps() ? builder.setSSLSocketFactory(clientConfig.getSSLSocketFactory()).build() : builder.build();

        m_tokenHolder = new TokenHolder();
        int connectTimeout = clientConfig.getConnectTimeout() > 0 ? clientConfig.getConnectTimeout() : getTimeoutFromEnvOrSysProperty(RESTCONF_CONNECT_TIMEOUT_MS, DEFAULT_RESTCONF_CONNECT_TIMEOUT_MS);
        int readTimeout = clientConfig.getReadTimeout() > 0 ? clientConfig.getReadTimeout() : getTimeoutFromEnvOrSysProperty(RESTCONF_SOCKET_READ_TIMEOUT_MS, DEFAULT_RESTCONF_SOCKET_READ_TIMEOUT_MS);
        this.setConnectTimeout(connectTimeout);
        this.setSocketTimeout(readTimeout);
    }

    public int getConnectTimeout() {
        return m_connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        m_connectTimeout = connectTimeout;
    }

    public int getSocketTimeout() {
        return m_socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        m_socketTimeout = socketTimeout;
    }

    public void setEventHandlerReference(ServiceRegistration<EventHandler> eventHandlerReference) {
        m_eventHandlerReference = eventHandlerReference;
    }

    public ServiceRegistration<EventHandler> getEventHandlerReference() {
        return m_eventHandlerReference;
    }

    // Wiki:
    //   1. https://www.w3.org/Protocols/HTTP/HTRESP.html
    //   2. https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
    //   3. https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
    // 2xx codes indicate success.
    private static final Set<Integer> SUCCESSFUL_HTTP_CODE = new HashSet<>();
    static {
        SUCCESSFUL_HTTP_CODE.add(HttpURLConnection.HTTP_OK);
        SUCCESSFUL_HTTP_CODE.add(HttpURLConnection.HTTP_CREATED);
        SUCCESSFUL_HTTP_CODE.add(HttpURLConnection.HTTP_ACCEPTED);
        SUCCESSFUL_HTTP_CODE.add(HttpURLConnection.HTTP_NOT_AUTHORITATIVE);
        SUCCESSFUL_HTTP_CODE.add(HttpURLConnection.HTTP_NO_CONTENT);
        SUCCESSFUL_HTTP_CODE.add(HttpURLConnection.HTTP_RESET);
        SUCCESSFUL_HTTP_CODE.add(HttpURLConnection.HTTP_PARTIAL);
    }

    private int getTimeoutFromEnvOrSysProperty(String envOrSysPropName, String defaultValue) {
        int timeout;
        try {
            timeout = Integer.parseInt(SystemPropertyUtils.getInstance()
                    .getFromEnvOrSysProperty(envOrSysPropName, defaultValue));
        } catch (NumberFormatException e) {
            LOGGER.error(null, "Exception while getting {} ", envOrSysPropName, e);
            timeout = Integer.parseInt(defaultValue);
        }
        return timeout;
    }

    @Override
    public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
        // Invoke get token info from tokenHolder. It is not annotated with RestClient --> Just return token in tokenHolder
        if (method.equals(m_meta.tokenInfoMethod)) {
            return m_tokenHolder.getToken();
        }

        // If the method is not annotated with RestClient --> execute it normally
        if (!method.isAnnotationPresent(RestClient.class)) {
            return method.invoke(obj, args);
        }

        if(isDisconnected() && !method.getAnnotation(RestClient.class).byPassCB()){
            LOGGER.debug("Not Invoking {} method due to Http client circuit open", method.getName());
            incrementFailureCount();
            throw new HttpClientCircuitOpenException();
        }

        // Extract annotation & special interface from input args for extra processing
        RestRequestParam requestParam = extractRestRequestParam(method, args);
        // If login method is invoked --> cache the TokenResponse for further usage
        if (method.equals(m_meta.loginMethod)) {
            TokenResponse tr = (TokenResponse) processMethodInvocation(requestParam, m_clientConfig, method,
                    () -> m_clientConfig.getBasicAuthorization(), m_client);
            m_tokenHolder.setSafeToken(new TokenResponseWrapper(tr));
            return tr;
        }

        // For any other method, Bearer access_token will be included in the Authorization header
        // If logout method is invoke --> cleanup token cache
        if (method.equals(m_meta.logoutMethod)) {
            TokenResponseWrapper tokenResponseWrapper = m_tokenHolder.getToken();
            AuthorizationProvider provider = getAuthorizationProvider();
            if (tokenResponseWrapper != null) {
                processMethodInvocation(requestParam, m_clientConfig, method, provider, m_client);
            }
            m_tokenHolder.setSafeToken(null);
            return null;
        }
        // If refreshAccessToken is invoked --> update token access with the new one
        if (method.equals(m_meta.refreshTokenMethod)) {
            TokenResponse tr;
            try {
                AuthorizationProvider provider = getAuthorizationProvider();
                tr = (TokenResponse) processMethodInvocation(requestParam, m_clientConfig, method, provider, m_client);
                m_tokenHolder.setSafeToken(new TokenResponseWrapper(tr));
            } catch (Exception e) {
                LOGGER.warn("Refresh token failed, getting new token", e);
                LoginStaticParamsProvider loginStaticParamsProvider = m_clientConfig.getLoginStaticParamsProvider();
                if (loginStaticParamsProvider == null) {
                    tr = (TokenResponse) m_meta.loginMethod.invoke(obj);
                } else {
                    Object[] params = buildExtraParamFromParamProvider(loginStaticParamsProvider);
                    tr = (TokenResponse) m_meta.loginMethod.invoke(obj, params);
                }
            }
            return tr;
        }
        // If there is no token in the cache --> handle it normally without authorization header
        if (m_tokenHolder.getToken() == null) {
            return processMethodInvocation(requestParam, m_clientConfig, method, requestParam.authProvider, m_client);
        }
        // If there is token in the ache --> we need to check & refresh token if it is nearly expired
        validateToken();

        try {
            AuthorizationProvider provider = getAuthorizationProvider();
            // At this place, token if fine for access REST API
            return processMethodInvocation(requestParam, m_clientConfig, method, provider, true, m_client);
        } catch (AuthException ex) {
            // sometime token is invalid due to some reasons like server is rebooted
            LOGGER.warn("Token {} is invalid, getting new token and trying again", m_tokenHolder.getToken(), ex);
            LoginStaticParamsProvider loginStaticParamsProvider = m_clientConfig.getLoginStaticParamsProvider();
            if (loginStaticParamsProvider == null) {
                m_meta.loginMethod.invoke(obj);
            } else {
                Object[] params = buildExtraParamFromParamProvider(loginStaticParamsProvider);
                m_meta.loginMethod.invoke(obj, params);
            }
            // Now, token is fine, try again
            AuthorizationProvider provider = getAuthorizationProvider();

            LOGGER.debug("retrying call to {} using new token {}", method, m_tokenHolder.getToken());
            return processMethodInvocation(requestParam, m_clientConfig, method, provider, m_client);
        }
    }

    public boolean isRefreshTokenExpired(TokenResponseWrapper token) {
        if (token == null) {
            return true;
        }
        return System.currentTimeMillis() >= token.getRefreshExpiresAt();
    }

    public boolean needRefreshAccessToken(RestClientConfig clientConfig, TokenResponseWrapper token) {
        if (token == null) {
            return true;
        }
        long refreshTime = clientConfig.getTokenRefreshTime() > 0 ?
                clientConfig.getTokenRefreshTime() : TimeUnit.MINUTES.toMillis(REFRESH_TIME_IN_MINUTES);
        return System.currentTimeMillis() >= token.getExpiresAt() - refreshTime;
    }

    private AuthorizationProvider getAuthorizationProvider() {
        TokenResponseWrapper tokenResponseWrapper = m_tokenHolder.getToken();
        return () -> tokenResponseWrapper != null ? (getTokenType(tokenResponseWrapper.getTokenResponse()) + tokenResponseWrapper.getTokenResponse().getAccessToken()) : null;
    }

    private void validateToken() throws Exception {
        try {
            m_tokenHolder.lock.writeLock().lock();
            TokenResponseWrapper tokenResponseWrapper = m_tokenHolder.getToken();
            if (needRefreshAccessToken(m_clientConfig, tokenResponseWrapper)) {
                LoginStaticParamsProvider loginStaticParamsProvider = m_clientConfig.getLoginStaticParamsProvider();
                RefreshTokenStaticParamsProvider refreshTokenStaticParamsProvider = m_clientConfig.getRefreshTokenStaticParamsProvider();

                if (isRefreshTokenExpired(tokenResponseWrapper)) {
                    TokenResponse tr = callLogin(loginStaticParamsProvider);
                    m_tokenHolder.setToken(new TokenResponseWrapper(tr));
                    LOGGER.debug("Login returned new token. {}", tr.getAccessToken());
                } else {
                    // Refresh token is not expired --> use it to refresh token
                    TokenResponse tr = callRefreshToken(tokenResponseWrapper, refreshTokenStaticParamsProvider, loginStaticParamsProvider);
                    m_tokenHolder.setToken(new TokenResponseWrapper(tr));
                    LOGGER.debug("Refresh token returned token. {}", tr.getRefreshToken());
                }
            }
        } finally {
            m_tokenHolder.lock.writeLock().unlock();
        }
    }

    private TokenResponse callLogin(LoginStaticParamsProvider loginStaticParamsProvider) throws Exception {
        if (loginStaticParamsProvider == null) {
            RestRequestParam requestParam = extractRestRequestParam(m_meta.loginMethod, new Object[]{});
            return (TokenResponse) processMethodInvocation(requestParam, m_clientConfig, m_meta.loginMethod,
                    () -> m_clientConfig.getBasicAuthorization(), m_client);
        } else {
            // If not, we need to use loginMethod to get new access_token
            Object[] params = buildExtraParamFromParamProvider(loginStaticParamsProvider);
            RestRequestParam requestParam = extractRestRequestParam(m_meta.loginMethod, params);
            return (TokenResponse) processMethodInvocation(requestParam, m_clientConfig, m_meta.loginMethod,
                    () -> m_clientConfig.getBasicAuthorization(), m_client);
        }
    }

    private TokenResponse callRefreshToken(TokenResponseWrapper tokenResponseWrapper,
                                           RefreshTokenStaticParamsProvider refreshTokenStaticParamsProvider,
                                           LoginStaticParamsProvider loginStaticParamsProvider) throws Exception {
        try {
            String refreshToken = tokenResponseWrapper.getTokenResponse().getRefreshToken();
            AuthorizationProvider provider = () -> getTokenType(tokenResponseWrapper.getTokenResponse()) + tokenResponseWrapper.getTokenResponse().getAccessToken();
            if (refreshTokenStaticParamsProvider == null) {
                RestRequestParam requestParam = extractRestRequestParam(m_meta.refreshTokenMethod, new Object[]{refreshToken});
                return (TokenResponse) processMethodInvocation(requestParam, m_clientConfig, m_meta.refreshTokenMethod, provider, true, m_client);
            } else {
                Object[] params = buildExtraParamFromParamProvider(Arrays.asList(refreshToken), refreshTokenStaticParamsProvider);
                RestRequestParam requestParam = extractRestRequestParam(m_meta.refreshTokenMethod, params);
                return (TokenResponse) processMethodInvocation(requestParam, m_clientConfig, m_meta.refreshTokenMethod, provider, true, m_client);
            }
        } catch (Exception e) {
            LOGGER.warn("Refresh token failed, getting new token", e);
            return callLogin(loginStaticParamsProvider);
        }
    }

    private RestRequestParam extractRestRequestParam(Method method, Object[] args) {
        RestRequestParam requestParam = new RestRequestParam();
        if (args == null) {
            return requestParam;
        }
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                continue;
            }
            if (AuthorizationProvider.class.isAssignableFrom(arg.getClass())) {
                requestParam.authProvider = (AuthorizationProvider) arg;
            } else if (LoginStaticParamsProvider.class.isAssignableFrom(arg.getClass())) {
                requestParam.loginParamsProvider = (LoginStaticParamsProvider) arg;
            } else if (LogoutStaticParamsProvider.class.isAssignableFrom(arg.getClass())) {
                requestParam.logoutParamsProvider = (LogoutStaticParamsProvider) arg;
            } else if (RefreshTokenStaticParamsProvider.class.isAssignableFrom(arg.getClass())) {
                requestParam.refreshTokenParamsProvider = (RefreshTokenStaticParamsProvider) arg;
            } else if (RestClientResultProcessor.class.isAssignableFrom(arg.getClass())) {
                requestParam.responseParser = (RestClientResultProcessor) arg;
            } else if (HttpRequestBaseProcessor.class.isAssignableFrom(arg.getClass())) {
                requestParam.requestProcessor = (HttpRequestBaseProcessor) arg;
            } else if (HttpErrorCodeProcessor.class.isAssignableFrom(arg.getClass())) {
                requestParam.httpErrorCodeProcessor = (HttpErrorCodeProcessor) arg;
            } else if (ConnectionErrorProcessor.class.isAssignableFrom(arg.getClass())) {
                requestParam.connectionErrorProcessor = (ConnectionErrorProcessor) arg;
            } else if (method.getParameterAnnotations() != null && method.getParameterAnnotations().length > 0
                    && i < method.getParameterAnnotations().length) {
                // Parse annotation in parameters
                Annotation[] annotations = method.getParameterAnnotations()[i];
                if (annotations != null && annotations.length > 0) {
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof Param) {
                            if (arg instanceof List) {
                                requestParam.listPathParams.put(((Param) annotation).value(), (List<String>) arg);
                            } else {
                                requestParam.pathParams.put(((Param) annotation).value(), String.valueOf(arg));
                            }
                        } else if (annotation instanceof FormParam) {
                            requestParam.formParams.put(((FormParam) annotation).value(), String.valueOf(arg));
                        } else if (annotation instanceof RawBodyData) {
                            requestParam.rawBodyData = String.valueOf(arg);
                        } else if (annotation instanceof EffectiveBaseUrl) {
                            requestParam.effectiveBaseUrl = String.valueOf(arg);
                        } else if (annotation instanceof HeaderParam) {
                            requestParam.additionalHeaders.put(((HeaderParam)annotation).value(), String.valueOf(arg));
                        }
                    }
                }
            }
        }
        return requestParam;
    }

    private Object processMethodInvocation(RestRequestParam requestParam, RestClientConfig clientConfig,
                                           Method method, AuthorizationProvider authorizationProvider, CloseableHttpClient httpClient) throws Exception {
        return processMethodInvocation(requestParam, clientConfig, method, authorizationProvider, false, httpClient);
    }

    private Object processMethodInvocation(RestRequestParam requestParam, RestClientConfig clientConfig,
                                           Method method, AuthorizationProvider authorizationProvider, boolean invalidTokenRetry, CloseableHttpClient httpClient) throws Exception {
        long startTime = System.currentTimeMillis();
        RestClient annotation = method.getAnnotation(RestClient.class);
        String path = annotation.path(); // path defined in Method

        BaseURL baseURLAnnotation = method.getAnnotation(BaseURL.class);
        String baseUrl = getBaseUrl(baseURLAnnotation, clientConfig); // base url defined in Method or Client Config

        // Replace parameter in URI defined in method & class
        if (!requestParam.pathParams.isEmpty()) {
            for (Map.Entry<String, String> entry : requestParam.pathParams.entrySet()) {
                path = path.replaceAll(Pattern.quote("{" + entry.getKey() + "}"), entry.getValue());
            }
        }

        // Replace list parameter
        if (!requestParam.listPathParams.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : requestParam.listPathParams.entrySet()) {
                StringJoiner params = new StringJoiner("&");
                for (String value : entry.getValue()) {
                    params.add(entry.getKey() + "=" + value);
                }
                path = path.replaceAll(entry.getKey() + "=\\{" + entry.getKey() + "\\}", params.toString());
            }
        }

        // Build URL
        if (StringUtils.isNotEmpty(requestParam.effectiveBaseUrl)) {
            baseUrl = requestParam.effectiveBaseUrl;
        }
        String url = isFullURL(path) ? path : baseUrl + path;

        // Build request method: GET, POST...
        HttpRequestBase action = annotation.method().createHttpMethodInstance(url);
        RequestConfig.Builder requestConfig = RequestConfig.custom();
        requestConfig.setConnectTimeout(m_connectTimeout);
        requestConfig.setSocketTimeout(m_socketTimeout);
        action.setConfig(requestConfig.build());

        // Add header to the request
        if (!annotation.mediaType().isEmpty()) {
            action.addHeader(HttpHeaders.CONTENT_TYPE, annotation.mediaType());
        }
        if (StringUtils.isNotEmpty(annotation.acceptMediaType())) {
            action.addHeader(HttpHeaders.ACCEPT, annotation.acceptMediaType());
        }
        requestParam.additionalHeaders.forEach((name, value) -> action.addHeader(name, value));
        if (method.isAnnotationPresent(AuthScheme.class) && method.getAnnotation(AuthScheme.class).value().equals(AuthType.BASIC)) {
            action.addHeader(HttpHeaders.AUTHORIZATION, m_clientConfig.getBasicAuthorization());
        } else if (authorizationProvider != null && authorizationProvider.getAuthorization() != null) {
            action.addHeader(HttpHeaders.AUTHORIZATION, authorizationProvider.getAuthorization());
        }
        addUserContextHeaders(action);
        // Build FormParam if any
        if (!requestParam.formParams.isEmpty() && action instanceof HttpEntityEnclosingRequestBase) {
            List<NameValuePair> form = new LinkedList<>();
            requestParam.formParams.forEach((key, value) -> form.add(new BasicNameValuePair(key, value)));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
            ((HttpEntityEnclosingRequestBase) action).setEntity(entity);
        }

        // build body data if any
        if (requestParam.rawBodyData != null && !requestParam.rawBodyData.isEmpty() && action instanceof HttpEntityEnclosingRequestBase) {
            StringEntity entity = new StringEntity(requestParam.rawBodyData, Consts.UTF_8);
            entity.setContentType(annotation.mediaType());
            ((HttpEntityEnclosingRequestBase) action).setEntity(entity);
        }

        if (requestParam.requestProcessor != null) {
            requestParam.requestProcessor.process(action);
        }

        String target = getTarget(baseUrl);
        boolean success = false;
        // Submit the request
        try (CloseableHttpResponse response = createCloseableHttpResponse(action, httpClient, annotation.byPassCB())) {

            int statusCode = response.getStatusLine().getStatusCode();
            LOGGER.debug("Got status line {}, for request: {}, invalid token retry: {}", response.getStatusLine(),
                    url, invalidTokenRetry);
            if (!isSuccessfulHttpCode(statusCode)) {
                String responseText = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "";

                if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED && invalidTokenRetry) {
                    LOGGER.debug("ERROR: {}, retrying with new token, response: {} . Failed to request {} ," +
                                    "connection timeout ms: {}, socket timeout ms: {}",
                            statusCode, responseText, url, action.getConfig().getConnectTimeout(),
                            action.getConfig().getSocketTimeout());

                    // throw auth exception to get new token and exec rest again.
                    throw new AuthException(AuthErrorCode.INVALID_ACCESS_TOKEN);
                }
                if (statusCode == HttpURLConnection.HTTP_FORBIDDEN && invalidTokenRetry) {
                    LOGGER.debug("ERROR: {}, retrying with new token, response: {} . Failed to request {} ," +
                                    "connection timeout ms: {}, socket timeout ms: {}",
                            statusCode, responseText, url, action.getConfig().getConnectTimeout(),
                            action.getConfig().getSocketTimeout());

                    // throw auth exception to get new token and exec rest again.
                    throw new AuthException(AuthErrorCode.OPERATION_NOT_AUTHORIZED);
                }
                if (clientConfig.getInvalidTokenProcessor() != null) {
                    clientConfig.getInvalidTokenProcessor().process(statusCode, responseText, invalidTokenRetry);
                }

                LOGGER.error("ERROR: {}, response: {} . Failed to request {} ," +
                                "connection timeout ms: {}, socket timeout ms: {}",
                        statusCode, responseText, url, action.getConfig().getConnectTimeout(),
                        action.getConfig().getSocketTimeout());
                return requestParam.httpErrorCodeProcessor != null ?
                        requestParam.httpErrorCodeProcessor.process(statusCode, responseText) : null;
            }

            // Parsing the result
            success = true;
            return parseResult(requestParam, response, method.getReturnType());
        } catch (Exception e) {
            if (isConnectionErrorException(e)) {
                Set<ConnectionErrorProcessor> globalConnectionErrorProcessors = clientConfig.getGlobalConnectionErrorProcessors();

                if (globalConnectionErrorProcessors != null && !globalConnectionErrorProcessors.isEmpty()) {

                    globalConnectionErrorProcessors.forEach(connectionErrorProcessor -> {
                        if (connectionErrorProcessor != null) {
                            connectionErrorProcessor.process(e);
                        }
                    });
                }

                if (requestParam.connectionErrorProcessor != null) {
                    requestParam.connectionErrorProcessor.process(e);
                }
            }
            throw e;
        } finally {
            long elapsedTime = System.currentTimeMillis() - startTime;
            updateHttpMetrics(baseUrl, target, success, elapsedTime);
        }
    }

    private void addUserContextHeaders(HttpRequestBase action) {
        UserContext userContext = RequestContext.getLoggedInUserCtxtTL();
        String application = RequestContext.getApplicationTL();
        if (userContext!= null && StringUtils.isNotEmpty(userContext.getUsername())) {
            action.addHeader(USER_CONTEXT_HEADER, userContext.getUsername());
        }
        if (userContext!= null && StringUtils.isNotEmpty(userContext.getSessionId())) {
            action.addHeader(SESSION_ID_HEADER, userContext.getSessionId());
        }
        if (StringUtils.isNotEmpty(application)) {
            action.addHeader(APPLICATION_HEADER, application);
        }
    }

    private String getTarget(String aUrl) {
        try {
            URL url = new URL(aUrl);
            return url.getHost();
        } catch (Exception e) {
            LOGGER.error("Error while getting target from url: {}", aUrl, e);
            return "";
        }
    }

    private void updateHttpMetrics(String baseUrl, String target, Boolean success, long elapsedTime) {
        m_httpMonitor.updateRequestCount(1, baseUrl, target, success);
        m_httpMonitor.updateRequestTime(elapsedTime, baseUrl, target);
    }

    protected InitialContext getInitialContext() throws Exception {
        return new InitialContext();
    }

    private boolean isFullURL(String path) {
        return path.startsWith("http:") || path.startsWith("https:");
    }

    private boolean isSuccessfulHttpCode(int httpStatus) {
        return SUCCESSFUL_HTTP_CODE.contains(httpStatus);
    }

    private <T> T parseResult(RestRequestParam requestParam, CloseableHttpResponse rawResponse, Class<T> returnType) throws Exception {
        // If the invoked method is void -> return nothing
        if (Void.TYPE.equals(returnType)) {
            return null;
        }
        // Looking at the input args, if client define RestClientResultProcessor --> delegate parser to it
        if (requestParam.responseParser != null) {
            return (T) requestParam.responseParser.parse(rawResponse);
        }
        // Return string value immediately
        if (String.class.equals(returnType)) {
            return (T) RestClientUtils.getInstance().getResponseText(rawResponse);
        }
        // If the response is Collection -> parse it using collection fashion
        if (Collection.class.isAssignableFrom(returnType)) {
            return (T) MAPPER.readValue(RestClientUtils.getInstance().getResponseText(rawResponse), new TypeReference<List<T>>() {
            });
        }
        // If the return class having a constructor with one String argument -> use it
        try {
            Constructor<?> constructor = returnType.getConstructor(String.class);
            if (constructor != null) {
                return (T) constructor.newInstance(RestClientUtils.getInstance().getResponseText(rawResponse));
            }
        } catch (NoSuchMethodException | SecurityException e) {
            // Silently ignore the exception, because we will try using Jackson
        }
        // parsing object using Jackson API
        return MAPPER.readValue(RestClientUtils.getInstance().getResponseText(rawResponse), returnType);
    }

    public CloseableHttpResponse createCloseableHttpResponse(HttpRequestBase action, CloseableHttpClient httpClient) throws IOException {
        return createCloseableHttpResponse(action, httpClient, false);
    }

    public CloseableHttpResponse createCloseableHttpResponse(HttpRequestBase action, CloseableHttpClient httpClient, boolean byPassCB) throws IOException {
        if (isDisconnected() && !byPassCB) {
            LOGGER.debug(null, "Http client circuit is open can't perform the request : {} with URI: {}", action.getMethod(), action.getURI());
            incrementFailureCount();
            throw new HttpClientCircuitOpenException();
        }

        CloseableHttpResponse httpResponse;
        try {
            httpResponse = getHttpResponse(action, httpClient);
        } catch (Exception e) {
            if (m_eventHandlerReference != null && shouldTripCircuit(e)) {
                tripCircuit(e);
                incrementFailureCount();
                throw new HttpClientCircuitOpenException(e);
            }
            throw e;
        }
        return httpResponse;
    }

    protected CloseableHttpResponse getHttpResponse(HttpRequestBase action, CloseableHttpClient client) throws IOException {
        return client.execute(action);
    }

    @Override
    public int getCircuitBreakerConnectionStatus(){
        return super.getCircuitBreakerConnectionStatus();
    }

    @Override
    public long getFailureCountDueToCircuitOpen(){
        return super.getFailureCountDueToCircuitOpen();
    }

    private String getTokenType(TokenResponse tokenResponse) {
        String tokenType = tokenResponse != null ? tokenResponse.getTokenType() : BEARER_TOKEN_TYPE;

        if (StringUtils.isEmpty(tokenType)) {
            tokenType = BEARER_TOKEN_TYPE;
        }

        return tokenType.endsWith(SPACE) ? tokenType : tokenType.concat(SPACE);
    }

    private Object[] buildExtraParamFromParamProvider(StaticParamsProvider paramsProvider) {
        return buildExtraParamFromParamProvider(Collections.emptyList(), paramsProvider);
    }

    private Object[] buildExtraParamFromParamProvider(List<String> inputParam, StaticParamsProvider paramsProvider) {

        List<String> params = new LinkedList<>(inputParam == null ? Collections.emptyList() : inputParam);

        List<Map.Entry<String, String>> paramFromProvider = paramsProvider != null ? paramsProvider.getParams() : Collections.emptyList();
        params.addAll(paramFromProvider.stream().map(Map.Entry::getValue).collect(Collectors.toCollection(LinkedList::new)));

        return params.toArray();
    }

    private boolean isConnectionErrorException(Throwable e) {
        while (e != null) {
            if (e instanceof UnknownHostException
                    || e instanceof SocketException
                    || e instanceof SocketTimeoutException
                    || e instanceof ClientProtocolException
                    || e instanceof IOException
                    || e instanceof HttpClientCircuitOpenException) {
                LOGGER.debug(null, "Connection error occurred with exception {}", e.getMessage(), e);
                return true;
            }
            e = e.getCause();
        }
        return false;
    }

    private String getBaseUrl(BaseURL baseURLAnnotation, RestClientConfig clientConfig) {
        if (baseURLAnnotation == null) {
            return clientConfig.getBaseUrl();
        }

        String urlValue = baseURLAnnotation.value();

        if (StringUtils.isEmpty(urlValue)) {
            return clientConfig.getBaseUrl();
        }

        if (urlValue.startsWith(RestClientConfig.ENV_CONFIG_PREFIX)) {
            String envValue = clientConfig.getEnvConfiguration(urlValue);
            return StringUtils.isEmpty(envValue) ? clientConfig.getBaseUrl() : envValue;
        }

        return urlValue;
    }

    private RestClientMeta extractRestClientMeta(Class clazz) {
        RestClientMeta meta = new RestClientMeta();

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Login.class)) {
                meta.loginMethod = method;
            } else if (method.isAnnotationPresent(Logout.class)) {
                meta.logoutMethod = method;
            } else if (method.isAnnotationPresent(RefreshAccessToken.class)) {
                meta.refreshTokenMethod = method;
            } else if (method.isAnnotationPresent(TokenInfo.class)) {
                meta.tokenInfoMethod = method;
            } else if (method.isAnnotationPresent(BaseURL.class)) {
                meta.baseUrlMethod = method;
            }
        }
        return meta;
    }

    private static class RestRequestParam {
        AuthorizationProvider authProvider;
        LoginStaticParamsProvider loginParamsProvider;
        LogoutStaticParamsProvider logoutParamsProvider;
        RefreshTokenStaticParamsProvider refreshTokenParamsProvider;
        RestClientResultProcessor responseParser;
        HttpRequestBaseProcessor requestProcessor;
        HttpErrorCodeProcessor httpErrorCodeProcessor;
        ConnectionErrorProcessor connectionErrorProcessor;
        String effectiveBaseUrl;
        Map<String, String> pathParams = new HashMap<>();
        Map<String, String> additionalHeaders = new HashMap<>();
        Map<String, List<String>> listPathParams = new HashMap<>();
        Map<String, String> formParams = new HashMap<>();
        String rawBodyData;
    }

    protected static class RestClientMeta {
        Method loginMethod;
        Method logoutMethod;
        Method refreshTokenMethod;
        Method tokenInfoMethod;
        Method baseUrlMethod;
    }

    private static class TokenHolder {
        private TokenResponseWrapper m_token;
        private ReadWriteLock lock;

        public TokenHolder() {
            lock = new ReentrantReadWriteLock();
        }

        public TokenResponseWrapper getToken() {
            try {
                lock.readLock().lock();
                return m_token;
            } finally {
                lock.readLock().unlock();
            }
        }

        public void setSafeToken(TokenResponseWrapper token) {
            try {
                lock.writeLock().lock();
                this.m_token = token;
            } finally {
                lock.writeLock().unlock();
            }
        }

        public void setToken(TokenResponseWrapper token) {
            this.m_token = token;
        }
    }
}
