package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.service.NetconfRpcBuilder;
import com.x.hhqs.joa.in.service.NetconfService;
import com.x.hhqs.joa.in.service.ProfileManagerService;
import com.x.hhqs.joa.in.service.RestconfService;
import com.x.hhqs.joa.in.util.ConvertUtil;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.vo.LanPort;
import com.x.hhqs.joa.in.vo.Profile;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component("LST-LANPORT")
public class LstLanPortHandler extends AbstractHandler {

	private static final String TITLE = "list of Lan info";

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private RestconfService restconfService;

	@Autowired
	private ProfileManagerService profileManagerService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	@Override
	protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
		List<LanPort> lanPortList = new ArrayList<LanPort>();
		if (OssCmdMetaParser.isOltCmd(ossCmdMeta)) {
			lanPortList = getUplinkLanPort(ossCmdMeta);
		} else {
			lanPortList = getOnuLanPort(ossCmdMeta);
		}
		String[][] rtrvArray = createRtrvArray(lanPortList, LanPort.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	private List<LanPort> getUplinkLanPort(OssCmdMeta ossCmdMeta)
			throws JSONException, Rt1QueryException, IOException, DocumentException {
		List<LanPort> lanPortList = addNtPortIdToLanPort(ossCmdMeta);
		if (!lanPortList.isEmpty()) {
			addNtPortConfigDataToLanPort(ossCmdMeta, lanPortList);
		}

		// ignore ports which only retrieved via netconf, not display on nv
		return lanPortList.stream().filter(
				lanPort -> !StringUtils.isEmpty(lanPort.getAdminStatus()) || !StringUtils.isEmpty(lanPort.getSpeed()))
				.collect(Collectors.toList());
	}

	private List<LanPort> getOnuLanPort(OssCmdMeta ossCmdMeta)
			throws JSONException, Rt1QueryException, IOException, DocumentException {
		List<LanPort> lanPortList = addIbnUniDataToLanPort(ossCmdMeta);
		addNvUniDataToLanPort(ossCmdMeta, lanPortList);
		return lanPortList;
	}

	private List<LanPort> addNtPortIdToLanPort(OssCmdMeta ossCmdMeta)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceId", OssCmdMetaParser.oltTarget(ossCmdMeta));
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_NT_PORT_FTL, templateArgs);
		List<Element> ntPortEleList = netconfService.sendRequestToVirtualizerAndParseResults(rpcRequest, ntPortXPath());

		return ntPortEleList.stream().map(ntPortEle -> {
			String ntPortEleXml = ntPortEle.asXML();
			String ntId = getFirstValFromAnv(ntPortEleXml, ntPortXPath("port-id")); // 1/6/c1/1
			String portName = getFirstValFromAnv(ntPortEleXml, ntPortXPath("port-name")); // ntio-1:qsfp:1-1
			if (!isValidPortName(portName)) {
				return null;
			}
			String portId = portNameToPortId(portName); // NA-NA-NTIO1-11
			if (OssCmdMetaParser.isPortIdCmd(ossCmdMeta) && !portId.equals("NA-NA-" + OssCmdMetaParser.portSlotNo(ossCmdMeta) + "-"
					+ OssCmdMetaParser.portPortNo(ossCmdMeta))) {
				return null;
			}

			LanPort lanPort = new LanPort();
			lanPort.setOltId(OssCmdMetaParser.oltId(ossCmdMeta));
			lanPort.setPortId(portId);
			lanPort.setNtId(ntId);
			return lanPort;
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	private void addNtPortConfigDataToLanPort(OssCmdMeta ossCmdMeta, List<LanPort> lanPortList)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		List<String> ntIdList = lanPortList.stream().map(lanPort -> lanPort.getNtId()).collect(Collectors.toList());
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceId", OssCmdMetaParser.oltTarget(ossCmdMeta));
		templateArgs.put("ntIdList", ntIdList);
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_NT_PORT_CONFIG_DATA_FTL, templateArgs);
		List<Element> ntPortEleList = netconfService.sendRequestToVirtualizerAndParseResults(rpcRequest,
				ntPortConfigureXPath());

		ntPortEleList.forEach(ntPortEle -> {
			String ntPortEleXml = ntPortEle.asXML();
			String portId = getFirstValFromAnv(ntPortEleXml, ntPortConfigureXPath("port-id"));

			lanPortList.stream().filter(lanPort -> {
				return lanPort.getNtId().equals(portId);
			}).findFirst().ifPresent(lanPort -> {
				lanPort.setAdminStatus(
						parseUplinkAdminState(getFirstValFromAnv(ntPortEleXml, ntPortConfigureXPath("admin-state"))));
				String autoNegotiate = getFirstValFromAnv(ntPortEleXml, ntPortConfigureXPath("autonegotiate"));
				String speed = getFirstValFromAnv(ntPortEleXml, ntPortConfigureXPath("speed"));
				lanPort.setSpeed(parseUplinkSpeed(autoNegotiate, speed));
			});
		});
	}

	private List<LanPort> addIbnUniDataToLanPort(OssCmdMeta ossCmdMeta)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		String ontTarget = OssCmdMetaParser.ontTarget(ossCmdMeta);
		templateArgs.put("ontTarget", ontTarget);
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_ONT_LANPORT_FTL, templateArgs);
		List<Element> ontUniEleList = netconfService.sendRequestAndParseResults(rpcRequest, ontUniXPath());

		List<LanPort> lanPortList = new ArrayList<LanPort>();
		Map<String, String> usBwMapper = new HashMap<String, String>();
		Map<String, String> dsBwMapper = new HashMap<String, String>();
		for (Iterator<Element> iterator = ontUniEleList.iterator(); iterator.hasNext();) {
			Element ontUniEle = (Element) iterator.next();
			String ontUniEleXml = ontUniEle.asXML();
			String uniId = getFirstValFromIbn(ontUniEleXml, ontUniXPath("uni-id"));
			if (!uniId.startsWith("LAN")) {
				continue;
			}
			String lanId = uniId.replace("LAN", "");
			if (OssCmdMetaParser.isOnuPortIdCmd(ossCmdMeta) && !lanId.equals(OssCmdMetaParser.portPortNo(ossCmdMeta))) {
				continue;
			}

			LanPort lanPort = new LanPort();
			lanPortList.add(lanPort);
			lanPort.setOltId(OssCmdMetaParser.oltId(ossCmdMeta));
			lanPort.setPonId(OssCmdMetaParser.ponId(ossCmdMeta));
			lanPort.setOnuId(OssCmdMetaParser.onuId(ossCmdMeta));
			lanPort.setPortId("NA-NA-1-" + lanId);
			lanPort.setUniId("ENET_" + uniId);
			lanPort.setVenetId("VENET_" + ConvertUtil.encodeURI(ontTarget) + "_" + uniId);
			lanPort.setTarget(ontTarget);
			lanPort.setLanId(uniId);
			lanPort.setOnuIp(OssCmdMetaParser.onuIp(ossCmdMeta));

			String speedProfile = getFirstValFromIbn(ontUniEleXml, ontUniXPath("uni-speed-profile"));
			addBwToLanPort(lanPort, ontTarget, speedProfile, usBwMapper, dsBwMapper);
		}
		return lanPortList;
	}

	private void addBwToLanPort(LanPort lanPort, String ontTarget, String speedProfile, Map<String, String> usBwMapper,
			Map<String, String> dsBwMapper) throws JSONException, Rt1QueryException, IOException, DocumentException {
		if (StringUtils.isEmpty(speedProfile)) {
			return;
		}
		if (usBwMapper.containsKey(speedProfile)) {
			lanPort.setRateLimitUs(usBwMapper.get(speedProfile));
			lanPort.setRateLimitDs(dsBwMapper.get(speedProfile));
			return;
		}

		// speed-profile
		Map<String, String> xPathMap = new HashMap<String, String>();
		xPathMap.put("shaper-profile-id", "//prof-ont-speed:shaper-profile-id");
		xPathMap.put("traffic-descriptor-profile-id", "//prof-ont-speed:traffic-descriptor-profile-id");
		Profile speedProfileVo = profileManagerService.getProfilesUsedByIntent(speedProfile, "ont", ontTarget,
				RpcConsts.ONT_SPEED_CONTROL_PROFILE_UI, xPathMap);

		// traffic-descriptor-profile
		xPathMap = new HashMap<String, String>();
		xPathMap.put("maximum-bandwidth", "//prof-traffic-desc:maximum-bandwidth");
		String trafficProfile = speedProfileVo.getSpecificData().get("traffic-descriptor-profile-id");

		if (!StringUtils.isEmpty(trafficProfile)) {
			Profile trafficProfileVo = profileManagerService.getProfilesUsedByIntent(trafficProfile, "ont", ontTarget,
					RpcConsts.TRAFFIC_DESCRIPTOR_PROFILE_UI, xPathMap);
			Map<String, String> specificData = trafficProfileVo.getSpecificData();
			String rateLimitUs = ConvertUtil.bitToKbit(specificData.get("maximum-bandwidth"));
			lanPort.setRateLimitUs(rateLimitUs);
			usBwMapper.put(speedProfile, rateLimitUs);
		}

		// shaper profile
		xPathMap = new HashMap<String, String>();
		xPathMap.put("pir", "//prof-shaper-profile:pir");
		String shaperProfile = speedProfileVo.getSpecificData().get("shaper-profile-id");
		if (!StringUtils.isEmpty(shaperProfile)) {
			Profile trafficProfileVo = profileManagerService.getProfilesUsedByIntent(shaperProfile, "ont", ontTarget,
					RpcConsts.SHAPER_PROFILE_UI, xPathMap);
			String rateLimitDs = trafficProfileVo.getSpecificData().get("pir"); // kbit
			lanPort.setRateLimitDs(rateLimitDs);
			dsBwMapper.put(speedProfile, rateLimitDs);
		}
	}

	private void addNvUniDataToLanPort(OssCmdMeta ossCmdMeta, List<LanPort> lanPortList)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		if (CollectionUtils.isEmpty(lanPortList)) {
			return;
		}

		String deviceName = OssCmdMetaParser.oltTarget(ossCmdMeta) + ".LT" + OssCmdMetaParser.slotNo(ossCmdMeta); // FX-TEST1.LT2
		String ontTarget = OssCmdMetaParser.ontTarget(ossCmdMeta);
		addLanSpeedToLanPort(deviceName, ontTarget, lanPortList);
		addLanStatusToLanPort(deviceName, ontTarget, lanPortList);
	}

	private void addLanSpeedToLanPort(String deviceName, String ontTarget, List<LanPort> lanPortList)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceID", deviceName);
		templateArgs.put("ontTarget", ontTarget);
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_ONT_TEMPLATE_NAME_FTL, templateArgs);
		String ontTemplateName = netconfService.sendRequestToVirtualizerAndParseFirstValue(rpcRequest,
				ontTemplateXPath());

		if (StringUtils.isEmpty(ontTemplateName)) {
			return;
		}

		List<String> uniIdList = lanPortList.stream().map(lanPort -> lanPort.getUniId()).collect(Collectors.toList());
		Map<String, Object> lanSpeedTemplateArgs = new HashMap<String, Object>();
		lanSpeedTemplateArgs.put("deviceID", deviceName);
		lanSpeedTemplateArgs.put("ontTemplateName", ontTemplateName);
		lanSpeedTemplateArgs.put("uniIdList", uniIdList);
		String lanSpeedRpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_LAN_SPEED_BY_TEMPLATE_NAME_FTL,
				lanSpeedTemplateArgs);
		List<Element> ifMountedEleList = netconfService.sendRequestToVirtualizerAndParseResults(lanSpeedRpcRequest,
				ifMountedXpath("interface"));

		ifMountedEleList.forEach(ifMountedEle -> {
			String ifMountedEleXml = ifMountedEle.asXML();
			String name = getFirstValFromAnv(ifMountedEleXml, ifMountedXpath("name"));
			Optional<LanPort> optionalLanPort = lanPortList.stream().filter(lanPort -> lanPort.getUniId().equals(name))
					.findFirst();
			if (optionalLanPort.isPresent()) {
				LanPort lanPort = optionalLanPort.get();
				lanPort.setSpeed(parseSpeed(getFirstValFromAnv(ifMountedEleXml, ethMountedXpath("speed"))));
				lanPort.setDuplex(parseDuplex(getFirstValFromAnv(ifMountedEleXml, ethMountedXpath("duplex"))));
			}
		});
	}

	private void addLanStatusToLanPort(String deviceName, String ontTarget, List<LanPort> lanPortList)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		for (Iterator<LanPort> iterator = lanPortList.iterator(); iterator.hasNext();) {
			LanPort lanPort = (LanPort) iterator.next();
			// /rest/ema/entity/LS-FX-MF-SF-LT/UNI/realMF14_193.201.LT3/VENET_ALCLF8AFFD92_LAN1?fetchDeviceAttributes=false&isChild=false&isOne=false&forCondition=false
			String adminStatus = restconfService.getEmaInstance(RpcConsts.lsMfAndSfModelName("LT"), "UNI", deviceName,
					lanPort.getVenetId(), RpcConsts.NOT_FETCH_DEVICE_REQUEST, "adminStatus");
			lanPort.setAdminStatus(parseStatus(adminStatus));
		}
	}

	// uni port duplex
	private String parseDuplex(String input) {
		String duplex = "";
		if ("full".equals(input)) {
			duplex = "Full";
		} else if ("half".equals(input)) {
			duplex = "Half";
		}
		return duplex;
	}

	// uni port speed
	private String parseSpeed(String input) {
		String speed = "";
		if ("eth-mounted:eth-if-speed-10mb".equals(input)) {
			speed = "10M";
		} else if ("eth-mounted:eth-if-speed-100mb".equals(input)) {
			speed = "100M";
		} else if ("eth-mounted:eth-if-speed-1gb".equals(input)) {
			speed = "1000M";
		} else if ("eth-mounted:eth-if-speed-10gb".equals(input)) {
			speed = "10000M";
		}
		return speed;
	}

	/**
	 * uplink port speed
	 * 
	 * <li>Auto: autonegotiate is true|limited and speed is null</li>
	 * <li>10M: autonegotiate is false|null and speed is 10</li>
	 * <li>100M: autonegotiate is false|null and speed is 100</li>
	 * <li>1000M: autonegotiate is false|null and speed is 1000</li>
	 * <li>10000M: autonegotiate is false|null and speed is 10000</li>
	 * <li>Auto-10M: autonegotiate is true|limited and speed is 10</li>
	 * <li>Auto-100M: autonegotiate is true|limited and speed is 100</li>
	 * <li>Auto-1000M: autonegotiate is true|limited and speed is 1000</li>
	 * <li>Auto-10000M: autonegotiate is true|limited and speed is 10000</li>
	 * 
	 */
	private String parseUplinkSpeed(String autonegotiate, String speed) {
		String result = "";
		boolean isAutoNegotiate = "true".equalsIgnoreCase(autonegotiate) || "limited".equalsIgnoreCase(autonegotiate);
		if (isAutoNegotiate && StringUtils.isEmpty(speed)) {
			result = "Auto";
		} else if ("10".equals(speed) || "100".equals(speed) || "1000".equals(speed) || "10000".equals(speed)) {
			if (isAutoNegotiate) {
				result = "Auto-" + speed + "M";
			} else {
				result = speed + "M";
			}
		}
		return result;
	}

	// uni port admin status
	private String parseStatus(String input) {
		String status = "";
		if ("UNLOCKED".equals(input)) {
			status = "UP";
		} else if ("LOCKED".equals(input)) {
			status = "DOWN";
		}
		return status;
	}

	// uplink port admin status
	private String parseUplinkAdminState(String input) {
		String status = "";
		if ("enable".equals(input)) {
			status = "UP";
		} else if ("disable".equals(input)) {
			status = "DOWN";
		}
		return status;
	}

	/**
	 * 
	 * @param portName
	 *                 <li>nt-a:sfp:1|nt-a:xfp:1 --> NA-NA-NTA-1</li>
	 *                 <li>nt-b:sfp:1|nt-b:xfp:1 --> NA-NA-NTB-1</li>
	 *                 <li>ntio-1:sfp:1|ntio-1:xfp:1-->NA-NA-NTIO1-1</li>
	 *                 <li>ntio-2:sfp:1|ntio-2:xfp:1-->NA-NA-NTIO2-1</li>
	 *                 <li>nt-a:qsfp:1-1 --> NA-NA-NTA-11</li>
	 *                 <li>nt-b:qsfp:1-1 --> NA-NA-NTB-11</li>
	 *                 <li>ntio-1:qsfp:1-1 --> NA-NA-NTIO1-11</li>
	 *                 <li>ntio-2:qsfp:1-1 --> NA-NA-NTIO2-11</li>
	 *                 <li>nt-a:cfp:1 --> NA-NA-NTA-101</li>
	 *                 <li>nt-b:cfp:1 --> NA-NA-NTB-101</li>
	 *                 <li>ntio-1:cfp:1 --> NA-NA-NTIO1-101</li>
	 *                 <li>ntio-2:cfp:1 --> NA-NA-NTIO2-101</li>
	 * @return
	 */
	private String portNameToPortId(String portName) {
		String[] portNames = portName.split(":");
		String ntName = portNames[0].replace("-", "").toUpperCase();
		String ntId = portNames[2].replace("-", "");
		if ("cfp".equals(portNames[1])) {
			ntId = String.valueOf(Integer.parseInt(ntId) + 100);
		}
		return "NA-NA-" + ntName + "-" + ntId;
	}

	/**
	 * 
	 * @param portName
	 *                 <li>nt-a:sfp:<>,nt-a:xfp:<>,nt-a:cfp:<>,nt-a:qsfp:<>-<></li>
	 *                 <li>nt-b:sfp:<>,nt-b:xfp:<>,nt-b:cfp:<>,nt-b:qsfp:<>-<></li>
	 *                 <li>ntio-1:sfp:<>,ntio-1:xfp:<>,ntio-1:cfp:<>,ntio-1:qsfp:<>-<></li>
	 *                 <li>ntio-2:sfp:<>,ntio-2:xfp:<>,ntio-2:cfp:<>,ntio-2:qsfp:<>-<></li>
	 * 
	 * @return
	 */
	private boolean isValidPortName(String portName) {
		if (StringUtils.isEmpty(portName)) {
			return false;
		}
		String validPattern = "^(nt-a|nt-b|ntio-1|ntio-2):(qsfp:\\d+-\\d+|xfp:\\d+|cfp:\\d+|sfp:\\d+)$";
		return portName.matches(validPattern);
	}

	private String ntPortXPath() {
		return "//state:port";
	}

	private String ntPortXPath(String leaf) {
		return "//state:" + leaf;
	}

	private String ntPortConfigureXPath() {
		return "//conf:port";
	}

	private String ntPortConfigureXPath(String leaf) {
		return "//conf:" + leaf;
	}

	private String ontUniXPath() {
		return "//ont:uni-service-configuration";
	}

	private String ontUniXPath(String leaf) {
		return "//ont:" + leaf;
	}

	private String ontStateXPath(String leaf) {
		return "//ont:ont-state/ont:" + leaf;
	}

	private String ontTemplateXPath() {
		return "//onus-from-template:template-ref";
	}

	private String ifMountedXpath(String leaf) {
		return "//if-mounted:" + leaf;
	}

	private String ethMountedXpath(String leaf) {
		return "//eth-mounted:" + leaf;
	}
}
