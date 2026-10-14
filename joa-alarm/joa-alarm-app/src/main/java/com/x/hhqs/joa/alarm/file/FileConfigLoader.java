package com.x.hhqs.joa.alarm.file;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Component
public class FileConfigLoader implements InitializingBean {

    @Resource
    private DestinationNode enableNode;

    @Getter
    private FileDeleteStrategy fileDeleteStrategy;

    @Override
    public void afterPropertiesSet() {
        try {
            this.fileDeleteStrategy = parseFileDeleteStrategy(enableNode.getFileAutoDeleteStrategy().getKeep());
        } catch (Exception e) {
            log.error("init ftp config failed.", e);
        }
    }

    protected FileDeleteStrategy parseFileDeleteStrategy(String keep) {
        log.info("keep: {}", keep);
        if (StringUtils.isEmpty(keep)) {
            return null;
        }

//        Pattern pattern = Pattern.compile("^(\\d+)(\\D+)$");
        Pattern pattern = Pattern.compile("^(?<num>\\d+)\\s+(?<unit>days?|hours?|minutes?|seconds?|files?)$");
        Matcher matcher = pattern.matcher(keep);

        if (!matcher.matches()) {
            return null;
        }

        int digit = Integer.parseInt(matcher.group("num"));
        if (digit <= 0) {
            return null;
        }

        String unit = matcher.group("unit").trim();
        if (unit.equalsIgnoreCase("files") || unit.equalsIgnoreCase("file")) {
            return FileDeleteStrategy.quantityLimitStrategy(digit);
        }

        return StringUtils.isEmpty(unit) ? null
                : FileDeleteStrategy.aliveTimeStrategy(digit, parseTimeUnit(unit));
    }

    private TimeUnit parseTimeUnit(String unit) {
        unit = unit.toLowerCase();
        if (!unit.endsWith("s")) {
            unit += "s";
        }
        return switch (unit) {
            case "days" -> TimeUnit.DAYS;
            case "hours" -> TimeUnit.HOURS;
            case "minutes" -> TimeUnit.MINUTES;
            case "seconds" -> TimeUnit.SECONDS;
            default -> null;
        };
    }
}

