package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.service.NetconfRpcBuilder;
import com.x.hhqs.joa.in.service.NetconfService;
import com.x.hhqs.joa.in.service.ProfileManagerService;
import com.x.hhqs.joa.in.util.ConvertUtil;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.util.RpcUtil;
import com.x.hhqs.joa.in.vo.Iptv;
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

@Component("LST-IPTV")
public class LstIptvHandler extends AbstractHandler {

	private static final String TITLE = "List of Iptv info";

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private ProfileManagerService profileManagerService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	@Override
	protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
		String ontTarget = OssCmdMetaParser.ontTarget(ossCmdMeta);

		List<Iptv> iptvList = addL2UserToIptvList(ontTarget, ossCmdMeta);
		if (!CollectionUtils.isEmpty(iptvList)) {
			addL2InfraToIptvList(iptvList);
			addMcastServiceProfileToIptvList(iptvList);
		}

		String[][] rtrvArray = createRtrvArray(iptvList, Iptv.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	// L2User include hsi and iptv, MCastUser only include IPTV.
	// L2User and MCastUser have same Target
	private List<Iptv> addL2UserToIptvList(String ontTarget, OssCmdMeta ossCmdMeta)
			throws IOException, DocumentException, Rt1QueryException {
		List<Iptv> iptvList = new ArrayList<Iptv>();

		// HSI#ALCLF8AFFD92_300 | IPTV#ALCLF8AFFD92_500
		List<String> l2UserTargetList = getL2UserTargetList(ontTarget, ossCmdMeta);
		if (CollectionUtils.isEmpty(l2UserTargetList)) {
			return iptvList;
		}

		Map<String, Object> mcastTemplateArgs = new HashMap<String, Object>();
		mcastTemplateArgs.put("mcastUserTargetList", l2UserTargetList);
		String mcastRpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_MCAST_USER_FTL, mcastTemplateArgs);
		List<Element> mcastUserEleList = netconfService.sendRequestAndParseResults(mcastRpcRequest, intentXPath());
		mcastUserEleList.forEach(mcastUserEle -> {
			String mcastUserEleXml = mcastUserEle.asXML();
			Iptv iptv = new Iptv();
			iptvList.add(iptv);
			// IPTV#ALCLF8AFFD92_500
			iptv.setMcastUserTarget(getElementVal(mcastUserEle, "target"));
			iptv.setL2UserTarget(iptv.getMcastUserTarget());
			iptv.setMcastServiceProfile(getFirstValFromIbn(mcastUserEleXml, mcastUserXPath("service-profile")));
		});
		if (CollectionUtils.isEmpty(iptvList)) {
			return iptvList;
		}

		List<String> mcastUserTargetList = iptvList.stream().map(iptv -> iptv.getMcastUserTarget())
				.collect(Collectors.toList());
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("l2UserTargetList", mcastUserTargetList);
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_L2_USER_BY_TARGET_FTL, templateArgs);
		List<Element> l2UserEleList = netconfService.sendRequestAndParseResults(rpcRequest, intentXPath());

		l2UserEleList.forEach(l2UserEle -> {
			String l2UserEleXml = l2UserEle.asXML();
			Iptv iptv = iptvList.stream()
					.filter(iptvObj -> getElementVal(l2UserEle, "target").equals(iptvObj.getMcastUserTarget()))
					.findFirst().get();

			iptv.setOltId(OssCmdMetaParser.oltId(ossCmdMeta));
			iptv.setPonId(OssCmdMetaParser.ponId(ossCmdMeta));
			iptv.setOnuIp(OssCmdMetaParser.onuIp(ossCmdMeta));
			iptv.setOnuId(OssCmdMetaParser.onuId(ossCmdMeta));
			iptv.setOnuPort(ConvertUtil.uniIdToOnuPort(getFirstValFromIbn(l2UserEleXml, l2UserXPath("uni-id"))));
			iptv.setUv(getUv(l2UserEleXml));
			// realMF14_193.201#IPTV
			iptv.setL2InfraTarget(OssCmdMetaParser.oltTarget(ossCmdMeta) + "#"
					+ getFirstValFromIbn(l2UserEleXml, l2UserXPath("l2-infra")));
		});
		return iptvList;
	}

	private void addL2InfraToIptvList(List<Iptv> iptvList) throws DocumentException, IOException, Rt1QueryException {
		List<String> l2InfraTargetList = iptvList.stream().map(iptv -> iptv.getL2InfraTarget())
				.collect(Collectors.toList());

		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("l2InfraTargetList", l2InfraTargetList);
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_L2_INFRA_FTL, templateArgs);
		String l2InfraResponse = netconfService.sendRequest(rpcRequest);

		for (Iptv iptv : iptvList) {
			String xPathExpression = l2InfraXPath(iptv.getL2InfraTarget());
			Element l2InfraEle = RpcUtil.getFirstElementByXPath(l2InfraResponse, xPathExpression, RpcConsts.MF_IBN_NS);
			iptv.setMvlan(getMvlan(l2InfraEle));
		}
	}

	private void addMcastServiceProfileToIptvList(List<Iptv> iptvList)
			throws JSONException, Rt1QueryException, IOException, DocumentException {
		for (Iterator<Iptv> iterator = iptvList.iterator(); iterator.hasNext();) {
			Iptv iptv = (Iptv) iterator.next();
			Map<String, String> xPathMap = new HashMap<String, String>();
			xPathMap.put("max-group-number", "//prof-mcast-usr:max-group-number");

			Profile onuTemplateProfileVo = profileManagerService.getProfilesUsedByIntent(iptv.getMcastServiceProfile(),
					"mcast-user", iptv.getMcastUserTarget(), RpcConsts.MULTICAST_USER_SERVICE_PROFILE_UI,
					xPathMap);
			String maxGrp = onuTemplateProfileVo.getSpecificData().get("max-group-number");
			iptv.setMaxGrp(maxGrp);
		}
	}

	private List<String> getL2UserTargetList(String ontTarget, OssCmdMeta ossCmdMeta)
			throws IOException, DocumentException, Rt1QueryException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("ontTarget", ontTarget);
		templateArgs.put("uniId", ConvertUtil.onuPortToUniId(ossCmdMeta));
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_L2_USER_FTL, templateArgs);
		return netconfService.sendRequestAndParseValues(rpcRequest, targetXPath());
	}

	private String getMvlan(Element l2InfraEle) {
		String sVlan = getElementVal(l2InfraEle, "s-vlan-id");
		String cVlan = getElementVal(l2InfraEle, "c-vlan-id");

		String mVlan = "";
		if (StringUtils.hasText(cVlan)) {
			mVlan = cVlan;
		} else if (StringUtils.hasText(sVlan)) {
			mVlan = sVlan;
		}
		return mVlan;
	}

	private String getUv(String l2UserEleXml) {
		String qVlanId = getFirstValFromIbn(l2UserEleXml, l2UserXPath("q-vlan-id"));
		String transparentQVlanId = getFirstValFromIbn(l2UserEleXml, l2UserXPath("transparent-q-vlan-id"));
		String nVlanId = getFirstValFromIbn(l2UserEleXml, l2UserXPath("n-vlan-id"));
		String multiQVlanId = getFirstValFromIbn(l2UserEleXml, l2UserXPath("multiple-q-vlan-id"));

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

	private String targetXPath() {
		return "//ibn:target";
	}

	private String l2UserXPath(String leaf) {
		return "//ibn:intent/ibn:intent-specific-data/l2user:l2-user/l2user:" + leaf;
	}

	private String mcastUserXPath(String leaf) {
		return "//ibn:intent/ibn:intent-specific-data/mcu:mcast-user/mcu:" + leaf;
	}

	private String l2InfraXPath(String l2InfraTarget) {
		return "//ibn:ibn/ibn:intent[ibn:target='" + l2InfraTarget + "']/ibn:intent-specific-data/l2infra:l2-infra";
	}

}
