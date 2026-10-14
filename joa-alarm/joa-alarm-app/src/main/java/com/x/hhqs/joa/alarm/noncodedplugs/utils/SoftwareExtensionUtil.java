package com.x.hhqs.joa.alarm.noncodedplugs.utils;

import com.google.common.base.Stopwatch;
import com.x.hhqs.joa.common.sharedfileservice.utils.SystemPropertyUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SoftwareExtensionUtil {

    public static final String ZIP_SUFFIX = ".zip";
    public static final String TAR_SUFFIX = ".tar";
    public static final String MAPPING_SUFFIX = "apping.txt";
    public static final String TMP_DIRECTORY = "TMP_DIRECTORY";
    public static final String TAR_GZ_ZIP_EXTENSION = ".tar.gz.zip";
    public static final String TGZ_ZIP_EXTENSION = ".tgz.zip";
    public static final String TAR_ZIP_EXTENSION = ".tar.zip";
    public static final String FTL_EXTENSION = ".ftl";
    public static final String DIRECTORY_PATH = "DIRECTORY_PATH";
    public static final String TAR_FILE_PATH = "TAR_FILE_PATH";
    public static final String TEMP = "_TEMP";
    public static final String LS_TEMP = "_LS";
    public static final String ONT_DIRECTORY = "ONT";
    public static final String ONT_CONFIG_MAPPING_FILE_NAME = "ONT_Config_File_Mapping.txt";
    public static final String SOFTWARE_SCRIPT_CONTENT_FILE_NAME = "software-script-content.js";
    public static final String SOFTWARE_SCRIPT_META_INFO_FILE_NAME = "software-script-meta-info.json";
    public static final String RESOURCES_DIRECTORY = "resources";
    public static final String SEPARATOR = "/";
    public static final String BOARD_MAPPING_FILE = "board_mapping.json";
    public static final String MULTI_BOARD_PACKAGE = "multi-board-package";
    public static final String MULTI_BOARD_PACKAGE_FILE = "multi-board-package.json";
    private static final int BUFFER_SIZE = 4096;
    public static final String NEW_LINE = "\n";
    private static final String ZIP_AND_TAR_EXTENSIONS = ZIP_SUFFIX + "," + TAR_SUFFIX;
    public static final String COMPRESS_REGEX = ".*(?<!(tar|zip))$";
    public static final String VENDOR_SPECIFIC_SW_FILE_NAME_PATTERN_ENV = "VENDOR_SPECIFIC_SW_FILE_NAME_PATTERN";
    public static final String VENDOR_SPECIFIC_SW_FILE_NAME_PATTERN_STRING = SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(VENDOR_SPECIFIC_SW_FILE_NAME_PATTERN_ENV,"^(PRE|CFG|XML|GFT|CFP)[a-zA-Z]{4}[0-9]{3}$");
    public static Pattern VENDOR_SPECIFIC_SW_FILE_NAME_PATTERN = Pattern.compile(VENDOR_SPECIFIC_SW_FILE_NAME_PATTERN_STRING);
    public static final String CORE_STAGING_AREA_ENV = "CORE_STAGING_AREA";
    public static final String ROOT_DIR = "/root";
    public static final String CORE_STAGING_AREA = SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(CORE_STAGING_AREA_ENV, ROOT_DIR);
    public static Set<String> SOFTWARE_SCRIPT_ARCHIVE = new HashSet<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(SoftwareExtensionUtil.class);
    
    public static final PathMatcher COMPRESS_MATCHER = FileSystems.getDefault().getPathMatcher("glob:**.{tar,zip}");
    // LS migration script
    public static final String MIGRATION_SCRIPT_BUNDLE_PREFIX = "ls-migration-script-bundle-";
    public static Set<String> MIGRATION_SCRIPT_BUNDLE_ARCHIVE = new HashSet<>();
    public static final String MIGRATION_SCRIPT_META_INFO = "meta-info.json";
    public static final String MIGRATION_SCRIPT_REQUIRED_ENV = "requires_environment";
    public static final String MIGRATION_SCRIPT_CALL_POINT_MAP = "call-point-mappings";
    public static final String CALL_POINT_MAP_BEFORE = "before";
    public static final String CALL_POINT_MAP_AFTER = "after";
    public static final String CALL_POINT_SCRIPT_EXTENSION = "py";
    public static String DEVICE_SW_SCRIPTS_TYPE_DEFAULT = "nashorn-js";
    public static String DEVICE_LS_MIGRATION_SCRIPTS_TYPE = "lightspan-python";
    public static final List<String> MIGRATION_SCRIPT_REQUIRED_FIELDS = Arrays.asList("name", MIGRATION_SCRIPT_REQUIRED_ENV, MIGRATION_SCRIPT_CALL_POINT_MAP);
    public static final List<String> CALL_POINT_MAP_REQUIRED_FIELDS = Arrays.asList(CALL_POINT_MAP_BEFORE, CALL_POINT_MAP_AFTER);

    public static boolean checkFile(String fileName) {
        if (StringUtils.isNotEmpty(fileName) && fileName.endsWith(TAR_SUFFIX)) {
            return true;
        }
        return false;
    }

    public static boolean checkFiles(String fileName, List<String> fileList) {
        if (StringUtils.isEmpty(fileName)) {
            return false;
        }

        try {
            if (fileName.endsWith(TAR_SUFFIX)) {
                return true;
            }

            if (fileName.endsWith(ZIP_SUFFIX)) {
                if (fileList.contains(SOFTWARE_SCRIPT_META_INFO_FILE_NAME) && fileList.contains(SOFTWARE_SCRIPT_CONTENT_FILE_NAME)) {
                    // hhqs-84705: is softwareScript file
                    SOFTWARE_SCRIPT_ARCHIVE.add(fileName);
                    return true;
                }
                // hhqs-114999: LS migration script
                if (fileName.contains(MIGRATION_SCRIPT_BUNDLE_PREFIX) && fileList.size() > 0) {
                    MIGRATION_SCRIPT_BUNDLE_ARCHIVE.add(fileName);
                    return true;
                }

                for (String file : fileList) {
                    if (StringUtils.isNotEmpty(file) &&
                        ((file.endsWith(TAR_SUFFIX) && fileList.size() == 1)
                                || (file.toLowerCase().endsWith(MAPPING_SUFFIX) && fileList.size() > 1)
                                || file.toLowerCase().endsWith(BOARD_MAPPING_FILE)
                                || file.toLowerCase().endsWith(MULTI_BOARD_PACKAGE_FILE))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error while checking file {}, {}", fileName, fileList, e);
            return false;
        }
        return isOnuVendorSpecificSwSingleFile(fileName);
    }

    public static int getPriority(String fileName, List<String> fileList) {
        if (StringUtils.isNotEmpty(fileName)) {
            if (fileName.endsWith(ZIP_SUFFIX)) {
                for (String file : fileList) {
                    if (StringUtils.isNotEmpty(file) && file.toLowerCase().endsWith(MULTI_BOARD_PACKAGE_FILE)) {
                        return 1;
                    }
                }
            }
        }
        return 0;
    }

    public static List<String> walkCompressFile(String filePath) throws IOException {
        File tmpDir = createTempDirWithSameParent(filePath);
        if (tmpDir == null) {
            throw new IOException(filePath + " missing or invalid file!");
        }
        try {
            extractFileRecursively(filePath, tmpDir);
            Collection<File> files = FileUtils.listFiles(tmpDir, new RegexFileFilter(COMPRESS_REGEX), DirectoryFileFilter.DIRECTORY);
            return files.stream().map(File::getAbsolutePath).collect(Collectors.toList());
        } finally {
            FileUtils.deleteQuietly(tmpDir);
        }
    }

    private static void extractFileRecursively(String filePath, File destinationTmp) {
        File extrasFile = new File(filePath);
        if (!extrasFile.exists()) {
            return;
        }
        String tmpDir = destinationTmp.getPath();
        copySilently(extrasFile, tmpDir);
        try (final Stream<Path> stream = Files.list(Paths.get(tmpDir))) {
            stream.filter(COMPRESS_MATCHER::matches)
                    .forEach(path -> {
                        extract(path.toString(), tmpDir);
                        File tmpFile = new File(tmpDir);
                        File[] files = tmpFile.listFiles((FileFilter)
                                FileFilterUtils.notFileFilter(new SuffixFileFilter(String.valueOf(path.getFileName()))));
                        if (Objects.nonNull(files)) {
                            Arrays.stream(files)
                                    .map(File::toPath)
                                    .filter(COMPRESS_MATCHER::matches)
                                    .forEach(file -> extractFileRecursively(file.toAbsolutePath().toString(),
                                            createTempDirWithSameParent(file.toAbsolutePath().toString())));
                        }
                    });
        } catch (IOException e) {
            LOGGER.error("Error while extracting file {} to {}", filePath, destinationTmp, e);
        }
    }

    private static File createTempDirWithSameParent(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            String tmpDir = file.getParentFile().getPath() + File.separator + file.getName() + LS_TEMP;
            createDirectory(tmpDir);
            return new File(tmpDir);
        }
        return null;
    }

    public static boolean copySilently(File file, String tmpDir) {
        try {
            Files.copy(Paths.get(file.getPath()), Paths.get(tmpDir + File.separator + file.getName()), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            LOGGER.error("Error while copying file {} to {}", file.getPath(), tmpDir, e);
            return false;
        }
    }

    private static void extract(String filePath, String destinationDir) {
        try {
            if (filePath.endsWith(ZIP_SUFFIX)) {
                unzip(filePath, destinationDir);
            } else if (filePath.endsWith(TAR_SUFFIX)) {
                untar(filePath, destinationDir);
            }
        } catch (IOException e) {
            LOGGER.error("Error while extract file {} to {}", filePath, destinationDir, e);
        }

    }

    public static String getExtension(){
        return ZIP_AND_TAR_EXTENSIONS;
    }

    public static boolean isOnuVendorSpecificSw(String swArchiveFilePath) throws IOException{
        Map<String, String> paths = new HashMap<>();
        try {
            paths = extractArchiveFile(swArchiveFilePath);

            LOGGER.debug("Start parsing installer from {}", paths);
            String tmpDirectoryPath = paths.get(TMP_DIRECTORY);
            File ontDirectory = new File(tmpDirectoryPath + SEPARATOR + ONT_DIRECTORY);
            File ontMappingFile = new File(tmpDirectoryPath + SEPARATOR + ONT_CONFIG_MAPPING_FILE_NAME);
            return ontDirectory.exists() && ontMappingFile.exists() && ontDirectory.listFiles().length > 0;
        } finally {
            String tmpDirectory = paths.get(TMP_DIRECTORY);
            // clean-up the staging area by removing extracted files
            deleteDirectory(tmpDirectory);
        }
    }

    public static boolean isOnuVendorSpecificSwSingleFile(String file) {
        if (StringUtils.isEmpty(file)) {
            return false;
        }
        String checkFileName = file.substring(file.lastIndexOf(SEPARATOR) + 1);
        String fileName = file.equals(checkFileName) ? file : checkFileName;
        return StringUtils.isNotEmpty(fileName) && VENDOR_SPECIFIC_SW_FILE_NAME_PATTERN.matcher(fileName).find();
    }

    public static String getOnuVendorSpecificSwSingleFilePattern() {
        return VENDOR_SPECIFIC_SW_FILE_NAME_PATTERN.pattern();
    }

    public static Map<String, String> extractArchiveFile(String swArchiveFilePath) throws IOException {
        Stopwatch stopWatch = Stopwatch.createStarted();
        LOGGER.info("Extracting sw archive file {}", swArchiveFilePath);
        logSwArchiveFileInfo(swArchiveFilePath);
        Map<String, String> paths = new HashMap<>();
        if (swArchiveFilePath.endsWith(TAR_GZ_ZIP_EXTENSION)) {
            swArchiveFilePath = swArchiveFilePath.replace(TAR_GZ_ZIP_EXTENSION, TAR_ZIP_EXTENSION);
        } else if (swArchiveFilePath.endsWith(TGZ_ZIP_EXTENSION)) {
            swArchiveFilePath = swArchiveFilePath.replace(TGZ_ZIP_EXTENSION, TAR_ZIP_EXTENSION);
        }
        String tempDirectory = swArchiveFilePath + TEMP;

        // 1. Create a temp directory in the staging area: TEMP_L6GQAA53.000.tar.zip
        createDirectory(tempDirectory);
        LOGGER.debug("Created a temp directory: {}", tempDirectory);

        String directoryPath;
        String fileName = swArchiveFilePath.substring(swArchiveFilePath.lastIndexOf(SEPARATOR) + 1);
        if(swArchiveFilePath.endsWith(TAR_SUFFIX)) {
            directoryPath = untar(swArchiveFilePath, tempDirectory);
            paths.put(TAR_FILE_PATH, swArchiveFilePath);
        } else if (isOnuVendorSpecificSwSingleFile(fileName)) {
            // Support for ONU vendor-specific software single file
            LOGGER.debug("Create mapping file '{}' and ONT directory for ONU vendor-specific software single file '{}'", ONT_CONFIG_MAPPING_FILE_NAME, fileName);

            // Create ONT directory
            directoryPath = tempDirectory + SEPARATOR + ONT_DIRECTORY;
            createDirectory(directoryPath);

            // Copy single file to ONT directory
            FileUtils.copyFileToDirectory(new File(swArchiveFilePath), new File(directoryPath));

            // Create ONT_Config_File_Mapping.txt
            StringBuilder newMappingFileContent = new StringBuilder();
            newMappingFileContent.append(fileName).append(NEW_LINE);
            newMappingFileContent.append("#--------------+-------------+------------+").append(NEW_LINE);
            newMappingFileContent.append("# hwpartnum    | mnemonic    | swpartnum  |").append(NEW_LINE);
            newMappingFileContent.append("#--------------+-------------+------------+").append(NEW_LINE);
            newMappingFileContent.append("|              |             | ").append(fileName).append(" |").append(NEW_LINE);
            newMappingFileContent.append("+--------------+-------------+------------+").append(NEW_LINE);
            try (FileWriter myWriter = new FileWriter(new File(tempDirectory + SEPARATOR + ONT_CONFIG_MAPPING_FILE_NAME), false)) {
                myWriter.write(String.valueOf(newMappingFileContent));
            }
            LOGGER.debug("Created mapping file '{}' and ONT directory for ONU vendor-specific software single file '{}'", ONT_CONFIG_MAPPING_FILE_NAME, fileName);
        } else {
            // 2. Unzip software archive file - return tar file path (L6GQAA53.000.tar)
            directoryPath = unzip(swArchiveFilePath, tempDirectory);
            LOGGER.debug("Unzipped archive file: {} with tarFilePath: {}", swArchiveFilePath, directoryPath);

            if (directoryPath.endsWith(TAR_SUFFIX)) {
                // 3. Untar tar file. Otherwise, we already have a directory to work with
                directoryPath = untar(directoryPath, tempDirectory);
                LOGGER.info("Extracted the device software archive: {}", swArchiveFilePath);
            }
        }

        paths.put(TMP_DIRECTORY, tempDirectory);
        paths.put(DIRECTORY_PATH, directoryPath);
        LOGGER.info("Extracted sw archive file {} took '{}' ms", swArchiveFilePath, stopWatch.elapsed(TimeUnit.MILLISECONDS));
        return paths;
    }

    private static void logSwArchiveFileInfo(String swArchiveFilePath) {
        if (LOGGER.isDebugEnabled()) {
            File swArchiveFile = new File(swArchiveFilePath);
            File stagingArea = new File(CORE_STAGING_AREA);
            LOGGER.info("Existence of software archive file '{}' is '{}'. List items in staging area:\n'{}'\n " +
                            "and disk used space/total space = '{}'/'{}'",
                    swArchiveFilePath, swArchiveFile.exists(), stagingArea.list(), stagingArea.getUsableSpace(), stagingArea.getTotalSpace());
        }
    }

    public static void createDirectory(String destDirectory) {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
    }

    public static void deleteDirectory(String destDirectory) {
        if (StringUtils.isEmpty(destDirectory)) {
            return;
        }

        try {
            FileUtils.deleteDirectory(new File(destDirectory));
        } catch (IOException e) {
            LOGGER.error("Exception while deleting directory: {}", destDirectory, e);
        }
    }

    public static String unzip(String zipFilePath, String desDirectory) throws IOException {
        String directoryPath = null;
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry = zipIn.getNextEntry();
            // first entry might be a directory, a file or a file inside directory
            if (zipEntry == null) {
                throw new IOException("Invalid format for software archive file: " + zipFilePath);
            }

            String entryName = zipEntry.getName();

            //if zip file contains only tar file, we need to return tar file path to make the next operation can process
            directoryPath = buildDirectoryPath(desDirectory, entryName);

            boolean isBuiltDirectoryPath = false;

            while (zipEntry != null) {
                entryName = zipEntry.getName();
                if(!isBuiltDirectoryPath && entryName.contains(File.separator)) {
                    // hhqs-46566: For zip file that has a directory inside it, usually most zipper will create the first entry that is the directory
                    // However, some zipper does not follow this convention. There might be cases where no directory entry found, that's why we have to do this check and create directory manually
                    directoryPath = buildDirectoryPath(desDirectory, entryName);
                    if (!zipEntry.isDirectory()) {
                        buildDirectoryFromPath(directoryPath);
                    }
                    isBuiltDirectoryPath = true;
                }
                String path = desDirectory + File.separator + entryName;
                if (!zipEntry.isDirectory()) {
                    // hhqs-51944: if there is not parent directory yet, we need to create one
                    File file = new File(entryName);
                    String parentPath = desDirectory;
                    if (file.getParentFile() != null) {
                        parentPath = desDirectory + File.separator + file.getParentFile();
                    }
                    File parentFile = new File(parentPath);
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    // if the entry is a file, extracts it
                    extractFile(zipIn, path);
                } else {
                    buildDirectoryFromPath(path);
                }
                zipEntry = zipIn.getNextEntry();
            }
        } catch (ZipException e) {
            throw new IOException("Invalid format for software archive file: " + zipFilePath, e);
        }
        return directoryPath;
    }

    private static String buildDirectoryPath(String destDirectory, String entryName) {
        String[] filePaths = entryName.split(File.separator);
        return destDirectory + File.separator + filePaths[0];
    }

    private static void buildDirectoryFromPath(String path) {
        File dir = new File(path);
        dir.mkdir();
    }

    /*
     * Untar a tar file
     */
    public static String untar(String tarFilePath, String destDirectory) throws IOException {
        String directoryPath = null;
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(new FileInputStream(tarFilePath))) {
            TarArchiveEntry entry = tarIn.getNextTarEntry();
            String entryName;
            // iterates over entries in the tar file
            while (entry != null) {
                entryName = entry.getName();
                String filePath = destDirectory + File.separator + entryName;
                if (!entry.isDirectory()) {
                    // if there is not parent directory yet, we need to create one
                    File file = new File(entryName);
                    String parentPath = destDirectory;
                    if (file.getParentFile() != null) {
                        parentPath = destDirectory + File.separator + file.getParentFile();
                    }
                    File parentFile = new File(parentPath);
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    // if the entry is a file, extracts it
                    extractFile(tarIn, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    if (directoryPath == null) {
                        //the first direcory inside .tar file is the sw image directory
                        directoryPath = filePath.substring(0, filePath.length() - 1);
                    }
                }
                entry = tarIn.getNextTarEntry();
            }
        }
        return directoryPath;
    }

    public static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[BUFFER_SIZE];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    /**
     * Extracts a file entry
     *
     * @param inputStream
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(InputStream inputStream, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = inputStream.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    public static final String formatRelativePath(String relativePath) {
        if (relativePath != null && !relativePath.isEmpty()) {
            if (!relativePath.startsWith(File.separator)) {
                relativePath = File.separator + relativePath;
            }
            if (!relativePath.endsWith(File.separator)) {
                relativePath = relativePath + File.separator;
            }
            return relativePath;
        }
        return File.separator;
    }

    public static File buildZipFile(String path, String name, String data) {
        File folder = new File(path);
        if(!folder.exists()) {
            folder.mkdir();
        }
        try (ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(new File(path, name)))) {
            ZipEntry zipEntry = new ZipEntry(MULTI_BOARD_PACKAGE_FILE);
            zipStream.putNextEntry(zipEntry);
            byte[] bytes = data.getBytes();
            zipStream.write(bytes, 0, bytes.length);
            zipStream.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new File(path, name);
    }

    public static JSONObject getMultiBoardPackageFile(File file) {
        List<String> fileList = getFileNameList(file);
        try {
            ZipFile zipFile = new ZipFile(file);
            for (String fileName: fileList) {
                if (fileName.equals(MULTI_BOARD_PACKAGE_FILE)) {
                    ZipEntry zipEntry = zipFile.getEntry(fileName);
                    if (zipEntry != null) {
                        InputStream inputStream = zipFile.getInputStream(zipEntry);
                        String jsonContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        inputStream.close();
                        return new JSONObject(jsonContent);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Cannot parse the multi board package from file {}", file.getName(), e);
        }
        return null;
    }

    public static List<String> getFileNameList(File file)
    {
        List<String> fileNameList =new ArrayList<String>();
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(file);
        } catch (IOException e) {
            LOGGER.error("Invalid format for software archive file: {}", file.getName(), e);
            return fileNameList;
        }

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                String fileName = entry.getName();
                fileNameList.add(fileName);
            }
        }
        return fileNameList;

    }

    public static boolean isPythonScript(String scriptType) {
        return DEVICE_LS_MIGRATION_SCRIPTS_TYPE.equals(scriptType);
    }

    public static void deleteFile(String filePath) throws IOException {
        Files.delete(Paths.get(filePath));
    }
}