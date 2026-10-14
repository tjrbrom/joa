package com.x.hhqs.joa.alarm.contextfield;

import com.x.hhqs.joa.alarm.contextfield.handlers.AttributeHandler;
import com.x.hhqs.joa.alarm.contextfield.handlers.AbstractAttributeHandler;
import com.x.hhqs.joa.alarm.contextfield.handlers.DefaultHandler;
import com.x.hhqs.joa.alarm.contextfield.handlers.DefaultValueAttributeHandler;
import com.x.hhqs.joa.alarm.utils.AlarmFieldManagerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class XAlarmFieldHandlersManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(XAlarmFieldHandlersManager.class);
    // "field name" --> handler
    private final Map<String, AttributeHandler> defaults = AlarmFieldManagerUtil.newCaseInsensitiveMap();

    private final ApplicationContext context;

    public XAlarmFieldHandlersManager(ApplicationContext context) {
        this.context = context;
    }

    @PostConstruct
    public void init() {
        LOGGER.info("init default alarm field handlers");
        Map<String, AbstractAttributeHandler> beans = context.getBeansOfType(AbstractAttributeHandler.class);
        for (AbstractAttributeHandler bean : beans.values()) {
            Class<?> clazz = bean.getClass();
            if (clazz.isAnnotationPresent(DefaultHandler.class)) {
                DefaultHandler defaultHandler = clazz.getAnnotation(DefaultHandler.class);
                for (String field : defaultHandler.field()) {
                    defaults.put(field, bean);
                    LOGGER.info("Alarm field: {} -> {}", field, bean);
                }
            }
        }

        defaults.put(AlarmFields.ALARM_CHECK, DefaultValueAttributeHandler.EMPTY);
        defaults.put(AlarmFields.MAINTENANCE_STATUS, DefaultValueAttributeHandler.EMPTY);
    }

    public AttributeHandler getAttributeHandler(String field) {
        return defaults.get(field);
    }
}
