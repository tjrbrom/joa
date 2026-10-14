package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.NbiConsts;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.ErrorKey;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.service.NetconfRpcBuilder;
import com.x.hhqs.joa.in.service.NetconfService;
import com.x.hhqs.joa.in.util.ConvertUtil;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.vo.OnuState;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("LST-ONUSTATE")
public class LstOnuStateHandler extends AbstractHandler {

	private static final String TITLE = "list of ONU state";

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	@Override
	protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
		String deviceTarget = OssCmdMetaParser.oltTarget(ossCmdMeta);
		String deviceId = deviceTarget + ".LT" + OssCmdMetaParser.slotNo(ossCmdMeta); // FX-TEST1.LT2

		List<OnuState> onuStateList = addOnuConfigDataToOnuState(ossCmdMeta, deviceId);
		if (!onuStateList.isEmpty()) {
			addOnuStateDataToOnuState(deviceId, onuStateList);
		}
		String[][] rtrvArray = createRtrvArray(onuStateList, OnuState.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	private List<OnuState> addOnuConfigDataToOnuState(OssCmdMeta ossCmdMeta,
			String deviceId) throws Rt1QueryException, IOException, DocumentException {
		String rpcRequest = "";
		if (OssCmdMetaParser.isOnuCmd(ossCmdMeta)) {
			Map<String, Object> templateArgs = new HashMap<String, Object>();
			templateArgs.put("deviceId", deviceId);
			templateArgs.put("ontTarget", OssCmdMetaParser.ontTarget(ossCmdMeta));
			rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_ONT_CONFIG_DATA_FTL, templateArgs);
		} else if (OssCmdMetaParser.isPonCmd(ossCmdMeta)) {
			String channelPair = getChannelPairName(deviceId, ossCmdMeta);
			Map<String, Object> templateArgs = new HashMap<String, Object>();
			templateArgs.put("deviceId", deviceId);
			templateArgs.put("channelPair", channelPair);
			rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_ONT_CONFIG_DATA_BY_CPAIR_FTL, templateArgs);
		} else {
			throw new Rt1QueryException(ErrorKey.INVALID_COMMAND);
		}

		List<Element> interfaceEleList = netconfService.sendRequestToVirtualizerAndParseResults(rpcRequest,
				interfaceXPath());
		List<OnuState> onuStateList = new ArrayList<OnuState>();
		interfaceEleList.forEach(interfaceEle -> {
			OnuState onuState = new OnuState();
			String interfaceEleXml = interfaceEle.asXML();
			String enabled = getFirstValFromAnv(interfaceEleXml, interfaceXPath("enabled"));
			onuState.setAdminState(parseAdminState(enabled));
			onuState.setOnuId(getFirstValFromAnv(interfaceEleXml, xponvaniXPath("onu-id")));
			onuState.setTarget(getFirstValFromAnv(interfaceEleXml, interfaceXPath("name")));
			addAuthToOnuState(onuState, interfaceEleXml);
			onuStateList.add(onuState);
		});
		return onuStateList;
	}

	private void addOnuStateDataToOnuState(String deviceId, List<OnuState> onuStateList)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		List<String> onuTargetList = onuStateList.stream().map(onuStateVo -> onuStateVo.getTarget())
				.collect(Collectors.toList());
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceID", deviceId);
		templateArgs.put("onuTargetList", onuTargetList);

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_INTERFACE_STATE_ONT_FTL, templateArgs);
		List<Element> intentEleList = netconfService.sendRequestToVirtualizerAndParseResults(rpcRequest,
				interfaceStateXPath());
		intentEleList.forEach(interfaceEle -> {
			String interfaceEleXml = interfaceEle.asXML();
			String name = getFirstValFromAnv(interfaceEleXml, interfaceXPath("name"));
			String operStatus = getFirstValFromAnv(interfaceEleXml, interfaceXPath("oper-status"));
			String lastChange = getFirstValFromAnv(interfaceEleXml, interfaceXPath("last-change"));
			String fiberDistance = getFirstValFromAnv(interfaceEleXml, fiberDistanceXPath());
			String lastDownReason = getFirstValFromAnv(interfaceEleXml, lastDownReasonXPath());

			OnuState onuState = onuStateList.stream().filter(vo -> vo.getTarget().equals(name)).findFirst().get();
			onuState.setOperState(ConvertUtil.getOperState(onuState.getAdminState(), operStatus, lastDownReason));
			onuState.setLastOffTime(parseLastOffTime(lastChange));
			onuState.setLength(ConvertUtil.mToKm(fiberDistance));
		});
	}

	private String getChannelPairName(String deviceId, OssCmdMeta ossCmdMeta)
			throws IOException, DocumentException, Rt1QueryException {
		String fiberTarget = OssCmdMetaParser.fiberTarget(ossCmdMeta);
		String fiberType = OssCmdMetaParser.fiberType(ossCmdMeta);// GPON | XGS
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceID", deviceId);
		templateArgs.put("channelGroup", fiberTarget);

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_CHANNEL_PAIR_FTL, templateArgs);
		String xPathExpr = channelPartitionNameXPath(deviceId, fiberTarget);
		// <fiberTarget>_CPAIR_GPON | <LS193.200_Fiber>_CPAIR_XGS
		return netconfService.sendRequestToVirtualizerAndParseValues(rpcRequest, xPathExpr).stream()
				.filter(cp -> cp.endsWith("_CPAIR_" + fiberType)).findFirst().get();
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

	// 2024-01-11T13:35:16+00:00 to UTC time
	private String parseLastOffTime(String lastChange) {
		if (StringUtils.isEmpty(lastChange)) {
			return "";
		}

		ZonedDateTime localDateTime = ZonedDateTime.parse(lastChange, DateTimeFormatter.ISO_DATE_TIME);
		ZonedDateTime utcDateTime = localDateTime.withZoneSameInstant(ZoneId.of("UTC"));

		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formattedDateTime = utcDateTime.format(outputFormatter);
		return formattedDateTime;
	}

	private void addAuthToOnuState(OnuState onuStateVo, String intentEleXml) {
		String mac = getFirstValFromAnv(intentEleXml, xponvaniXPath("expected-serial-number"));
		String pwd = ConvertUtil
				.hexToAscii(getFirstValFromAnv(intentEleXml, xponvaniXPath("expected-registration-id")));
		String loid = getFirstValFromAnv(intentEleXml, xponvaniOnuAuthXPath("expected-loid"));

		if (!StringUtils.isEmpty(mac) && !StringUtils.isEmpty(pwd)) {
			onuStateVo.setAuth(NbiConsts.AUTH_SN_PASSWORD);
			onuStateVo.setAuthInfo(pwd);
		} else if (!StringUtils.isEmpty(mac)) {
			onuStateVo.setAuth(NbiConsts.AUTH_MAC);
			onuStateVo.setAuthInfo(mac);
		} else if (!StringUtils.isEmpty(pwd)) {
			onuStateVo.setAuth(NbiConsts.AUTH_PASSWORD);
			onuStateVo.setAuthInfo(pwd);
		} else if (!StringUtils.isEmpty(loid)) {
			onuStateVo.setAuth(NbiConsts.AUTH_LOID);
			onuStateVo.setAuthInfo(loid);
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

	private String channelPartitionNameXPath(String deviceId, String channelGroupName) {
		return "//adh:device[adh:device-id='" + deviceId
				+ "']/adh:device-specific-data/if:interfaces/if:interface[bbf-xpon:channel-pair/bbf-xpon:channel-group-ref='"
				+ channelGroupName + "']/if:name";
	}
}
