package com.x.hhqs.joa.sa.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class BeanUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanUtil.class);

    private BeanUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Map<String, String> toStringMap(Object obj) {
        Map<String, String> result = new HashMap<>();
        if (obj == null) {
            return result;
        }

        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType() == String.class) {
                    field.setAccessible(true);
                    try {
                        result.put(field.getName(), (String) field.get(obj));
                    } catch (IllegalAccessException e) {
                        LOGGER.info("Error accessing field: {}", field.getName(), e);
                        result.put(field.getName(), null);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

        return result;
    }
}

