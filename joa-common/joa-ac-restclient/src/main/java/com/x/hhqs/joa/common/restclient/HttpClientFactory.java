package com.x.hhqs.joa.common.restclient;

import com.x.hhqs.joa.common.restclient.config.RestClientConfig;
import com.x.hhqs.joa.common.restclient.ha.HighAvailabilityConstants;
import com.x.hhqs.joa.common.restclient.processor.ConnectionErrorProcessor;
import com.x.hhqs.joa.common.restclient.processor.HttpErrorCodeProcessor;
import com.x.hhqs.joa.common.restclient.processor.HttpRequestBaseProcessor;
import com.x.hhqs.joa.common.restclient.provider.AuthorizationProvider;
import com.x.hhqs.joa.common.restclient.provider.StaticParamsProvider;
import com.x.hhqs.joa.common.restclient.processor.RestClientResultProcessor;
import com.x.hhqs.joa.common.restclient.annotation.RestClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;


public class HttpClientFactory {
    // access_token will be consider as "nearly expired token" after 25mins & then proxied REST Client will do some
    // stuff to get new token
    public static final int REFRESH_TIME_IN_MINUTES = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientFactory.class);
    // @formatter:off
    /**
     * This method will create a dynamic proxy instance of the input clazz.IF the skeleton interface extends {@link AuthRestClient}
     * then there are some magic stuffs related to access_token & refresh_token will be added.
     *
     * Example how to use:
     *   1. Define a skeleton interface and map API URI to a specific method using {@link RestClient}
     *      public interface SampleRestClient extends AuthRestClient {
     *          @RestClient(method = RequestMethod.POST, path = "/sayHello/{name}?{age}", mediaType = MediaType.TEXT_PLAIN)
     *          String sayHello(@Param("name") String name, @Param("age") int age);
     *      }
     *
     *  2. Define {@link RestClientConfig} for the RestClient's configuration
     *          RestClientConfig config = RestClientConfig.builder().baseUrl(m_authBaseUrl).logger(getLogger())
     *              .sslSocketFactory(getSSLConnectionSocketFactory()).build();
     *
     *  3. Retrieve magic instance:
     *          SampleRestClient restClient = HttpClientFactory.newRestClient(SampleRestClient.class, config);
     *
     *  4. Consumer REST API:
     *          restClient.sayHello();
     *
     * Supported special arguments in skeleton interface:
     *      - {@link AuthorizationProvider}: In case you don't want to have magic stuffs about access_token & refresh_token.
     *      Providing this argument to specify Authorization header in the request
     *      - {@link RestClientResultProcessor}: using this if you want to render the result yourself
     *      - {@link HttpRequestBaseProcessor}: using this if you want to customize {@link HttpRequestBase} yourself
     *      - {@link HttpErrorCodeProcessor}: using this if you want to add extra processing based on the HttpStatusCode
     *      - {@link StaticParamsProvider}: using this if you want to add some static extra form param in request
     *      Currently, It's using for login method, logout method and refresh token method. Example at {@see NSPAuthRestClient}
     *      - {@link ConnectionErrorProcessor}: using this if you want to handle connection error while executing request
     *
     * Example of special arguments:
     *  1. {@link AuthorizationProvider}:
     *      public interface SampleRestClient extends AuthRestClient {
     *         @RestClient(method = RequestMethod.POST, path = "/sayHello", mediaType = MediaType.TEXT_PLAIN)
     *         String sayHello(AuthorizationProvider provider);
     *      }
     *
     *      restClient.sayHello(() -> "Basic " + DatatypeConverter.printBase64Binary((username + ":" + password).getBytes()));
     *
     *  2. {@link RestClientResultProcessor}:
     *      public interface SampleRestClient extends AuthRestClient {
     *         @RestClient(method = RequestMethod.POST, path = "/sayHello", mediaType = MediaType.TEXT_PLAIN)
     *         String sayHello(RestClientResultProcessor parser);
     *      }
     *
     *      restClient.sayHello((String response) -> {
     *          ABC obj = new ABC();
     *          obj.setText(response.substring(1));
     *          return obj;
     *      });
     *
     *  3. {@link HttpRequestBaseProcessor}:
     *      public interface SampleRestClient extends AuthRestClient {
     *         @RestClient(method = RequestMethod.POST, path = "/sayHello", mediaType = MediaType.TEXT_PLAIN)
     *         String sayHello(HttpRequestBaseProcessor processor);
     *      }
     *
     *      restClient.sayHello((HttpRequestBase method) -> {
     *          method.addHeader(HttpHeaders.AUTHORIZATION, authorizationProvider.getAuthorization());
     *      });
     *
     * 4. {@link HttpErrorCodeProcessor}:
     *      public interface SampleRestClient extends AuthRestClient {
     *         @RestClient(method = RequestMethod.POST, path = "/sayHello", mediaType = MediaType.TEXT_PLAIN)
     *         String sayHello(HttpErrorCodeProcessor processor);
     *      }
     *
     *      restClient.sayHello((int statusCode, String responseText) -> {
     *          LOGGER.error(null, "HTTP Error Code: " + statusCode)
     *      });
     * 5. {@link StaticParamsProvider}
     *    1. Define RestClient with extra params like:
     *    public interface CustomAuthClient {
     *       @RestClient(method = RequestMethod.POST, path = "/rest-gateway/rest/api/v1/auth/token", mediaType = MediaType.APPLICATION_FORM_URLENCODED)
     *       @RefreshAccessToken
     *       TokenResponse refreshToken(@FormParam("refresh_token") String refreshToken, @FormParam("grant_type") String grantType); // Extra param is grant_type
     *    }
     *    2. Define StaticParamsProvider (Can be in RestClientConfig)
     *
     *    RestClientConfig.builder().refreshTokenParams(() -> return Arrays.asList(new SimpleEntry<String, String>("grant_type", "refresh_token"));
     * 6. {@link ConnectionErrorProcessor}
     *      public interface SampleRestClient extends AuthRestClient {
     *         @RestClient(method = RequestMethod.POST, path = "/sayHello", mediaType = MediaType.TEXT_PLAIN)
     *         String sayHello(ConnectionErrorProcessor connectionErrorProcessor);
     *      }
     *
     *      restClient.sayHello((Exception e) -> {
     *          log.error("Error with exception {}",e);
     *          throw new SomeCustomException(e);
     *      });
     * @param clazz        RestClient skeleton.
     *                     If it extends {@link AuthRestClient} then there are some magic stuffs related to access_token & refresh_token will be added.
     *                     If not, you need to define an instance of {@link AuthorizationProvider} in the method argument
     * @param clientConfig An instance of {@link RestClientConfig} specifies some configuration for the {@link AuthRestClient}
     * @param <T>          A dynamic instance of clazz
     * @return
     */
    // @formatter:on
    public static <T> T newRestClient(Class<T> clazz, RestClientConfig clientConfig, HttpMonitor httpMonitor) {
        return newRestClient(clazz, clientConfig, HighAvailabilityConstants.CORE_REST_HA_APP_NAME, httpMonitor);
    }

    public static <T> T newRestClient(Class<T> clazz, RestClientConfig clientConfig, String haAppName, HttpMonitor httpMonitor) {
        HttpClientInvocationHandler invocationHandler = new HttpClientInvocationHandler(clazz, clientConfig, httpMonitor);
        LOGGER.debug("Http Client: {} Registering HA Event handler", clazz.getSimpleName());
        ServiceRegistration<EventHandler> serviceReference = HighAvailabilityClientRegister.registerEventHandler(invocationHandler, haAppName);
        invocationHandler.setEventHandlerReference(serviceReference);
        HighAvailabilityClientRegister.registerMetrics(invocationHandler);
        if (clientConfig.getHighAvailabilityStateChangeListener() != null) {
            LOGGER.debug("Adding HA listener for http client : {}", clazz.getSimpleName());
            invocationHandler.addHighAvailabilityStateChangeListener(clientConfig.getHighAvailabilityStateChangeListener());
        }
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, invocationHandler);
    }

    public static synchronized void reRegisterEventHandler(Object restClient, String newHaApp) {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(restClient);
        if (invocationHandler instanceof HttpClientInvocationHandler) {
            ServiceRegistration<EventHandler> serviceReference = ((HttpClientInvocationHandler) invocationHandler).getEventHandlerReference();
            serviceReference.unregister();
            serviceReference = HighAvailabilityClientRegister.registerEventHandler((HttpClientInvocationHandler) invocationHandler, newHaApp);
            ((HttpClientInvocationHandler) invocationHandler).setEventHandlerReference(serviceReference);
        }
    }

    public static boolean isRefreshTokenExpired(TokenResponseWrapper token) {
        if (token == null) {
            return true;
        }
        return System.currentTimeMillis() >= token.getRefreshExpiresAt();
    }

    public static boolean needRefreshAccessToken(RestClientConfig clientConfig, TokenResponseWrapper token) {
        if (token == null) {
            return true;
        }
        long refreshTime = clientConfig.getTokenRefreshTime() > 0 ?
                clientConfig.getTokenRefreshTime() : TimeUnit.MINUTES.toMillis(REFRESH_TIME_IN_MINUTES);
        return System.currentTimeMillis() >= token.getExpiresAt() - refreshTime;
    }

    public static CloseableHttpResponse createCloseableHttpResponse(HttpRequestBase action, RestClientConfig clientConfig) throws IOException {
        CloseableHttpClient client = clientConfig.isHttps() ?
                HttpClients.custom().setSSLSocketFactory(clientConfig.getSSLSocketFactory()).build() : HttpClients.createDefault();
        return client.execute(action);
    }
}
