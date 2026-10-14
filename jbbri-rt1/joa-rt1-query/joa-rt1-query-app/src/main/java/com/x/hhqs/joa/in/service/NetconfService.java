package com.x.hhqs.joa.in.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.util.RpcUtil;

@Service
public class NetconfService {
	@Autowired
	private RestClientService restClientService;

	private static final Logger LOG = LoggerFactory.getLogger(NetconfService.class);

	public String sendRequest(String netconfReqXml)
			throws IOException, DocumentException, JSONException, Rt1QueryException {
		LOG.info(RpcUtil.formatXml(netconfReqXml));
		String response = restClientService.processNetconfRequest(netconfReqXml).getString("response");
		LOG.info(RpcUtil.formatXml(response));
		return response;
	}

	public Element sendRequestAndParseFirstResult(String netconfReqXml, String xPathExpr)
			throws IOException, DocumentException, JSONException, Rt1QueryException {
		return sendRequestAndParseFirstResult(netconfReqXml, xPathExpr, RpcConsts.MF_IBN_NS);
	}

	public Element sendRequestAndParseFirstResult(String netconfReqXml, String xPathExpr, Map<String, String> nsUris)
			throws IOException, DocumentException, JSONException, Rt1QueryException {
		String rpcResponse = sendRequest(netconfReqXml);
		return RpcUtil.getFirstElementByXPath(rpcResponse, xPathExpr, nsUris);
	}

	public String sendRequestAndParseFirstValue(String netconfReqXml, String xPathExpr)
			throws IOException, DocumentException, JSONException, Rt1QueryException {
		return sendRequestAndParseFirstValue(netconfReqXml, xPathExpr, RpcConsts.MF_IBN_NS);
	}
	public String sendRequestAndParseFirstValue(String netconfReqXml, String xPathExpr, Map<String, String> nsUris)
			throws IOException, DocumentException, JSONException, Rt1QueryException{
		String rpcResponse = sendRequest(netconfReqXml);
		return getFirstValueByXPathWithoutErr(rpcResponse, xPathExpr, nsUris);
	}


	public Map<String, String> sendRequestAndParseFirstValue(String netconfReqXml, Map<String, String> xPathExprMap)
			throws IOException, DocumentException, JSONException, Rt1QueryException {
		return sendRequestAndParseFirstValue(netconfReqXml, xPathExprMap, RpcConsts.MF_IBN_NS);
	}

	public Map<String, String> sendRequestAndParseFirstValue(String netconfReqXml, Map<String, String> xPathExprMap,
			Map<String, String> nsUris) throws IOException, DocumentException, JSONException, Rt1QueryException {
		String rpcResponse = sendRequest(netconfReqXml);
		return parseFirstValueFromResponse(rpcResponse, xPathExprMap, nsUris);
	}

	public List<Element> sendRequestAndParseResults(String netconfReqXml, String xPathExpr)
			throws IOException, DocumentException, JSONException, Rt1QueryException {
		String rpcResponse = sendRequest(netconfReqXml);
		return RpcUtil.getElementsByXPath(rpcResponse, xPathExpr, RpcConsts.MF_IBN_NS);
	}

	public List<String> sendRequestAndParseValues(String netconfReqXml, String xPathExpr)
			throws IOException, DocumentException, JSONException, Rt1QueryException {
		String rpcResponse = sendRequest(netconfReqXml);
		return RpcUtil.getValuesByXPath(rpcResponse, xPathExpr, RpcConsts.MF_IBN_NS);
	}

	public String sendRequestToVirtualizer(String netconfReqXml)
			throws IOException, DocumentException, JSONException, Rt1QueryException {
		LOG.info(RpcUtil.formatXml(netconfReqXml));
		String response = restClientService.processAvNetconfRequest(netconfReqXml).getString("response");
		LOG.info(RpcUtil.formatXml(response));
		return response;
	}

	public Element sendRequestToVirtualizerAndParseFirstResult(String netconfReqXml, String xPathExpr)
			throws IOException, DocumentException, JSONException, Rt1QueryException {
		String rpcResponse = sendRequestToVirtualizer(netconfReqXml);
		return RpcUtil.getFirstElementByXPath(rpcResponse, xPathExpr, RpcConsts.ANV_NS);
	}

	public String sendRequestToVirtualizerAndParseFirstResultAsXml(String netconfReqXml, String xPathExpr)
			throws IOException, DocumentException, JSONException, Rt1QueryException {
		Element element = sendRequestToVirtualizerAndParseFirstResult(netconfReqXml, xPathExpr);
		return element == null ? "" : element.asXML();
	}

	public List<Element> sendRequestToVirtualizerAndParseResults(String netconfReqXml, String xPathExpr)
			throws IOException, DocumentException, JSONException, Rt1QueryException {
		String rpcResponse = sendRequestToVirtualizer(netconfReqXml);
		return RpcUtil.getElementsByXPath(rpcResponse, xPathExpr, RpcConsts.ANV_NS);
	}

	public String sendRequestToVirtualizerAndParseFirstValue(String netconfReqXml, String xPathExpr)
			throws IOException, DocumentException, JSONException, Rt1QueryException {
		String rpcResponse = sendRequestToVirtualizer(netconfReqXml);
		return getFirstValueByXPathWithoutErr(rpcResponse, xPathExpr, RpcConsts.ANV_NS);
	}

	public Map<String, String> sendRequestToVirtualizerAndParseFirstValue(String netconfReqXml,
			Map<String, String> xPathExprMap) throws IOException, DocumentException, JSONException, Rt1QueryException {
		String rpcResponse = sendRequestToVirtualizer(netconfReqXml);
		return parseFirstValueFromResponse(rpcResponse, xPathExprMap, RpcConsts.ANV_NS);
	}

	public List<String> sendRequestToVirtualizerAndParseValues(String netconfReqXml, String xPathExpr)
			throws IOException, DocumentException, JSONException, Rt1QueryException {
		String rpcResponse = sendRequestToVirtualizer(netconfReqXml);
		return RpcUtil.getValuesByXPath(rpcResponse, xPathExpr, RpcConsts.ANV_NS);
	}

	private Map<String, String> parseFirstValueFromResponse(String rpcResponse, Map<String, String> xPathExprMap,
			Map<String, String> nsUris) throws DocumentException {
		Map<String, String> resultMap = new HashMap<String, String>();
		xPathExprMap.forEach(
				(key, xPathExpr) -> resultMap.put(key, getFirstValueByXPathWithoutErr(rpcResponse, xPathExpr, nsUris)));
		return resultMap;
	}

	private String getFirstValueByXPathWithoutErr(String xml, String xPathExpr, Map<String, String> nsUris) {
		try {
			return RpcUtil.getFirstValueByXPath(xml, xPathExpr, nsUris);
		} catch (Exception e) {
//			LOG.warn("getFirstValueByXPathWithoutErr error, xPath:" + xPathExpr + ", errorMsg:" + e.getMessage());
			return "";
		}
	}
}
