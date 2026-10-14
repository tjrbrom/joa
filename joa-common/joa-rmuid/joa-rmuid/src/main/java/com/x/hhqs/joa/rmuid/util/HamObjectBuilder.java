package com.x.hhqs.joa.rmuid.util;

import org.springframework.util.StringUtils;
import com.x.hhqs.joa.rmuid.generator.RmType;
import com.x.hhqs.joa.rmuid.model.HamObject;

public class HamObjectBuilder {

    private String hamId;

    private String resourceType;

    private String deviceIp;

    private Integer onuId;

    public static HamObjectBuilder instance(String hamId) {
        HamObjectBuilder builder = new HamObjectBuilder();
        builder.hamId(hamId);
        return builder;
    }

    public static HamObjectBuilder instance(String hamId, String resourceType) {
        HamObjectBuilder builder = new HamObjectBuilder();
        builder.hamId(hamId);
        builder.resourceType(resourceType);
        return builder;
    }

    public static HamObjectBuilder instance(String hamId, RmType rmType) {
        HamObjectBuilder builder = new HamObjectBuilder();
        builder.hamId(hamId);
        builder.resourceType(rmType.getValue());
        return builder;
    }

    private HamObjectBuilder hamId(String hamId) {
        this.hamId = hamId;
        return this;
    }

    public HamObjectBuilder resourceType(String resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public HamObjectBuilder withDeviceIp(String deviceIp) {
        this.deviceIp = deviceIp;
        return this;
    }

    public HamObjectBuilder withOnuId(Integer onuId) {
        this.onuId = onuId;
        return this;
    }

    public HamObject build() {
        HamObject ham = new HamObject(hamId, resourceType);

        if (StringUtils.hasText(deviceIp)) {
            ham.setDeviceIp(deviceIp);
            String hamId = replaceHamIdField(ham.getHamId(), "device", "ip", deviceIp);
            ham.setHamId(hamId);
        }

        if (onuId != null) {
            ham.setOnuId(onuId);
            String hamId = replaceHamIdField(ham.getHamId(), "ont", "ontid", onuId.toString());
            ham.setHamId(hamId);
        }

        return ham;
    }

    public static String replaceHamIdField(String hamId, String oldName, String name, String value) {
        if (!StringUtils.hasText(hamId)) {
            return hamId;
        }

        int start = hamId.indexOf("/" + oldName);
        if (start == -1) {
            return hamId;
        }

        int end = hamId.indexOf("/", start + oldName.length());
        if (end == -1) {
            return hamId.substring(0, start) + "/" + name + "=" + value;
        }

        return hamId.substring(0, start) + "/" + name + "=" + value + hamId.substring(end);
    }

}
