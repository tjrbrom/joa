package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.service.NetconfRpcBuilder;
import com.x.hhqs.joa.in.service.NetconfService;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.vo.PonInfo;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// NetConf Channel Pair implement (Currently in used)
@Component("LST-PONINFO_CPAIR_NETCONF")
public class LstPonInfoCPAIRNetconfHandler extends AbstractHandler {

	private static final String TITLE = "List of olt pon port information";

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	@Override
	protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
		String deviceTarget = OssCmdMetaParser.oltTarget(ossCmdMeta);
		String fiberTarget = OssCmdMetaParser.fiberTarget(ossCmdMeta);
		String fiberType = OssCmdMetaParser.fiberType(ossCmdMeta);// GPON | XGS
		String deviceName = deviceTarget + ".LT" + OssCmdMetaParser.slotNo(ossCmdMeta); // FX-TEST1.LT2

		String channelPair = getChannelPairName(deviceName, fiberTarget, fiberType);
		PonInfo ponInfo = getPonInfoVo(deviceName, channelPair);
		ponInfo.setTarget(fiberTarget);
		String[][] rtrvArray = createRtrvArray(ponInfo, PonInfo.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	private PonInfo getPonInfoVo(String deviceId, String channelPair)
			throws IOException, DocumentException, Rt1QueryException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceID", deviceId);
		templateArgs.put("interfaceName", channelPair);
		templateArgs.put("interfaceType", "bbf-xponift:channel-pair");

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_INTERFACE_STATE_FTL, templateArgs);
		Map<String, String> xPathExprMap = new HashMap<String, String>();
		xPathExprMap.put("adminState", createGetInterfaceStateXPath(deviceId, channelPair, "admin-status"));
		xPathExprMap.put("operState", createGetInterfaceStateXPath(deviceId, channelPair, "oper-status"));

		Map<String, String> netconfResultMap = netconfService.sendRequestToVirtualizerAndParseFirstValue(rpcRequest,
				xPathExprMap);
		String adminStatus = netconfResultMap.get("adminState").toUpperCase();
		String operStatus = netconfResultMap.get("operState").toUpperCase();
		return new PonInfo(adminStatus, operStatus);
	}

	private String getChannelPairName(String deviceId, String fiberTarget, String fiberType)
			throws IOException, DocumentException, Rt1QueryException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceID", deviceId);
		templateArgs.put("channelGroup", fiberTarget);

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_CHANNEL_PAIR_FTL, templateArgs);
		String xPathExpr = createGetChannelPartitionNameXPath(deviceId, fiberTarget);
		// <fiberTarget>_CPAIR_GPON | <LS193.200_Fiber>_CPAIR_XGS
		return netconfService.sendRequestToVirtualizerAndParseValues(rpcRequest, xPathExpr).stream()
				.filter(cp -> cp.endsWith("_CPAIR_" + fiberType)).findFirst().get();
	}

	private String createGetChannelPartitionNameXPath(String deviceId, String channelGroupName) {
		return "//adh:device[adh:device-id='" + deviceId
				+ "']/adh:device-specific-data/if:interfaces/if:interface[bbf-xpon:channel-pair/bbf-xpon:channel-group-ref='"
				+ channelGroupName + "']/if:name";
	}

	private String createGetInterfaceStateXPath(String deviceId, String channelPartitionName, String status) {
		return "//adh:device[adh:device-id='" + deviceId
				+ "']/adh:device-specific-data/if:interfaces-state/if:interface[if:name='" + channelPartitionName
				+ "']/if:" + status;
	}

}
