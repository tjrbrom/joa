package com.x.hhqs.joa.sa.utils;

/**
 * Constants class
 */
public class SystemConstants {

    private SystemConstants() {
    }

    //RPC response
    public static final String ERROR = "error";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String REQUEST_TYPE = "requestType";
    public static final String RESPONSE = "response";
    public static final String HAS_ERROR = "hasError";
    public static final String STATUS_CODE = "statusCode";
    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";

    //HTTP request
    public static final String HTTP_POST = "POST";
    public static final String HTTP_PATCH = "PATCH";
    public static final String HTTP_DELETE = "DELETE";
    public static final String HTTP_GET = "GET";
    public static final String HTTP_PUT = "PUT";
    public static final String TYPE_YANG_DATA_XML = "application/yang-data+xml";
    public static final String TYPE_YANG_DATA_JSON = "application/yang-data+json";
    public static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";

    //Template
    public static final String SERVICE_XML = "service.xml";
    public static final String PON_TYPE = "pontype";
    public static final String OLT_TYPE = "olttype";
    public static final String SERVICE_TYPE = "servicetype";
    public static final String ONU_SUBTYPE = "onusubtype";
    public static final String NUM_PATH = "/service-templates/service-template[@olttype='%s'][@pontype='%s'][@servicetype='%s']/oss-command[@name='%s']/netconf-request/@number";
    public static final String CONTENT_PATH = "/service-templates/service-template[@olttype='%s'][@pontype='%s'][@servicetype='%s']/oss-command[@name='%s']/netconf-request/request-instance[%d]/rpc";
    public static final String RPC_PATH = "/service-templates/service-template[@olttype='%s'][@pontype='%s'][@servicetype='%s']/oss-command[@name='%s']/netconf-request/request-instance[%d]/rpc";

    // i18n
    public static final String MODULE_PREFIX = "joa#rt1#eadd.rt1sa.";
    public static final String XPATH_EXPRESSION_EXCEPTION = MODULE_PREFIX + "xPathExpressionException";
    public static final String TEMPLATE_INDEX_NOT_FOUND = MODULE_PREFIX + "templateIndexNotFound";
    public static final String UNSUPPORTED_MODEL_DATA_TYPE = MODULE_PREFIX + "unsupportedModelDataType";
    public static final String UNSUPPORTED_SELECTOR_TYPE = MODULE_PREFIX + "unsupportedSelectorType";
    public static final String MATCH_DATA_NOT_FOUND = MODULE_PREFIX + "matchDataNotFound";
    public static final String NBI_META_DATA_NOT_FOUND = MODULE_PREFIX + "nbiMetaDataNotFound";
    public static final String SBI_META_DATA_NOT_FOUND = MODULE_PREFIX + "sbiMetaDataNotFound";
    public static final String CONTEXT_DATA_NOT_FOUND = MODULE_PREFIX + "contextDataNotFound";
    public static final String ONU_INFO_DATA_NOT_FOUND = MODULE_PREFIX + "onuInfoDataNotFound";
    public static final String USER_INFO_DATA_NOT_FOUND = MODULE_PREFIX + "userInfoDataNotFound";
    public static final String REGEX_NOT_MATCHED = MODULE_PREFIX + "regexNotMatched";
    public static final String XPATH_NOT_MATCHED = MODULE_PREFIX + "xpathNotMatched";
    public static final String CONDITION_FORMAT_INCORRECT = MODULE_PREFIX + "conditionFormatIncorrect";
}
