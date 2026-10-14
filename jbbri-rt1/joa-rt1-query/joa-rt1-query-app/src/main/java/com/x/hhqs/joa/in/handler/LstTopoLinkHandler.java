package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.ErrorKey;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.service.NetconfRpcBuilder;
import com.x.hhqs.joa.in.service.NetconfService;
import com.x.hhqs.joa.in.util.ConvertUtil;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.vo.TopoLink;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component("LST-TOPOLINK")
public class LstTopoLinkHandler extends AbstractHandler {

	private static final String TITLE = "List of Topo Link info";

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	@Override
	protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
		List<TopoLink> topoLinkList = new ArrayList<TopoLink>();
		if (OssCmdMetaParser.isPonCmd(ossCmdMeta)) {
			topoLinkList = getTopoLinkListByPon(ossCmdMeta);
		} else if (OssCmdMetaParser.isOltCmd(ossCmdMeta)) {
			topoLinkList = getTopoLinkListByOlt(ossCmdMeta);
		} else {
			throw new Rt1QueryException(ErrorKey.INVALID_COMMAND);
		}

		String[][] rtrvArray = createRtrvArray(topoLinkList, TopoLink.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	private List<TopoLink> getTopoLinkListByPon(OssCmdMeta ossCmdMeta)
			throws JSONException, Rt1QueryException, IOException, DocumentException {
		List<TopoLink> topoLinkList = new ArrayList<TopoLink>();
		String fiberTarget = OssCmdMetaParser.fiberTarget(ossCmdMeta);
		String oltIp = OssCmdMetaParser.oltIpVal(ossCmdMeta);
		String ponId = OssCmdMetaParser.ponIdVal(ossCmdMeta);

		List<TopoLink> posTopoLinkList = getPosTopoLinkListByFiberTarget(fiberTarget, oltIp, ponId);
		topoLinkList.addAll(posTopoLinkList);

		List<TopoLink> onuTopoLinkList = getOnuTopoLinkListByFiberTarget(fiberTarget, oltIp, ponId);
		topoLinkList.addAll(onuTopoLinkList);
		return topoLinkList;
	}

	private List<TopoLink> getTopoLinkListByOlt(OssCmdMeta ossCmdMeta)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		String oltIp = OssCmdMetaParser.oltIpVal(ossCmdMeta);
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceId", OssCmdMetaParser.oltTarget(ossCmdMeta));
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_FIBER_FTL, templateArgs);
		List<Element> intentEleList = netconfService.sendRequestAndParseResults(rpcRequest, intentXPath());

		List<TopoLink> topoLinkList = new ArrayList<TopoLink>();
		for (Iterator<Element> iterator = intentEleList.iterator(); iterator.hasNext();) {
			Element intentEle = (Element) iterator.next();
			String fiberTarget = getElementVal(intentEle, "target");
			String ponIdDesc = getFirstValFromIbn(intentEle.asXML(), fiberPonPortXPath("pon-id"));
			String ponId = ConvertUtil.ponIdDescToPonId(ponIdDesc);
			topoLinkList.addAll(getPosTopoLinkListByFiberTarget(fiberTarget, oltIp, ponId));
			topoLinkList.addAll(getOnuTopoLinkListByFiberTarget(fiberTarget, oltIp, ponId));
		}
		Collections.sort(topoLinkList);
		return topoLinkList;
	}

	private List<TopoLink> getOnuTopoLinkListByFiberTarget(String fiberTarget, String oltIp, String ponId)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("fiberTarget", fiberTarget);

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_ONT_WITH_SPLITTER_BY_PON_FTL, templateArgs);
		List<Element> intentEleList = netconfService.sendRequestAndParseResults(rpcRequest, intentXPath());
		return createOnuTopoLinkListFromIntentList(intentEleList, oltIp, ponId, fiberTarget);
	}

	private List<TopoLink> getPosTopoLinkListByFiberTarget(String fiberTarget, String oltIp, String ponId)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("fiberTarget", fiberTarget);

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_DEVICE_SPLITTER_BY_PON_FTL, templateArgs);
		List<Element> intentEleList = netconfService.sendRequestAndParseResults(rpcRequest, intentXPath());
		return createPosTopoLinkListFromIntentList(intentEleList, oltIp, ponId, fiberTarget);
	}

	private List<TopoLink> createOnuTopoLinkListFromIntentList(List<Element> intentEleList, String oltIp, String ponId,
			String fiberTarget) {
		return intentEleList.stream().filter(intentEle -> {
			String intentEleXml = intentEle.asXML();
			String upSplitter = getFirstValFromIbn(intentEleXml, ontXPath("splitter"));
			return StringUtils.hasText(upSplitter);
		}).map(intentEle -> {
			TopoLink topoLink = new TopoLink();
			topoLink.setPonId(ponId);
			topoLink.setName(getElementVal(intentEle, "target")); // onu name

			String intentEleXml = intentEle.asXML();
			String onuId = getFirstValFromIbn(intentEleXml, ontStateXPath("onu-id"));
			String topPosPort = getFirstValFromIbn(intentEleXml, ontXPath("topPosPort"));
			String upSplitter = getFirstValFromIbn(intentEleXml, ontXPath("splitter"));
			String topPosId = ponId + "-" + upSplitter.replaceAll(fiberTarget + "_", "");

			// APOINT is pos
			String aPoint = createPosPoint(oltIp, topPosId, topPosPort);
			// ZPOINT is onu
			String zPoint = createOnuPoint(oltIp, ponId, onuId);
			topoLink.setApoint(aPoint);
			topoLink.setZpoint(zPoint);
			return topoLink;
		}).collect(Collectors.toList());
	}

	private List<TopoLink> createPosTopoLinkListFromIntentList(List<Element> intentEleList, String oltIp, String ponId,
			String fiberTarget) {
		return intentEleList.stream().map(intentEle -> {
			TopoLink topoLink = new TopoLink();
			topoLink.setPonId(ponId);
			String posTarget = getElementVal(intentEle, "target");
			topoLink.setName(posTarget); // splitter name

			String intentEleXml = intentEle.asXML();
			String upSplitter = getFirstValFromIbn(intentEleXml, splitterXPath("up-splitter"));
			String posId = ponId + "-" + posTarget.replaceAll(fiberTarget + "_", "");
			if (!StringUtils.hasText(upSplitter)) {
				// layer 1, APOINT is pon
				String aPoint = createPonPoint(oltIp, ponId);
				String zPoint = createPosPoint(oltIp, posId, "0");
				topoLink.setApoint(aPoint);
				topoLink.setZpoint(zPoint);
			} else {
				String topPosId = ponId + "-" + upSplitter.replaceAll(fiberTarget + "_", "");
				String topPosPort = getFirstValFromIbn(intentEleXml, splitterXPath("topPosPort"));
				// APOINT is POS
				String aPoint = createPosPoint(oltIp, topPosId, topPosPort);
				String zPoint = createPosPoint(oltIp, posId, "0");
				topoLink.setApoint(aPoint);
				topoLink.setZpoint(zPoint);
			}
			return topoLink;
		}).collect(Collectors.toList());
	}

	// ONU/OLTIP:PON:ONUID (ONU/135.251.201.58:1-1-1-1:1)
	private String createOnuPoint(String oltIp, String ponId, String onuId) {
		StringBuffer sb = new StringBuffer();
		sb.append("ONU/");
		sb.append(oltIp);
		sb.append(":");
		sb.append(ponId);
		sb.append(":");
		sb.append(onuId);
		return sb.toString();
	}

	// PON/OLTIP:PON (PON/135.251.201.58:1-1-1-1)
	private String createPonPoint(String oltIp, String ponId) {
		StringBuffer sb = new StringBuffer();
		sb.append("PON/");
		sb.append(oltIp);
		sb.append(":");
		sb.append(ponId);
		return sb.toString();
	}

	// POS/OLTIP:POSID:TOPPOSPORT (POS/135.251.201.58:1-1-1-1-1:3)
	private String createPosPoint(String oltIp, String posId, String topPosPort) {
		StringBuffer sb = new StringBuffer();
		sb.append("POS/");
		sb.append(oltIp);
		sb.append(":");
		sb.append(posId);
		sb.append(":");
		sb.append(topPosPort);
		return sb.toString();
	}

	private String splitterXPath(String leaf) {
		return "//device-splitter:device-splitter/device-splitter:" + leaf;
	}

	private String fiberPonPortXPath(String leaf) {
		return "//fiber:fiber/fiber:pon-port/fiber:" + leaf;
	}

	private String intentXPath() {
		return "//ibn:ibn/ibn:intent";
	}

	private String ontXPath(String leaf) {
		return "//ont:ont/ont:" + leaf;
	}

	private String ontStateXPath(String leaf) {
		return "//ont:ont-state/ont:" + leaf;
	}

}
