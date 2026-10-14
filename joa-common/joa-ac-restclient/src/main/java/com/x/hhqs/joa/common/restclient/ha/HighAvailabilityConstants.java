package com.x.hhqs.joa.common.restclient.ha;

public class HighAvailabilityConstants {

    // OpenTsDB
    public static final String ENV_OPENTSDB_URL = "OPENTSDB_URL";

    // ElasticSearch
    public static final String ENV_LOGS_ES_URL = "LOGS_ES_URL";
    public static final String ENV_LOGS_ES_USER = "LOGS_ES_USER";
    public static final String ENV_LOGS_ES_PASSWORD = "LOGS_ES_PASSWORD";
    public static final String ENV_ALARM_BRIDGE_ES_URL = "ALARM_BRIDGE_ES_URL";
    public static final String ENV_ALARM_BRIDGE_ES_USER = "ALARM_BRIDGE_ES_USER";
    public static final String ENV_ALARM_BRIDGE_ES_PASSWORD = "ALARM_BRIDGE_ES_PASSWORD";
    public static final String ENV_BRIDGE_ES_URL = "BRIDGE_ES_URL";
    public static final String ENV_INVENTORY_ES_URL = "INVENTORY_ES_URL";
    public static final String ENV_INVENTORY_ES_USER = "INVENTORY_ES_USER";
    public static final String ENV_INVENTORY_ES_PASSWORD = "INVENTORY_ES_PASSWORD";
    public static final String ENV_INTENTS_ES_URL = "INTENTS_ES_URL";
    public static final String ENV_INTENTS_ES_USER = "INTENTS_ES_USER";
    public static final String ENV_INTENTS_ES_PASSWORD = "INTENTS_ES_PASSWORD";
    public static final String ENV_FMC_ES_URL="FMC_ES_URL";
    public static final String ENV_FMC_ES_USER="FMC_ES_USER";
    public static final String ENV_FMC_ES_PASSWORD="FMC_ES_PASSWORD";

    // Webdav
    public static final String ENV_SHARED_FILE_SERVER_URL = "SHARED_FILE_SERVER_URL";

    // Keycloak
    public static final String ENV_KEYCLOAK_URL = "KEYCLOAK_URL";
    public static final String ENV_KEYCLOAK_TRUST_STORE = "KEYCLOAK_TRUST_STORE";
    public static final String ENV_KEYCLOAK_TRUST_STORE_PASSWORD = "KEYCLOAK_TRUST_STORE_PASSWORD";

    // Fluentd
    public static final String ENV_FLUENTD_LOG_DIRECT_URL = "FLUENTD_LOG_DIRECT_URL";

    // General
    public static final String ENV_CURRENT_CONTINER_ID = "CURRENT_CONTAINER_ID";
    public static final String ENV_FLUENTD_LOG_DIRECT_TAG = "FLUENTD_LOG_DIRECT_TAG";

    // NSP
    public static final String ENV_NSP_URL = "NSP_URL";

    // Redis
    public static final String ENV_REDIS_URL = "REDIS_URL";

    // For OSGi Events
    public static final String HA_EVENT_TOPIC = "com/x/high/availability/checker/EVENT";
    public static final String HA_EVENT_APP = "application";
    public static final String HA_EVENT_TYPE = "ha";
    public static final String HA_EVENT_CONNECTION_STATE = "connectionState";
    public static final String OSGI_EVENT_TYPE = "type";
    public static final String OPENTSDB_APP_NAME = "OpentsDB";

    public static final String ES_HA_APP_NAME = "ElasticSearch";
    public static final String FLUENTD_HA_APP_NAME = "Fluentd";
    public static final String KAFKA_HA_APP_NAME = "Kafka";
    public static final String KEYCLOAK_HA_APP_NAME = "Keycloak";
    public static final String NSP_HA_APP_NAME = "NSP";
    public static final String DB_HA_APP_NAME = "Database";
    public static final String REDIS_HA_APP_NAME = "REDIS";
    public static final String WEBDAV_HA_APP_NAME = "Webdav";

    public static final String AC_APP_NAME = "core";

    public static final String CORE_REST_HA_APP_NAME = "Core Rest HA";

    public static final String LITE_SERVICE_MGMT_URL = "LITE_SERVICE_MGMT_URL";
    public static final String X_ACS_HA_APP_NAME = "X_ACS";
    public static final String TRM_HA_APP_NAME = "TRM";
    public static final String CHECKER_ERROR_MESSAGE = "cannot connect to %s";
    public static final String EDENNET_HA_APP_NAME = "EdenNet";
    public static final String EDENNET_RABBITMQ_HA_APP_NAME = "EdenNetRabbitMQ";
}
