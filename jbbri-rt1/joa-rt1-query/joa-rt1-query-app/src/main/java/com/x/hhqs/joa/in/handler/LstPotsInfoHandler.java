package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.service.NetconfRpcBuilder;
import com.x.hhqs.joa.in.service.NetconfService;
import com.x.hhqs.joa.in.service.ProfileManagerService;
import com.x.hhqs.joa.in.util.ConvertUtil;
import com.x.hhqs.joa.in.util.CryptoUtil;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.vo.PotsInfo;
import com.x.hhqs.joa.in.vo.Profile;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component("LST-POTSINFO")
public class LstPotsInfoHandler extends AbstractHandler {

	private static final String TITLE = "list of Pots info";

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private ProfileManagerService profileManagerService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	@Autowired
	private CryptoUtil cryptoUtil;

	@Override
	protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
		List<PotsInfo> potsInfoList = addVoipConfigDataToPotsInfo(ossCmdMeta);
		if (!potsInfoList.isEmpty()) {
			addVoipStateDataToPotsInfo(ossCmdMeta, potsInfoList);
			addSipAgentProfileToPotsInfo(ossCmdMeta, potsInfoList);
		}
		String[][] rtrvArray = createRtrvArray(potsInfoList, PotsInfo.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	private List<PotsInfo> addVoipConfigDataToPotsInfo(OssCmdMeta ossCmdMeta)
			throws Rt1QueryException, IOException, DocumentException {
		String deviceId = OssCmdMetaParser.oltTarget(ossCmdMeta) + ".LT" + OssCmdMetaParser.slotNo(ossCmdMeta); // FX-TEST1.LT2
		String voipUniId = "POTS_TEL" + OssCmdMetaParser.onuPortNo(ossCmdMeta); // POTS_TEL1
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceId", deviceId);
		templateArgs.put("ontTarget", OssCmdMetaParser.ontTarget(ossCmdMeta));
		templateArgs.put("voipUniId", voipUniId);

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_VOIP_CONFIG_DATA_FTL, templateArgs);
		List<Element> sipTermEleList = netconfService.sendRequestToVirtualizerAndParseResults(rpcRequest,
				sipTerminationXPath());

		List<PotsInfo> potsInfoList = new ArrayList<PotsInfo>();
		for (Iterator<Element> iterator = sipTermEleList.iterator(); iterator.hasNext();) {
			String sipTermEleXml = iterator.next().asXML();
			String digestPassword = getFirstValFromAnv(sipTermEleXml, onusFromTemplateXPath("digest-password"));

			PotsInfo potsInfo = new PotsInfo();
			potsInfoList.add(potsInfo);
			potsInfo.setVoipUniId(getFirstValFromAnv(sipTermEleXml, onusFromTemplateXPath("name")));
			potsInfo.setSipUserName(getFirstValFromAnv(sipTermEleXml, onusFromTemplateXPath("auth-user-name")));
			potsInfo.setSipUserPwd(cryptoUtil.decrypt(digestPassword));
			potsInfo.setPn(getFirstValFromAnv(sipTermEleXml, onusFromTemplateXPath("sip-uni-display-name")));
		}
		return potsInfoList;
	}

	private void addVoipStateDataToPotsInfo(OssCmdMeta ossCmdMeta, List<PotsInfo> potsInfoList)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		String deviceId = OssCmdMetaParser.oltTarget(ossCmdMeta) + ".LT" + OssCmdMetaParser.slotNo(ossCmdMeta);
		List<String> voipUniIdList = potsInfoList.stream().map(potsInfo -> potsInfo.getVoipUniId())
				.collect(Collectors.toList());
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceId", deviceId);
		templateArgs.put("ontTarget", OssCmdMetaParser.ontTarget(ossCmdMeta));
		templateArgs.put("voipUniIdList", voipUniIdList);

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_VOIP_STATE_DATA_FTL, templateArgs);
		List<Element> onuEleList = netconfService.sendRequestToVirtualizerAndParseResults(rpcRequest, onuXPath());

		for (Iterator<Element> iterator = onuEleList.iterator(); iterator.hasNext();) {
			String onuEleXml = iterator.next().asXML();
			String voipUni = getFirstValFromAnv(onuEleXml, interfaceXPath("name"));
			potsInfoList.stream().filter(pots -> pots.getVoipUniId().equals(voipUni)).findFirst()
					.ifPresent(potsInfo -> {
						String callState = getFirstValFromAnv(onuEleXml,
								sipSignalingSipTermXPath("sip-termination-call-state"));
						String registerState = getFirstValFromAnv(onuEleXml,
								sipSignalingSipTermXPath("sip-termination-registration-state"));

						potsInfo.setLineState(parseLineState(callState, registerState));
						potsInfo.setServiceState(
								parseServiceState(getFirstValFromAnv(onuEleXml, interfaceXPath("oper-status"))));
					});
		}
	}

	private void addSipAgentProfileToPotsInfo(OssCmdMeta ossCmdMeta, List<PotsInfo> potsInfoList)
			throws JSONException, Rt1QueryException, IOException, DocumentException {
		Map<String, String> xPathMap = new HashMap<String, String>();
		xPathMap.put("rx-gain", "//prof-sip-agent:rx-gain");
		xPathMap.put("tx-gain", "//prof-sip-agent:tx-gain");
		xPathMap.put("echo-cancel-enabled", "//prof-sip-agent:echo-cancel-enabled");
		Profile sipAgentProfileVo = profileManagerService.getProfilesUsedByIntent("ont",
				OssCmdMetaParser.ontTarget(ossCmdMeta), RpcConsts.SIP_AGENT_PROFILE_UI, xPathMap);

		potsInfoList.forEach(potsInfo -> {
			potsInfo.setRxGain(parseGain(sipAgentProfileVo.getSpecificData().get("rx-gain")));
			potsInfo.setTxGain(parseGain(sipAgentProfileVo.getSpecificData().get("tx-gain")));
			potsInfo.setEchoCancel(getEchoCancel(sipAgentProfileVo.getSpecificData().get("echo-cancel-enabled")));
		});
	}

	private String parseGain(String input) {
		return ConvertUtil.divideByGiven(input, "10");
	}

	private String getEchoCancel(String input) {
		String echoCancel = "";
		if ("enabled".equals(input)) {
			echoCancel = "Enable";
		} else if ("disabled".equals(input)) {
			echoCancel = "Disable";
		}
		return echoCancel;
	}

	private String parseLineState(String callState, String registerState) {
		String lineState = "Other";
		if ("onhook".equals(callState)) {
			lineState = "Idle";
		} else if ("offhook-dialtone".equals(callState)) {
			lineState = "Off-hook";
		} else if ("dialing".equals(callState)) {
			lineState = "Dialing";
		} else if ("play-ring-tone".equals(callState)) {
			lineState = "Ringing";
		} else if ("play-ringback-tone".equals(callState)) {
			lineState = "Ringing-back";
		} else if ("connecting".equals(callState)) {
			lineState = "Connecting";
		} else if ("connected".equals(callState)) {
			lineState = "Connected";
		} else if ("releasing".equals(callState)) {
			lineState = "Releasing";
		} else if ("initial".equals(registerState) || "registered".equals(registerState)) {
			lineState = "Registering";
		} else if ("reg-fail-auth".equals(registerState) || "reg-fail-timeout".equals(registerState)
				|| "reg-fail-server".equals(registerState)) {
			lineState = "Register-failed";
		}
		return lineState;
	}

	private String parseServiceState(String operStatus) {
		String serviceState = "";
		if ("up".equals(operStatus)) {
			serviceState = "Normal";
		} else if ("down".equals(operStatus)) {
			serviceState = "EndLocal";
		}
		return serviceState;
	}

	private String sipTerminationXPath() {
		return "//onus-from-template:sip-termination";
	}

	private String onusFromTemplateXPath(String leaf) {
		return "//onus-from-template:" + leaf;
	}

	private String onuXPath() {
		return "//onu:onu";
	}

	private String interfaceXPath(String leaf) {
		return "//interfaces-state:" + leaf;
	}

	private String sipSignalingSipTermXPath(String leaf) {
		return "//sip-signaling-state:" + leaf;
	}
}
