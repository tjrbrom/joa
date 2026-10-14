package com.x.hhqs.joa.sa.model;

import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.util.CommonConstants;
import com.x.hhqs.joa.sa.netconf.NetconfRequest;
import com.x.hhqs.joa.sa.utils.SystemConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("placeholder")
public class PlaceholderProcessor implements ModelDataProcessor {

    @Autowired
    private JoaMessage joaMessage;

    @Override
    public void process(NetconfRequest.Data data, Map<String, Object> model) throws OssCommandException {

        String name = data.getName();
        String value = data.getValue();
        Boolean optional = data.getOptional();
        String regex = "\\$\\{([^:]+):([^}]+)}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String mapName = matcher.group(1);
            String key = matcher.group(2);
            try {
                matchMap(mapName, key, matcher, result);
            } catch (OssCommandException e) {
                if (Boolean.FALSE.equals(optional)) {
                    throw e;
                } else {
                    return;
                }
            }
        }
        matcher.appendTail(result);
        model.put(name, result.toString());
        if (Boolean.TRUE.equals(data.getToContext())) {
            ModelDataContainer.getSharedData().getContext().put(name, result.toString());
        }
    }

    private void matchMap(String mapName, String key, Matcher matcher, StringBuilder result) throws OssCommandException {
        Map<String, String> nbiMeta = ModelDataContainer.getSharedData().getNbiMeta();
        Map<String, String> sbiMeta = ModelDataContainer.getSharedData().getSbiMeta();
        Map<String, String> context = ModelDataContainer.getSharedData().getContext();
        Map<String, String> onuInfo = ModelDataContainer.getSharedData().getOnuInfo();
        Map<String, String> userInfo = ModelDataContainer.getSharedData().getUserInfo();
        if ("nbiMetaData".equals(mapName)) {
            if (nbiMeta.containsKey(key)) {
                matcher.appendReplacement(result, nbiMeta.get(key));
            } else {
                throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.NBI_META_DATA_NOT_FOUND, new Object[]{key}));
            }
        } else if ("sbiMetaData".equals(mapName)) {
            if (sbiMeta.containsKey(key)) {
                matcher.appendReplacement(result, sbiMeta.get(key));
            } else {
                throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.SBI_META_DATA_NOT_FOUND, new Object[]{key}));
            }
        } else if ("context".equals(mapName)) {
            if (context.containsKey(key)) {
                matcher.appendReplacement(result, context.get(key));
            } else {
                throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.CONTEXT_DATA_NOT_FOUND, new Object[]{key}));
            }
        } else if ("onuInfo".equals(mapName)) {
            if (onuInfo.containsKey(key)) {
                matcher.appendReplacement(result, onuInfo.get(key));
            } else {
                throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.ONU_INFO_DATA_NOT_FOUND, new Object[]{key}));
            }
        } else if ("userInfo".equals(mapName)) {
            if (userInfo.containsKey(key)) {
                matcher.appendReplacement(result, userInfo.get(key));
            } else {
                throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.USER_INFO_DATA_NOT_FOUND, new Object[]{key}));
            }
        }
    }
}
