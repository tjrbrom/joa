package com.x.hhqs.joa.sa.model;

import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.util.CommonConstants;
import com.x.hhqs.joa.sa.utils.SystemConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ConditionEvaluator {

    private static final String NBI_META_DATA_PREFIX = "nbiMetaData";
    private static final String SBI_META_DATA_PREFIX = "sbiMetaData";
    private static final String CONTEXT_DATA_PREFIX = "context";
    private static final String META_DATA_FORMAT = "\\$\\{([^:]+):([^}]+)}";

    @Autowired
    private JoaMessage joaMessage;

    public boolean evaluate(String condition) throws OssCommandException {
        if (condition == null || condition.isEmpty()) {
            return true;
        }
        Map<String, String> nbiMeta = ModelDataContainer.getSharedData().getNbiMeta();
        Map<String, String> sbiMeta = ModelDataContainer.getSharedData().getSbiMeta();
        Map<String, String> context = ModelDataContainer.getSharedData().getContext();
        if (condition.contains("=")) {
            return evaluateEqualsCondition(condition, nbiMeta, sbiMeta, context);
        } else if (condition.startsWith("!")) {
            return evaluateNegationCondition(condition, nbiMeta, sbiMeta, context);
        } else if (condition.startsWith("$")) {
            return evaluateCondition(condition, nbiMeta, sbiMeta, context);
        } else {
            throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.CONDITION_FORMAT_INCORRECT, new Object[]{condition}));
        }
    }

    private boolean evaluateCondition(String condition, Map<String, String> nbiMeta, Map<String, String> sbiMeta, Map<String, String> context) throws OssCommandException {
        Pattern pattern = Pattern.compile(META_DATA_FORMAT);
        Matcher matcher = pattern.matcher(condition);
        if (matcher.find()) {
            String prefix = matcher.group(1);
            String model = matcher.group(2);
            switch (prefix) {
                case NBI_META_DATA_PREFIX:
                    return nbiMeta.containsKey(model);
                case SBI_META_DATA_PREFIX:
                    return sbiMeta.containsKey(model);
                case CONTEXT_DATA_PREFIX:
                    return context.containsKey(model);
                default:
                    throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.CONDITION_FORMAT_INCORRECT, new Object[]{condition}));
            }
        } else {
            throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.CONDITION_FORMAT_INCORRECT, new Object[]{condition}));
        }
    }

    private boolean evaluateNegationCondition(String condition, Map<String, String> nbiMeta, Map<String, String> sbiMeta, Map<String, String> context) throws OssCommandException {
        String negationCondition = condition.substring(1);
        Pattern pattern = Pattern.compile(META_DATA_FORMAT);
        Matcher matcher = pattern.matcher(negationCondition);
        if (matcher.find()) {
            String prefix = matcher.group(1);
            String model = matcher.group(2);
            switch (prefix) {
                case NBI_META_DATA_PREFIX:
                    return !nbiMeta.containsKey(model);
                case SBI_META_DATA_PREFIX:
                    return !sbiMeta.containsKey(model);
                case CONTEXT_DATA_PREFIX:
                    return !context.containsKey(model);
                default:
                    throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.CONDITION_FORMAT_INCORRECT, new Object[]{condition}));
            }
        } else {
            throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.CONDITION_FORMAT_INCORRECT, new Object[]{condition}));
        }
    }

    private boolean evaluateEqualsCondition(String condition, Map<String, String> nbiMeta, Map<String, String> sbiMeta, Map<String, String> context) throws OssCommandException {
        String[] keyValue = condition.split("=");
        if (keyValue.length == 2) {
            String key = keyValue[0];
            String value = keyValue[1];
            Pattern pattern = Pattern.compile(META_DATA_FORMAT);
            Matcher matcher = pattern.matcher(key);
            if (matcher.find()) {
                String prefix = matcher.group(1);
                String model = matcher.group(2);
                switch (prefix) {
                    case NBI_META_DATA_PREFIX:
                        return value.equals(nbiMeta.get(model));
                    case SBI_META_DATA_PREFIX:
                        return value.equals(sbiMeta.get(model));
                    case CONTEXT_DATA_PREFIX:
                        return value.equals(context.get(model));
                    default:
                        throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.CONDITION_FORMAT_INCORRECT, new Object[]{condition}));
                }
            } else {
                throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.CONDITION_FORMAT_INCORRECT, new Object[]{condition}));
            }
        } else {
            throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.CONDITION_FORMAT_INCORRECT, new Object[]{condition}));
        }
    }
}
