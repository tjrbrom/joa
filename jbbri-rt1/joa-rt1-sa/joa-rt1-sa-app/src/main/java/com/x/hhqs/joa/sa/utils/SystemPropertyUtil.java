package com.x.hhqs.joa.sa.utils;

/**
 * Get value from system properties and environment variables
 */
public class SystemPropertyUtil {
    private SystemPropertyUtil(){
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
    public static String getFromEnvOrSysProperty(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null) {
            value = System.getProperty(name);
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
    public static String readFromEnvOrSysProperty(String name) {
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
    public static String getFromEnvOrSysProperty(String name) {
        return getFromEnvOrSysProperty(name, null);
    }
}
