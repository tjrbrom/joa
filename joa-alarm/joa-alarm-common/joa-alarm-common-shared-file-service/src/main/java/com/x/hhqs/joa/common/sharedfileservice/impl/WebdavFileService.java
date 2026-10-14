package com.x.hhqs.joa.common.sharedfileservice.impl;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.impl.SardineException;
import com.x.hhqs.joa.common.sharedfileservice.api.SharedFileService;
import com.x.hhqs.joa.common.sharedfileservice.config.WebdavFileServiceProperties;
import com.x.hhqs.joa.common.sharedfileservice.models.SharedFileInfo;
import com.x.hhqs.joa.common.sharedfileservice.utils.FileServerInfo;
import com.x.hhqs.joa.common.sharedfileservice.utils.FileServerUtil;
import com.x.hhqs.joa.common.sharedfileservice.utils.UploadFileInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.xml.namespace.QName;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.x.hhqs.joa.common.sharedfileservice.utils.FileServerUtil.COLON;
import static com.x.hhqs.joa.common.sharedfileservice.utils.FileServerUtil.SEPARATOR;
import static com.x.hhqs.joa.common.sharedfileservice.utils.FileServerUtil.getCurrentUser;

@Service
public class WebdavFileService implements SharedFileService {
    private static final int RETRY_INTERVAL_MS = 1000;
    private static final int MAX_RETRY_COUNT = 3;
    private static final Logger LOGGER = LoggerFactory.getLogger(WebdavFileService.class);

    private final WebdavFileServiceProperties webdavFileServiceProperties;
    private final String DEFAULT_DIR;

    public FileServerInfo m_fileServerInfo;
    private SSLConnectionSocketFactory m_sslConnectionFactory;
    private String m_stagingArea;

    private static final String IPV6_FORMAT = "[%s]";
    private static final String PROTOCOL_REGEX = "^\\w+://";

    private static final QName QNAME = new QName("DAV:", "lockdiscovery", "D");
    public static Pattern IPV6_PATTERN = Pattern.compile("^((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4}))*(\\]?)|(\\[?)((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4})){7}$");


    public WebdavFileService(@Autowired WebdavFileServiceProperties webdavFileServiceProperties)
            throws KeyManagementException, NoSuchAlgorithmException {
        this.webdavFileServiceProperties = webdavFileServiceProperties;
        DEFAULT_DIR = webdavFileServiceProperties.buildDefaultDirectory();
        m_fileServerInfo = webdavFileServiceProperties.getFileServerInfo();
        m_stagingArea = webdavFileServiceProperties.getStagingArea();
        TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        } catch (KeyStoreException e) {
            LOGGER.error(e.getMessage(), e);
        }
        m_sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
    }

    @Override
    public void createDirectory(String directory) throws Exception {
        executeWithWebdavSessionTemplate(Operation.CREATE, new WebdavSessionTemplate<Void>() {
            @Override
            public Void execute(Sardine sardine) throws Exception {
                LOGGER.debug("Checking and creating base directory: {} if not exists on the file server",
                        m_fileServerInfo.getBasePath());
//                        LOGGER.sensitiveData(m_fileServerInfo.getBasePath()));
                FileServerUtil.createDirectoryIfNotExists(m_fileServerInfo.getRootPath(),
                        m_fileServerInfo.getBaseDirectory(), sardine);
                LOGGER.debug("Creating directory: {}", directory);
                FileServerUtil.createDirectoryIfNotExists(m_fileServerInfo.getBasePath(), directory, sardine);
                LOGGER.debug("Directory: {} is created", directory);
                return null;
            }

        });
    }

    @Override
    public boolean isResourceExisting(String resourceName) throws Exception {
        return executeWithWebdavSessionTemplate(Operation.HEAD, new WebdavSessionTemplate<Boolean>() {
            @Override
            public Boolean execute(Sardine sardine) throws Exception {
                String directoryPath = m_fileServerInfo.getBasePath() + resourceName;
                try {
                    LOGGER.debug("User {} is checking if resource path {} existing",
                            getCurrentUser(), directoryPath);
//                            LOGGER.sensitiveData(getCurrentUser()), LOGGER.sensitiveData(directoryPath));
                    boolean result = WebdavSardineUtil.exists(sardine, directoryPath);
                    LOGGER.debug("Result: {}", result);
                    return result;
                } catch (Exception e) {
                    LOGGER.error("Exception while checking resource exists", e);
                    return false;
                }
            }
        });
    }

    @Override
    public List<String> listFilesInRemoteDirectory(String fileServerBasePath, String swBaseDirectory) throws Exception {
        final List<String> listFiles = new ArrayList<>();
        return executeWithWebdavSessionTemplate(Operation.PROPFIND, new WebdavSessionTemplate<List<String>>() {
            @Override
            public List<String> execute(Sardine sardine) throws Exception {
                if (WebdavSardineUtil.exists(sardine, fileServerBasePath)) {
                    LOGGER.debug("User {} is listing file path {}", getCurrentUser(),
                            fileServerBasePath);
//                            LOGGER.sensitiveData(fileServerBasePath));
                    List<DavResource> resources = WebdavSardineUtil.list(sardine, fileServerBasePath);

                    LOGGER.debug("resource list: {} from fileServerBasePath {}", resources, 
                            fileServerBasePath);
//                            LOGGER.sensitiveData(fileServerBasePath));

                    String baseUrl = m_fileServerInfo.getUrl();

                    for (DavResource davResource : resources) {
                        String davResourcePath = baseUrl + davResource.getPath();
                        if (davResource.isDirectory()) {
                            /*from resources list, we have something like

                            /webdav/local-core-av/sharedfs/non-coded-device-plugs/LS-DF-16GW-20A.06/
                            /webdav/local-core-av/sharedfs/non-coded-device-plugs/LS-DF-16GW-20A.06/annotations/
                            /webdav/local-core-av/sharedfs/non-coded-device-plugs/LS-DF-16GW-20A.06/model/

                            purpose of this check is to ignore parent directory /webdav/local-core-av/sharedfs/non-coded-device-plugs/LS-DF-16GW-20A.06/
                            */
                            LOGGER.debug("Start comparing davResourcePath {} to fileServerBasePath {}", 
                                    davResourcePath, fileServerBasePath);
//                                    LOGGER.sensitiveData(davResourcePath), LOGGER.sensitiveData(fileServerBasePath));
                            if (!fileServerBasePath.equals(davResourcePath)) {
                                listFiles.addAll(listFilesInRemoteDirectory(davResourcePath, swBaseDirectory));
                            }
                            continue;
                        }

                        String basePathTobeExcluded = FileServerInfo.DEFAULT_ROOT_DIR + SEPARATOR
                                + DEFAULT_DIR + SEPARATOR + swBaseDirectory + SEPARATOR;
                        String resourcePath = davResource.getPath().replaceFirst(basePathTobeExcluded, "");
                        listFiles.add(resourcePath);
                    }
                } else {
                    throw new IOException("File path does not exists: " + fileServerBasePath);
                }
                return listFiles;
            }
        });
    }

    @Override
    public List<SharedFileInfo> uploadFileToFileServer(String localPath, String remoteDirectory) throws Exception {
        return executeWithWebdavSessionTemplate(Operation.PUT, new WebdavSessionTemplate<List<SharedFileInfo>>() {
            @Override
            public List<SharedFileInfo> execute(Sardine sardine) throws Exception {
                List<SharedFileInfo> fileInfos = new ArrayList<>();
                createDirectory(remoteDirectory);
                File localFile = new File(localPath);
                if (localFile.isDirectory()) {
                    LOGGER.debug("Uploading files from local-dir: {} to remote-dir: {}",
                            localPath, remoteDirectory);
//                            LOGGER.sensitiveData(localPath), LOGGER.sensitiveData(remoteDirectory));
                    File[] subFiles = localFile.listFiles();
                    if (subFiles != null && subFiles.length > 0) {
                        for (File file : subFiles) {
                            String fileName = file.getName();
                            String remoteFilePath = FileServerUtil.formatSeparatorPath(m_fileServerInfo.getBasePath(),
                                    remoteDirectory, file.getName());
                            String localFilePath = file.getAbsolutePath();
                            LOGGER.debug("Uploading item: {} to the File Server", fileName);
                            if (file.isFile()) {
                                // upload single file
                                uploadSingleFile(localFile.getName(), localFilePath, remoteFilePath, sardine, fileInfos);
                            } else {
                                // upload sub directory
                                String parentDir = remoteDirectory + SEPARATOR + file.getName();
                                fileInfos.addAll(uploadFileToFileServer(localFilePath, parentDir));
                            }
                        }
                    }
                } else {
                    String remoteFilePath = FileServerUtil.formatSeparatorPath(
                            m_fileServerInfo.getBasePath(), remoteDirectory, localFile.getName()
                    );
                    uploadSingleFile(null, localFile.getAbsolutePath(), remoteFilePath, sardine, fileInfos);
                }
                return fileInfos;
            }

        });
    }

    @Override
    public void downloadFilesFromFileServer(String remoteDirectory, String localUrl) throws Exception {
        LOGGER.debug("User {} is downloading files from remote-file-path: {} to local-file-path: {}",
                getCurrentUser(), remoteDirectory,
//                LOGGER.sensitiveData(getCurrentUser()), LOGGER.sensitiveData(remoteDirectory),
                localUrl);
//                LOGGER.sensitiveData(localUrl));
        executeWithWebdavSessionTemplate(Operation.GET, new WebdavSessionTemplate<Void>() {
            @Override
            public Void execute(Sardine sardine) throws Exception {
                String baseUrl = m_fileServerInfo.getUrl();
                String remotePath = m_fileServerInfo.getBasePath() + remoteDirectory;
                String localStagingPath = m_stagingArea + SEPARATOR + localUrl;
                try {
                    if (WebdavSardineUtil.exists(sardine, remotePath)) {
                        createLocalDirectory(localUrl);
                        List<DavResource> resources = WebdavSardineUtil.list(sardine, remotePath);
                        LOGGER.debug("resource list: {} from remotePath {}", resources, 
                                remotePath);
//                                LOGGER.sensitiveData(remotePath));
                        for (DavResource davResource : resources) {
                            String davResourcePath = baseUrl + davResource.getPath();
                            if (davResource.isDirectory()) {
                                /*from resources list, we have something like

                                /webdav/local-core-av/sharedfs/non-coded-device-plugs/LS-DF-16GW-20A.06/
                                /webdav/local-core-av/sharedfs/non-coded-device-plugs/LS-DF-16GW-20A.06/annotations/
                                /webdav/local-core-av/sharedfs/non-coded-device-plugs/LS-DF-16GW-20A.06/model/

                                purpose of this check is to ignore parent directory /webdav/local-core-av/sharedfs/non-coded-device-plugs/LS-DF-16GW-20A.06/
                                */
                                LOGGER.debug("Start comparing davResourcePath {} to remotePath {}", 
                                        davResourcePath, remotePath);
//                                        LOGGER.sensitiveData(davResourcePath), LOGGER.sensitiveData(remotePath));
                                if (!remotePath.equals(davResourcePath)) {
                                    String remoteSubDir = remoteDirectory + davResource.getName() + SEPARATOR;
                                    String localSubDir = localUrl + SEPARATOR + davResource.getName();
                                    downloadFilesFromFileServer(remoteSubDir, localSubDir);
                                }
                            } else {
                                String localFilePath = localStagingPath + SEPARATOR + davResource.getName();
                                downloadSingleFileFromFileServer(davResourcePath, localFilePath, sardine);
                            }
                        }
                    } else {
                        throw new Exception("remotePath: '" + remotePath + "' does not exist, downloading file from file server fail");
                    }
                } catch (Exception e) {
                    LOGGER.error("Exception while checking resource exists", e);
                    throw e;
                }
                return null;
            }
        });
    }

    private void createLocalDirectory(String localDirName) {
        String localStagingPath = m_stagingArea + SEPARATOR + localDirName;
        File localFilePath = new File(localStagingPath);
        LOGGER.debug("User {} is creating local dirs at path {}", 
                getCurrentUser(), localStagingPath);
//                LOGGER.sensitiveData(getCurrentUser()), LOGGER.sensitiveData(localStagingPath));
        if (!localFilePath.exists()) {
            localFilePath.mkdirs();
            LOGGER.debug("User {} has created local dirs at path {}",
                    getCurrentUser(), localStagingPath);
//                    LOGGER.sensitiveData(getCurrentUser()), LOGGER.sensitiveData(localStagingPath));
        }
    }

    private void downloadSingleFileFromFileServer(String remoteFile, String localFile, Sardine sardine)
            throws Exception {
        try (InputStream in = WebdavSardineUtil.get(sardine, remoteFile)) {
            Path destFilePath = Paths.get(localFile);
            LOGGER.debug("User {} is downloading file: {} from remote-file-path to local-file-path: {}",
                    getCurrentUser(), remoteFile,
//                    LOGGER.sensitiveData(getCurrentUser()), LOGGER.sensitiveData(remoteFile),
                    destFilePath);
//                    LOGGER.sensitiveData(destFilePath));
            copyFile(in, destFilePath, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("User {} has successfully downloaded file: {} from remote-file-path to local-file-path: {}",
                    getCurrentUser(), remoteFile,
//                    LOGGER.sensitiveData(getCurrentUser()), LOGGER.sensitiveData(remoteFile),
                    destFilePath);
//                    LOGGER.sensitiveData(destFilePath));
        } catch (Exception e) {
            LOGGER.error("Error occurred when retrieving file '" + remoteFile + "' from file server", e);
            throw e;
        }
    }

    protected void copyFile(InputStream in, Path destFilePath, StandardCopyOption... copyOption) throws IOException {
        Files.copy(in, destFilePath, StandardCopyOption.REPLACE_EXISTING);
    }

    private void uploadSingleFile(String parentName, String localFilePath, String remoteFilePath, Sardine sardine, List<SharedFileInfo> fileInfos) throws Exception {
        LOGGER.debug("User {} is uploading single file: {} from local-file-path to remote-file-path: {}",
                getCurrentUser(), localFilePath,
//                LOGGER.sensitiveData(getCurrentUser()), LOGGER.sensitiveData(localFilePath),
                remoteFilePath);
//                LOGGER.sensitiveData(remoteFilePath));
        File localFile = new File(localFilePath);
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(localFile))) {
            WebdavSardineUtil.put(sardine, remoteFilePath, inputStream);
            SharedFileInfo fileInfo = new SharedFileInfo(localFile.getName(), parentName);
            fileInfos.add(fileInfo);
            LOGGER.info("User {} has successfully uploaded single file: {} from local-file-path to remote-file-path: {}",
                    getCurrentUser(), localFilePath, remoteFilePath);
//                    LOGGER.sensitiveData(getCurrentUser()), LOGGER.sensitiveData(localFilePath), LOGGER.sensitiveData(remoteFilePath));
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found: {}", e.getMessage(), e);
            throw new Exception("File not found: " + e.getMessage());
        }
    }

    private void checkConnection(Sardine sardine) throws Exception {
        try {
            String path = m_fileServerInfo.getRootPath() + SEPARATOR;
            LOGGER.debug("checking connection with path {}",
                    path);
//                    LOGGER.sensitiveData(path));
            WebdavSardineUtil.exists(sardine, path);
        } catch (Exception e) {
            LOGGER.error("Error occurred when checking sardine connection", e);
            throw new IOException("File server is not reachable", e);
        }
    }

    @Override
    public boolean isFileServerConnected() throws Exception {
        return executeWithWebdavSessionTemplate(Operation.HEAD, sardine -> {
            LOGGER.debug("Sardine File server connected successfully.");
            return true;
        });
    }

    private void handleConnectionFailure(Exception e) {

    }

    @Override
    public FileServerInfo getFileServerInfos() {
        return m_fileServerInfo;
    }

    protected FileServerInfo getFileServerInfo() {
        return m_fileServerInfo;
    }

    protected FileServerInfo getFileServerInfo(String username, String password, String url, String basedirectory) {
        return webdavFileServiceProperties.getFileServerInfo();
    }

    protected <T> T executeWithWebdavSessionTemplate(Operation operation, WebdavSessionTemplate<T> template) throws Exception {
        int retry = 0;
        Sardine sardine = null;
        while (retry <= MAX_RETRY_COUNT) {
            try {
                sardine = getSardine();// retry
                checkConnection(sardine);
                return template.execute(sardine);
            } catch (Exception e) {
                if (isRetryable(e)) {
                    retry++;
                    LOGGER.error("Connection refused, retry: {}/{}", retry, MAX_RETRY_COUNT, e);
                    sleep(RETRY_INTERVAL_MS);
                } else {
                    LOGGER.error("Exception while executing action on file server {}",
                            this.getFileServerInfos().getUrl(), e);
//                            LOGGER.sensitiveData(this.getFileServerInfos().getUrl()), e);
//                    handleConnectionFailure(e);
                    throw e;
                }
                if (retry > MAX_RETRY_COUNT) {
                    LOGGER.error("Max retries reached, still cannot connect to Webdav {}",
                            this.getFileServerInfos().getUrl(), e);
//                            LOGGER.sensitiveData(this.getFileServerInfos().getUrl()), e);
//                    handleConnectionFailure(e);
                    throw e;
                }
            } finally {
                if (sardine != null) {
                    sardine.shutdown();
                }
            }
        }
        return null;
    }

    private boolean isRetryable(Exception e) {
        return e instanceof SocketTimeoutException || e instanceof NoHttpResponseException || e instanceof SocketException ||
                (e instanceof SardineException && responseCanBeRetried(e)) || e.getMessage().contains("File server is not reachable");
    }

    private boolean responseCanBeRetried(Exception e) {
        String errorMessage = e.getMessage();
        return errorMessage.contains("Not a valid DAV response") || errorMessage.contains("424 Failed Dependency");
    }

    protected void sleep(long ms) throws InterruptedException {
        Thread.sleep(RETRY_INTERVAL_MS);
    }

    protected interface WebdavSessionTemplate<T> {
        T execute(Sardine sardine) throws Exception;
    }

    protected Sardine getSardine() throws MalformedURLException {
        Sardine sardine = new WebdavSardineImpl() {
            @Override
            protected SSLConnectionSocketFactory createDefaultSecureSocketFactory() {
                return m_sslConnectionFactory;
            }
        };
        sardine.setCredentials(m_fileServerInfo.getUsername(), m_fileServerInfo.getPassword());
        sardine.enablePreemptiveAuthentication(new URL(m_fileServerInfo.getRootPath()));
        return sardine;
    }

    @Override
    public void uploadFileToFileServer(List<UploadFileInfo> uploadFilesInfo) throws Exception {
        executeWithWebdavSessionTemplate(Operation.PUT, new WebdavSessionTemplate<Void>() {
            @Override
            public Void execute(Sardine sardine) throws Exception {
                for (UploadFileInfo fileInfo : uploadFilesInfo) {
                    String fileName = fileInfo.getFileName();
                    String remoteFileDirectory = fileInfo.getTargetFileDirectory();

                    LOGGER.debug("User {} is uploading single file: {} to remote directory: {} using input stream",
                            getCurrentUser(), fileName,
//                            LOGGER.sensitiveData(getCurrentUser()), fileName,
                            remoteFileDirectory);
//                            LOGGER.sensitiveData(remoteFileDirectory));

                    //hhqs-55382 in case fileName contain directory path, we need to extract path from fileName and build directories on fileServer beforehand
                    String[] filePath = fileName.split(SEPARATOR);
                    for (int index = 0; index < filePath.length; index++) {
                        if (index == (filePath.length - 1)) {
                            fileName = filePath[index];
                            break;
                        }
                        remoteFileDirectory += (filePath[index] + SEPARATOR);
                    }
                    createDirectory(remoteFileDirectory);
                    String remoteFilePath = m_fileServerInfo.getBasePath() + remoteFileDirectory + fileName;
                    WebdavSardineUtil.put(sardine, remoteFilePath, fileInfo.getFileInputStream());
                    LOGGER.info("User {} has successfully uploaded single file: {} to remote-file-path: {} using input stream",
                            getCurrentUser(), fileName,
//                            LOGGER.sensitiveData(getCurrentUser()), fileName,
                            remoteFilePath);
//                            LOGGER.sensitiveData(remoteFilePath));
                }
                return null;
            }

        });
    }

    @Override
    public void deleteResource(String resource) throws Exception {
        executeWithWebdavSessionTemplate(Operation.DELETE, new WebdavSessionTemplate<Void>() {
            @Override
            public Void execute(Sardine sardine) throws Exception {
                deleteSingleResource(resource, sardine);
                return null;
            }
        });
    }

    @Override
    public void deleteResources(Set<String> resources) throws Exception {
        executeWithWebdavSessionTemplate(Operation.DELETE, new WebdavSessionTemplate<Void>() {
            @Override
            public Void execute(Sardine sardine) throws Exception {
                LOGGER.debug("Deleting the resource list {}", resources);
                for (String resource : resources) {
                    deleteSingleResource(resource, sardine);
                }
                return null;
            }
        });
    }

    protected void deleteSingleResource(String resource, Sardine sardine) throws IOException {
        String directoryPath = m_fileServerInfo.getBasePath() + resource;
        LOGGER.info("User {} is deleting a resource: {} at path {}",
                getCurrentUser(), resource, directoryPath);
//                LOGGER.sensitiveData(getCurrentUser()), resource, LOGGER.sensitiveData(directoryPath));
        if (WebdavSardineUtil.exists(sardine, directoryPath)) {
            sardine.delete(WebdavSardineUtil.encodeURL(directoryPath));
            LOGGER.info("User {} has successfully deleted resource {} at path {}",
                    getCurrentUser(), resource, directoryPath);
//                    LOGGER.sensitiveData(getCurrentUser()), resource, LOGGER.sensitiveData(directoryPath));
        } else {
            LOGGER.info("Resource: {} does not exist at path {}", resource,
                    directoryPath);
//                    LOGGER.sensitiveData(directoryPath));
        }
    }

    protected void setStagingArea(String stagingArea) {
        m_stagingArea = stagingArea;
    }

    public String getStagingArea() {
        return m_stagingArea + SEPARATOR;
    }

    @Override
    public boolean isResourceLocked(String resource) throws Exception {
        final boolean[] isLocked = {false};
        executeWithWebdavSessionTemplate(Operation.GET, new WebdavSessionTemplate<Void>() {
            @Override
            public Void execute(Sardine sardine) throws Exception {
                String remotePath = m_fileServerInfo.getBasePath() + resource;
                    Set<QName> props = new HashSet();
                    props.add(QNAME);
                    List<DavResource> davResources = sardine.propfind(remotePath, 0, props);
                    davResources.stream().forEach(davResource -> isLocked[0] = Boolean.valueOf(davResource.getCustomProps().get("lockdiscovery")));
                return null;
            }
        });
        return isLocked[0];
    }

    @Override
    public List<String> listSubdirectoriesInRemoteDirectory(String directoryPath) throws Exception {
        final List<String> listFiles = new ArrayList<>();
        return executeWithWebdavSessionTemplate(Operation.PROPFIND, new WebdavSessionTemplate<List<String>>() {
            @Override
            public List<String> execute(Sardine sardine) throws Exception {
                String fileServerBasePath = m_fileServerInfo.getBasePath() + directoryPath;
                if (WebdavSardineUtil.exists(sardine, fileServerBasePath)) {
                    LOGGER.debug("User {} is listing file path {}",
                            getCurrentUser(), fileServerBasePath);
//                            LOGGER.sensitiveData(getCurrentUser()), LOGGER.sensitiveData(fileServerBasePath));
                    List<DavResource> resources = WebdavSardineUtil.list(sardine, fileServerBasePath);

                    LOGGER.debug("resource list: {} from fileServerBasePath {}", resources,
                            fileServerBasePath);
//                            LOGGER.sensitiveData(fileServerBasePath));

                    String baseUrl = m_fileServerInfo.getUrl();

                    for (DavResource davResource : resources) {
                        String davResourcePath = baseUrl + davResource.getPath();
                        if (davResource.isDirectory()) {
                            /*from resources list, we have something like

                            /webdav/local-core-av/sharedfs/non-coded-device-plugs/LS-DF-16GW-20A.06/
                            /webdav/local-core-av/sharedfs/non-coded-device-plugs/LS-DF-16GW-20A.06/annotations/
                            /webdav/local-core-av/sharedfs/non-coded-device-plugs/LS-DF-16GW-20A.06/model/

                            purpose of this check is to ignore parent directory /webdav/local-core-av/sharedfs/non-coded-device-plugs/LS-DF-16GW-20A.06/
                            */
                            LOGGER.debug("Start comparing davResourcePath {} to fileServerBasePath {}",
                                    davResourcePath, fileServerBasePath);
//                                    LOGGER.sensitiveData(davResourcePath), LOGGER.sensitiveData(fileServerBasePath));
                            if (!fileServerBasePath.equals(davResourcePath)) {
                                listFiles.add(davResourcePath);
                            }
                        }
                    }
                } else {
                    throw new IOException("File path does not exists: " + fileServerBasePath);
                }
                return listFiles;
            }
        });
    }

    @Override
    public String lockResource(String filePath) throws Exception {
        return executeWithWebdavSessionTemplate(Operation.CREATE, new WebdavSessionTemplate<String>() {
            @Override
            public String execute(Sardine sardine) throws Exception {
                String token = null;
                if (WebdavSardineUtil.exists(sardine, filePath)) {
                    LOGGER.debug("User {} is locking the resource {}",
                            getCurrentUser(), filePath);
//                            LOGGER.sensitiveData(getCurrentUser()), LOGGER.sensitiveData(filePath));
                    token = sardine.lock(filePath);
                    LOGGER.debug("User {} has locked the resource {}",
                            getCurrentUser(), filePath);
//                            LOGGER.sensitiveData(getCurrentUser()), LOGGER.sensitiveData(filePath));
                } else {
                    throw new IOException("Resource does not exist: " + filePath);
                }
                return token;
            }
        });
    }

    @Override
    public String refreshLock(String filePath, String lockToken, String file) throws Exception {
        return executeWithWebdavSessionTemplate(Operation.CREATE, new WebdavSessionTemplate<String>() {
            @Override
            public String execute(Sardine sardine) throws Exception {
                String token = null;
                if (WebdavSardineUtil.exists(sardine, filePath)) {
                    LOGGER.debug("User {} is refreshing lock of the resource {}",
                            getCurrentUser(), filePath);
//                            LOGGER.sensitiveData(getCurrentUser()), LOGGER.sensitiveData(filePath));
                    token = sardine.refreshLock(filePath, lockToken, file);
                    LOGGER.debug("User {} has refreshed lock of the resource {}",
                            getCurrentUser(), filePath);
//                            LOGGER.sensitiveData(getCurrentUser()), LOGGER.sensitiveData(filePath));
                } else {
                    throw new IOException("Resource does not exist: " + filePath);
                }
                return token;
            }
        });
    }

    @Override
    public void unlockResource(String filePath, String lockToken) throws Exception {
        executeWithWebdavSessionTemplate(Operation.CREATE, new WebdavSessionTemplate<Void>() {
            @Override
            public Void execute(Sardine sardine) throws Exception {
                if (WebdavSardineUtil.exists(sardine, filePath)) {
                    LOGGER.debug("User {} is unlocking the resource {}",
                            getCurrentUser(), filePath);
//                            LOGGER.sensitiveData(getCurrentUser()), LOGGER.sensitiveData(filePath));
                    sardine.unlock(filePath, lockToken);
                    LOGGER.debug("User {} has unlocked the resource {}",
                            getCurrentUser(), filePath);
//                            LOGGER.sensitiveData(getCurrentUser()), LOGGER.sensitiveData(filePath));
                } else {
                    throw new IOException("Resource does not exist: " + filePath);
                }
                return null;
            }
        });
    }

    @Override
    public Map<String, Date> getFilesCreationTime(String remotePath) throws Exception {
        final Map<String, Date> filesCreationTime = new HashMap<>();

        // only support to get 1 level for now.
        executeWithWebdavSessionTemplate(Operation.GET, (WebdavSessionTemplate<Void>) sardine -> {
            List<DavResource> davResources = WebdavSardineUtil.list(sardine, remotePath);
            for (DavResource davResource : davResources) {
                String davResourcePath = m_fileServerInfo.getUrl() + davResource.getPath();
                if (!remotePath.equals(davResourcePath)) {
                    filesCreationTime.put(davResource.getPath(), davResource.getCreation());
                }
            }
            return null;
        });
        return filesCreationTime;
    }

    protected enum Operation {
        PUT, GET, DELETE, CREATE, HEAD, PROPFIND
    }

    @Override
    public String convertToExternalUrl(String internalUrl) {
        if (StringUtils.isEmpty(internalUrl)) {
            LOGGER.warn("InternalUrl must not be null");
            return null;
        }
        String urlWithoutProtocol = internalUrl.replaceFirst(PROTOCOL_REGEX, StringUtils.EMPTY);
        //regex url
        if (!urlWithoutProtocol.contains(COLON)) {
            return internalUrl;
        }
        String fileServerUrl = m_fileServerInfo.getUrl();
        if (!internalUrl.contains(fileServerUrl)) {
            LOGGER.warn("Invalid internalUrl {}", internalUrl);
            return null;
        }
        String k8sPublicIp = getK8sPublicIp();
        String externalUrl = getSharedFileServerExternalUrl();
        if (IPV6_PATTERN.matcher(k8sPublicIp).find()) {
            externalUrl = externalUrl.replace(k8sPublicIp, String.format(IPV6_FORMAT, k8sPublicIp));
        }
        return internalUrl.replace(fileServerUrl, externalUrl);
    }

    public String getSharedFileServerExternalUrl() {
        return this.webdavFileServiceProperties.getSharedFileServerExternalUrl();
    }

    protected String getK8sPublicIp() {
        return this.webdavFileServiceProperties.getK8sPublicIp();
    }
}