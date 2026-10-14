package com.x.hhqs.joa.common.sharedfileservice.api;

import com.x.hhqs.joa.common.sharedfileservice.models.SharedFileInfo;
import com.x.hhqs.joa.common.sharedfileservice.utils.FileServerInfo;
import com.x.hhqs.joa.common.sharedfileservice.utils.UploadFileInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SharedFileService {

    /**
     * Create a directory at the specified url
     *
     * @param directory The directory name to be created, directory can be single directory 'folder1' 
     *        or multiple directories 'folder1/folder2/folder3'
     * @throws Exception if any network or IO error occurred.
     */
    void createDirectory(String directory) throws Exception;

    /**
     * Performs a HTTP <code>HEAD</code> request to see if a resource exists or not.
     *
     * @param resourceName The resource name to be checked.
     * @return Anything outside of the 200-299 response code range returns false.
     * @throws Exception if any network or IO error occurred.
     */
    boolean isResourceExisting(String resourceName) throws  Exception;
    
    /**
     * Get list files from File Server
     * @param remoteDirectory The directory name of File Server
     * @param swBaseDirectory The software base directory
     * @return List<String> Files Path in remote directory
     * @throws Exception if any network or IO error occurred.
     */
    List<String> listFilesInRemoteDirectory(String remoteDirectory, String swBaseDirectory) throws Exception;

    /**
     * Upload local file/directory to the File Server.
     * @param localUrl Path to the local file/directory to be uploaded.
     * @param remoteDirectory The directory name of the File Server
     * @return List<SharedFileInfo> Files uploaded
     * @throws Exception if any network or IO error occurred.
     */
    List<SharedFileInfo> uploadFileToFileServer(String localUrl, String remoteDirectory) throws Exception;

    /**
     * Upload the list file to the File Server
     * @param uploadFilesInfo: list file being uploaded.
     * @throws Exception if any network or IO error occurred.
     */
    void uploadFileToFileServer(List<UploadFileInfo> uploadFilesInfo) throws Exception;

    /**
     * Delete a resource on file server
     *
     * @param resource Resource to be deleted, can be a folder or a filename
     * @throws Exception if any network or IO error occurred.
     */
    void deleteResource(String resource) throws Exception;

    /**
     * Delete the resources on file server
     *
     * @param resources Resource list to be deleted, can be a folders or a filenames
     * using Set to avoid duplicate resource.
     * @throws Exception if any network or IO error occurred.
     */
    void deleteResources(Set<String> resources) throws Exception;

    /**
     * Return the file server informations such as: username, password, host, port ...
     */
    FileServerInfo getFileServerInfos();
    
    /**
     * Checks File server reachability
     * 
     * @return boolean - Return 'true' If file server is reachable
     * @throws Exception
     */
    boolean isFileServerConnected() throws Exception;

    /**
     * Download remote file/directory from the File Server to the local.
     * @param remoteDirectory The directory name of the File Server to be downloaded
     * @param localUrl Path
     * @throws Exception if any network or IO error occurred.
     */
    void downloadFilesFromFileServer(String remoteDirectory, String localUrl) throws Exception;

    String getStagingArea();

    boolean isResourceLocked(String resource) throws Exception;

    List<String> listSubdirectoriesInRemoteDirectory(String remoteDirectory) throws Exception;

    String lockResource(String filePath) throws Exception;

    String refreshLock(String filePath, String lockToken, String file) throws Exception;

    void unlockResource(String filePath, String lockToken) throws Exception;

    Map<String, Date> getFilesCreationTime(String resource) throws Exception;

    String convertToExternalUrl(String internalUrl);
}
