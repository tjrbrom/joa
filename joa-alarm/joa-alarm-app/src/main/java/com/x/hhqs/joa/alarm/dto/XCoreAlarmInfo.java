package com.x.hhqs.joa.alarm.dto;

public class XCoreAlarmInfo {
    private Long alarmSeq;
    private String alarmTitle;
    private AlarmStatus alarmStatus;
    private AlarmType alarmType;
    private AlarmSeverity origSeverity;
    private String eventTime;
    private String alarmId;
    private String specificProblemID;
    private String specificProblem;
    private String neUID;
    private String neName;
    private String neType;
    private String objectUID;
    private String objectName;
    private String objectType;
    private String locationInfo;
    private String holderType;
    private String alarmCheck;
    private Integer layer;
    private String addInfo;
    private RelateType relateType;
    private String correlatedSourceAlarmSN;
    private String maintenanceStatus;
    private String location;

    protected JoaAlarmInfo toJoaAlarm() {
        JoaAlarmInfo alarmInfo = new JoaAlarmInfo();
        alarmInfo.setAlarmTitle(this.getAlarmTitle());
        alarmInfo.setAlarmStatus(this.getAlarmStatusCode());
        alarmInfo.setAlarmType(this.getAlarmTypeName());
        alarmInfo.setOrigSeverity(this.getOrigSeverityCode());
        alarmInfo.setEventTime(this.getEventTime());
        alarmInfo.setAlarmId(this.getAlarmId());
        alarmInfo.setSpecificProblemID(this.getSpecificProblemID());
        alarmInfo.setSpecificProblem(this.getSpecificProblem());
        alarmInfo.setNeUID(this.getNeUID());
        alarmInfo.setNeName(this.getNeName());
        alarmInfo.setNeType(this.getNeType());
        alarmInfo.setObjectUID(this.getObjectUID());
        alarmInfo.setObjectName(this.getObjectName());
        alarmInfo.setObjectType(this.getObjectType());
        alarmInfo.setLocationInfo(this.getLocationInfo());
        alarmInfo.setHolderType(this.getHolderType());
        alarmInfo.setAlarmCheck(this.getAlarmCheck());
        alarmInfo.setLayer(this.getLayer());
        alarmInfo.setAddInfo(this.getAddInfo());
        alarmInfo.setRelateType(this.getRelateTypeCode());
        alarmInfo.setCorrelatedSourceAlarmSN(this.getCorrelatedSourceAlarmSN());
        alarmInfo.setMaintenanceStatus(this.getMaintenanceStatus());
        alarmInfo.setLocation(this.getLocation());
        return alarmInfo;
    }

    public Long getAlarmSeq() {
        return alarmSeq;
    }

    public void setAlarmSeq(Long alarmSeq) {
        this.alarmSeq = alarmSeq;
    }

    public String getAlarmTitle() {
        return alarmTitle;
    }

    public void setAlarmTitle(String alarmTitle) {
        this.alarmTitle = alarmTitle;
    }

    public AlarmStatus getAlarmStatus() {
        return alarmStatus;
    }

    public void setAlarmStatus(AlarmStatus alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

    public Integer getAlarmStatusCode() {
        return this.alarmStatus != null ? this.alarmStatus.getCode() : null;
    }

    public void setAlarmStatusWithName(String name) {
        this.alarmStatus = AlarmStatus.fromValue(name);
    }

    public AlarmType getAlarmType() {
        return alarmType;
    }

    public String getAlarmTypeName() {
        return this.alarmType != null ? this.alarmType.name() : null;
    }

    public void setAlarmType(AlarmType alarmType) {
        this.alarmType = alarmType;
    }

    public void setAlarmTypeWithName(String name) {
        this.alarmType = AlarmType.fromValue(name);
    }

    public AlarmSeverity getOrigSeverity() {
        return origSeverity;
    }

    public void setOrigSeverity(AlarmSeverity origSeverity) {
        this.origSeverity = origSeverity;
    }

    public Integer getOrigSeverityCode() {
        return this.origSeverity != null ? this.origSeverity.getCode() : null;
    }

    public void setOrigSeverityWithName(String name) {
        this.origSeverity = AlarmSeverity.fromValue(name);
    }

    public String getEventTime() {
        return eventTime;
    }

    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }

    public String getSpecificProblemID() {
        return specificProblemID;
    }

    public void setSpecificProblemID(String specificProblemID) {
        this.specificProblemID = specificProblemID;
    }

    public String getSpecificProblem() {
        return specificProblem;
    }

    public void setSpecificProblem(String specificProblem) {
        this.specificProblem = specificProblem;
    }

    public String getNeUID() {
        return neUID;
    }

    public void setNeUID(String neUID) {
        this.neUID = neUID;
    }

    public String getNeName() {
        return neName;
    }

    public void setNeName(String neName) {
        this.neName = neName;
    }

    public String getNeType() {
        return neType;
    }

    public void setNeType(String neType) {
        this.neType = neType;
    }

    public String getObjectUID() {
        return objectUID;
    }

    public void setObjectUID(String objectUID) {
        this.objectUID = objectUID;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getLocationInfo() {
        return locationInfo;
    }

    public void setLocationInfo(String locationInfo) {
        this.locationInfo = locationInfo;
    }

    public String getHolderType() {
        return holderType;
    }

    public void setHolderType(String holderType) {
        this.holderType = holderType;
    }

    public String getAlarmCheck() {
        return alarmCheck;
    }

    public void setAlarmCheck(String alarmCheck) {
        this.alarmCheck = alarmCheck;
    }

    public Integer getLayer() {
        return layer;
    }

    public void setLayer(Integer layer) {
        this.layer = layer;
    }

    public String getAddInfo() {
        return addInfo;
    }

    public void setAddInfo(String addInfo) {
        this.addInfo = addInfo;
    }

    public RelateType getRelateType() {
        return relateType;
    }

    public void setRelateType(RelateType relateType) {
        this.relateType = relateType;
    }

    public Integer getRelateTypeCode() {
        return this.relateType != null ? this.relateType.getCode() : null;
    }

    public void setRelateTypeWithName(String name) {
        this.relateType = RelateType.fromValue(name);
    }

    public String getCorrelatedSourceAlarmSN() {
        return correlatedSourceAlarmSN;
    }

    public void setCorrelatedSourceAlarmSN(String correlatedSourceAlarmSN) {
        this.correlatedSourceAlarmSN = correlatedSourceAlarmSN;
    }

    public String getMaintenanceStatus() {
        return maintenanceStatus;
    }

    public void setMaintenanceStatus(String maintenanceStatus) {
        this.maintenanceStatus = maintenanceStatus;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
