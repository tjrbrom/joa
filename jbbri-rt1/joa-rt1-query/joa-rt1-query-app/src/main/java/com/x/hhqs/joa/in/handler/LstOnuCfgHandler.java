package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.NbiConsts;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.service.NetconfRpcBuilder;
import com.x.hhqs.joa.in.service.NetconfService;
import com.x.hhqs.joa.in.service.ProfileManagerService;
import com.x.hhqs.joa.in.util.ConvertUtil;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.vo.OnuCfg;
import com.x.hhqs.joa.in.vo.Profile;
import org.dom4j.DocumentException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component("LST-ONUCFG")
public class LstOnuCfgHandler extends AbstractHandler {

	private static final String TITLE = "list of ONU info";

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private ProfileManagerService profileManagerService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	@Override
	protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
		String deviceTarget = OssCmdMetaParser.oltTarget(ossCmdMeta);
		String deviceId = deviceTarget + ".LT" + OssCmdMetaParser.slotNo(ossCmdMeta); // FX-TEST1.LT2

		OnuCfg onuCfg = addOnuConfigDataToOnuCfg(ossCmdMeta, deviceId);
		addOnuStateDataToOnuCfg(deviceId, onuCfg);
		addOnuBwToOnuCfg(onuCfg);
		String[][] rtrvArray = createRtrvArray(onuCfg, OnuCfg.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	private OnuCfg addOnuConfigDataToOnuCfg(OssCmdMeta ossCmdMeta, String deviceId)
			throws Rt1QueryException, IOException, DocumentException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceId", deviceId);
		templateArgs.put("ontTarget", OssCmdMetaParser.ontTarget(ossCmdMeta));
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_ONT_CONFIG_DATA_FTL, templateArgs);
		String interfaceEleXml = netconfService
				.sendRequestToVirtualizerAndParseFirstResult(rpcRequest, interfaceXPath()).asXML();

		OnuCfg onuCfg = new OnuCfg();
		String enabled = getFirstValFromAnv(interfaceEleXml, interfaceXPath("enabled"));
		onuCfg.setAdminState(parseAdminState(enabled));
		onuCfg.setTarget(getFirstValFromAnv(interfaceEleXml, interfaceXPath("name")));
		addAuthToOnuCfg(onuCfg, interfaceEleXml);
		return onuCfg;
	}

	private void addOnuStateDataToOnuCfg(String deviceId, OnuCfg onuCfg)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceID", deviceId);
		templateArgs.put("onuTargetList", Arrays.asList(onuCfg.getTarget()));

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_INTERFACE_STATE_ONT_FTL, templateArgs);
		String interfaceEleXml = netconfService.sendRequestToVirtualizerAndParseFirstResultAsXml(rpcRequest,
				interfaceStateXPath());
		String operStatus = getFirstValFromAnv(interfaceEleXml, interfaceXPath("oper-status"));
		String fiberDistance = getFirstValFromAnv(interfaceEleXml, fiberDistanceXPath());
		String lastDownReason = getFirstValFromAnv(interfaceEleXml, lastDownReasonXPath());

		onuCfg.setOperState(parseOperState(onuCfg.getAdminState(), operStatus, lastDownReason));
		onuCfg.setLength(ConvertUtil.mToKm(fiberDistance));
	}

	// UsBw: ontIntent --> speed-profile --> traffic-descriptor-profile
	// DnBw: ontIntent --> speed-profile --> shaper-profile
	private void addOnuBwToOnuCfg(OnuCfg onuCfg)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("ontTarget", onuCfg.getTarget());
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_ONT_CONFIG_FTL, templateArgs);

		String speedProfile = netconfService.sendRequestAndParseFirstValue(rpcRequest, "//ont:ont-speed-profile");
		if (StringUtils.isEmpty(speedProfile)) {
			return;
		}

		// speed-profile
		Map<String, String> xPathMap = new HashMap<String, String>();
		xPathMap.put("shaper-profile-id", "//prof-ont-speed:shaper-profile-id");
		xPathMap.put("traffic-descriptor-profile-id", "//prof-ont-speed:traffic-descriptor-profile-id");
		Profile speedProfileVo = profileManagerService.getProfilesUsedByIntent(speedProfile, "ont", onuCfg.getTarget(),
				RpcConsts.ONT_SPEED_CONTROL_PROFILE_UI, xPathMap);

		// traffic-descriptor-profile
		xPathMap = new HashMap<String, String>();
		xPathMap.put("assured-bandwidth", "//prof-traffic-desc:assured-bandwidth");
		xPathMap.put("fixed-bandwidth", "//prof-traffic-desc:fixed-bandwidth");
		xPathMap.put("maximum-bandwidth", "//prof-traffic-desc:maximum-bandwidth");
		String trafficProfile = speedProfileVo.getSpecificData().get("traffic-descriptor-profile-id");

		if (!StringUtils.isEmpty(trafficProfile)) {
			Profile trafficProfileVo = profileManagerService.getProfilesUsedByIntent(trafficProfile, "ont",
					onuCfg.getTarget(), RpcConsts.TRAFFIC_DESCRIPTOR_PROFILE_UI, xPathMap);
			Map<String, String> specificData = trafficProfileVo.getSpecificData();
			onuCfg.setUsAssuredBw(ConvertUtil.bitToMbit(specificData.get("assured-bandwidth")));
			onuCfg.setUsFixedBw(ConvertUtil.bitToMbit(specificData.get("fixed-bandwidth")));
			onuCfg.setUsMaxBw(ConvertUtil.bitToMbit(specificData.get("maximum-bandwidth")));
		}

		// shaper profile
		xPathMap = new HashMap<String, String>();
		xPathMap.put("pir", "//prof-shaper-profile:pir");
		String shaperProfile = speedProfileVo.getSpecificData().get("shaper-profile-id");
		if (!StringUtils.isEmpty(shaperProfile)) {
			Profile trafficProfileVo = profileManagerService.getProfilesUsedByIntent(shaperProfile, "ont",
					onuCfg.getTarget(), RpcConsts.SHAPER_PROFILE_UI, xPathMap);
			onuCfg.setDsMaxBw(ConvertUtil.kbitToMbit(trafficProfileVo.getSpecificData().get("pir")));
		}
	}

	private String parseAdminState(String enabled) {
		String adminState = "";
		if ("true".equals(enabled)) {
			adminState = "UP";
		} else if ("false".equals(enabled)) {
			adminState = "DOWN";
		}
		return adminState;
	}

	// UP | DOWN | LOS | Power-Off
	private String parseOperState(String adminState, String operStatus, String lastDownReason) {
		String operState = ConvertUtil.getOperState(adminState, operStatus, lastDownReason);
		return "other".equals(operState) ? "DOWN" : operState;
	}

	private void addAuthToOnuCfg(OnuCfg onuCfgVo, String intentEleXml) {
		String mac = getFirstValFromAnv(intentEleXml, xponvaniXPath("expected-serial-number"));
		String pwd = ConvertUtil
				.hexToAscii(getFirstValFromAnv(intentEleXml, xponvaniXPath("expected-registration-id")));
		String loid = getFirstValFromAnv(intentEleXml, xponvaniOnuAuthXPath("expected-loid"));

		if (!StringUtils.isEmpty(mac) && !StringUtils.isEmpty(pwd)) {
			onuCfgVo.setAuthType(NbiConsts.AUTH_SN_PASSWORD);
			onuCfgVo.setAuthInfo(pwd);
		} else if (!StringUtils.isEmpty(mac)) {
			onuCfgVo.setAuthType(NbiConsts.AUTH_MAC);
			onuCfgVo.setAuthInfo(mac);
		} else if (!StringUtils.isEmpty(pwd)) {
			onuCfgVo.setAuthType(NbiConsts.AUTH_PASSWORD);
			onuCfgVo.setAuthInfo(pwd);
		} else if (!StringUtils.isEmpty(loid)) {
			onuCfgVo.setAuthType(NbiConsts.AUTH_LOID);
			onuCfgVo.setAuthInfo(loid);
		}
	}

	private String interfaceXPath() {
		return "//if:interfaces/if:interface";
	}

	private String interfaceXPath(String leaf) {
		return "//if:interface/if:" + leaf;
	}

	private String xponvaniXPath(String leaf) {
		return "//if:interface/bbf-xponvani:v-ani/bbf-xponvani:" + leaf;
	}

	private String xponvaniOnuAuthXPath(String leaf) {
		return "//if:interface/bbf-xponvani:v-ani/bbf-xponvani-onu-auth:" + leaf;
	}

	private String interfaceStateXPath() {
		return "//if:interfaces-state/if:interface";
	}

	private String fiberDistanceXPath() {
		return "//bbf-xponvani:onu-fiber-distance";
	}

	private String lastDownReasonXPath() {
		return "//x-sdan-if-xponvani-aug:onu-last-down-reason";
	}

}
