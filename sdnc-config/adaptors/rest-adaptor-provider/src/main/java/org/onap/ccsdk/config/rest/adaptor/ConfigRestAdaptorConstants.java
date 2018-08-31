package org.onap.ccsdk.config.rest.adaptor;

public class ConfigRestAdaptorConstants {
    private ConfigRestAdaptorConstants() {

    }

    public static final String SDNC_ROOT_DIR_ENV_VAR_KEY = "SDNC_CONFIG_DIR";
    public static final String REST_ADAPTOR_PROPERTIES_FILE_NAME = "config-rest-adaptor.properties";
    public static final String PROXY_URL_KEY = "proxyUrl";
    public static final String PROXY_URLS_VALUE_SEPARATOR = ",";
    public static final String AAF_USERNAME_KEY = "aafUserName";
    public static final String AAF_PSSWD_KEY = "aafPassword";
    public static final String COMMON_SERVICE_VERSION_KEY = "commonServiceVersion";

    public static final String PROPERTY_ENV_PROD = "field";
    public static final String PROPERTY_ENV_SOLO = "solo";

    public static final String REST_ADAPTOR_BASE_PROPERTY = "org.onap.ccsdk.config.rest.adaptors.";
    public static final String REST_ADAPTOR_ENV_TYPE = "envtype";
    public static final String REST_ADAPTOR_TYPE_GENERIC = "generic";
    public static final String REST_ADAPTOR_TYPE_SSL = "ssl";

    public static final String SSL_SERVICE_BASEURL = ".url";
    public static final String SSL_SERVICE_APP = ".application";
    public static final String SSL_SERVICE_TRUST = ".ssl.trust";
    public static final String SSL_SERVICE_TRUST_PSSWD = ".ssl.trust.psswd";
    public static final String SSL_SERVICE_KEY = ".ssl.key";
    public static final String SSL_SERVICE_KEY_PSSWD = ".ssl.key.psswd";

    public static final String SERVICE_TYPE_PROPERTY = ".type";
    public static final String SERVICE_EANABLED_PROPERTY = ".enable";
    public static final String SERVICE_ENV_PROPERTY = ".env";
    public static final String SERVICE_BASEURL_PROPERTY = ".url";
    public static final String SERVICE_PROPERTYFILE = ".propertyfile";
    public static final String SERVICE_USER_PROPERTY = ".user";
    public static final String SERVICE_APPID_PROPERTY = ".appId";
    public static final String SERVICE_PSSWD_PROPERTY = ".passwd";
    public static final String SERVICE_CLIENTAUTH_PROPERTY = ".clientAuth";
    public static final String SERVICE_AUTHORIZATION_PROPERTY = ".authorization";

    public static final String SELECTOR_AAI = "aai";
    public static final String SELECTOR_ALTS = "alts";
    public static final String SELECTOR_EIPAM = "eipam";
    public static final String SELECTOR_COSMS = "cosms";
    public static final String SELECTOR_RESTCONF = "restconf";
    public static final String SELECTOR_MODEL_SERVICE = "modelservice";
    public static final String SELECTOR_POLICY_MANAGER = "policymanager";
    public static final String SELECTOR_NRD = "networkresourcediscovery";
    public static final String SELECTOR_NSM = "nsm";

}
