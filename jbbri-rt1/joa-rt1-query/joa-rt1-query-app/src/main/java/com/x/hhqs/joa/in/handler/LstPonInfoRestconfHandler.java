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
import com.x.hhqs.joa.in.vo.PonInfo;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// RestConf Channel partition implement
@Component("LST-PONINFO_RESTCONF")
public class LstPonInfoRestconfHandler extends AbstractHandler {

	private static final String TITLE = "List of olt pon port information";

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private RestconfService restconfService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	@Override
	protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
		String deviceTarget = OssCmdMetaParser.oltTarget(ossCmdMeta);
		String fiberTarget = OssCmdMetaParser.fiberTarget(ossCmdMeta);
		String fiberType = OssCmdMetaParser.fiberType(ossCmdMeta);// GPON | XGS
		String deviceName = deviceTarget + ".LT" + OssCmdMetaParser.slotNo(ossCmdMeta); // FX-TEST1.LT2

		String channelPartition = getChannelPartitionName(deviceName, fiberTarget, fiberType);
		// /rest/ema/entity/LS-FX-MF-SF-LT/ChannelPartition/FX-TEST1.LT2/BSO_Fiber_CPART_GPON?fetchDeviceAttributes=true&isChild=false&isOne=true
		String operStatus = restconfService.getEmaInstance(RpcConsts.lsMfAndSfModelName("LT"), "ChannelPartition",
				deviceName, channelPartition, RpcConsts.FETCH_DEVICE_REQUEST, "operationState");

		// /rest/ema/entity/LS-FX-MF-SF-LT/ChannelPartition/FX-TEST1.LT2/BSO_Fiber_CPART_GPON?fetchDeviceAttributes=false&isChild=false&isOne=false
		String adminState = restconfService.getEmaInstance(RpcConsts.lsMfAndSfModelName("LT"), "ChannelPartition",
				deviceName, channelPartition, RpcConsts.NOT_FETCH_DEVICE_REQUEST, "adminStatus");
		adminState = getAdminState(adminState);

		PonInfo ponInfo = new PonInfo(adminState, operStatus);
		ponInfo.setTarget(fiberTarget);
		String[][] rtrvArray = createRtrvArray(ponInfo, PonInfo.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	private String getChannelPartitionName(String deviceId, String fiberTarget, String fiberType)
			throws IOException, DocumentException, Rt1QueryException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceID", deviceId);
		templateArgs.put("channelGroup", fiberTarget);

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_CHANNEL_PARTITION_FTL, templateArgs);
		String xPathExpr = createGetChannelPartitionNameXPath(deviceId, fiberTarget);
		// filter: <fiberTarget>_CPART_GPON | <fiberTarget>_CPART_XGS
		return netconfService.sendRequestToVirtualizerAndParseValues(rpcRequest, xPathExpr).stream()
				.filter(cp -> cp.endsWith("_CPART_" + fiberType)).findFirst().get();
	}

	private String createGetChannelPartitionNameXPath(String deviceId, String channelGroupName) {
		return "//adh:device[adh:device-id='" + deviceId
				+ "']/adh:device-specific-data/if:interfaces/if:interface[bbf-xpon:channel-partition/bbf-xpon:channel-group-ref='"
				+ channelGroupName + "']/if:name";
	}

	// UNLOCKED | LOCKED
	private String getAdminState(String adminState) {
		if ("UNLOCKED".equals(adminState)) {
			return "UP";
		} else if ("LOCKED".equals(adminState)) {
			return "DOWN";
		}
		return "";
	}

	/**
	 * 
	 * @param PONID=1-1-6-1
	 * @return LT6
	 */
	protected String getLtIdDesc(OssCmdMeta ossCmdMeta) {
		return "LT" + OssCmdMetaParser.slotNo(ossCmdMeta);
	}
}
