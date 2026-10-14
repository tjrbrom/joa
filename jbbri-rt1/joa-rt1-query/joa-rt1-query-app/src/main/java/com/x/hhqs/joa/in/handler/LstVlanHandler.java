package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.service.NetconfRpcBuilder;
import com.x.hhqs.joa.in.service.NetconfService;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.vo.Vlan;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component("LST-VLAN")
public class LstVlanHandler extends AbstractHandler {

	private static final String TITLE = "list of Vlan";

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	@Override
	protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceId", OssCmdMetaParser.oltTarget(ossCmdMeta));
		templateArgs.put("intentType", "l2-infra");
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.SEARCH_INTENTS, templateArgs);
		List<Element> intentEleList = netconfService.sendRequestAndParseResults(rpcRequest, intentXPath());

		List<Vlan> vlanList = new ArrayList<Vlan>();
		for (Iterator<Element> iterator = intentEleList.iterator(); iterator.hasNext();) {
			Element intentEle = (Element) iterator.next();
			String intentEleXml = intentEle.asXML();
			String svlan = getFirstValFromIbn(intentEleXml, l2InfraXPath("s-vlan-id"));
			String cvlan = getFirstValFromIbn(intentEleXml, l2InfraXPath("c-vlan-id"));
			String serviceDesc = getFirstValFromIbn(intentEleXml, l2InfraXPath("service-description"));
			List<String> nnidList = getValsFromIbn(intentEleXml, l2InfraXPath("nni-id"));
			if (!isSingleVlan(svlan, cvlan)) {
				continue;
			}

			if (OssCmdMetaParser.isVlanDesignated(ossCmdMeta)
					&& !OssCmdMetaParser.vlan(ossCmdMeta).equals(getVlan(svlan, cvlan))) {
				continue;
			}

			Vlan vlan = new Vlan();
			vlanList.add(vlan);
			vlan.setOltId(OssCmdMetaParser.oltIpVal(ossCmdMeta));
			vlan.setOnuIp(OssCmdMetaParser.onuIpVal(ossCmdMeta));
			vlan.setTarget(getElementVal(intentEle, "target"));
			vlan.setVlan(getVlan(svlan, cvlan));
			vlan.setVlanMode(getVlanMode(svlan, cvlan));
			vlan.setDesc(parseServiceDesc(serviceDesc, 0));
			vlan.setMvlanFlag(parseServiceDesc(serviceDesc, 1));
			vlan.setService(parseServiceDesc(serviceDesc, 2));
			vlan.setPortList(parseNNIDList(nnidList));
		}
		String[][] rtrvArray = createRtrvArray(vlanList, Vlan.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	// NA-NA-LAG-1 | NA-NA-NTIO1-1 | NA-NA-NTB-11 | NA-NA-NTIO1-101
	private String parseNNIDList(List<String> nnidList) {
		return nnidList.stream().filter(nnid -> isValidNNID(nnid)).map(nnid -> nnidToPortId(nnid))
				.collect(Collectors.joining("|"));
	}

	/**
	 * 
	 * @param nnid
	 *             <li>LAG-1 --> NA-NA-LAG-1</li>
	 *             <li>nt-a:sfp:1|nt-a:xfp:1 --> NA-NA-NTA-1</li>
	 *             <li>nt-b:sfp:1|nt-b:xfp:1 --> NA-NA-NTB-1</li>
	 *             <li>ntio-1:sfp:1|ntio-1:xfp:1-->NA-NA-NTIO1-1</li>
	 *             <li>ntio-2:sfp:1|ntio-2:xfp:1-->NA-NA-NTIO2-1</li>
	 *             <li>nt-a:qsfp:1-1 --> NA-NA-NTA-11</li>
	 *             <li>nt-b:qsfp:1-1 --> NA-NA-NTB-11</li>
	 *             <li>ntio-1:qsfp:1-1 --> NA-NA-NTIO1-11</li>
	 *             <li>ntio-2:qsfp:1-1 --> NA-NA-NTIO2-11</li>
	 *             <li>nt-a:cfp:1 --> NA-NA-NTA-101</li>
	 *             <li>nt-b:cfp:1 --> NA-NA-NTB-101</li>
	 *             <li>ntio-1:cfp:1 --> NA-NA-NTIO1-101</li>
	 *             <li>ntio-2:cfp:1 --> NA-NA-NTIO2-101</li>
	 * @return
	 */
	private String nnidToPortId(String nnid) {
		if (nnid.startsWith("LAG-")) {
			return "NA-NA-" + nnid;
		}

		String[] nnids = nnid.split(":");
		String ntName = nnids[0].replace("-", "").toUpperCase();
		String ntId = nnids[2].replace("-", "");
		if ("cfp".equals(nnids[1])) {
			ntId = String.valueOf(Integer.parseInt(ntId) + 100);
		}
		return "NA-NA-" + ntName + "-" + ntId;
	}

	/**
	 * 
	 * @param nnid
	 *             <li>LAG-<></li>
	 *             <li>nt-a:sfp:<>,nt-a:xfp:<>,nt-a:cfp:<>,nt-a:qsfp:<>-<></li>
	 *             <li>nt-b:sfp:<>,nt-b:xfp:<>,nt-b:cfp:<>,nt-b:qsfp:<>-<></li>
	 *             <li>ntio-1:sfp:<>,ntio-1:xfp:<>,ntio-1:cfp:<>,ntio-1:qsfp:<>-<></li>
	 *             <li>ntio-2:sfp:<>,ntio-2:xfp:<>,ntio-2:cfp:<>,ntio-2:qsfp:<>-<></li>
	 * 
	 * @return
	 */
	private boolean isValidNNID(String nnid) {
		if (StringUtils.isEmpty(nnid)) {
			return false;
		}
		String lagPattern = "^LAG-\\d+$";
		String ntPattern = "^(nt-a|nt-b|ntio-1|ntio-2):(qsfp:\\d+-\\d+|xfp:\\d+|cfp:\\d+|sfp:\\d+)$";
		return nnid.matches(lagPattern) || nnid.matches(ntPattern);
	}

	// DESC|MVLANFLAG|SERVICE
	private String parseServiceDesc(String serviceDesc, int num) {
		String rst = "";
		if (!StringUtils.isEmpty(serviceDesc) && serviceDesc.split("\\|").length == 3) {
			rst = serviceDesc.split("\\|")[num];
		}
		return rst;
	}

	// svlan:QINQ, cvlan:COMMON
	private String getVlanMode(String svlan, String cvlan) {
		return StringUtils.hasText(svlan) ? "QINQ" : (StringUtils.hasText(cvlan) ? "COMMON" : "");
	}

	private String getVlan(String svlan, String cvlan) {
		return StringUtils.hasText(svlan) ? svlan : cvlan;
	}

	private boolean isSingleVlan(String svlan, String cvlan) {
		if (StringUtils.hasText(svlan) && StringUtils.hasText(cvlan)) {
			return false; // s+c
		}
		return StringUtils.hasText(svlan) || StringUtils.hasText(cvlan);
	}

	private String intentXPath() {
		return "//ibn:intent";
	}

	private String l2InfraXPath(String leaf) {
		return "//l2infra:" + leaf;
	}
}
