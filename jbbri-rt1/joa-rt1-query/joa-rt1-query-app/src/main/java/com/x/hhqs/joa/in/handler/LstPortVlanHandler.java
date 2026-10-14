package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.service.NetconfRpcBuilder;
import com.x.hhqs.joa.in.service.NetconfService;
import com.x.hhqs.joa.in.util.ConvertUtil;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.util.RpcUtil;
import com.x.hhqs.joa.in.vo.PortVlan;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component("LST-PORTVLAN")
public class LstPortVlanHandler extends AbstractHandler {

	private static final String TITLE = "List of Port vlan";

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	@Override
	protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
		String ontTarget = OssCmdMetaParser.ontTarget(ossCmdMeta);
		String uniId = ConvertUtil.onuPortToUniId(ossCmdMeta);
		//String onu_no = getOnuNoByTarget(ontTarget);

		List<PortVlan> portVlanList = addL2UserToPortVlanList(ontTarget, "", uniId, ossCmdMeta);
		addL2InfraToPortVlanList(portVlanList);

		String[][] rtrvArray = createRtrvArray(portVlanList, PortVlan.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	private List<PortVlan> addL2UserToPortVlanList(String ontTarget, String onu_no, String uniId, OssCmdMeta ossCmdMeta)
			throws IOException, DocumentException, Rt1QueryException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("ontTarget", ontTarget);
		templateArgs.put("uniId", uniId);
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_L2_USER_FTL, templateArgs);
		List<Element> intentEleList = netconfService.sendRequestAndParseResults(rpcRequest, intentXPath());

		List<PortVlan> portVlanList = new ArrayList<PortVlan>();
		for (Iterator<Element> iterator = intentEleList.iterator(); iterator.hasNext();) {
			Element intentEle = (Element) iterator.next();
			String intentEleXml = intentEle.asXML();
			String l2UserUniId = getFirstValFromIbn(intentEleXml, l2UserXPath("uni-id"));
			String l2Infra = getFirstValFromIbn(intentEleXml, l2UserXPath("l2-infra"));

			PortVlan portVlan = new PortVlan();
			portVlanList.add(portVlan);
			portVlan.setOltId(OssCmdMetaParser.oltId(ossCmdMeta));
			portVlan.setPonId(OssCmdMetaParser.ponId(ossCmdMeta));
			portVlan.setOnuId(OssCmdMetaParser.onuId(ossCmdMeta));
			portVlan.setOnuPort(ConvertUtil.uniIdToOnuPort(l2UserUniId));
			portVlan.setOnuIp(OssCmdMetaParser.onuIp(ossCmdMeta));
			portVlan.setUv(getUv(intentEleXml));
			portVlan.setL2Infra(OssCmdMetaParser.oltTarget(ossCmdMeta) + "#" + l2Infra);
			portVlan.setL2User(getElementVal(intentEle, "target"));
		}
		return portVlanList;
	}

	private void addL2InfraToPortVlanList(List<PortVlan> portVlanList)
			throws DocumentException, IOException, Rt1QueryException {
		if (CollectionUtils.isEmpty(portVlanList)) {
			return;
		}
		List<String> l2InfraTargetList = portVlanList.stream().map(portVlan -> portVlan.getL2Infra())
				.collect(Collectors.toList());

		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("l2InfraTargetList", l2InfraTargetList);
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_L2_INFRA_FTL, templateArgs);
		String l2InfraResponse = netconfService.sendRequest(rpcRequest);

		for (PortVlan portVlan : portVlanList) {
			String xPathExpression = createGetL2InfraXPath(portVlan.getL2Infra());
			Element l2InfraEle = RpcUtil.getFirstElementByXPath(l2InfraResponse, xPathExpression, RpcConsts.MF_IBN_NS);
			portVlan.setSvlan(getElementVal(l2InfraEle, "s-vlan-id"));
			portVlan.setCvlan(getElementVal(l2InfraEle, "c-vlan-id"));
		}
	}

	private String getUv(String intentEleXml) {
		String qVlanId = getFirstValFromIbn(intentEleXml, l2UserXPath("q-vlan-id"));
		String transparentQVlanId = getFirstValFromIbn(intentEleXml, l2UserXPath("transparent-q-vlan-id"));
		String nVlanId = getFirstValFromIbn(intentEleXml, l2UserXPath("n-vlan-id"));
		String multiQVlanId = getFirstValFromIbn(intentEleXml, l2UserXPath("multiple-q-vlan-id"));

		String uv = "";
		if (StringUtils.hasText(qVlanId)) {
			uv = qVlanId;
		} else if (StringUtils.hasText(transparentQVlanId)) {
			uv = transparentQVlanId;
		} else if (StringUtils.hasText(nVlanId)) {
			uv = nVlanId;
		} else if (StringUtils.hasText(multiQVlanId)) {
			uv = multiQVlanId;
		}
		return uv;
	}

	private String intentXPath() {
		return "//ibn:intent";
	}

	private String l2UserXPath(String leaf) {
		return "//l2user:" + leaf;
	}

	private String createGetL2InfraXPath(String l2InfraTarget) {
		return "//ibn:ibn/ibn:intent[ibn:target='" + l2InfraTarget + "']/ibn:intent-specific-data/l2infra:l2-infra";
	}

}
