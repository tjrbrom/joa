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

// Netconf Channel partition implement
@Component("LST-PONINFO_NETCONF")
public class LstPonInfoNetconfHandler extends AbstractHandler {

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

		String channelPartition = getChannelPartitionName(deviceName, fiberTarget, fiberType);
		PonInfo ponInfo = getPonInfoVo(deviceName, channelPartition);
		ponInfo.setTarget(fiberTarget);
		String[][] rtrvArray = createRtrvArray(ponInfo, PonInfo.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	private PonInfo getPonInfoVo(String deviceId, String channelPartition)
			throws IOException, DocumentException, Rt1QueryException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceID", deviceId);
		templateArgs.put("interfaceName", channelPartition);
		templateArgs.put("interfaceType", "bbf-xponift:channel-partition");

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_INTERFACE_STATE_FTL, templateArgs);
		Map<String, String> xPathExprMap = new HashMap<String, String>();
		xPathExprMap.put("adminState", createGetChannelPartitionStateXPath(deviceId, channelPartition, "admin-status"));
		xPathExprMap.put("operState", createGetChannelPartitionStateXPath(deviceId, channelPartition, "oper-status"));

		Map<String, String> netconfResultMap = netconfService.sendRequestToVirtualizerAndParseFirstValue(rpcRequest,
				xPathExprMap);
		String adminStatus = netconfResultMap.get("adminState").toUpperCase();
		String operStatus = netconfResultMap.get("operState").toUpperCase();
		return new PonInfo(adminStatus, operStatus);
	}

	private String getChannelPartitionName(String deviceId, String fiberTarget, String fiberType)
			throws IOException, DocumentException, Rt1QueryException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceID", deviceId);
		templateArgs.put("channelGroup", fiberTarget);

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_CHANNEL_PARTITION_FTL, templateArgs);
		String xPathExpr = createGetChannelPartitionNameXPath(deviceId, fiberTarget);
		// <fiberTarget>_CPART_GPON | <LS193.200_Fiber>_CPART_XGS
		return netconfService.sendRequestToVirtualizerAndParseValues(rpcRequest, xPathExpr).stream()
				.filter(cp -> cp.endsWith("_CPART_" + fiberType)).findFirst().get();
	}

	private String createGetChannelPartitionNameXPath(String deviceId, String channelGroupName) {
		return "//adh:device[adh:device-id='" + deviceId
				+ "']/adh:device-specific-data/if:interfaces/if:interface[bbf-xpon:channel-partition/bbf-xpon:channel-group-ref='"
				+ channelGroupName + "']/if:name";
	}

	private String createGetChannelPartitionStateXPath(String deviceId, String channelPartitionName, String status) {
		return "//adh:device[adh:device-id='" + deviceId
				+ "']/adh:device-specific-data/if:interfaces-state/if:interface[if:name='" + channelPartitionName
				+ "']/if:" + status;
	}

}
