package com.x.hhqs.joa.common.sharedfileservice.utils;

import java.util.Map;

public class SystemPropertyUtils {
    private static SystemPropertyUtils INSTANCE = new SystemPropertyUtils();
    private SystemPropertyUtils(){

    }

    public static SystemPropertyUtils getInstance() {
        return INSTANCE;
    }

    /**
     * For unit tests.
     * Caution: if this method used, one should make sure to reset the instance upon test teardown.
     * @param instance
     */
    public static void setInstance(SystemPropertyUtils instance) {
        INSTANCE = instance;
    }

    /**
     * Attempts to read the value of an Environment Variable and return. If the environment doesn't have that variable, attempts to fall
     * back to System Property. Returns the {@literal defaultValue} if there is no such System property.
     *
     * @param name
     *        Name of the Environment Variable / System Property
     * @param defaultValue
     *        Default Value to return upon unavailability of Env Variable / System Property.
     */
    public String getFromEnvOrSysProperty(String name, String defaultValue) {
        Map<String, String> envMap = System.getenv();
        if (envMap == null) {
            return defaultValue;
        }
        String value = System.getenv(name);
        if (value == null) {
            value = System.getProperty(name, null);
        }
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Variant of {@link #getFromEnvOrSysProperty(String, String)} which throws {@link IllegalArgumentException} instead of returning
     * {@literal defaultValue}.
     *
     * @param name
     *        Name of the Environment Variable / System Property
     */
    public String readFromEnvOrSysProperty(String name) {
        String value = getFromEnvOrSysProperty(name);
        if (value == null) {
            throw new IllegalArgumentException("name=" + name);
        }
        return value;
    }

    /**
     * Variant of {@link #getFromEnvOrSysProperty(String, String)} where the {@literal defaultValue} is {@literal NULL}.
     *
     * @param name
     *        Name of the Environment Variable / System Property
     */
    public String getFromEnvOrSysProperty(String name) {
        return getFromEnvOrSysProperty(name, null);
    }
}