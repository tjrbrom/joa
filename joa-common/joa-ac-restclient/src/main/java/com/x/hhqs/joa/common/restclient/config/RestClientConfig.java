package com.x.hhqs.joa.common.restclient.config;

import com.x.hhqs.joa.common.restclient.processor.ConnectionErrorProcessor;
import com.x.hhqs.joa.common.restclient.provider.LoginStaticParamsProvider;
import com.x.hhqs.joa.common.restclient.provider.LogoutStaticParamsProvider;
import com.x.hhqs.joa.common.restclient.provider.RefreshTokenStaticParamsProvider;
import com.x.hhqs.joa.common.restclient.processor.InvalidTokenProcessor;
import com.x.hhqs.joa.common.restclient.ha.HighAvailabilityStateChangeListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;

import javax.xml.bind.DatatypeConverter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RestClientConfig {

    public static final String DOT = ".";
    public static final String EMPTY = "";
    public static final String ENV_CONFIG_PREFIX = "#env" + DOT;

    /**
     * Showing that the target URL is HTTPS
     */
    private final boolean m_isHttps;
    /**
     * Base URL of the request for example: https://host:port/base1/base2
     */
    private final String m_baseUrl;
    /**
     * Used for HTTPS connection
     */
    private final SSLConnectionSocketFactory m_sslSocketFactory;
    /**
     * Value of basic auth built from username & password
     */
    private final String m_basicAuthorization;
    /**
     * Token life time. If current access_token's life greater than this --> access_token will be refreshed
     */
    private final long m_tokenRefreshTime;

    private int m_connectTimeout;

    private int m_readTimeout;

    /**
     * Max connection per route for Http client
     */
    private int m_maxConnectionsPerRoute;

    /**
     * Max total of connection for Http client
     */
    private int m_maxTotalConnections;

    /**
     * Extra param for login method
     * Eg: {"grant_type":"client_credentials"}
     */
    private LoginStaticParamsProvider m_loginStaticParamsProvider;

    /**
     * Extra param for logout method
     * Eg: {"grant_type":"client_credentials"}
     */
    private LogoutStaticParamsProvider m_logoutStaticParamsProvider;

    /**
     * Extra param for refresh token method
     * Eg: {"grant_type":"refresh_token"}
     */
    private RefreshTokenStaticParamsProvider m_refreshTokenStaticParamsProvider;

    /**
     * Used for handling connection error
     */
    private Set<ConnectionErrorProcessor> m_globalConnectionErrorProcessors;

    /**
     * Used for handling invalid token case
     */
    private InvalidTokenProcessor m_invalidTokenProcessor;

    /**
     * Extra config
     */
    private Map<String, String> m_extraConfig;

    /**
     * HA Listener
     */
    private HighAvailabilityStateChangeListener m_highAvailabilityStateChangeListener;


    public RestClientConfig(boolean isHttps, String baseUrl, SSLConnectionSocketFactory sslSocketFactory,
                            String basicAuthorization, int tokenRefreshTime) {
        m_isHttps = isHttps;
        m_baseUrl = baseUrl;
        m_sslSocketFactory = sslSocketFactory;
        m_basicAuthorization = basicAuthorization;
        m_tokenRefreshTime = tokenRefreshTime;
    }

    public RestClientConfig(boolean isHttps, String baseUrl, SSLConnectionSocketFactory sslSocketFactory,
                            String basicAuthorization, long tokenRefreshTime,
                            LoginStaticParamsProvider loginStaticParamsProvider,
                            LogoutStaticParamsProvider logoutStaticParamsProvider,
                            RefreshTokenStaticParamsProvider refreshTokenStaticParamsProvider,
                            Set<ConnectionErrorProcessor> connectionErrorProcessor,
                            InvalidTokenProcessor invalidTokenProcessor,
                            Map<String, String> extraConfig,
                            HighAvailabilityStateChangeListener highAvailabilityStateChangeListener,
                            int connectTimeout, int readTimeout, int maxConnectionsPerRoute, int maxTotalConnections) {
        m_isHttps = isHttps;
        m_baseUrl = baseUrl;
        m_sslSocketFactory = sslSocketFactory;
        m_basicAuthorization = basicAuthorization;
        m_tokenRefreshTime = tokenRefreshTime;
        m_loginStaticParamsProvider = loginStaticParamsProvider;
        m_logoutStaticParamsProvider = logoutStaticParamsProvider;
        m_refreshTokenStaticParamsProvider = refreshTokenStaticParamsProvider;
        m_globalConnectionErrorProcessors = connectionErrorProcessor;
        m_invalidTokenProcessor = invalidTokenProcessor;
        m_extraConfig = extraConfig;
        m_highAvailabilityStateChangeListener = highAvailabilityStateChangeListener;
        m_connectTimeout = connectTimeout;
        m_readTimeout = readTimeout;
        m_maxConnectionsPerRoute = maxConnectionsPerRoute;
        m_maxTotalConnections = maxTotalConnections;
    }

    public boolean isHttps() {
        return m_isHttps;
    }

    public String getBaseUrl() {
        return m_baseUrl;
    }

    public int getConnectTimeout() {
        return m_connectTimeout;
    }

    public int getReadTimeout() {
        return m_readTimeout;
    }

    public int getMaxConnectionsPerRoute() {
        return m_maxConnectionsPerRoute;
    }

    public int getMaxTotalConnections() {
        return m_maxTotalConnections;
    }

    public SSLConnectionSocketFactory getSSLSocketFactory() {
        return m_sslSocketFactory;
    }

    public String getBasicAuthorization() {
        return m_basicAuthorization;
    }

    public long getTokenRefreshTime() {
        return m_tokenRefreshTime;
    }

    public LoginStaticParamsProvider getLoginStaticParamsProvider() {
        return m_loginStaticParamsProvider;
    }

    public LogoutStaticParamsProvider getLogoutStaticParamsProvider() {
        return m_logoutStaticParamsProvider;
    }

    public RefreshTokenStaticParamsProvider getRefreshTokenStaticParamsProvider() {
        return m_refreshTokenStaticParamsProvider;
    }

    public Set<ConnectionErrorProcessor> getGlobalConnectionErrorProcessors() {
        return m_globalConnectionErrorProcessors;
    }

    public InvalidTokenProcessor getInvalidTokenProcessor() {
        return m_invalidTokenProcessor;
    }

    public Map<String, String> getExtraConfig() {
        return m_extraConfig;
    }

    public HighAvailabilityStateChangeListener getHighAvailabilityStateChangeListener() {
        return m_highAvailabilityStateChangeListener;
    }

    // Value must be start with #env.
    // E.g: #env.NSP_URL
    public String getEnvConfiguration(String value) {

        if (value.startsWith(ENV_CONFIG_PREFIX)) {
            String envName = value.replace(ENV_CONFIG_PREFIX, EMPTY);

            return getExtraConfig().get(envName);
        }
        return null;
    }

    public static class RestClientConfigBuilder {

        private String m_baseUrl;
        private SSLConnectionSocketFactory m_sslSocketFactory;
        private String m_basicAuthorization;
        private long m_tokenRefreshTime = -1;
        private int m_connectTimeout;
        private int m_readTimeout;
        private int m_maxConnectionsPerRoute;
        private int m_maxTotalConnections;

        private LoginStaticParamsProvider m_loginStaticParamsProvider;
        private LogoutStaticParamsProvider m_logoutStaticParamsProvider;
        private RefreshTokenStaticParamsProvider m_refreshTokenStaticParamsProvider;

        private Set<ConnectionErrorProcessor> m_connectionErrorProcessors = new HashSet<>();
        private InvalidTokenProcessor m_invalidTokenProcessor;

        private Map<String, String> m_extraConfig;

        private HighAvailabilityStateChangeListener m_highAvailabilityStateChangeListener;

        public RestClientConfigBuilder baseUrl(String scheme, String host, int port) {
            m_baseUrl = scheme + "://" + host + ":" + port;
            return this;
        }

        public RestClientConfigBuilder baseUrl(String baseUrl) {
            m_baseUrl = baseUrl;
            return this;
        }

        public RestClientConfigBuilder connectTimeout(int connectTimeout) {
            m_connectTimeout = connectTimeout;
            return this;
        }

        public RestClientConfigBuilder readTimeout(int readTimeout) {
            m_readTimeout = readTimeout;
            return this;
        }

        public RestClientConfigBuilder maxConnectionsPerRoute(int maxConnectionsPerRoute) {
            m_maxConnectionsPerRoute = maxConnectionsPerRoute;
            return this;
        }

        public RestClientConfigBuilder maxTotalConnections(int maxTotalConnections) {
            m_maxTotalConnections = maxTotalConnections;
            return this;
        }

        public RestClientConfigBuilder sslSocketFactory(SSLConnectionSocketFactory sslSocketFactory) {
            m_sslSocketFactory = sslSocketFactory;
            return this;
        }

        public RestClientConfigBuilder credential(String username, String password) {
            m_basicAuthorization = "Basic " + DatatypeConverter.printBase64Binary((username + ":" + password).getBytes());
            return this;
        }

        public RestClientConfigBuilder tokenRefreshTime(long time) {
            m_tokenRefreshTime = time;
            return this;
        }

        public RestClientConfigBuilder loginParams(LoginStaticParamsProvider paramsProvider) {
            m_loginStaticParamsProvider = paramsProvider;
            return this;
        }

        public RestClientConfigBuilder logoutParams(LogoutStaticParamsProvider paramsProvider) {
            m_logoutStaticParamsProvider = paramsProvider;
            return this;
        }

        public RestClientConfigBuilder refreshTokenParams(RefreshTokenStaticParamsProvider paramsProvider) {
            m_refreshTokenStaticParamsProvider = paramsProvider;
            return this;
        }

        public RestClientConfigBuilder globalConnectionErrorProcessor(Set<ConnectionErrorProcessor> connectionErrorProcessor) {
            m_connectionErrorProcessors.addAll(connectionErrorProcessor);
            return this;
        }

        public RestClientConfigBuilder globalConnectionErrorProcessor(ConnectionErrorProcessor connectionErrorProcessor) {
            m_connectionErrorProcessors.add(connectionErrorProcessor);
            return this;
        }

        public RestClientConfigBuilder globalInvalidTokenProcessor(InvalidTokenProcessor invalidTokenProcessor) {
            m_invalidTokenProcessor = invalidTokenProcessor;
            return this;
        }

        public RestClientConfigBuilder extraConfig(Map<String, String> extraConfig) {
            m_extraConfig = extraConfig;
            return this;
        }

        public RestClientConfigBuilder highAvailabilityStateChangeListener(HighAvailabilityStateChangeListener highAvailabilityStateChangeListener) {
            m_highAvailabilityStateChangeListener = highAvailabilityStateChangeListener;
            return this;
        }

        public RestClientConfig build() {
            boolean isHttps = StringUtils.isNotEmpty(m_baseUrl) ? m_baseUrl.toLowerCase().startsWith("https:") : Boolean.FALSE;
            if (isHttps && m_sslSocketFactory == null) {
                throw new IllegalArgumentException("HTTPS requires SSLConnectionSocketFactory for URL: " + m_baseUrl);
            }
            return new RestClientConfig(isHttps, m_baseUrl, m_sslSocketFactory, m_basicAuthorization, m_tokenRefreshTime,
                    m_loginStaticParamsProvider, m_logoutStaticParamsProvider, m_refreshTokenStaticParamsProvider,
                    m_connectionErrorProcessors, m_invalidTokenProcessor, m_extraConfig , m_highAvailabilityStateChangeListener,
                    m_connectTimeout, m_readTimeout, m_maxConnectionsPerRoute, m_maxTotalConnections);
        }
    }

    public static RestClientConfigBuilder builder() {
        return new RestClientConfigBuilder();
    }
}
