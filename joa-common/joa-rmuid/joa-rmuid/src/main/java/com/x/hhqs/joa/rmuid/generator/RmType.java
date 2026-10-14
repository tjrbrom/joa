package com.x.hhqs.joa.rmuid.generator;

public enum RmType {
    OMC("OMC"),
    OLT("OLT"),
    ONU("ONU"),
    POS("POS"),
    Holder("EQH"),
    Board("CRD"),
    Port("PRT"),
    Topology("TPL"),
    MediaGateway("MGW"),
    ProtectGroup("PRG"),
    PtpToVLAN("PTV"),
    PonVLAN("VLN"),
    LAG("LAG"),
    SFR("SFR"),
    LLDP("LDP"),
    unKnown("unKnown");

    private String value = null;

    private RmType(String value) {
        this.value = value;
    }

    public static RmType getRmType(String value) {
        for (RmType cientType : RmType.values()) {
            if (cientType.value.equalsIgnoreCase(value)) {
                return cientType;
            }
        }
        return unKnown;
    }

    public String getValue() {
        return value;
    }
}
