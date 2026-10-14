package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.service.RestconfService;
import com.x.hhqs.joa.in.util.ConvertUtil;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.vo.PortMacAddress;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Component("LST-PORTMACADDRESS")
public class LstPortMacAddressHandler extends AbstractHandler {

	private static final String TITLE = "list of MAC Address";

	@Autowired
	private RestconfService restconfService;

	@Override
	protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
		String deviceName = OssCmdMetaParser.oltTarget(ossCmdMeta) + ".LT" + OssCmdMetaParser.slotNo(ossCmdMeta); // realMF14_193.101.LT2
		String specifiedVlan = OssCmdMetaParser.vlan(ossCmdMeta);

		List<PortMacAddress> portMacAddressList = new ArrayList<PortMacAddress>();
		Set<String> interfaceNames = getVlanInterfaceNames(ossCmdMeta, deviceName);
		for (Iterator<String> iterator = interfaceNames.iterator(); iterator.hasNext();) {
			String interfaceName = ((String) iterator.next()).replaceAll("\"", "");
			if(StringUtils.isEmpty(interfaceName)) {
				continue;
			}

			// /rest/ema/entity/LS-FX-MF-SF-LT/VLAN%20Interface/realMF14_193.101.LT2/VSI_NBELFA45915D_VEIP_VEIP_700__d_70?fetchDeviceAttributes=false&isChild=false&isOne=false
			String vlanTag0 = restconfService.getEmaInstance(RpcConsts.lsMfAndSfModelName("LT"), "VLAN%20Interface",
					deviceName, interfaceName, RpcConsts.NOT_FETCH_DEVICE_REQUEST,
					"extraAttributes/frameproc-vlan-tag0");
			if (StringUtils.hasText(specifiedVlan) && !specifiedVlan.equals(vlanTag0)) {
				continue;
			}

			// /rest/ema/entity/LS-FX-MF-SF-LT/VLAN%20Interface/realMF14_193.101.LT2/VSI_NBELFA45915D_VEIP_VEIP_700__d_70?fetchDeviceAttributes=true&isChild=false&isOne=true&forCondition=false
			String mac = restconfService.getEmaInstance(RpcConsts.lsMfAndSfModelName("LT"), "VLAN%20Interface",
					deviceName, interfaceName, RpcConsts.FETCH_DEVICE_REQUEST, "extraAttributes/mac-addresses");

			PortMacAddress portMacAddress = new PortMacAddress();
			portMacAddress.setVlan(vlanTag0);
			portMacAddress.setMac(parseMac(mac));
			portMacAddressList.add(portMacAddress);
		}

		String[][] rtrvArray = createRtrvArray(portMacAddressList, PortMacAddress.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	private Set<String> getVlanInterfaceNames(OssCmdMeta ossCmdMeta, String deviceName)
			throws IOException, DocumentException, Rt1QueryException {
		// /rest/ema/entity/LS-FX-MF-SF-LT/UNI/realMF14_193.101.LT2/VENET_NBELFA45915D_VEIP/VLAN%20Interface
		return restconfService.getEmaChildInstance(RpcConsts.lsMfAndSfModelName("LT"), "UNI", deviceName,
				getInstanceName(ossCmdMeta), "VLAN%20Interface");
	}

	// VENET_<ONT target>_<UNI id>
	private String getInstanceName(OssCmdMeta ossCmdMeta) throws UnsupportedEncodingException {
		return "VENET_" + ConvertUtil.encodeURI(OssCmdMetaParser.ontTarget(ossCmdMeta)) + "_"
				+ ConvertUtil.portIdToUniId(ossCmdMeta);
	}

	private String parseMac(String input) {
		String mac = "";
		if (StringUtils.hasText(input)) {
			mac = input.replaceAll(":", "-");
		}
		return mac;
	}
}