package com.x.hhqs.joa.rt1agent.security.keycloak;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.json.JSONObject;
import org.keycloak.authorization.client.AuthorizationDeniedException;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.authorization.client.util.Http;
import org.keycloak.authorization.client.util.HttpResponseException;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.constants.ServiceUrlConstants;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.authorization.AuthorizationRequest;
import org.keycloak.representations.idm.authorization.AuthorizationResponse;
import org.keycloak.representations.idm.authorization.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.rt1agent.command.EaddCode;
import com.x.hhqs.joa.rt1agent.security.Authentication;
import com.x.hhqs.joa.rt1agent.security.PermissionCheckResult;
import com.x.hhqs.joa.rt1agent.security.SecurityService;
import com.x.hhqs.joa.rt1agent.security.User;
import com.x.hhqs.joa.rt1agent.service.AcRestService;

@Service
@ConditionalOnProperty(prefix = "joa.rt1.security", name = "mode", havingValue = "keycloak")
public class KeycloakSercurityService implements SecurityService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakSercurityService.class);

    private static final String CORE = "CORE";

    private static final String VAULT_AUTHORIZATION = "VAULT_AUTHORIZATION";

    private Configuration configuration;

    private AuthzClient authzClient;

    @Autowired
    private KeyclockConfig keyclockConfig;

    @Value("${joa.rt1agent.httclient.maxTotal:20}")
    private int maxTotal = 20;

    @Value("${joa.rt1agent.httclient.maxPerRoute:10}")
    private int maxPerRoute = 10;

    @Value("${joa.rt1agent.httclient.connectionReuse:true}")
    private boolean connectionReuse = true;

    @Value("${joa.rt1.login.ignorePapFetchFailure:false}")
    private boolean ignorePapFetchFailure = false;

    @Autowired
    private JoaMessage i18n;

    @Autowired
    private AcRestService acRestService;

    @PostConstruct
    public void init() {
        LOGGER.info("security.mode: keycloak");
        this.configuration = getConfiguration();
    }

    protected AuthzClient getAuthzClient() {
        if (authzClient == null) {
            authzClient = AuthzClient.create(configuration);
        }
        return authzClient;
    }

    @Override
    public Authentication login(String username, String password) {
        User user = new User(username);
        KeycloakAuthentication authentication = new KeycloakAuthentication(user);

        String refreshToken = null;
        try {
            AuthzClient authzClient = getAuthzClient();
            AccessTokenResponse atp = authzClient.obtainAccessToken(username, password);
            refreshToken = atp.getRefreshToken();

            if (!hasClientAccess(atp.getToken(), keyclockConfig.getClientId())) {
                throw new Exception(
                    i18n.getMessage(EaddCode.EADD_PREFIX + "noAccessUser",
                        new Object[] {username, keyclockConfig.getClientId()}));
            }
            authentication.setValid(true);
            LOGGER.debug("User:{} obtain AccessToken success, try to get user Permissions.", username);

            AuthorizationRequest request = new AuthorizationRequest();
            request.addPermission(keyclockConfig.getResourceId());
            AuthorizationResponse response = authzClient.authorization(atp.getToken())
                .authorize(request);

            AccessToken rpt = toAccessToken(response.getToken());
            authentication.setAccessToken(rpt);

            updateUserAuthorities(user, rpt.getAuthorization().getPermissions());

            LOGGER.debug("User:{} get Permissions success, try to get user PAPs.", username);
            updateUserPapInfos(user, authentication.getAccessToken().getResourceAccess(CORE));

            return authentication;
        } catch (HttpResponseException e) {
            LOGGER.error(e.toString());
            LOGGER.error(e.getMessage(), e);
            authentication.setErrorMessage(getErrorDescription(e));
        } catch (AuthorizationDeniedException e) {
            LOGGER.error(e.getMessage(), e);
            // {"error":"access_denied","error_description":"not_authorized"}
            authentication.setErrorMessage("not_authorized");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            authentication.setErrorMessage(e.getMessage());
        } finally {
            if (refreshToken != null)
                logoutKeycloak(refreshToken);
        }

        return authentication;
    }

    protected void updateUserPapInfos(User user, Access access) throws Exception {
        if (access == null) {
            LOGGER.warn("User '{}' does not have resource_access: CORE.", user.getUsername());
            return;
        }

        Set<String> papGroups = getPapGroups(access.getRoles());
        if (CollectionUtils.isEmpty(papGroups)) {
            LOGGER.warn("User '{}' does not have CORE PAP_GROUP.", user.getUsername());
        } else {
            user.setPapGroups(papGroups);
            updateUserPaps(user);
        }
    }

    protected void updateUserPaps(User user) throws Exception {
        try {
            Set<String> papGroups = user.getPapGroups();
            if (CollectionUtils.isEmpty(papGroups)) {
                return;
            }

            if (papGroups.contains("ALLPAPS")) {
                user.setPaps(Set.of("*"));
                return;
            }

            Map<String, Set<String>> papGroupToPaps = acRestService.getPapGroupMap();
            Set<String> paps = new HashSet<>();
            papGroups.stream()
                .map(p -> papGroupToPaps.get(p))
                .filter(s -> s != null)
                .forEach(s -> paps.addAll(s));

            if (paps.isEmpty()) {
                LOGGER.warn("User '{}' does not have CORE PAP.", user.getUsername());
            }
            user.setPaps(paps);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            if (!ignorePapFetchFailure) {
                throw new Exception(i18n.getMessage(EaddCode.EADD_PREFIX + "getPapFailed"));
            }
        }
    }

    protected void updateUserAuthorities(User user, Collection<Permission> permissions) {
        for (Permission permission : permissions) {
            if (permission.getResourceName().equals(keyclockConfig.getResourceId())) {
                user.setAuthorities(permission.getScopes().stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toSet()));
                break;
            }
        }
    }

    protected Set<String> getPapGroups(Set<String> roles) {
        if (CollectionUtils.isEmpty(roles)) {
            return new HashSet<>();
        }

        return roles.stream()
            .filter(role -> role.startsWith("CORE_PAPGROUP_"))
            .map(role -> role.substring("CORE_PAPGROUP_".length()))
            .map(papGroup -> papGroup.toUpperCase())
            .collect(Collectors.toSet());
    }

    protected String getErrorDescription(HttpResponseException exception) {
        try {
            JSONObject jsonObject = new JSONObject(new String(exception.getBytes()));
            if (jsonObject.has("error_description")) {
                return jsonObject.getString("error_description");
            }
            return jsonObject.getString("error");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new String(exception.getBytes());
        }
    }

    protected boolean hasClientAccess(String token, String clientId) {
        try {
            AccessToken at = toAccessToken(token);
            return at.getResourceAccess(clientId) != null;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    private void logoutKeycloak(String refreshToken) {
        try {
            String url = KeycloakUriBuilder.fromUri(configuration.getAuthServerUrl()).clone()
                .path(ServiceUrlConstants.TOKEN_SERVICE_LOGOUT_PATH)
                .build(configuration.getRealm()).toString();

            Http http = new Http(configuration, configuration.getClientCredentialsProvider());

            http.post(url)
                .authentication()
                .client()
                .form()
                .param("refresh_token", refreshToken)
                .param("client_id", configuration.getResource())
                .param("client_secret", (String) configuration.getCredentials().get("secret"))
                .response()
                .execute();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void logout(Authentication authentication) {
        // empty for access token is logout after authorization
    }

    protected AccessToken toAccessToken(String rpt) {
        AccessToken accessToken;
        try {
            accessToken = new JWSInput(rpt).readJsonContent(AccessToken.class);
        } catch (JWSInputException cause) {
            throw new RuntimeException("Failed to deserialize RPT", cause);
        }
        return accessToken;
    }

    public Configuration getConfiguration() {
        Map<String, Object> clientCredentials = new HashMap<>();
        clientCredentials.put("secret", keyclockConfig.getSecret());
        return new Configuration(keyclockConfig.getAuthServerUrl(), keyclockConfig.getRealm(),
            keyclockConfig.getClientId(), clientCredentials, getHttpClient());
    }

    public HttpClient getHttpClient() {
        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
            LOGGER.error(e.getMessage(), e);
        }
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
            NoopHostnameVerifier.INSTANCE);

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("https", sslsf)
            .register("http", new PlainConnectionSocketFactory())
            .build();

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
            socketFactoryRegistry);
        connectionManager.setMaxTotal(maxTotal);
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
            .setSSLSocketFactory(sslsf)
            .setConnectionManager(connectionManager);
        if (!connectionReuse) {
            httpClientBuilder.setConnectionReuseStrategy(new NoConnectionReuseStrategy());
        }

        CloseableHttpClient httpClient = httpClientBuilder.build();
        return httpClient;
    }

    @Override
    public boolean hasAuthorization(Authentication authentication, String commandCode) {
        KeycloakAuthentication keycloakAuthentication = (KeycloakAuthentication) authentication;
        if (keycloakAuthentication.getAccessToken() == null) {
            LOGGER.warn("rpt is null");
            return false;
        }

        Set<String> authorities = keycloakAuthentication.getUserDetails().getAuthorities();
        if (CollectionUtils.isEmpty(authorities)) {
            return false;
        }

        return authorities.contains(commandCode.toUpperCase());
    }

    @Override
    public PermissionCheckResult checkSecondaryAuthPermission(String username, String password) throws Exception {
        String refreshToken = null;
        try {
            AuthzClient authzClient = getAuthzClient();
            AccessTokenResponse atp = authzClient.obtainAccessToken(username, password);
            refreshToken = atp.getRefreshToken();

            AccessToken accessToken = toAccessToken(atp.getToken());
            Access access = accessToken.getResourceAccess(CORE);
            if (access == null) {
                LOGGER.warn("User '{}' does not have resource_access: CORE.", username);
                return PermissionCheckResult.deny();
            }

            Set<String> roles = access.getRoles();
            if (roles != null && roles.contains(VAULT_AUTHORIZATION)) {
                return PermissionCheckResult.allow();
            }

            return PermissionCheckResult.deny();
        } catch (HttpResponseException e) {
            LOGGER.error(e.toString());
            LOGGER.error(e.getMessage(), e);
            return PermissionCheckResult.error(getErrorDescription(e));
        } catch (AuthorizationDeniedException e) {
            LOGGER.error(e.getMessage(), e);
            return PermissionCheckResult.error("not_authorized");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return PermissionCheckResult.error(e.getMessage());
        } finally {
            if (refreshToken != null)
                logoutKeycloak(refreshToken);
        }
    }

}


/*-
joa.rt1agent:
  security:
    keycloak:
      auth-server-url: https://core-sso-headless:8443/core-sso
      realm: master
      clientId: CORE_JOA
      secret: changeit
      resourceId: CORE_JOA_RT1
	*/
@Component
@ConditionalOnProperty(prefix = "joa.rt1.security", name = "mode", havingValue = "keycloak")
@ConfigurationProperties(prefix = "joa.rt1.security.keycloak")
class KeyclockConfig {
    private String authServerUrl;
    private String realm;
    private String clientId;
    private String secret;
    private String resourceId;

    public String getAuthServerUrl() {
        return authServerUrl;
    }

    public void setAuthServerUrl(String authServerUrl) {
        this.authServerUrl = authServerUrl;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

}
