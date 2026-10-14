package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.NbiConsts;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.ErrorKey;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.service.NetconfRpcBuilder;
import com.x.hhqs.joa.in.service.NetconfService;
import com.x.hhqs.joa.in.util.ConvertUtil;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.vo.Onu;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

@Component("LST-ONU")
public class LstOnuHandler extends AbstractHandler {

	private static final String TITLE = "List of Onu";

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	@Override
	protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
		List<Onu> onuList = new ArrayList<Onu>();
		if (OssCmdMetaParser.isOnuCmd(ossCmdMeta)) {
			onuList = getOnuListByOnuId(ossCmdMeta);
		} else if (OssCmdMetaParser.isPonCmd(ossCmdMeta)) {
			onuList = getOnuListByPon(ossCmdMeta);
		} else if (OssCmdMetaParser.isOltCmd(ossCmdMeta)) {
			onuList = getOntListByOlt(ossCmdMeta);
		} else {
			throw new Rt1QueryException(ErrorKey.INVALID_COMMAND);
		}

		String[][] rtrvArray = createRtrvArray(onuList, Onu.class);
		return createOssCmdResult(TITLE, rtrvArray);
	}

	private List<Onu> getOntListByOlt(OssCmdMeta ossCmdMeta)
			throws Rt1QueryException, IOException, DocumentException, JAXBException, SAXException {
		String oltIp = OssCmdMetaParser.oltIpVal(ossCmdMeta);
		String deviceTarget = OssCmdMetaParser.oltTarget(ossCmdMeta);
		Map<String, String> ponIdMapper = createPonIdMapper(deviceTarget);

		List<Onu> onuList = new ArrayList<Onu>();
		for (Iterator<String> iterator = ponIdMapper.keySet().iterator(); iterator.hasNext();) {
			String fiberTarget = (String) iterator.next();
			String ponId = ponIdMapper.get(fiberTarget);
			onuList.addAll(getOnuListByFiberTarget(oltIp, fiberTarget, ponId));
		}
		Collections.sort(onuList);
		return onuList;
	}

	private List<Onu> getOnuListByPon(OssCmdMeta ossCmdMeta)
			throws JAXBException, SAXException, IOException, DocumentException, Rt1QueryException {
		String oltIp = OssCmdMetaParser.oltIpVal(ossCmdMeta);
		String ponId = OssCmdMetaParser.ponIdVal(ossCmdMeta);
		String fiberTarget = OssCmdMetaParser.fiberTarget(ossCmdMeta);
		return getOnuListByFiberTarget(oltIp, fiberTarget, ponId);
	}

	private List<Onu> getOnuListByFiberTarget(String oltIp, String fiberTarget, String ponId)
			throws Rt1QueryException, IOException, DocumentException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("fiberTarget", fiberTarget);

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_ONT_BY_PON_FTL, templateArgs);
		List<Element> intentEleList = netconfService.sendRequestAndParseResults(rpcRequest, intentXPath());
		return createOnuListFromIntentList(intentEleList, oltIp, ponId);
	}

	private List<Onu> getOnuListByOnuId(OssCmdMeta ossCmdMeta)
			throws JAXBException, SAXException, IOException, DocumentException, Rt1QueryException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, IntrospectionException {
		String oltIp = OssCmdMetaParser.oltIpVal(ossCmdMeta);
		String ponId = OssCmdMetaParser.ponIdVal(ossCmdMeta);

		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("ontTarget", OssCmdMetaParser.ontTarget(ossCmdMeta));

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_ONT_FTL, templateArgs);
		List<Element> intentEleList = netconfService.sendRequestAndParseResults(rpcRequest, intentXPath());
		return createOnuListFromIntentList(intentEleList, oltIp, ponId);
	}

	private List<Onu> createOnuListFromIntentList(List<Element> intentEleList, String oltIp, String ponId) {
		return intentEleList.stream().map(intentEle -> {
			Onu ont = new Onu();
			ont.setOltId(oltIp);
			ont.setPonId(ponId);
			ont.setName(getElementVal(intentEle, "target"));
			ont.setTarget(ont.getName());

			// parse ont intent
			String intentEleXml = intentEle.asXML();
			ont.setOnuNo(getFirstValFromIbn(intentEleXml, ontStateXPath("onu-id")));
			ont.setOnuType(getFirstValFromIbn(intentEleXml, ontXPath("ont-type")));
			ont.setDesc(getFirstValFromIbn(intentEleXml, ontXPath("ont-description")));
			ont.setMac(getMac(intentEleXml));
			ont.setPwd(getFirstValFromIbn(intentEleXml, ontXPath("expected-registration-id")));
			ont.setLoid(getFirstValFromIbn(intentEleXml, ontXPath("expected-loid")));
			ont.setSwver(getFirstValFromIbn(intentEleXml, ontStateXPath("actual-active-software-on-device")));
			ont.setAuth(getOnuAuth(intentEleXml));
			return ont;
		}).collect(Collectors.toList());
	}

	private String getOnuAuth(String intentEleXml) {
		String mac = getFirstValFromIbn(intentEleXml, ontXPath("expected-serial-number"));
		String password = getFirstValFromIbn(intentEleXml, ontXPath("expected-registration-id"));
		String loid = getFirstValFromIbn(intentEleXml, ontXPath("expected-loid"));
		
		String auth = "";
		if (!StringUtils.isEmpty(mac) && !StringUtils.isEmpty(password)) {
			auth = NbiConsts.AUTH_SN_PASSWORD;
		} else if (!StringUtils.isEmpty(mac)) {
			auth = NbiConsts.AUTH_MAC;
		} else if (!StringUtils.isEmpty(password)) {
			auth = NbiConsts.AUTH_PASSWORD;
		} else if (!StringUtils.isEmpty(loid)) {
			auth = NbiConsts.AUTH_LOID;
		}
		return auth;
	}

	private String getMac(String intentEleXml) {
		String expectedSerialNumber = getFirstValFromIbn(intentEleXml, ontXPath("expected-serial-number"));
		String detectedSerialNumber = getFirstValFromIbn(intentEleXml, ontStateXPath("detected-serial-number"));
		return StringUtils.hasText(detectedSerialNumber) ? detectedSerialNumber : expectedSerialNumber;
	}

	// fiberTargetName:1-1-<slot>-<pon>
	private Map<String, String> createPonIdMapper(String deviceFxTarget)
			throws JAXBException, SAXException, IOException, DocumentException, Rt1QueryException {
		Map<String, String> ponIdMapper = new HashMap<String, String>();
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("deviceId", deviceFxTarget);

		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_FIBER_FTL, templateArgs);
		List<Element> intentEleList = netconfService.sendRequestAndParseResults(rpcRequest, intentXPath());
		intentEleList.stream().forEach(intentEle -> {
			String fiberTarget = getElementVal(intentEle, "target");
			String ponIdDesc = getFirstValFromIbn(intentEle.asXML(), fiberPonPortXPath("pon-id"));
			ponIdMapper.put(fiberTarget, ConvertUtil.ponIdDescToPonId(ponIdDesc));
		});
		return ponIdMapper;
	}

	private String ontXPath(String leaf) {
		return "//ont:ont/ont:" + leaf;
	}

	private String ontStateXPath(String leaf) {
		return "//ont:ont-state/ont:" + leaf;
	}

	private String fiberPonPortXPath(String leaf) {
		return "//fiber:fiber/fiber:pon-port/fiber:" + leaf;
	}

	private String intentXPath() {
		return "//ibn:ibn/ibn:intent";
	}
}
