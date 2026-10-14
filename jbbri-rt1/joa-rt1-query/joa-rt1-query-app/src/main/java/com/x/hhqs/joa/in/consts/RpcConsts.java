package com.x.hhqs.joa.in.consts;

import java.util.HashMap;
import java.util.Map;

public class RpcConsts {

	// rest response field
	public static final String HTTP_ERR_CODE = "httpErrCode";

	public static final String ERROR = "error";

	public static final String RESPONSE = "response";

	public static final String FORMATTED_DEFAULT_MESSAGE = "formattedDefaultMessage";

	// Profile type UI name
	public static final String ONT_SPEED_CONTROL_PROFILE_UI = "ONT Speed Control Profile";

	public static final String TRAFFIC_DESCRIPTOR_PROFILE_UI = "Traffic Descriptor Profile";

	public static final String SHAPER_PROFILE_UI = "Shaper Profile";

	public static final String ONU_TEMPLATE_UI = "ONU Template";

	public static final String MULTICAST_USER_SERVICE_PROFILE_UI = "Multicast User Service Profile";

	public static final String SIP_AGENT_SERVICE_PROFILE_UI = "VoIP SIP Agent Service Profile";

	public static final String SIP_AGENT_PROFILE_UI = "SIP Agent Profile";

	/**
	 * model name of LS-FX-MF-<> or LS-FX-SF-<>
	 * <p>
	 * LS-FX-MF-SF-LT / LS-FX-MF-SF-System
	 * 
	 * @return
	 */
	public static String lsMfAndSfModelName(String model) {
		return "LS-FX-MF-SF-" + model;
	}

	/**
	 * fetchDeviceAttributes=true means it will retrieve operational state data
	 * nodes
	 * <p>
	 * isChild=false&isOne=true means it will update last user change time
	 * 
	 * @return
	 */
	public static final String FETCH_DEVICE_REQUEST = "fetchDeviceAttributes=true&isChild=false&isOne=true";

	public static final String NOT_FETCH_DEVICE_REQUEST = "fetchDeviceAttributes=false&isChild=false&isOne=false";

	// anv freemarker template and it's namespace
	public static final Map<String, String> ANV_NS = new HashMap<String, String>() {
		{
			put("anv", "http://www.x.com/management-solutions/anv");
			put("adh", "http://www.x.com/management-solutions/anv-device-holders");
			put("if", "urn:ietf:params:xml:ns:yang:ietf-interfaces");
			put("bbf-xpon", "urn:bbf:yang:bbf-xpon");
			put("bbf-xponift", "urn:bbf:yang:bbf-xpon-if-type");
			put("bbf-xponvani", "urn:bbf:yang:bbf-xponvani");
			put("bbf-xponvani-onu-auth", "urn:bbf:yang:bbf-xponvani-onu-authentication");
			put("x-sdan-if-xponvani-aug",
					"http://www.x.com/Fixed-Networks/BBA/yang/x-sdan-if-xponvani-aug");
			put("onu", "urn:bbf:params:xml:ns:yang:bbf-fiber-onu-emulated-mount");
			put("onus-from-template", "http://www.x.com/Fixed-Networks/BBA/yang/onus-from-template");
			put("if-mounted", "urn:ietf:params:xml:ns:yang:ietf-interfaces-mounted");
			put("eth-mounted", "urn:ieee:params:xml:ns:yang:ethernet-mounted");
			put("state", "urn:x.com:sros:ns:yang:sr:state");
			put("conf", "urn:x.com:sros:ns:yang:sr:conf");
			put("onu", "urn:bbf:params:xml:ns:yang:bbf-fiber-onu-emulated-mount");
			put("onus-from-template", "http://www.x.com/Fixed-Networks/BBA/yang/onus-from-template");
			put("interfaces-state", "urn:ietf:params:xml:ns:yang:ietf-interfaces-mounted");
			put("sip-signaling-state", "urn:bbf:yang:bbf-sip-voip-mounted");
		}
	};

	public static final String GET_CHANNEL_PARTITION_FTL = "anv/GetChannelPartition.ftl";

	public static final String GET_CHANNEL_PAIR_FTL = "anv/GetChannelPair.ftl";

	public static final String GET_INTERFACE_STATE_FTL = "anv/GetInterfaceState.ftl";

	public static final String GET_ONT_CONFIG_DATA_FTL = "anv/GetOntConfigData.ftl";

	public static final String GET_ONT_CONFIG_DATA_BY_CPAIR_FTL = "anv/GetOntConfigDataByCPAIR.ftl";

	public static final String GET_INTERFACE_STATE_ONT_FTL = "anv/GetInterfaceState-ONT.ftl";

	public static final String GET_ONT_TEMPLATE_NAME_FTL = "anv/GetOntTemplateName.ftl";

	public static final String GET_LAN_SPEED_BY_TEMPLATE_NAME_FTL = "anv/GetLanSpeedByOntTemplate.ftl";

	public static final String GET_LAN_PORT_STATUS_FTL = "anv/GetLanPortStatus.ftl";

	public static final String GET_NT_PORT_FTL = "anv/GetNtPort.ftl";

	public static final String GET_NT_PORT_CONFIG_DATA_FTL = "anv/GetNtPortConfigData.ftl";

	public static final String GET_VOIP_CONFIG_DATA_FTL = "anv/GetVoipConfigData.ftl";

	public static final String GET_VOIP_STATE_DATA_FTL = "anv/GetVoipStateData.ftl";

	// mf ibn freemarker template and it's namespace
	public static final Map<String, String> MF_IBN_NS = new HashMap<String, String>() {
		{
			put("ibn", "http://www.x.com/management-solutions/ibn");
			put("dmf", "http://www.x.com/management-solutions/device-mf");
			put("device-mf", "http://www.x.com/management-solutions/device-mf");
			put("fiber", "http://www.x.com/management-solutions/fiber");
			put("ont", "http://www.x.com/management-solutions/ont");
			put("l2user", "http://www.x.com/management-solutions/l2-user");
			put("l2infra", "http://www.x.com/management-solutions/l2-infra");
			put("mcu", "http://www.x.com/management-solutions/mcast-user");
			put("svsipu", "http://www.x.com/management-solutions/standard-voip-sip-user");
			put("svsipua", "http://www.x.com/management-solutions/standard-voip-sip-agent");
			put("device-splitter", "http://www.x.com/management-solutions/device-splitter");
		}
	};

	public static final String GET_DEVICE_FTL = "ibn/GetDevice.ftl";

	public static final String GET_FIBER_FTL = "ibn/GetFiber.ftl";

	public static final String GET_ONT_FTL = "ibn/GetOnt.ftl";

	public static final String GET_ONT_BY_PON_FTL = "ibn/GetOntByPon.ftl";

	public static final String GET_ONT_BY_PONS_FTL = "ibn/GetOntByPons.ftl";

	public static final String GET_ONT_CONFIG_FTL = "ibn/GetOntConfig.ftl";

	public static final String GET_ONT_NO_BY_TARGET_FTL = "ibn/GetOntNoByTarget.ftl";

	public static final String GET_ONT_LANPORT_FTL = "ibn/GetOntLanPort.ftl";
	
	public static final String GET_ONT_WITH_SPLITTER_BY_PON_FTL = "ibn/GetOntWithSplitterByPon.ftl";

	public static final String GET_L2_USER_FTL = "ibn/GetL2User.ftl";

	public static final String GET_L2_USER_BY_TARGET_FTL = "ibn/GetL2UserByTarget.ftl";

	public static final String GET_L2_INFRA_FTL = "ibn/GetL2Infra.ftl";

	public static final String GET_MCAST_USER_FTL = "ibn/GetMcastUser.ftl";

	public static final String GET_SIP_USER_FTL = "ibn/GetStandardVoipSipUser.ftl";

	public static final String GET_SIP_AGENT_FTL = "ibn/GetStandardVoipSipAgent.ftl";
	
	public static final String GET_DEVICE_SPLITTER_BY_PON_FTL = "ibn/GetDeviceSplitterByPon.ftl";

	public static final String SEARCH_INTENTS = "ibn/SearchIntents.ftl";

	// profile manager freemarker template and it's namespace
	public static final Map<String, String> PROFILE_NS = new HashMap<String, String>() {
		{
			put("profile-mgr", "http://www.x.com/management-solutions/profile-mgr");
			put("prof-ont-speed", "http://www.x.com/management-solutions/ont-speed-control-profile");
			put("prof-traffic-desc", "http://www.x.com/management-solutions/traffic-descriptor-profiles");
			put("prof-shaper-profile", "http://www.x.com/management-solutions/shaper-profile");
			put("prof-onu-template", "http://www.x.com/management-solutions/onu-template");
			put("prof-mcast-usr", "http://www.x.com/management-solutions/multicast-user-service-profile");
			put("prof-voip-agent", "http://www.x.com/management-solutions/voip-sip-agent-service-profile");
			put("prof-sip-agent", "http://www.x.com/management-solutions/sip-agent-profile");
		}
	};

	public static final String GET_PROFILES_USED_BY_INTENT = "profile/GetProfilesUsedByIntent.ftl";

	public static final String GET_PROFILE_SPECIFIC_DATA = "profile/GetProfileSpecificData.ftl";

	public static final Map<String, String> MDS_NS = new HashMap<String, String>() {
		{
			put("mds", "http://www.x.com/management-solutions/manager-directory-service");
		}
	};

	public static final String GET_MDS_FTL = "mds/GetMds.ftl";

}
