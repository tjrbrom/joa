package com.x.hhqs.joa.common.sharedfileservice.impl;

import com.github.sardine.DavResource;
import com.github.sardine.Version;
import com.github.sardine.impl.SardineImpl;
import com.github.sardine.impl.handler.LockResponseHandler;
import com.github.sardine.impl.methods.HttpLock;
import com.github.sardine.impl.methods.HttpPropFind;
import com.github.sardine.model.Exclusive;
import com.github.sardine.model.Lockdiscovery;
import com.github.sardine.model.Lockinfo;
import com.github.sardine.model.Lockscope;
import com.github.sardine.model.Locktype;
import com.github.sardine.model.Multistatus;
import com.github.sardine.model.Propfind;
import com.github.sardine.model.Response;
import com.github.sardine.model.Write;
import com.github.sardine.util.SardineUtil;
import com.x.hhqs.joa.common.sharedfileservice.utils.SystemPropertyUtils;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.VersionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.ProxySelector;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class WebdavSardineImpl extends SardineImpl {
    private static final String DEFAULT_WEBDAV_CONNECT_TIMEOUT_MS = "15000";
    private static final String DEFAULT_WEBDAV_SOCKET_READ_TIMEOUT_MS = "60000";
    private static final String DEFAULT_WEBDAV_RESOURCE_LOCK_TIMEOUT = "90";
    private static final QName QNAME = new QName("DAV:", "lockdiscovery", "D");
    private static final Logger LOGGER = LoggerFactory.getLogger(WebdavSardineImpl.class);

    public WebdavSardineImpl() {
        super();
    }

    @Override
    protected List<DavResource> propfind(String url, int depth, Propfind body) throws IOException {
        HttpPropFind entity = new HttpPropFind(url);
        entity.setDepth(depth < 0 ? "infinity" : Integer.toString(depth));
        entity.setEntity(new StringEntity(SardineUtil.toXml(body), "UTF-8"));
        Multistatus multistatus = this.execute(entity, new WebdavMultiStatusResponseHandler());
        List<Response> responses = multistatus.getResponse();
        List<DavResource> resources = new ArrayList<DavResource>(responses.size());
        for (Response response : responses) {
            try {
                DavResource davResource = new DavResource(response);
                final boolean[] isLocked = {false};
                response.getPropstat().stream().forEach(propstat -> {
                    Lockdiscovery lockdiscovery = propstat.getProp().getLockdiscovery();
                    if(lockdiscovery != null) {
                        isLocked[0] = lockdiscovery.getActivelock().isEmpty();
                    }
                });
                davResource.getCustomPropsNS().put(QNAME, String.valueOf(!isLocked[0]));
                resources.add(davResource);
            } catch (URISyntaxException e) {
                LOGGER.warn(String.format("Ignore resource with invalid URI %s caused by %s",
                 response.getHref().get(0), e.getMessage()));
            }
        }
        return resources;
    }

    @Override
    protected HttpClientBuilder configure(ProxySelector selector, CredentialsProvider credentials) {
        Registry<ConnectionSocketFactory> schemeRegistry = this.createDefaultSchemeRegistry();
        HttpClientConnectionManager cm = this.createDefaultConnectionManager(schemeRegistry);
        String version = Version.getSpecification();
        if (version == null) {
            version = VersionInfo.UNAVAILABLE;
        }

        int connectTimeout = 0;
        int socketTimeout = 0;
        try {
            connectTimeout = Integer.parseInt(SystemPropertyUtils.getInstance()
                    .getFromEnvOrSysProperty("WEBDAV_CONNECT_TIMEOUT_MS", DEFAULT_WEBDAV_CONNECT_TIMEOUT_MS));
        } catch (NumberFormatException e) {
            LOGGER.error("Exception while getting WEBDAV_CONNECT_TIMEOUT_MS ", e);
            connectTimeout = Integer.parseInt(DEFAULT_WEBDAV_CONNECT_TIMEOUT_MS);
        }

        try {
            socketTimeout = Integer.parseInt(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(
                    "WEBDAV_SOCKET_READ_TIMEOUT_MS", DEFAULT_WEBDAV_SOCKET_READ_TIMEOUT_MS));
        } catch (NumberFormatException e) {
            LOGGER.error("Exception while getting WEBDAV_SOCKET_READ_TIMEOUT_MS ", e);
            socketTimeout = Integer.parseInt(DEFAULT_WEBDAV_SOCKET_READ_TIMEOUT_MS);
        }

        return HttpClients.custom().setUserAgent("Sardine/" + version)
                .setDefaultCredentialsProvider(credentials)
                .setRedirectStrategy(this.createDefaultRedirectStrategy())
                .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(connectTimeout)
                        .setSocketTimeout(socketTimeout).setExpectContinueEnabled(false).build())
                .setConnectionManager(cm).setRoutePlanner(
                        this.createDefaultRoutePlanner(this.createDefaultSchemePortResolver(), selector));
    }

    @Override
    public String lock(String url) throws IOException {
        HttpLock entity = new HttpLock(url);
        int lockTimeout = 0;
        try {
            lockTimeout = Integer.parseInt(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(
                    "WEBDAV_RESOURCE_LOCK_TIMEOUT", DEFAULT_WEBDAV_RESOURCE_LOCK_TIMEOUT));
        } catch (NumberFormatException e) {
            LOGGER.error("Exception while getting WEBDAV_RESOURCE_LOCK_TIMEOUT ", e);
            lockTimeout = Integer.parseInt(DEFAULT_WEBDAV_RESOURCE_LOCK_TIMEOUT);
        }
        entity.setTimeout(lockTimeout);
        Lockinfo body = new Lockinfo();
        Lockscope scopeType = new Lockscope();
        scopeType.setExclusive(new Exclusive());
        body.setLockscope(scopeType);
        Locktype lockType = new Locktype();
        lockType.setWrite(new Write());
        body.setLocktype(lockType);
        entity.setEntity(new StringEntity(SardineUtil.toXml(body), "UTF-8"));
        // Return the lock token
        return this.execute(entity, new LockResponseHandler());
    }
}