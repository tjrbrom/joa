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
import com.x.hhqs.joa.in.vo.Pos;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component("LST-POS")
public class LstPosHandler extends AbstractHandler {

	private static final String TITLE = "List of Pos info";

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	@Override
	protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
		List<Pos> posList = new ArrayList<Pos>();
		if (OssCmdMetaParser.isPonCmd(ossCmdMeta)) {
			posList = getPosListByPon(ossCmdMeta);
		} else if (OssCmdMetaParser.isOltCmd(ossCmdMeta)) {
			posList = getPosListByOlt(ossCmdMeta);
		} else {
			throw new Rt1QueryException(ErrorKey.INVALID_COMMAND);
		}

		String[][] rtrvArray = createRtrvArray(posList, Pos.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	private List<Pos> getPosListByPon(OssCmdMeta ossCmdMeta)
			throws JAXBException, SAXException, IOException, DocumentException, Rt1QueryException {
		String fiberTarget = OssCmdMetaParser.fiberTarget(ossCmdMeta);
		String ponId = OssCmdMetaParser.ponIdVal(ossCmdMeta);
		return getPosListByFiberTarget(fiberTarget, ponId);
	}

	private List<Pos> getPosListByOlt(OssCmdMeta ossCmdMeta)
			throws Rt1QueryException, IOException, DocumentException, JAXBException, SAXException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceId", OssCmdMetaParser.oltTarget(ossCmdMeta));
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_FIBER_FTL, templateArgs);
		List<Element> intentEleList = netconfService.sendRequestAndParseResults(rpcRequest, intentXPath());

		List<Pos> posList = new ArrayList<Pos>();
		for (Iterator<Element> iterator = intentEleList.iterator(); iterator.hasNext();) {
			Element intentEle = (Element) iterator.next();
			String fiberTarget = getElementVal(intentEle, "target");
			String ponIdDesc = getFirstValFromIbn(intentEle.asXML(), fiberPonPortXPath("pon-id"));
			String ponId = ConvertUtil.ponIdDescToPonId(ponIdDesc);
			posList.addAll(getPosListByFiberTarget(fiberTarget, ponId));
		}
		Collections.sort(posList);
		return posList;
	}

	private List<Pos> getPosListByFiberTarget(String fiberTarget, String ponId)
			throws Rt1QueryException, IOException, DocumentException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("fiberTarget", fiberTarget);

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_DEVICE_SPLITTER_BY_PON_FTL, templateArgs);
		List<Element> intentEleList = netconfService.sendRequestAndParseResults(rpcRequest, intentXPath());
		List<Pos> posList = createPosListFromIntentList(intentEleList, ponId, fiberTarget);
		addLayerToPos(posList);
		return posList;
	}

	private List<Pos> createPosListFromIntentList(List<Element> intentEleList, String ponId, String fiberTarget) {
		return intentEleList.stream().map(intentEle -> {
			Pos pos = new Pos();
			// jing_0101_2(fiberName_posId)
			String posTarget = getElementVal(intentEle, "target");
			pos.setTarget(posTarget);
			pos.setName(posTarget);
			String posPort = posTarget.replaceAll(fiberTarget + "_", "");
			pos.setPosId(ponId + "-" + posPort); // rack-shelf-slot-pon-posId

			String intentEleXml = intentEle.asXML();
			pos.setTopPosId(getTopPosId(intentEleXml, ponId, fiberTarget)); // rack-shelf-slot-pon-posId
			pos.setRation(getFirstValFromIbn(intentEleXml, splitterXPath("ratio")));
			pos.setTopPosPort(getFirstValFromIbn(intentEleXml, splitterXPath("topPosPort")));
			return pos;
		}).collect(Collectors.toList());
	}

	private String getTopPosId(String intentEleXml, String ponId, String fiberTarget) {
		String topPosId = "";
		String upSplitter = getFirstValFromIbn(intentEleXml, splitterXPath("up-splitter"));
		if (!StringUtils.isEmpty(upSplitter)) {
			String posPort = upSplitter.replaceAll(fiberTarget + "_", "");
			topPosId = ponId + "-" + posPort;
		}
		return topPosId;
	}

	private void addLayerToPos(List<Pos> posList) {
		Map<String, Pos> posMap = createPosIdMap(posList);
		for (Iterator<Pos> iterator = posList.iterator(); iterator.hasNext();) {
			Pos pos = (Pos) iterator.next();
			int layer = getLayer(posMap, pos, 1);
			pos.setLayer(String.valueOf(layer));
		}
	}

	private Map<String, Pos> createPosIdMap(List<Pos> posList) {
		Map<String, Pos> posMap = new HashMap<String, Pos>();
		for (Iterator<Pos> iterator = posList.iterator(); iterator.hasNext();) {
			Pos pos = (Pos) iterator.next();
			posMap.put(pos.getPosId(), pos);
		}
		return posMap;
	}

	private int getLayer(Map<String, Pos> posMap, Pos pos, int i) {
		if (StringUtils.isEmpty(pos.getTopPosId())) {
			return i;
		}

		Pos newPos = (Pos) posMap.get(pos.getTopPosId());
		if (StringUtils.isEmpty(newPos.getTopPosId())) {
			return ++i;
		}
		return getLayer(posMap, newPos, ++i);
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
}
