package com.x.hhqs.joa.alarm.service.impl;

import static com.x.hhqs.joa.alarm.noncodedplugs.utils.SoftwareExtensionUtil.unzip;
import static com.x.hhqs.joa.alarm.noncodedplugs.utils.SoftwareExtensionUtil.deleteFile;

import com.x.hhqs.joa.alarm.entity.AlarmReporter;
import com.x.hhqs.joa.alarm.entity.AlarmSeverity;
import com.x.hhqs.joa.alarm.dto.AlarmXmlConfigInfo;
import com.x.hhqs.joa.alarm.dto.AlarmTypeInfo;
import com.x.hhqs.joa.alarm.entity.AlarmDefinitionInfo;
import com.x.hhqs.joa.alarm.noncodedplugs.NonCodedPlugUtils;
import com.x.hhqs.joa.alarm.noncodedplugs.loader.PlugExtensionAlarmLoader;
import com.x.hhqs.joa.alarm.service.ACRestService;
import com.x.hhqs.joa.alarm.service.AlarmDefinitionDaoService;
import com.x.hhqs.joa.alarm.vo.ManagerType;
import com.x.hhqs.joa.common.sharedfileservice.api.SharedFileService;
import com.x.hhqs.joa.common.restclient.Pair;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AlarmInitService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmInitService.class);
    private static final String COLON = ":";
    private static final String SLASH = "/";
    public static final String ZIP_EXTENSION = ".zip";
    public static final String NON_CODED_DEVICE_PLUGS = "non-coded-device-plugs";
    public static final String DOWNLOAD_OF_PLUG_DEFINITION_FROM_SHARED_FILE_SERVICE_FAILED_ERROR = "Download of the device plug definition from the shared file service failed: %s , see logs for more details ";

    public static final Map<String, PlugExtensionAlarmLoader> PLUG_EXTENSION_ALARM_LOADER_MAP = new HashMap<>();
    public static final Map<AlarmReporter, List<AlarmTypeInfo>> ALL_SUPPORTED_ALARMS = new HashMap<>();

    private List<InputStream> inputStreamList = new ArrayList<>();

    private final ACRestService acRestService;
    private final AlarmDefinitionDaoService alarmDefinitionDaoService;
    private final SharedFileService sharedFileService;

    public AlarmInitService(ACRestService acRestService, AlarmDefinitionDaoService alarmDefinitionDaoService, SharedFileService sharedFileService) {
        this.acRestService = acRestService;
        this.alarmDefinitionDaoService = alarmDefinitionDaoService;
        this.sharedFileService = sharedFileService;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
    public void transactionalInit() throws Exception {
        try {
            initCache();
            alarmDefinitionDaoService.truncateTable();
            getAllSupportedAlarmTypes();
            loadAllPlugExtensionAlarms();
            initAllAlarmDefinitions();
        } finally {
            initCache();
        }
    }

    public void initCache() {
        if (!PLUG_EXTENSION_ALARM_LOADER_MAP.isEmpty()) {
            PLUG_EXTENSION_ALARM_LOADER_MAP.forEach((k, v) -> v.destroy());
            PLUG_EXTENSION_ALARM_LOADER_MAP.clear();
        }
        if (!ALL_SUPPORTED_ALARMS.isEmpty()) {
            ALL_SUPPORTED_ALARMS.clear();
        }
    }

    @PreDestroy
    public void destroy() {
        initCache();
    }

    public int countAlarmDefinitions() {
        return alarmDefinitionDaoService.count();
    }

    protected void initAllAlarmDefinitions() {
        List<AlarmDefinitionInfo> alarmDefinitionInfos = new ArrayList<>();
        List<AlarmTypeInfo> ignoreAlarmTypes = new ArrayList<>();
        ALL_SUPPORTED_ALARMS.forEach((alarmReporter, supportedAlarms) -> {
            if (alarmReporter.equals(AlarmReporter.DEVICE)) {
                for (AlarmTypeInfo supportedAlarm : supportedAlarms) {
                    if (!supportedAlarm.getAlarmTypeReporter().isEmpty()) {
                        String reporter = supportedAlarm.getAlarmTypeReporter().stream().toList().get(0);
                        if (PLUG_EXTENSION_ALARM_LOADER_MAP.containsKey(reporter)) {
                            PlugExtensionAlarmLoader alarmLoader = PLUG_EXTENSION_ALARM_LOADER_MAP.get(reporter);
                            if (alarmLoader.existAlarmXmlConfigType(supportedAlarm.getAlarmTypeQualifier())) {
                                AlarmXmlConfigInfo alarmXmlConfigInfo = alarmLoader.getAlarmXmlConfigType(supportedAlarm.getAlarmTypeQualifier());
                                alarmXmlConfigInfo.alarmReporter(alarmReporter);
                                alarmXmlConfigInfo.alarmTypeId(alarmXmlConfigInfo.getNsModuleName() + COLON + alarmXmlConfigInfo.getAlarmType());
                                alarmDefinitionInfos.add(alarmXmlConfigInfo.toAlarmDefinition());
                            } else {
                                ignoreAlarmTypes.add(supportedAlarm);
                            }
                        }
                    }
                }
            } else {
                for (AlarmTypeInfo supportedAlarm : supportedAlarms) {
                    String alarmTypeId = supportedAlarm.getAlarmTypeId();
                    String alarmType;
                    if (alarmTypeId.contains(COLON)) {
                        alarmType = alarmTypeId.substring(alarmTypeId.lastIndexOf(COLON) + 1);
                    } else {
                        alarmType = alarmTypeId;
                    }
                    alarmDefinitionInfos.add(new AlarmXmlConfigInfo()
                            .alarmReporter(alarmReporter)
                            .nsModuleName("")
                            .alarmType(alarmType)
                            .alarmTypeId(supportedAlarm.getAlarmTypeId())
                            .severity(AlarmSeverity.UNKNOWN)
//                            .category("")
                            .description("")
                            .adviceForRepair("")
                            .additionalInfo("")
                            .toAlarmDefinition()
                    );
                }
            }
        });
        // distinct alarm definitions
        List<AlarmDefinitionInfo> uniqueAlarms = alarmDefinitionInfos.stream()
                .collect(Collectors.toMap(
//                        alarm -> new UniqueKey(alarm.getNsModuleName(), alarm.getAlarmType(), alarm.getAlarmTypeId()),
                        AlarmDefinitionInfo::getAlarmTypeId,
                        alarm -> alarm,
                        (existing, replacement) -> existing))  // If duplicate, keep the existing one
                .values()
                .stream()
                .toList();

        alarmDefinitionDaoService.saveBatch(uniqueAlarms);
    }

//    private static class UniqueKey {
//        private final String nsPrefix;
//        private final String alarmType;
//        private final String alarmTypeId;
//
//        public UniqueKey(String nsPrefix, String alarmType, String alarmTypeId) {
//            this.nsPrefix = nsPrefix;
//            this.alarmType = alarmType;
//            this.alarmTypeId = alarmTypeId;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//            UniqueKey that = (UniqueKey) o;
//            return Objects.equals(nsPrefix, that.nsPrefix) &&
//                    Objects.equals(alarmType, that.alarmType) &&
//                    Objects.equals(alarmTypeId, that.alarmTypeId);
//        }
//
//        @Override
//        public int hashCode() {
//            return Objects.hash(nsPrefix, alarmType, alarmTypeId);
//        }
//    }

    protected void loadAllPlugExtensionAlarms() throws Exception {
        String appPath = NON_CODED_DEVICE_PLUGS + SLASH;
        if (sharedFileService.isResourceExisting(appPath)) {
            List<String> dirs = sharedFileService.listSubdirectoriesInRemoteDirectory(appPath);
            for (String url : dirs) {
                LOGGER.info("Directory url: {}", url);
                String replace = url.replace(sharedFileService.getFileServerInfos().getBasePath(), StringUtils.EMPTY)
                        .replace(NON_CODED_DEVICE_PLUGS + SLASH, StringUtils.EMPTY);
                String typeVersion = replace.substring(0, replace.lastIndexOf(SLASH));
                LOGGER.info("Extension type version: {}", typeVersion);
                downloadExtension(typeVersion);
            }
        }
    }

    protected void downloadExtension(String typeVersion) throws Exception {
        String localStagingPath = sharedFileService.getStagingArea() + typeVersion;
        String extensionFile = typeVersion + ZIP_EXTENSION;
        String extensionZipFilePath = localStagingPath + SLASH + extensionFile;
        try {
            String webdavDestinationDir = NON_CODED_DEVICE_PLUGS + SLASH + typeVersion + SLASH;
            // copy files from file-server to staging area
            LOGGER.debug("Downloading files from file server");
            try {
                /*
                Download a zip file with the specified extension from WebDAV - hhqs-159498
                Example: https://[ip]/core-webdav/webdav/local-core-av/sharedfs/non-coded-device-plugs/LS-FX-IHUB-FANT-F-FX16-23.9/LS-FX-IHUB-FANT-F-FX16-23.9.zip
                */
                sharedFileService.downloadFilesFromFileServer(webdavDestinationDir, typeVersion);
                if (isFileExists(extensionZipFilePath)) {
                    unzip(extensionZipFilePath, localStagingPath);
                    LOGGER.debug("{} file has been extracted successfully", extensionZipFilePath);
                    deleteFile(extensionZipFilePath);
                    LOGGER.debug("extension zip file deleted: {}", extensionZipFilePath);
                } else {
                    // migration case, we only need to download the file from WebDAV, there's no need to download and extract the zip locally
                    LOGGER.debug("{} file does not exist, device extension is already extracted in webdav", extensionZipFilePath);
                }
            } catch (Exception e) {
                LOGGER.error("Error occured while retrieving files from file-server for type-version " + typeVersion, e);
                String errorMsg = String.format(DOWNLOAD_OF_PLUG_DEFINITION_FROM_SHARED_FILE_SERVICE_FAILED_ERROR, NonCodedPlugUtils.trimErrorMsg(e.getMessage()));
                throw new Exception(errorMsg, e);
            }

            // load alarms
            // final/
            // ./model/alarm.xml
            PlugExtensionAlarmLoader plugExtensionAlarmLoader = loadAlarms(typeVersion);
            PLUG_EXTENSION_ALARM_LOADER_MAP.put(typeVersion, plugExtensionAlarmLoader);
        } finally {
            NonCodedPlugUtils.closingInputStreams(getInputStreamList());
            String destDirname = sharedFileService.getStagingArea() + SLASH + typeVersion;
            File unpackedDir = new File(destDirname);
            if (unpackedDir.exists()) {
                try {
                    FileUtils.cleanDirectory(unpackedDir);
                    FileUtils.deleteDirectory(unpackedDir);
                } catch (IOException e) {
                    LOGGER.error("Error occured while deleting the unpacked content for type-version" + typeVersion, e);
                }
            }
        }
    }

    protected PlugExtensionAlarmLoader loadAlarms(String typeVersion) throws Exception {
        String destDirname = sharedFileService.getStagingArea() + SLASH + typeVersion;
        List<String> modulePaths = new ArrayList<>();
        Map<URL, InputStream> moduleStream = buildModuleStreamMap(destDirname, modulePaths);
        InputStream alarmConfigStream = getAlarmConfigStream(destDirname);
        PlugExtensionAlarmLoader plugExtensionAlarmLoader = new PlugExtensionAlarmLoader();
        plugExtensionAlarmLoader.setPlugId(typeVersion);
        plugExtensionAlarmLoader.setUnpackedArchivePath(destDirname);
        plugExtensionAlarmLoader.setModuleStreamMap(moduleStream);
        plugExtensionAlarmLoader.setAlarmConfigStream(alarmConfigStream);
        plugExtensionAlarmLoader.setYangModulePath(modulePaths);
        plugExtensionAlarmLoader.load();
        if (plugExtensionAlarmLoader.getInputStreamList() == null || plugExtensionAlarmLoader.getInputStreamList().isEmpty()) {
            plugExtensionAlarmLoader.addInputStreamList(moduleStream);
            plugExtensionAlarmLoader.addInputStreamList(alarmConfigStream);
        }
        setInputStreamList(plugExtensionAlarmLoader.getInputStreamList());
        return plugExtensionAlarmLoader;
    }

    protected Map<URL, InputStream> buildModuleStreamMap(String destDirname, List<String> modulePaths) throws IOException {
        String integratedYangPath = NonCodedPlugUtils.getIntegratedYangsDirectory(destDirname);
        Map<URL, InputStream> yangFilePath = new HashMap<URL, InputStream>();
        String[] extensions = new String[] {"yang"};
        Iterator<File> iterator = FileUtils.iterateFiles(new File(integratedYangPath), extensions, true);

        while (iterator.hasNext()) {
            File yangFile = iterator.next();
            URL url = yangFile.toURI().toURL();
            yangFilePath.put(url, url.openStream());
            modulePaths.add(url.getPath());
        }
        Collections.sort(modulePaths);
        return yangFilePath;
    }

    protected InputStream getAlarmConfigStream(String destDirname) throws IOException {
        String alarmXmlPath = NonCodedPlugUtils.getAlarmXmlPath(destDirname);
        File alarmXmlFile = new File(alarmXmlPath);
        URL url = alarmXmlFile.toURI().toURL();
        return url.openStream();
    }

    protected boolean isFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    protected void getAllSupportedAlarmTypes() {
        List<Pair<String, String>> managerPairList = acRestService.getAllManagerNameTypePairs();

        managerPairList.forEach(manager -> {
            String type = manager.getSecond();
            String target = manager.getFirst();

            if (type.equals(ManagerType.LOCAL.getManagerType()) || type.equals(ManagerType.NAV.getManagerType())) {
                List<AlarmTypeInfo> alarmTypes = acRestService.getAllSupportedAlarmTypesByCoreTarget(target);
                Function<AlarmTypeInfo, AlarmReporter> groupFunction =
                        type.equals(ManagerType.LOCAL.getManagerType()) ? this::determineACAlarmReport : this::determineAVAlarmReport;
                alarmTypes.stream()
                        .collect(Collectors.groupingBy(groupFunction))
                        .forEach((key, value) -> ALL_SUPPORTED_ALARMS.merge(key, value, (oldList, newList) -> {
                            oldList.addAll(newList);
                            return oldList;
                        }));
            }
        });
    }

    private AlarmReporter determineACAlarmReport(AlarmTypeInfo alarmType) {
        Set<String> reporters = alarmType.getAlarmTypeReporter();

        if (reporters == null || reporters.isEmpty()) {
            return AlarmReporter.UNKNOWN;
        }

        if (reporters.contains("local")) {
            return AlarmReporter.AC;
        }

        return AlarmReporter.UNKNOWN;
    }

    private AlarmReporter determineAVAlarmReport(AlarmTypeInfo alarmType) {
        Set<String> reporters = alarmType.getAlarmTypeReporter();

        if (reporters == null || reporters.isEmpty()) {
            return AlarmReporter.UNKNOWN;
        }

        if (reporters.contains("local")) {
            return AlarmReporter.AV;
        }

        return AlarmReporter.DEVICE;
    }

    public List<InputStream> getInputStreamList() {
        return inputStreamList;
    }

    public void setInputStreamList(List<InputStream> inputStreamList) {
        this.inputStreamList = inputStreamList;
    }
}
