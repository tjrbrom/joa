package com.x.hhqs.joa.alarm.noncodedplugs.utils;

import com.x.hhqs.joa.alarm.entity.AlarmSeverity;
import com.x.hhqs.joa.alarm.dto.AlarmXmlConfigInfo;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import noNamespace.AlarmList;
import noNamespace.AlarmType;
import noNamespace.AlarmTypeList;
import noNamespace.DomainDocument;
import noNamespace.SeverityListType;
import noNamespace.SeverityTypeWithAttribute;

public class AlarmConfigurationUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlarmConfigurationUtil.class);

    private static final String ALARM_XSD = "/xsd/alarm.xsd";

    public static final AlarmConfigurationUtil INSTANCE = new AlarmConfigurationUtil();

    private static AlarmConfigurationUtil test;

    private AlarmConfigurationUtil() {}

    public static AlarmConfigurationUtil getInstance() {
        if (test != null) {
            return test;
        }
        return INSTANCE;
    }

    public static void setInstanceForTest(AlarmConfigurationUtil instance) {
        test = instance;
    }

    public Map<String, AlarmXmlConfigInfo> cachePlugAlarmConfiguration(String alarmConfiguration) {
        Map<String, AlarmXmlConfigInfo> cachedAlarmXmlConfigInfoMap = new HashMap<>();
        try {
            DomainDocument domainDocument = DomainDocument.Factory.parse(alarmConfiguration);
            if (domainDocument.getDomain() != null) {
                DomainDocument.Domain domain = domainDocument.getDomain();
                if (domain.getAlarms() != null) {
                    AlarmList alarmList = domain.getAlarms();
                    if (alarmList != null) {
                        List<AlarmTypeList> alarmTypeLists = alarmList.getAlarmtypeList();
                        if (alarmTypeLists != null) {
                            for (AlarmTypeList alarmTypeList : alarmTypeLists) {
                                String nameSpace = alarmTypeList.getNamespace();
                                List<AlarmType> alarmTypes = alarmTypeList.getAlarmList();
                                for (AlarmType alarmType : alarmTypes) {
                                    String alarmXmlTypeId = alarmType.getAlarmTypeId();

                                    String severity = "";
                                    if (alarmType.getSeverity() != null) {
                                        severity = alarmType.getSeverity().getStringValue();
                                    } else {
                                        SeverityListType severityList = alarmType.getSeverityList();

                                        List<SeverityTypeWithAttribute> severityWithAttributeList = severityList.getSeverityList();

                                        for (SeverityTypeWithAttribute severityTypeWithAttribute : severityWithAttributeList) {
                                            if (severityTypeWithAttribute.getDefaultSeverity()) {
                                                severity = severityTypeWithAttribute.getStringValue();
                                                break;
                                            }
                                        }
                                    }

                                    String description = alarmType.getDescription();
                                    String adviceForRepair = alarmType.getAdviceForRepair();
                                    String additionalInfo = alarmType.getAdditionalInfo();

                                    String alarmTypeQualifier = String.format("(%s)%s", nameSpace, alarmXmlTypeId);

                                    cachedAlarmXmlConfigInfoMap.put(alarmTypeQualifier, new AlarmXmlConfigInfo()
                                            .namespace(nameSpace)
                                            .alarmType(alarmXmlTypeId)
                                            .alarmTypeQualifier(alarmTypeQualifier)
                                            .description(description)
                                            .adviceForRepair(adviceForRepair)
                                            .additionalInfo(additionalInfo)
                                            .severity(AlarmSeverity.forValue(severity)));
                                }
                            }
                        }
                    }
                }
            }
        }catch (XmlException e) {
            LOGGER.error("Failed to parse alarm configuration.", e);
            throw new IllegalStateException("Invalid alarm configuration", e);
        }
        return cachedAlarmXmlConfigInfoMap;
    }
}