package com.x.hhqs.joa.alarm.noncodedplugs.loader;

import com.x.hhqs.joa.alarm.entity.AlarmSeverity;
import com.x.hhqs.joa.alarm.dto.AlarmXmlConfigInfo;
import com.x.hhqs.joa.alarm.noncodedplugs.NonCodedPlugUtils;
import com.x.hhqs.joa.alarm.noncodedplugs.utils.AlarmConfigurationUtil;
import com.x.hhqs.joa.alarm.noncodedplugs.utils.AlarmUtil;
import com.x.hhqs.joa.alarm.yang.SchemaContextPool;
import com.x.hhqs.joa.alarm.yang.utils.IdentityUtil;
import com.x.hhqs.joa.alarm.yang.parser.YangParserUtil;
import com.x.hhqs.joa.common.restclient.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class PlugExtensionAlarmLoader {
    private static final String YANG_FILES = "*.yang";
    public static final String ALARM_IDENTITY = "alarm-identity";
    public static final String IETF_ALARM_IDENTITY = "alarm-type-id";

    private static final Logger LOGGER = LoggerFactory.getLogger(PlugExtensionAlarmLoader.class);

    private String plugId;

    private String unpackedArchivePath;

    private List<String> yangModulePath;

    private Map<URL, InputStream> moduleStream;

    private InputStream alarmConfigStream;

    private List<InputStream> inputStreamList;

    private Map<String, AlarmXmlConfigInfo> cachedAlarmXmlConfigInfoMap;

    private SchemaContextPool pool;

    public PlugExtensionAlarmLoader() {
        inputStreamList = new ArrayList<>();
        cachedAlarmXmlConfigInfoMap = new HashMap<>();
        pool = new SchemaContextPool();
    }

    /**
     * Build the ModuleStream Map based on plug yang files
     */
    protected Map<URL, InputStream> buildModuleStreamMap() throws IOException {
        if (moduleStream == null) {
            moduleStream = new HashMap<URL, InputStream>();
            if (unpackedArchivePath != null) {
                yangModulePath = new ArrayList<>();
                String integratedYangPath = NonCodedPlugUtils.getIntegratedYangsDirectory(unpackedArchivePath);
                String[] extensions = new String[] {"yang"};
                Iterator<File> iterator = FileUtils.iterateFiles(new File(integratedYangPath), extensions, true);

                while (iterator.hasNext()) {
                    File yangFile = iterator.next();
                    URL url = yangFile.toURI().toURL();
                    moduleStream.put(url, url.openStream());
                }
            } else {
                for (String path : yangModulePath) {
                    moduleStream.put(getClass().getResource(path), getClass().getResourceAsStream(path));
                }
            }
        }
        return moduleStream;
    }

    public void load() throws Exception {
        Map<URL, InputStream> moduleStream = null;
        InputStream alarmConfigStream = null;
        try {
            LOGGER.info("Starting load plug alarms " + getPlugId());
            moduleStream = buildModuleStreamMap();
            alarmConfigStream = getAlarmConfigStream();
            initAlarmConfig(alarmConfigStream);
            initYangModelContext(moduleStream);
            fillAdditionalAttributes();
            LOGGER.info("Plug alarms loaded successfully-{}", getPlugId());
        } catch (Exception e) {
            throw new RuntimeException("Unable to load plug alarms, plug id: " + getPlugId(), e);
        } finally {
            addInputStreamList(moduleStream);
            addInputStreamList(alarmConfigStream);
        }
    }

    private void fillAdditionalAttributes() {
        SchemaContext schemaContext = pool.getOrLoad(null);
        if (schemaContext != null) {
            IdentityUtil util = new IdentityUtil(schemaContext);
            List<Pair<String, Set<IdentitySchemaNode>>> moduleNameIdentitiesPairs = util.getModuleNameIdentitiesPairs();
            for (Pair<String, Set<IdentitySchemaNode>> moduleNameIdentitiesPair : moduleNameIdentitiesPairs) {
                String moduleName = moduleNameIdentitiesPair.getFirst();
                Set<IdentitySchemaNode> allIdentities = moduleNameIdentitiesPair.getSecond();
                for (IdentitySchemaNode identity : allIdentities) {
                    if (YangParserUtil.isBaseIdentity(ALARM_IDENTITY, identity) || YangParserUtil.isBaseIdentity(IETF_ALARM_IDENTITY, identity)) {
                        String alarmTypeQualifierWithoutRevision = AlarmUtil.getAlarmIdentityWithOutRevision(identity.getQName().toString());
                        AtomicReference<String> category = new AtomicReference<>(null);
                        QName identityQName = identity.getQName();

//                        util.findClosestAlarmTypesBase(identityQName).ifPresentOrElse(result -> {
//                            LOGGER.debug("Closest alarm types base is {}", result.getLocalName());
//                            category.set(result.getLocalName());
//                        }, () -> LOGGER.debug("No closest alarm types base found."));

                        String targetNs1 = "https://www.x.com/Fixed-Networks/BBA/yang/x-base-alarm-types";
                        util.resolveBaseInNamespace(identityQName, targetNs1).ifPresentOrElse(result -> {
                            LOGGER.debug("Found base identity in target namespace: " + result.getLocalName());
                            category.set(result.getLocalName());
                        }, () -> LOGGER.debug("No identity found in target namespace."));

                        if (category.get() == null) {
                            String targetNs2 = "https://www.x.com/Fixed-Networks/BBA/yang/base-alarm-types";
                            util.resolveBaseInNamespace(identityQName, targetNs2).ifPresentOrElse(result -> {
                                LOGGER.debug("Found base identity in target namespace: " + result.getLocalName());
                                category.set(result.getLocalName());
                            }, () -> LOGGER.debug("No identity found in target namespace."));
                        }

                        if (cachedAlarmXmlConfigInfoMap.containsKey(alarmTypeQualifierWithoutRevision)) {
                            AlarmXmlConfigInfo alarmXmlConfigInfo = cachedAlarmXmlConfigInfoMap.get(alarmTypeQualifierWithoutRevision);
                            alarmXmlConfigInfo.nsModuleName(moduleName);
                            alarmXmlConfigInfo.category(category.get());
                        } else {
                            cachedAlarmXmlConfigInfoMap.put(alarmTypeQualifierWithoutRevision, new AlarmXmlConfigInfo()
                                    .nsModuleName(moduleName)
                                    .namespace(identity.getQName().getNamespace().toString())
                                    .alarmType(identity.getQName().getLocalName())
                                    .alarmTypeQualifier(alarmTypeQualifierWithoutRevision)
                                    .severity(AlarmSeverity.UNKNOWN)
                                    .category(category.get())
                                    .description("")
                                    .adviceForRepair("")
                                    .additionalInfo(""));
                        }
                    }
                }
            }
        }
    }

    public void destroy() {
        pool.clear();
        // todo
        cachedAlarmXmlConfigInfoMap.clear();
    }

    protected void initAlarmConfig(InputStream alarmXmlModelStream) throws IOException {
        if (alarmXmlModelStream != null) {
            try {
                String alarmConfiguration = IOUtils.toString(alarmXmlModelStream);
                cachedAlarmXmlConfigInfoMap = AlarmConfigurationUtil.getInstance().cachePlugAlarmConfiguration(alarmConfiguration);
                LOGGER.debug("All alarms are cached successfully which is read from alarm.xml for plug :{}", getPlugId());
            } catch (IOException e) {
                LOGGER.error("Error while reading alarm configuration from alarm.xml file during plug deployment for plug :{}", getPlugId(), e);
                throw new RuntimeException("Error while reading alarm configuration from alarm.xml", e);
            }
        }
    }

    protected void initYangModelContext(Map<URL, InputStream> moduleStream) throws IOException {
        LOGGER.info("Starting load plug yang model context {}", getPlugId());
        List<YangTextSchemaSource> sources = loadYangFiles(moduleStream);
        pool.getOrLoad(sources);
        LOGGER.info("Plug yang model context loaded successfully-{}", getPlugId());
    }

    public boolean existAlarmXmlConfigType(String alarmTypeQualifier) {
        return cachedAlarmXmlConfigInfoMap.containsKey(alarmTypeQualifier);
    }

    public AlarmXmlConfigInfo getAlarmXmlConfigType(String alarmTypeQualifier) {
        return cachedAlarmXmlConfigInfoMap.get(alarmTypeQualifier);
    }

    public List<YangTextSchemaSource> loadYangFiles(Map<URL, InputStream> moduleStream) throws IOException {
        List<YangTextSchemaSource> byteSources = new ArrayList<>();
        for (Map.Entry<URL, InputStream> entry : moduleStream.entrySet()) {
            URL url = entry.getKey();
            InputStream is = entry.getValue();
            byteSources.add(YangParserUtil.getYangSource(url, is));
        }
        return byteSources;
    }

    protected InputStream getAlarmConfigStream() throws IOException {
        if (this.alarmConfigStream == null) {
            String alarmXmlPath = NonCodedPlugUtils.getAlarmXmlPath(unpackedArchivePath);
            File alarmXmlFile = new File(alarmXmlPath);
            URL url = alarmXmlFile.toURI().toURL();
            return url.openStream();
        }
        return this.alarmConfigStream;
    }

    public void addInputStreamList(InputStream inputStream) {
        if (inputStream != null) {
            inputStreamList.add(inputStream);
        }
    }

    public <T> void addInputStreamList(Map<T, InputStream> map) {
        if (map != null) {
            map.forEach((k, v) -> inputStreamList.add(v));
        }
    }

    public List<InputStream> getInputStreamList() {
        return inputStreamList;
    }
    
    public void setYangModulePath(List<String> yangModulePath) {
        this.yangModulePath = yangModulePath;
    }

    public void setModuleStreamMap(Map<URL, InputStream> yangModuleFilePath) {
        this.moduleStream = yangModuleFilePath;
    }

    public void setUnpackedArchivePath(String unpackedArchivePath) {
        this.unpackedArchivePath = unpackedArchivePath;
    }

    public String getUnpackedArchivePath() {
        return unpackedArchivePath;
    }

    public String getPlugId() {
        return plugId;
    }

    public void setPlugId(String plugId) {
        this.plugId = plugId;
    }

    public void setAlarmConfigStream(InputStream alarmConfigStream) {
        this.alarmConfigStream = alarmConfigStream;
    }

    protected Map<String, AlarmXmlConfigInfo> getCachedAlarmXmlConfigInfoMapForTest() {
        return cachedAlarmXmlConfigInfoMap;
    }
}