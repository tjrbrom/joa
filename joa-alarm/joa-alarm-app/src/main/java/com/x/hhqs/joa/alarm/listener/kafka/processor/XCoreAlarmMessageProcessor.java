package com.x.hhqs.joa.alarm.listener.kafka.processor;

import com.x.hhqs.joa.alarm.config.XAlarmProperties;
import com.x.hhqs.joa.alarm.contextfield.AlarmFields;
import com.x.hhqs.joa.alarm.contextfield.XAlarmFieldHandlersManager;
import com.x.hhqs.joa.alarm.contextfield.handlers.AttributeHandler;
import com.x.hhqs.joa.alarm.dto.AlarmSeverity;
import com.x.hhqs.joa.alarm.dto.AlarmStatus;
import com.x.hhqs.joa.alarm.dto.JoaAlarmInfo;
import com.x.hhqs.joa.alarm.dto.XAlarmContext;
import com.x.hhqs.joa.alarm.dto.XAlarmContextFields;
import com.x.hhqs.joa.alarm.dto.XAlarmVO;
import com.x.hhqs.joa.alarm.entity.AlarmReporter;
import com.x.hhqs.joa.alarm.entity.JoaAlarm;
import com.x.hhqs.joa.alarm.exception.UnsupportedAlarmException;
import com.x.hhqs.joa.alarm.service.ACRestService;
import com.x.hhqs.joa.alarm.service.AlarmDefinitionService;
import com.x.hhqs.joa.alarm.service.impl.XCoreIsQueryService;
import com.x.hhqs.joa.alarm.utils.HamUtils;
import com.x.hhqs.joa.alarm.utils.NotificationParser;
import com.x.hhqs.joa.alarm.vo.AlarmDefinitionInfoVO;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

@Component("xCoreAlarmMessageProcessor")
public class XCoreAlarmMessageProcessor implements AlarmMessageProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(XCoreAlarmMessageProcessor.class);

    private final XAlarmFieldHandlersManager xAlarmFieldHandlersManager;
    private final AlarmDefinitionService alarmDefinitionService;
    private final ACRestService acRestService;
    private final XCoreIsQueryService xCoreIsQueryService;
    private final XAlarmProperties xAlarmProperties;

    public XCoreAlarmMessageProcessor(XAlarmFieldHandlersManager xAlarmFieldHandlersManager,
                                               AlarmDefinitionService alarmDefinitionService,
                                               XCoreIsQueryService xCoreIsQueryService, ACRestService acRestService,
                                               XAlarmProperties xAlarmProperties) {
        this.xAlarmFieldHandlersManager = xAlarmFieldHandlersManager;
        this.alarmDefinitionService = alarmDefinitionService;
        this.xCoreIsQueryService = xCoreIsQueryService;
        this.acRestService = acRestService;
        this.xAlarmProperties = xAlarmProperties;
    }

    @Override
    public List<JoaAlarm> process(String notification) {
        XAlarmVO xAlarmVO = NotificationParser.getInstance().parseDeviceAlarm(notification);
        if (xAlarmVO != null &&
                xAlarmVO.getAlarmNotificationOrigin() != null &&
                xAlarmProperties.getAllowedOrigins().contains(xAlarmVO.getAlarmNotificationOrigin())) {
            String alarmTypeId = xAlarmVO.getAlarmTypeId();
            AlarmDefinitionInfoVO alarmDefinition = alarmDefinitionService.getAlarmDefinitionByAlarmTypeId(alarmTypeId);
            long totalStart = System.currentTimeMillis();
            long start = System.currentTimeMillis();
            String alarmId = xAlarmVO.getAlarmId();
            XAlarmContext xAlarmContext = extractXAlarmContext(alarmId, xAlarmVO, alarmDefinition);
            LOGGER.debug("extractXAlarmContext took " + (System.currentTimeMillis() - start));
            start = System.currentTimeMillis();
            JoaAlarm joaAlarm = constructJoaAlarm(xAlarmContext);
            LOGGER.debug("constructJoaAlarm took " + (System.currentTimeMillis() - start));
            LOGGER.debug("total took " + (System.currentTimeMillis() - totalStart));
            return List.of(joaAlarm);
        }
        return List.of();
    }

    protected XAlarmContext extractXAlarmContext(String alarmId, XAlarmVO xAlarmVO, AlarmDefinitionInfoVO alarmDefinition) {
        long start = System.currentTimeMillis();
        Optional<JsonObject> optionalAlarmJsonInfo = Optional.empty();
        int maxRetries = 3;
        int retryIntervalMs = 1000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            optionalAlarmJsonInfo = xCoreIsQueryService.retrieveXJsonAlarmInfo(alarmId);
            if (optionalAlarmJsonInfo.isPresent()) {
                break;
            }

            if (attempt < maxRetries) {
                try {
                    Thread.sleep(retryIntervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        XAlarmContext xAlarmContext = optionalAlarmJsonInfo
                .map(jsonObject -> XAlarmContext.of(xAlarmVO, alarmDefinition, jsonObject))
                .orElseThrow(() -> new UnsupportedAlarmException("No alarm json info with alarm id [" + alarmId + "]"));
        extractXAlarmContext(xAlarmContext, alarmDefinition, alarmId);
        LOGGER.debug("retrieveXJsonAlarmInfo took " + (System.currentTimeMillis() - start));
        return xAlarmContext;
    }

    public void extractXAlarmContext(XAlarmContext xAlarmContext, AlarmDefinitionInfoVO alarmDefinition, String alarmId) {

        if (alarmDefinition != null) {
            AlarmReporter alarmReporter = alarmDefinition.getAlarmReporter();
            xAlarmContext.setDeviceAlarm(AlarmReporter.DEVICE == alarmReporter);
        }
        long start = System.currentTimeMillis();
        if (xAlarmContext.isDeviceAlarm()) {
            if (xAlarmContext.getAlarmJsonInfo().containsKey(XAlarmContextFields.MAIN_DEVICE_FIELD) &&
                    xAlarmContext.getAlarmJsonInfo().containsKey(XAlarmContextFields.DEVICE_REF_ID)) {
                JsonObject alarmJsonInfo = xAlarmContext.getAlarmJsonInfo();

                if (!alarmJsonInfo.containsKey(XAlarmContextFields.HAM_OBJECT_ID)) {
                    throw new UnsupportedAlarmException("No ham object id with alarm id [" + alarmId + "]");
                }

                JsonObject finalAlarmJsonInfo = alarmJsonInfo;
                if (!alarmJsonInfo.containsKey(XAlarmContextFields.HAM_OBJECT_FRIENDLY_NAME)) {
                    String hamObjectId = alarmJsonInfo.getString(XAlarmContextFields.HAM_OBJECT_ID);
                    start = System.currentTimeMillis();
                    // retrieve hamObjectFriendlyName with hamObjectId
                    Optional<JsonObject> optionalHamInfo = xCoreIsQueryService.retrieveHamInfo(hamObjectId);
                    LOGGER.debug("retrieveHamInfo took " + (System.currentTimeMillis() - start));
                    alarmJsonInfo = optionalHamInfo.map(jsonObject -> {
                        if (jsonObject.containsKey(XAlarmContextFields.HAM_OBJECT_FRIENDLY_NAME_KEY)) {
                            JsonObjectBuilder builder = Json.createObjectBuilder();
                            finalAlarmJsonInfo.forEach(builder::add);
                            builder.add(XAlarmContextFields.HAM_OBJECT_FRIENDLY_NAME, jsonObject.get(XAlarmContextFields.HAM_OBJECT_FRIENDLY_NAME_KEY));
                            return builder.build();
                        } else {
                            throw new UnsupportedAlarmException("No ham object friendly name with alarm id [" + alarmId + "]");
                        }
                    }).orElseThrow(() -> new UnsupportedAlarmException("No ham object id with alarm id [" + alarmId + "]"));
                    xAlarmContext.setAlarmJsonInfo(alarmJsonInfo);
                }

                String hamObjectFriendlyName = alarmJsonInfo.getString(XAlarmContextFields.HAM_OBJECT_FRIENDLY_NAME);
                String rmuidType = HamUtils.getInstance().hamType2RmuidType(hamObjectFriendlyName);
                xAlarmContext.addAttr(XAlarmContextFields.RMUID_TYPE, rmuidType);
                String mainDeviceRefId = xAlarmContext.getAlarmJsonInfo().getString(XAlarmContextFields.MAIN_DEVICE_FIELD);

                start = System.currentTimeMillis();
                // retrieve neIp neName  yang leaf from intent
                Optional<JsonObject> optionalJsonObject = xCoreIsQueryService.queryDeviceInfo(mainDeviceRefId);
                LOGGER.debug("queryDeviceInfo took " + (System.currentTimeMillis() - start));
                optionalJsonObject.map(obj -> obj.getJsonObject("configuration")).ifPresent(confNode -> {
                    addFirstStringIfExists(confNode, XAlarmContextFields.NE_IP, XAlarmContextFields.NE_IP, xAlarmContext);
                    addFirstStringIfExists(confNode, XAlarmContextFields.NE_NAME, XAlarmContextFields.NE_NAME, xAlarmContext);
                    addFirstStringIfExists(confNode, XAlarmContextFields.NE_LOCATION, XAlarmContextFields.NE_LOCATION, xAlarmContext);
                    addStringListIfExists(confNode, XAlarmContextFields.PLANNED_TYPE, XAlarmContextFields.PLANNED_TYPE, xAlarmContext);
                });

                String deviceRefId = alarmJsonInfo.getString(XAlarmContextFields.DEVICE_REF_ID);
                Optional<JsonObject> optionalSlotPlannedType = Optional.empty();
                if (deviceRefId.equals(mainDeviceRefId) || deviceRefId.equals(mainDeviceRefId.concat(".IHUB"))) {
                    optionalSlotPlannedType = xCoreIsQueryService.querySlotPlannedType(mainDeviceRefId, List.of("NTA", "NTB"));
                    LOGGER.debug("querySlotPlannedType took " + (System.currentTimeMillis() - start));
                } else  {
                    int lastDot = deviceRefId.lastIndexOf(".");
                    if (lastDot > 0 && lastDot < deviceRefId.length() - 1) {
                        String part1 = deviceRefId.substring(0, lastDot);
                        String part2 = deviceRefId.substring(lastDot + 1);
                        start = System.currentTimeMillis();
                        optionalSlotPlannedType = xCoreIsQueryService.querySlotPlannedType(part1, List.of(part2));
                        LOGGER.debug("querySlotPlannedType took " + (System.currentTimeMillis() - start));
                    }
                }
                optionalSlotPlannedType.ifPresent(jsonObject -> {
                    addFirstStringIfExists(jsonObject, XAlarmContextFields.PLANNED_TYPE, XAlarmContextFields.SLOT_PLANNED_TYPE, xAlarmContext);
                });
            } else {
                throw new UnsupportedAlarmException("No device id in alarm json info with alarm id [" + alarmId + "]");
            }
        } else {
            start = System.currentTimeMillis();
            String siteName = acRestService.retrieveCoreSiteName();
            xAlarmContext.addAttr(XAlarmContextFields.SITE_NAME, siteName);
            LOGGER.debug("retrieveCoreSiteName took " + (System.currentTimeMillis() - start));
        }
    }

    private void addFirstStringIfExists(JsonObject node, String jsonField, String field,XAlarmContext ctx) {
        if (node.containsKey(jsonField)) {
            JsonArray arr = node.getJsonArray(jsonField);
            if (!arr.isEmpty() && arr.get(0).getValueType() == JsonValue.ValueType.STRING) {
                ctx.addAttr(field, ((JsonString) arr.get(0)).getString());
            }
        }
    }

    private void addStringListIfExists(JsonObject node, String jsonField, String field, XAlarmContext ctx) {
        if (node.containsKey(jsonField)) {
            JsonArray arr = node.getJsonArray(jsonField);
            if (!arr.isEmpty()) {
                List<String> list = arr.stream()
                        .filter(v -> v.getValueType() == JsonValue.ValueType.STRING)
                        .map(v -> ((JsonString) v).getString())
                        .toList();
                ctx.addAttr(field, list);
            }
        }
    }

    public JoaAlarm constructJoaAlarm(XAlarmContext xAlarmContext) {
        JoaAlarmInfo info = new JoaAlarmInfo();
        // alarmTitle
        String alarmTitle = getValue(AlarmFields.ALARM_TITLE, xAlarmContext);
        info.setAlarmTitle(alarmTitle);
        // alarmStatus
        int alarmStatus = getIntValue(AlarmFields.ALARM_STATUS, xAlarmContext, AlarmStatus.Active.getCode());
        info.setAlarmStatus(alarmStatus);
        // alarmType
        String alarmType = getValue(AlarmFields.ALARM_TYPE, xAlarmContext);
        info.setAlarmType(alarmType);
        // origSeverity
        int origSeverity = getIntValue(AlarmFields.ORIG_SEVERITY, xAlarmContext, AlarmSeverity.WARNING.getCode());
        info.setOrigSeverity(origSeverity);
        // eventTime
        String eventTime = getValue(AlarmFields.EVENT_TIME, xAlarmContext);
        info.setEventTime(eventTime);
        // alarmId
        String alarmId = getValue(AlarmFields.ALARM_ID, xAlarmContext);
        info.setAlarmId(alarmId);
        // specificProblemID
        String specificProblemID = getValue(AlarmFields.SPECIFIC_PROBLEM_ID, xAlarmContext);
        info.setSpecificProblemID(specificProblemID);
        // specificProblem
        String specificProblem = getValue(AlarmFields.SPECIFIC_PROBLEM, xAlarmContext);
        info.setSpecificProblem(specificProblem);

        // neUID option3
//                String neUID = genRmuidWithAlarmReporter(alarmReporter);
        String neUID = getValue(AlarmFields.NE_UID, xAlarmContext);
        info.setNeUID(neUID);
        // neName or target
        String neName = getValue(AlarmFields.NE_NAME, xAlarmContext);
        info.setNeName(neName);
        // neType need es rmuid
        String neType = getValue(AlarmFields.NE_TYPE, xAlarmContext);
        info.setNeType(neType);
        // objectUID need es rmuid
        String objectUID = getValue(AlarmFields.OBJECT_UID, xAlarmContext);
        info.setObjectUID(objectUID);
        // objectName
        String objectName = getValue(AlarmFields.OBJECT_NAME, xAlarmContext);
        info.setObjectName(objectName);
        // objectType
        String objectType = getValue(AlarmFields.OBJECT_TYPE, xAlarmContext);
        info.setObjectType(objectType);

        // locationInfo
        String locationInfo = getValue(AlarmFields.LOCATION_INFO, xAlarmContext);
        info.setLocationInfo(locationInfo);
        // addInfo
        String addInfo = getValue(AlarmFields.ADDINFO, xAlarmContext);
        info.setAddInfo(addInfo);
        // holderType
        String holderType = getValue(AlarmFields.HOLDER_TYPE, xAlarmContext);
        info.setHolderType(holderType);
        // alarmCheck
        String alarmCheck = getValue(AlarmFields.ALARM_CHECK, xAlarmContext);
        info.setAlarmCheck(alarmCheck);
        // layer
        int layer = getIntValue(AlarmFields.LAYER, xAlarmContext);
        info.setLayer(layer);
        // relateType
        int relateType = getIntValue(AlarmFields.RELATE_TYPE, xAlarmContext);
        info.setRelateType(relateType);
        // correlatedSourceAlarmSN
        String correlatedSourceAlarmSN = getValue(AlarmFields.CORRELATED_SOURCE_ALARM_SN, xAlarmContext);
        info.setCorrelatedSourceAlarmSN(correlatedSourceAlarmSN);
        // maintenanceStatus
        String maintenanceStatus = getValue(AlarmFields.MAINTENANCE_STATUS, xAlarmContext);
        info.setMaintenanceStatus(maintenanceStatus);
        // location
        String location = getValue(AlarmFields.LOCATION, xAlarmContext);
        info.setLocation(location);

        int channelIndex = getIntValue(AlarmFields.CHANNEL_INDEX, xAlarmContext);
        JoaAlarm joaAlarm = new JoaAlarm()
                .alarmId(alarmId)
                .channelIndex(channelIndex)
                .createTime(Timestamp.valueOf(eventTime))
                .neName(neName)
                .jsonValue(info);
        LOGGER.debug("Parsed alarm: alarmId={}, neName={}, channelIndex={}",
                alarmId, neName, channelIndex);
        return joaAlarm;
    }

    protected int getIntValue(String field, XAlarmContext xAlarmContext) {
        return getIntValue(field, xAlarmContext, 0);
    }

    protected int getIntValue(String field, XAlarmContext xAlarmContext, int def) {
        String value = getValue(field, xAlarmContext);

        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        return def;
    }

    protected String getValue(String field, XAlarmContext xAlarmContext) {
        AttributeHandler attributeHandler = xAlarmFieldHandlersManager.getAttributeHandler(field);

        if (attributeHandler == null) {
            LOGGER.error("Cannot find attributeHandler: " + field);
            return "";
        }

        return attributeHandler.getValue(xAlarmContext);
    }
}
