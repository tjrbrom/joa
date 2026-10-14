package com.x.hhqs.joa.common.rt1fwk.impl;

import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.rt1fwk.api.OssCommandValidator;
import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.rt1fwk.pojo.ValidAttribute;
import com.x.hhqs.joa.common.rt1fwk.pojo.Validation;
import com.x.hhqs.joa.common.util.CommonConstants;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import com.x.hhqs.joa.rt1message.RT1Param;
import com.x.hhqs.joa.rt1message.RT1PayloadBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class OssCommandValidatorImpl implements OssCommandValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(OssCommandValidatorImpl.class);

    @Autowired
    private JoaMessage i18n;



    @Override
    public void validate(RT1InputMessage rt1InputMsg, Validation validation) throws OssCommandException {
        if(!validation.isEnabled()) {
            return;
        }
        if(notBlank(rt1InputMsg.getTid())) {
            checkValidation(validation.getTid(), "TID", rt1InputMsg.getTid());
        }
        if(notBlank(rt1InputMsg.getCTag())){
            checkValidation(validation.getCtag(), "CTAG", rt1InputMsg.getCTag());
        }

        if(validation.getAidGroups() != null && validation.getAidGroups().size() > 0) {
            checkGroups(validation.getAidGroups(), parseString(rt1InputMsg.getAid()));
        }
        if(validation.getParamBlocks() != null && validation.getParamBlocks().size() > 0) {
            Map<String, String> map = new HashMap<>();
            if (notEmpty(rt1InputMsg.getPayloadBlock())) {
                for (RT1PayloadBlock payloadBlock : rt1InputMsg.getPayloadBlock()) {
                    parseBlock(payloadBlock, map);
                }
            }
            checkGroups(validation.getParamBlocks(), map);
        }
    }

    private void checkGroups(List<Map<String, ValidAttribute>> groups, Map<String, String> rt1Group) throws OssCommandException {
        boolean passCheck = false;
        OssCommandException exception = null;
        for (Map<String, ValidAttribute> map : groups) {
            for (Map.Entry<String, ValidAttribute> entry : map.entrySet()) {
                String key = entry.getKey();
                ValidAttribute value = entry.getValue();

                try {
                    this.checkValidation(value, key, rt1Group.get(key));
                    passCheck = true;
                } catch (OssCommandException e) {
                    passCheck = false;
                    exception = e;
                    break;
                }
            }
            if (passCheck) {
                break;
            }
        }
        if(!passCheck) {
            throw exception;
        }
    }

    private void parseBlock(RT1PayloadBlock payloadBlock, Map<String, String> map) {


        ArrayList<RT1Param> params = payloadBlock.getParam();
        if (notEmpty(params)) {
            for (RT1Param rt1Param : params) {
                map.put(rt1Param.getParamName().trim(), rt1Param.getParamValue().trim());
            }
        }
    }

    private boolean notEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

    private Map<String, String> parseString(String message) {
        //TODO delete ""
        Map<String, String> map = new HashMap<>();
        if(notBlank(message)) {
            if (message.contains(",")) {
                String[] splits = message.split(",", -1);
                for (String split : splits) {
                    if (notBlank(split)) {
                        if (split.contains("=")) {
                            String[] internSplit = split.split("=", -1);
                            map.put(internSplit[0].trim(), internSplit[1].trim());
                        } else {
                            map.put(split.trim(), "");
                        }
                    }
                }
            } else {
                if(message.contains("=")) {
                    String[] internSplit = message.split("=", -1);
                    map.put(internSplit[0].trim(), internSplit[1].trim());
                } else {
                    map.put(message.trim(), "");
                }

            }
        }

        return map;
    }

    private boolean notBlank(String str) {
        return str != null && str.trim().length() != 0;
    }

    private void checkValidation(ValidAttribute validAttribute, String key, String attrInRT1) throws OssCommandException {
        if(validAttribute != null) {
            // required=true, but in rt1message it is empty
            if(validAttribute.isRequired() && !notBlank(attrInRT1)){
                throw new OssCommandException(CommonConstants.IMP, i18n.getMessage(CommonConstants.EADD_PREFIX +
                        "missing.parameter", new Object[]{validAttribute.getName()}));
            }

            if(notBlank(validAttribute.getRegexpr()) && attrInRT1 != null){
                this.patternMatch(validAttribute.getRegexpr(), key, attrInRT1);
            }
        }
    }

    private void patternMatch(String regexpr, String key, String rt1Message) throws OssCommandException {
        Pattern patter = Pattern.compile(regexpr);
        Matcher matcher = patter.matcher(rt1Message.trim());
        boolean isMatches= matcher.matches();
        if(!isMatches){
            LOGGER.error("The parameter <" + rt1Message + "> value is invalid");
            throw new OssCommandException(CommonConstants.IIPF, i18n.getMessage(CommonConstants.EADD_PREFIX +
                    "pattern.notmatch", new Object[]{regexpr, key + "=" + rt1Message}));
        }
    }
}
