package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.service.NetconfRpcBuilder;
import com.x.hhqs.joa.in.service.NetconfService;
import com.x.hhqs.joa.in.service.RestconfService;
import com.x.hhqs.joa.in.util.ConvertUtil;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.vo.OnuLanInfo;
import org.dom4j.DocumentException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Component("LST-ONULANINFO")
public class LstOnuLanInfoHandler extends AbstractHandler {

	private static final String TITLE = "list of lan port info";

	// the path to parse the JSON object
	public static final String OPERATION_STATE_PATH = "operationState";
	
	public static final String ADMIN_STATUS_PATH = "adminStatus";

	public static final String CONFIGURED_MODE_PATH = "extraAttributes/configured-mode";

	public static final String CONFIGURED_SPEED_PATH = "extraAttributes/configured-speed";

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	@Autowired
	private RestconfService restconfService;

	@Override
	protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
		String deviceName = OssCmdMetaParser.oltTarget(ossCmdMeta) + ".LT" + OssCmdMetaParser.slotNo(ossCmdMeta); // realMF14_193.201.LT3
		List<OnuLanInfo> onuLanInfoList = new ArrayList<OnuLanInfo>();

		if (onuUniIsExist(ossCmdMeta)) {
			String instanceName = getInstanceName(ossCmdMeta); // VENET_Gont-ALCLF8AFFD92_LAN1

			// /rest/ema/entity/LS-FX-MF-SF-LT/UNI/realMF14_193.201.LT3/VENET_Gont-ALCLF8AFFD92_LAN1?fetchDeviceAttributes=true&isChild=false&isOne=false&forCondition=false
			Map<String, String> emaInstanceMap = restconfService.getEmaInstance(RpcConsts.lsMfAndSfModelName("LT"),
					"UNI", deviceName, instanceName, RpcConsts.FETCH_DEVICE_REQUEST,
					Arrays.asList(OPERATION_STATE_PATH, CONFIGURED_MODE_PATH, CONFIGURED_SPEED_PATH));
			OnuLanInfo onuLanInfo = new OnuLanInfo();
			onuLanInfoList.add(onuLanInfo);
			onuLanInfo.setOperStatus(emaInstanceMap.get(OPERATION_STATE_PATH));
			onuLanInfo.setTarget(OssCmdMetaParser.ontTarget(ossCmdMeta));
			onuLanInfo.setLanId(ConvertUtil.onuPortToUniId(ossCmdMeta)); // LAN1 | VEIP

			// /rest/ema/entity/LS-FX-MF-SF-LT/UNI/realMF14_193.201.LT3/VENET_Gont-ALCLF8AFFD92_LAN1?fetchDeviceAttributes=false&isChild=false&isOne=false
			emaInstanceMap = restconfService.getEmaInstance(RpcConsts.lsMfAndSfModelName("LT"), "UNI", deviceName,
					instanceName, RpcConsts.NOT_FETCH_DEVICE_REQUEST, Arrays.asList(ADMIN_STATUS_PATH, CONFIGURED_MODE_PATH, CONFIGURED_SPEED_PATH));
			onuLanInfo.setAdminStatus(getAdminState(emaInstanceMap.get(ADMIN_STATUS_PATH)));
			onuLanInfo.setSpeed(parseSpeed(emaInstanceMap.get(CONFIGURED_SPEED_PATH)));
			onuLanInfo.setDuplex(parseDuplex(emaInstanceMap.get(CONFIGURED_MODE_PATH)));
		}

		String[][] rtrvArray = createRtrvArray(onuLanInfoList, OnuLanInfo.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	private boolean onuUniIsExist(OssCmdMeta ossCmdMeta)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("ontTarget", OssCmdMetaParser.ontTarget(ossCmdMeta));
		templateArgs.put("uniId", ConvertUtil.onuPortToUniId(ossCmdMeta));
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_ONT_LANPORT_FTL, templateArgs);
		String ontUni = netconfService.sendRequestAndParseFirstValue(rpcRequest, ontUniIdXPath());
		return !StringUtils.isEmpty(ontUni);
	}

	// VENET_<ONT target>_<UNI id>
	private String getInstanceName(OssCmdMeta ossCmdMeta) throws UnsupportedEncodingException {
		return "VENET_" + ConvertUtil.encodeURI(OssCmdMetaParser.ontTarget(ossCmdMeta)) + "_"
				+ ConvertUtil.onuPortToUniId(ossCmdMeta);
	}

	private String getAdminState(String adminState) {
		if ("UNLOCKED".equals(adminState)) {
			return "UP";
		} else if ("LOCKED".equals(adminState)) {
			return "DOWN";
		}
		return "";
	}

	private String parseDuplex(String input) {
		String duplex = "";
		if ("full".equals(input)) {
			duplex = "Full";
		} else if ("half".equals(input)) {
			duplex = "Half";
		}
		return duplex;
	}

	private String parseSpeed(String input) {
		String speed = "";
		if (!StringUtils.isEmpty(input) && input.startsWith("eth-mounted:eth-if-speed-")) {
			input = input.split(":")[1];
		}

		if ("eth-if-speed-10mb".equals(input)) {
			speed = "10M";
		} else if ("eth-if-speed-100mb".equals(input)) {
			speed = "100M";
		} else if ("eth-if-speed-1gb".equals(input)) {
			speed = "1000M";
		} else if ("eth-if-speed-10gb".equals(input)) {
			speed = "10000M";
		}
		return speed;
	}

	private String ontUniIdXPath() {
		return "//ont:uni-service-configuration/ont:uni-id";
	}

}
