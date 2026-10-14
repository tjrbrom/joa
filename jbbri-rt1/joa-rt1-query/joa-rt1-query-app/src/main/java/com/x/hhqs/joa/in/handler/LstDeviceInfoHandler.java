package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.service.NetconfRpcBuilder;
import com.x.hhqs.joa.in.service.NetconfService;
import com.x.hhqs.joa.in.service.RestconfService;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.vo.DevInfo;
import org.dom4j.DocumentException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component("LST-DEVINFO")
public class LstDeviceInfoHandler extends AbstractHandler {

	private static final String TITLE = "List of Device Info";

	// the path to parse the JSON object
	private static final String PROVIDING_SVC_PATH = "extraAttributes/elements/0/1/1";

	public static final String USED_MEM_PATH = "extraAttributes/used-memory";

	public static final String USED_CPU_PATH = "extraAttributes/used-cpu";

	public static final String TEMPERATURE_SENSOR1_PATH = "extraAttributes/temperature-sensor/0/1/1";

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private RestconfService restconfService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	@Override
	protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
		String deviceTarget = OssCmdMetaParser.oltTarget(ossCmdMeta);
		DevInfo devInfo = addDeviceMfToDevInfoVo(deviceTarget);
		addDevMemCpuTempToDevInfoVo(deviceTarget, devInfo);

		String[][] rtrvArray = createRtrvArray(devInfo, DevInfo.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	private DevInfo addDeviceMfToDevInfoVo(String deviceTarget)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceId", deviceTarget);

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_DEVICE_FTL, templateArgs);
		String intentEleXml = netconfService.sendRequestAndParseFirstResult(rpcRequest, intentXPath()).asXML();

		DevInfo devInfoVo = new DevInfo();
		devInfoVo.setDevName(deviceTarget);
		devInfoVo.setTarget(deviceTarget);
		devInfoVo.setDevIp(getFirstValFromIbn(intentEleXml, deviceXPath("ip-address")));
		devInfoVo.setDt(getFirstValFromIbn(intentEleXml, deviceXPath("hardware-type")));
		devInfoVo.setDeVer(getFirstValFromIbn(intentEleXml, deviceXPath("device-version")));
		return devInfoVo;
	}

	private void addDevMemCpuTempToDevInfoVo(String deviceTarget, DevInfo devInfo)
			throws NoSuchMessageException, Rt1QueryException, DocumentException, IOException {
		String ntName = getProvidingSvcNt(deviceTarget);
		// /rest/ema/entity/LS-FX-MF-SF-System/Slot/Demo2-DockerLSHost-mf/Nt-A?fetchDeviceAttributes=true&isChild=false&isOne=true
		Map<String, String> emaInstanceMap = restconfService.getEmaInstance(RpcConsts.lsMfAndSfModelName("System"),
				"Slot", deviceTarget, ntName, RpcConsts.FETCH_DEVICE_REQUEST,
				Arrays.asList(USED_MEM_PATH, USED_CPU_PATH, TEMPERATURE_SENSOR1_PATH));
		devInfo.setMem(bigDecimalToStr(emaInstanceMap.get(USED_MEM_PATH)));
		devInfo.setCpu(bigDecimalToStr(emaInstanceMap.get(USED_CPU_PATH)));
		devInfo.setTemperature(jsonArrToStr(emaInstanceMap.get(TEMPERATURE_SENSOR1_PATH)));
	}

	private String getProvidingSvcNt(String deviceTarget)
			throws NoSuchMessageException, Rt1QueryException, DocumentException, IOException {
		// /rest/ema/entity/LS-FX-MF-SF-System/LS-OLT/Demo2-DockerLSHost-mf/Demo2-DockerLSHost-mf?fetchDeviceAttributes=true&isChild=false&isOne=true
		String standByStatus = restconfService.getEmaInstance(RpcConsts.lsMfAndSfModelName("System"), "LS-OLT",
				deviceTarget, deviceTarget, RpcConsts.FETCH_DEVICE_REQUEST, PROVIDING_SVC_PATH);
		if ("PROVIDING-SERVICE".equals(standByStatus)) { // PROVIDING-SERVICE or IDLE
			return "Nt-A";
		} else {
			return "Nt-B";
		}
	}

	// ["50"] -> 50
	private String jsonArrToStr(String input) {
		String rst = "";
		if (StringUtils.hasText(input)) {
			rst = input.replaceAll("\\[\"(\\d+)\"\\]", "$1");
		}
		return rst;
	}

	// 2.0 -> 2
	private String bigDecimalToStr(String input) {
		String rst = "";
		if (StringUtils.hasText(input)) {
			rst = input.replaceAll("\\.\\d+$", "");
		}
		return rst;
	}

	private String intentXPath() {
		return "//ibn:ibn/ibn:intent";
	}

	private String deviceXPath(String leaf) {
		return "//dmf:device-mf/dmf:" + leaf;
	}
}
