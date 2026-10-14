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

@Component("nbiMetaData")
public class NbiMetaDataProcessor implements ModelDataProcessor {

    @Autowired
    private JoaMessage joaMessage;

    @Override
    public void process(NetconfRequest.Data data, Map<String, Object> model) throws OssCommandException {
        Map<String, String> nbiMeta = ModelDataContainer.getSharedData().getNbiMeta();
        String name = data.getName();
        String value = data.getValue();
        String regex = "\\$\\{nbiMetaData:(\\w+)}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        if (matcher.find()) {
            String matchKey = matcher.group(1);
            if (nbiMeta.containsKey(matchKey)) {
                String matchValue = nbiMeta.get(matchKey);
                model.put(name, matchValue);
                if (Boolean.TRUE.equals(data.getToContext())) {
                    Map<String, String> contextData = ModelDataContainer.getSharedData().getContext();
                    contextData.put(name, matchValue);
                }
            } else {
                if (Boolean.FALSE.equals(data.getOptional())) {
                    throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.NBI_META_DATA_NOT_FOUND, new Object[]{value}));
                }
            }
        } else {
            if (Boolean.FALSE.equals(data.getOptional())) {
                throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.MATCH_DATA_NOT_FOUND, new Object[]{value}));
            }
        }
    }
}
