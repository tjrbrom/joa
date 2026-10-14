package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.service.NetconfRpcBuilder;
import com.x.hhqs.joa.in.service.NetconfService;
import com.x.hhqs.joa.in.service.ProfileManagerService;
import com.x.hhqs.joa.in.util.CryptoUtil;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.vo.MgCfg;
import com.x.hhqs.joa.in.vo.Profile;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("LST-MGCFG")
public class LstMgCfgHandler extends AbstractHandler {

	private static final String TITLE = "list of MG port configuration";

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
		List<MgCfg> mgCfgList = new ArrayList<MgCfg>();
		Map<String, String> sipAgentMap = getSipAgentMap(ossCmdMeta);

		if (!sipAgentMap.isEmpty()) {
			addSipUserToMgCfgList(mgCfgList, sipAgentMap, ossCmdMeta);
			addL2InfraToMgCfgList(mgCfgList);
			addSipAgentServiceProfileToIptvList(mgCfgList, ossCmdMeta);
		}

		String[][] rtrvArray = createRtrvArray(mgCfgList, MgCfg.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	private Map<String, String> getSipAgentMap(OssCmdMeta ossCmdMeta)
			throws IOException, DocumentException, Rt1QueryException {
		Map<String, String> sipAgentMap = new HashMap<String, String>();
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("ontTarget", OssCmdMetaParser.ontTarget(ossCmdMeta));
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_SIP_AGENT_FTL, templateArgs);

		netconfService.sendRequestAndParseResults(rpcRequest, intentXPath()).stream().findFirst()
				.ifPresent(sipAgentEle -> {
					String sipAgentEleXml = sipAgentEle.asXML();
					String voipL2InfraName = getFirstValFromIbn(sipAgentEleXml, sipAgentXPath("voip-l2-infra-name"));
					sipAgentMap.put("ip-address", getFirstValFromIbn(sipAgentEleXml, sipAgentXPath("ip-address")));
					sipAgentMap.put("netmask", getFirstValFromIbn(sipAgentEleXml, sipAgentXPath("netmask")));
					sipAgentMap.put("service-profile",
							getFirstValFromIbn(sipAgentEleXml, sipAgentXPath("service-profile")));
					sipAgentMap.put("l2InfraTarget", OssCmdMetaParser.oltTarget(ossCmdMeta) + "#" + voipL2InfraName);
					sipAgentMap.put("sipAgentTarget", getElementVal(sipAgentEle, "target"));
				});
		return sipAgentMap;
	}

	private void addSipUserToMgCfgList(List<MgCfg> mgCfgList, Map<String, String> sipAgentMap, OssCmdMeta ossCmdMeta)
			throws IOException, DocumentException, Rt1QueryException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("ontTarget", OssCmdMetaParser.ontTarget(ossCmdMeta));
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_SIP_USER_FTL, templateArgs);

		List<Element> sipUserEleList = netconfService.sendRequestAndParseResults(rpcRequest, intentXPath());
		if (sipUserEleList.isEmpty()) {
			mgCfgList.add(createMgCfgBySipAgentMap(sipAgentMap));
		} else {
			sipUserEleList.forEach(sipUserEle -> {
				// only one sipAgent on each ont
				MgCfg mgCfg = createMgCfgBySipAgentMap(sipAgentMap);
				mgCfgList.add(mgCfg);

				// multiple sipUser on each ont
				String sipUserEleXml = sipUserEle.asXML();
				String sipPassword = getFirstValFromIbn(sipUserEleXml, sipUserXPath("sip-password"));
				mgCfg.setUni(getFirstValFromIbn(sipUserEleXml, sipUserXPath("uni-id")));
				mgCfg.setSipRegDm(getFirstValFromIbn(sipUserEleXml, sipUserXPath("sip-address-of-record")));
				mgCfg.setPppoeUser(getFirstValFromIbn(sipUserEleXml, sipUserXPath("sip-user-name")));
				mgCfg.setPPPOEPWD(cryptoUtil.decrypt(sipPassword));
				mgCfg.setSipUserTarget(getElementVal(sipUserEle, "target"));
			});
		}
	}

	private void addL2InfraToMgCfgList(List<MgCfg> mgCfgList) throws DocumentException, IOException, Rt1QueryException {
		// voip l2 infra is configured on sipAgent, it should be the same one.
		List<String> l2InfraTargetList = mgCfgList.stream().map(mgCfg -> mgCfg.getL2InfraTarget()).distinct()
				.collect(Collectors.toList());

		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("l2InfraTargetList", l2InfraTargetList);
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_L2_INFRA_FTL, templateArgs);
		Element l2InfraEle = netconfService.sendRequestAndParseFirstResult(rpcRequest, l2InfraXPath());

		mgCfgList.forEach(mgCfg -> {
			mgCfg.setSvlan(getElementVal(l2InfraEle, "s-vlan-id"));
			mgCfg.setVoipVlan(getElementVal(l2InfraEle, "c-vlan-id"));
		});
	}

	private void addSipAgentServiceProfileToIptvList(List<MgCfg> mgCfgList, OssCmdMeta ossCmdMeta)
			throws JSONException, Rt1QueryException, IOException, DocumentException {
		// service profile is configured on sipAgent, it should be the same one.
		String serviceProfile = mgCfgList.stream().map(mgCfg -> mgCfg.getServiceProfile()).findFirst().get();

		Map<String, String> xPathMap = new HashMap<String, String>();
		xPathMap.put("gateway-address", "//prof-voip-agent:gateway-address");
		xPathMap.put("ipaddress-acquisition", "//prof-voip-agent:ipaddress-acquisition");
		xPathMap.put("registrar-proxy-address", "//prof-voip-agent:registrar-proxy-address");
		xPathMap.put("user-agent-proxy-address", "//prof-voip-agent:user-agent-proxy-address");
		Profile onuTemplateProfileVo = profileManagerService.getProfilesUsedByIntent(serviceProfile,
				"standard-voip-sip-agent", OssCmdMetaParser.ontTarget(ossCmdMeta),
				RpcConsts.SIP_AGENT_SERVICE_PROFILE_UI, xPathMap);

		mgCfgList.forEach(mgCfg -> {
			mgCfg.setIpGateWay(onuTemplateProfileVo.getSpecificData().get("gateway-address"));
			mgCfg.setIpMode(getIpMode(onuTemplateProfileVo.getSpecificData().get("ipaddress-acquisition")));
			mgCfg.setMgcIp1(onuTemplateProfileVo.getSpecificData().get("registrar-proxy-address"));
			mgCfg.setMgcIp2(onuTemplateProfileVo.getSpecificData().get("user-agent-proxy-address"));
		});
	}

	private MgCfg createMgCfgBySipAgentMap(Map<String, String> sipAgentMap) {
		MgCfg mgCfg = new MgCfg();
		mgCfg.setPt("SIP");
		mgCfg.setIpAddress(sipAgentMap.get("ip-address"));
		mgCfg.setIpMask(sipAgentMap.get("netmask"));
		mgCfg.setServiceProfile(sipAgentMap.get("service-profile"));
		mgCfg.setL2InfraTarget(sipAgentMap.get("l2InfraTarget"));
		mgCfg.setSipAgentTarget(sipAgentMap.get("sipAgentTarget"));
		return mgCfg;
	}

	private String getIpMode(String input) {
		String ipMode = "";
		if ("dhcp".equals(input)) {
			ipMode = "DHCP";
		} else if ("static".equals(input)) {
			ipMode = "STATIC";
		}
		return ipMode;
	}

	private String sipUserXPath(String leaf) {
		return "//svsipu:" + leaf;
	}

	private String sipAgentXPath(String leaf) {
		return "//svsipua:" + leaf;
	}

	private String l2InfraXPath() {
		return "//l2infra:l2-infra";
	}

	private String intentXPath() {
		return "//ibn:intent";
	}
}
