package com.x.hhqs.joa.common.rt1fwk.impl;

import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.restclient.RestClient;
import com.x.hhqs.joa.common.rt1fwk.api.DeviceQueryService;
import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.rt1fwk.pojo.DeviceInfo;
import com.x.hhqs.joa.common.util.CommonConstants;

import freemarker.template.Configuration;
import freemarker.template.Template;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DeviceQueryServiceImpl implements DeviceQueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceQueryServiceImpl.class);

    @Autowired
    private RestClient restClient;

    @Autowired
    private Configuration freemarkerConfig;

    @Autowired
    private JoaMessage i18n;

    /**
     * @param oltId:oltIp or oltName, oltId MUST NOT BE null
     * @return DeviceInfo
     * @throws OssCommandException
     */
    @Override
    public DeviceInfo queryOlt(String oltId) throws OssCommandException {

        String rpcTemplate = "common/GetDevice.ftl";
        Map<String, Object> params = new HashMap<>();
        if (oltId != null && !isIpAddress(oltId)) {
            params.put("oltIdType", "NAME");
        } else {
            params.put("oltIdType", "IP");
        }
        params.put("oltId", oltId);

        String rpc = this.buildRpcRequest(rpcTemplate, params);
        JSONObject jsonObject = restClient.processNetconfRequest(false, method ->
                ((HttpPost) method).setEntity(new StringEntity(rpc, ContentType.APPLICATION_JSON)));
        if (jsonObject.getBoolean(CommonConstants.ERROR)) {
            LOGGER.error("Error when queryDeviceNameByIpAddress: \n{}", jsonObject);
            throw new OssCommandException(CommonConstants.SEOF, jsonObject.getString(CommonConstants.ERROR_MESSAGE));
        } else {
            String response = jsonObject.getString(CommonConstants.RESPONSE);
            LOGGER.info(response);
            if (getValueByRegx(response, "<ibn:target>(.+?)</ibn:target>") == null) {
                throw new OssCommandException(CommonConstants.IRNE, i18n.getMessage(CommonConstants.EADD_PREFIX +
                        "olt.notFound", new Object[]{oltId}));
            }
            DeviceInfo devInfo = new DeviceInfo();
            devInfo.setOltIP(getValueByRegx(response, "<dmf:ip-address>(.+?)</dmf:ip-address>"));
            devInfo.setOltName(getValueByRegx(response, "<dmf:ne-name>(.+?)</dmf:ne-name>"));
            devInfo.setOltTargetName(getValueByRegx(response, "<ibn:target>(.+?)</ibn:target>"));
            devInfo.setOltType(getValueByRegx(response, "<ibn:intent-type>(.+?)</ibn:intent-type>"));
            devInfo.setOltVersion(getValueByRegx(response, "<dmf:device-version>(.+?)</dmf:device-version>"));
            devInfo.setPap(getValueByRegx(response, "<dmf:partition-access-profile>(.+?)</dmf:partition-access-profile>"));
            return devInfo;
        }
    }

    @Override
    public DeviceInfo queryPon(String oltId, String ponId) throws OssCommandException {
        DeviceInfo devInfo = this.queryOlt(oltId);
        String rpcTemplate = "common/GetFiber.ftl";
        Map<String, Object> params = new HashMap<>();
        params.put("deviceName", devInfo.getOltTargetName());
        params.put("ponId", this.getPonId(ponId));

        String rpc = this.buildRpcRequest(rpcTemplate, params);

        JSONObject jsonObject = restClient.processNetconfRequest(false, method ->
                ((HttpPost) method).setEntity(new StringEntity(rpc, ContentType.APPLICATION_JSON)));
        if (jsonObject.getBoolean(CommonConstants.ERROR)) {
            LOGGER.error("Error when queryFiberNameByDeviceNameAndPonId: \n{}", jsonObject);
            throw new OssCommandException(CommonConstants.SEOF, jsonObject.getString(CommonConstants.ERROR_MESSAGE));
        } else {
            String response = jsonObject.getString(CommonConstants.RESPONSE);
            LOGGER.info(response);
            if (this.getValueByRegx(response, "<ibn:target>(.+?)</ibn:target>") == null) {
                throw new OssCommandException(CommonConstants.IRNE, i18n.getMessage(CommonConstants.EADD_PREFIX +
                        "pon.notFound", new Object[]{oltId, ponId}));
            }
            devInfo.setPonTargetName(this.getValueByRegx(response, "<ibn:target>(.+?)</ibn:target>"));
            devInfo.setPonType(this.getValueByRegx(response, "<fiber:xpon-type>(.+?)</fiber:xpon-type>"));
            devInfo.setPonId(String.valueOf(params.get("ponId")));

            return devInfo;
        }
    }


    @Override
    public DeviceInfo queryOnu(String oltId, String ponId, String onuIdType, String onuId) throws OssCommandException {
        DeviceInfo devInfo = this.queryPon(oltId, ponId);
        String rpcTemplate = "common/GetOntByAuth.ftl";
        Map<String, Object> params = new HashMap<>();
        params.put("fiberName", devInfo.getPonTargetName());
        params.put("onuIdType", onuIdType);
        params.put("onuId", onuId);
        String rpc = this.buildRpcRequest(rpcTemplate, params);
        LOGGER.info("onu rpc request:" + rpc);
        JSONObject jsonObject = restClient.processNetconfRequest(false, method ->
                ((HttpPost) method).setEntity(new StringEntity(rpc, ContentType.APPLICATION_JSON)));
        if (jsonObject.getBoolean(CommonConstants.ERROR)) {
            LOGGER.error("Error when queryOntByFiberNameAndOnuId: \n{}", jsonObject);
            throw new OssCommandException(CommonConstants.SEOF, jsonObject.getString(CommonConstants.ERROR_MESSAGE));
        } else {
            String response = jsonObject.getString(CommonConstants.RESPONSE);
            LOGGER.info(response);
            if (this.getValueByRegx(response, "<ibn:target>(.+?)</ibn:target>") == null) {
                throw new OssCommandException(CommonConstants.IRNE, i18n.getMessage(CommonConstants.EADD_PREFIX +
                        "onu.notFound", new Object[]{oltId, ponId, onuId}));
            }
            devInfo.setOnuTargetName(this.getValueByRegx(response, "<ibn:target>(.+?)</ibn:target>"));
            devInfo.setOnuAid(this.getValueByRegx(response, "<ont:onu-id>(.+?)</ont:onu-id>"));
            devInfo.setOnuType(this.getValueByRegx(response, "<ont:ont-type>(.+?)</ont:ont-type>"));
            devInfo.setOnuName(this.getValueByRegx(response, "<ont:ont-name>(.+?)</ont:ont-name>"));
            devInfo.setOnuIP(this.getValueByRegx(response, "<ont:ont-ip>(.+?)</ont:ont-ip>"));
            devInfo.setOnuLoid(this.getValueByRegx(response, "<ont:expected-loid>(.+?)</ont:expected-loid>"));
            devInfo.setOnuSlid(this.getValueByRegx(response, "<ont:expected-registration-id?(.+?)</ont:expected-registration-id>"));
            devInfo.setOnuSerNum(this.getValueByRegx(response, "<ont:expected-serial-number>(.+?)</ont:expected-serial-number>"));

            return devInfo;
        }
    }

    @Override
    public DeviceInfo queryOnu(String onuId) throws OssCommandException {
        DeviceInfo devInfo = new DeviceInfo();

        String rpcTemplate = "common/QueryOnt.ftl";
        Map<String, Object> params = new HashMap<>();
        if (onuId != null && !isIpAddress(onuId)) {
            params.put("onuIdType", "NAME");
        } else {
            params.put("onuIdType", "IP");
        }
        params.put("onuId", onuId);
        String rpc = this.buildRpcRequest(rpcTemplate, params);
        LOGGER.info("onu rpc request:" + rpc);
        JSONObject jsonObject = restClient.processNetconfRequest(false, method ->
                ((HttpPost) method).setEntity(new StringEntity(rpc, ContentType.APPLICATION_JSON)));
        if (jsonObject.getBoolean(CommonConstants.ERROR)) {
            LOGGER.error("Error when queryOnt: \n{}", jsonObject);
            throw new OssCommandException(CommonConstants.SEOF, jsonObject.getString(CommonConstants.ERROR_MESSAGE));
        } else {
            String response = jsonObject.getString(CommonConstants.RESPONSE);
            LOGGER.info(response);
            if (this.getValueByRegx(response, "<ibn:target>(.+?)</ibn:target>") == null) {
                throw new OssCommandException(CommonConstants.IRNE, i18n.getMessage(CommonConstants.EADD_PREFIX +
                        "onu.notFoundByIp", new Object[]{onuId}));
            }
            devInfo.setOnuTargetName(this.getValueByRegx(response, "<ibn:target>(.+?)</ibn:target>"));
            devInfo.setOnuAid(this.getValueByRegx(response, "<ont:onu-id>(.+?)</ont:onu-id>"));
            devInfo.setOnuType(this.getValueByRegx(response, "<ont:ont-type>(.+?)</ont:ont-type>"));
            devInfo.setPonTargetName(this.getValueByRegx(response, "<ont:fiber-name>(.+?)</ont:fiber-name>"));
            devInfo.setOnuIP(this.getValueByRegx(response, "<ont:ont-ip>(.+?)</ont:ont-ip>"));
            devInfo.setOnuName(this.getValueByRegx(response, "<ont:ont-name>(.+?)</ont:ont-name>"));
            devInfo.setOnuLoid(this.getValueByRegx(response, "<ont:expected-loid>(.+?)</ont:expected-loid>"));
            devInfo.setOnuSlid(this.getValueByRegx(response, "<ont:expected-registration-id>(.+?)</ont:expected-registration-id>"));
            devInfo.setOnuSerNum(this.getValueByRegx(response, "<ont:expected-serial-number>(.+?)</ont:expected-serial-number>"));
        }

        //query fiber info from fiberName
        rpcTemplate = "common/SearchIntent.ftl";
        params.clear();
        params.put("intentType", "fiber");
        params.put("targetName", devInfo.getPonTargetName());
        String finalRpc = this.buildRpcRequest(rpcTemplate, params);
        LOGGER.info("search intent rpc request:" + finalRpc);
        jsonObject = restClient.processNetconfRequest(false, method ->
                ((HttpPost) method).setEntity(new StringEntity(finalRpc, ContentType.APPLICATION_JSON)));
        if (jsonObject.getBoolean(CommonConstants.ERROR)) {
            LOGGER.error("Error when SearchFiber: \n{}", jsonObject);
            throw new OssCommandException(CommonConstants.SEOF, jsonObject.getString(CommonConstants.ERROR_MESSAGE));
        } else {
            String response = jsonObject.getString(CommonConstants.RESPONSE);
            LOGGER.info(response);
            if (this.getValueByRegx(response, "<ibn:target>(.+?)</ibn:target>") == null) {
                throw new OssCommandException(CommonConstants.IRNE, i18n.getMessage(CommonConstants.EADD_PREFIX +
                        "pon.notFoundByName", new Object[]{devInfo.getPonTargetName()}));
            }
            devInfo.setPonType(this.getValueByRegx(response, "<xpon-type>(.+?)</xpon-type>"));
            devInfo.setPonId(this.getValueByRegx(response, "<pon-id>(.+?)</pon-id>"));
            devInfo.setOltTargetName(this.getValueByRegx(response, "<device-name>(.+?)</device-name>"));
        }

        //query device info from deviceName
        rpcTemplate = "common/SearchIntent.ftl";
        params.clear();
        params.put("intentType", "device-mf");
        params.put("targetName", devInfo.getOltTargetName());
        String intentRpc = this.buildRpcRequest(rpcTemplate, params);
        LOGGER.info("search intent rpc request:" + intentRpc);
        jsonObject = restClient.processNetconfRequest(false, method ->
                ((HttpPost) method).setEntity(new StringEntity(intentRpc, ContentType.APPLICATION_JSON)));
        if (jsonObject.getBoolean(CommonConstants.ERROR)) {
            LOGGER.error("Error when SearchFiber: \n{}", jsonObject);
            throw new OssCommandException(CommonConstants.SEOF, jsonObject.getString(CommonConstants.ERROR_MESSAGE));
        } else {
            String response = jsonObject.getString(CommonConstants.RESPONSE);
            LOGGER.info(response);
            if (this.getValueByRegx(response, "<ibn:target>(.+?)</ibn:target>") == null) {
                throw new OssCommandException(CommonConstants.IRNE, i18n.getMessage(CommonConstants.EADD_PREFIX +
                        "olt.notFoundByName", new Object[]{devInfo.getOltTargetName()}));
            }
            devInfo.setOltIP(getValueByRegx(response, "<ip-address>(.+?)</ip-address>"));
            devInfo.setOltName(getValueByRegx(response, "<ne-name>(.+?)</ne-name>"));
            devInfo.setOltType(getValueByRegx(response, "<ibn:intent-type>(.+?)</ibn:intent-type>"));
            devInfo.setOltVersion(getValueByRegx(response, "<device-version>(.+?)</device-version>"));
            devInfo.setPap(getValueByRegx(response, "<partition-access-profile>(.+?)</partition-access-profile>"));
        }

        return devInfo;
    }

    @Override
    public List<DeviceInfo> queryOnuList(String onuIdType, String onuId) {
        return null;
    }


    private String getValueByRegx(String response, String regx) {
        Pattern pattern = Pattern.compile(regx);
        Matcher matcher = pattern.matcher(response);
        List<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        if (!matches.isEmpty()) {
            String match = matches.get(0);
            return match.replaceAll("<(.*?)>", "");
        }
        return null;
    }

    /**
     * parse 1-1-3-4 to LT3.PON4
     *
     * @param ponId, like 1-1-1-1
     * @return String
     * @throws OssCommandException
     */
    private String getPonId(String ponId) throws OssCommandException {
        //validate is not necessary here
        if (ponId == null) {
            throw new OssCommandException(CommonConstants.IMP, i18n.getMessage(CommonConstants.EADD_PREFIX
                    + "ponid.notfound"));
        }
        String[] nums = ponId.split("-");
        if (nums.length != 4) {
            throw new OssCommandException(CommonConstants.IIPF, i18n.getMessage(CommonConstants.EADD_PREFIX
                    + "ponid.formatincorrect"));
        }
        return "LT" + nums[2] + ".PON" + nums[3];
    }

    public String buildRpcRequest(String ftlName, Map<String, Object> templateArgs) throws OssCommandException {

        try {
            Template template = freemarkerConfig.getTemplate(ftlName);
            StringWriter writer = new StringWriter();
            template.process(templateArgs, writer);
            return writer.toString();
        } catch (Exception e) {
            LOGGER.error("Rpc query failed:", e);
            throw new OssCommandException(CommonConstants.EEEH, e.getMessage(), e);
        }


    }

    /**
     * ipv4 only,range 0.0.0.0 ~ 255.255.255.255
     *
     * @param input
     * @return boolean
     */
    private boolean isIpAddress(String input) {
        return input.matches("^((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.|$)){4}$");
    }

}
