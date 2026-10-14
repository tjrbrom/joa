package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.rt1fwk.api.OssCommandHandler;
import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.service.CustomFieldService;
import com.x.hhqs.joa.in.service.NetconfRpcBuilder;
import com.x.hhqs.joa.in.service.NetconfService;
import com.x.hhqs.joa.in.util.CustomFieldUtil;
import com.x.hhqs.joa.in.util.RpcUtil;
import com.x.hhqs.joa.in.vo.NbiResponseDefinition;
import com.x.hhqs.joa.in.vo.customField.Attribute;
import com.x.hhqs.joa.in.vo.customField.OssCommand;
import com.x.hhqs.joa.in.vo.customField.Rpc;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractHandler implements OssCommandHandler {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractHandler.class);

	@Autowired
	private JoaMessage messageSource;

	@Autowired
	private NetconfService netconfService;

	@Autowired
	private NetconfRpcBuilder netconfRpcBuilder;

	@Autowired
	private CustomFieldService customFieldService;

	// ut is not execute in SpringBoot environment. it need to set nbiSpec manually.
	@Value("${nbispec:CM}")
	private String nbiSpec;

	protected abstract OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception;

	@Override
	public OssCmdResult execute(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws OssCommandException {
		try {
			LOG.info("execute RT1:"+ossCmdMeta);
			return executeRt1Msg(ossCmdMeta, userinfo);
		} catch (Rt1QueryException qe) {
			LOG.error("execute error: ", qe);
			throw new OssCommandException(qe.getErrorCode(), getMessage(qe.getMessageKey(), qe.getMessageObjs()));
		} catch (Exception e) {
			LOG.error("execute error: " + e.getMessage(), e);
			throw new OssCommandException("EEEH", e.getMessage());
		}
	}

	protected String[][] createRtrvArray(Object vo, Class<?> voClass)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IntrospectionException,
			Rt1QueryException, JSONException, IOException, DocumentException {
		return createRtrvArray(Arrays.asList(vo), voClass);
	}

	protected String[][] createRtrvArray(List<?> voList, Class<?> voClass)
			throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			Rt1QueryException, JSONException, IOException, DocumentException {
		updateCustomField(voList, voClass);
		sortVoList(voList);
		Field[] fields = Arrays.stream(voClass.getDeclaredFields()).filter(field -> needOutputField(field, voClass))
				.toArray(Field[]::new);
		String[][] rtrvArray = new String[fields.length][voList.size() + 1];

		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			// first line of table (OLTID,PONID,ONUNO ...)
			rtrvArray[i][0] = field.getAnnotation(NbiResponseDefinition.class).fieldName();
			for (int j = 0; j < voList.size(); j++) {
				Object vo = voList.get(j);
				PropertyDescriptor prop = new PropertyDescriptor(field.getName(), vo.getClass());
				rtrvArray[i][j + 1] = (String) prop.getReadMethod().invoke(vo, new Object[] {});
			}
		}
		return rtrvArray;
	}

	protected String[][] addToRtrvArray(String[][] rtrvArray, List<?> voList, Class<?> voClass)
			throws IntrospectionException, IllegalAccessException, InvocationTargetException {
		if (voList == null || voList.isEmpty()) {
			return rtrvArray;
		}

		int originalCols = rtrvArray[0].length;
		int newCols = originalCols + voList.size();
		int rows = rtrvArray.length;
		String[][] newArray = new String[rows][newCols];
		for (int i = 0; i < rows; i++) {
			System.arraycopy(rtrvArray[i], 0, newArray[i], 0, originalCols);
		}

		sortVoList(voList);
		Field[] fields = Arrays.stream(voClass.getDeclaredFields())
				.filter(field -> needOutputField(field, voClass))
				.toArray(Field[]::new);
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			for (int j = 0; j < voList.size(); j++) {
				Object vo = voList.get(j);
				PropertyDescriptor prop = new PropertyDescriptor(field.getName(), vo.getClass());
				Object value = prop.getReadMethod().invoke(vo);
				newArray[i][originalCols + j] = value != null ? value.toString() : "";
			}
		}
		return newArray;
	}

	protected OssCmdResult createOssCmdResult(String title, String[][] rtrvArray) {
		OssCmdResult result = new OssCmdResult(true);
		result.setRtrvTitle(title);
		result.setRtrvArray(rtrvArray);
		return result;
	}

	protected String getOnuNoByTarget(String ontTarget)
			throws Rt1QueryException, JSONException, IOException, DocumentException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		templateArgs.put("ontTarget", ontTarget);
		String rpcRequest = netconfRpcBuilder.buildRpcRequest(RpcConsts.GET_ONT_NO_BY_TARGET_FTL, templateArgs);
		return netconfService.sendRequestAndParseFirstValue(rpcRequest, "//ont:ont-state/ont:onu-id");
	}

	protected String getElementVal(Element element, String leaf) {
		Element ele = element.element(leaf);
		return ele == null ? "" : ele.getText();
	}

	protected String getFirstValFromIbn(String xml, String xPathExpr) {
		try {
			return RpcUtil.getFirstValueByXPath(xml, xPathExpr, RpcConsts.MF_IBN_NS);
		} catch (Exception e) {
			return "";
		}
	}

	protected List<String> getValsFromIbn(String xml, String xPathExpr) {
		try {
			return RpcUtil.getValuesByXPath(xml, xPathExpr, RpcConsts.MF_IBN_NS);
		} catch (Exception e) {
			return Arrays.asList();
		}
	}

	protected String getFirstValFromAnv(String xml, String xPathExpr) {
		try {
			return RpcUtil.getFirstValueByXPath(xml, xPathExpr, RpcConsts.ANV_NS);
		} catch (Exception e) {
			return "";
		}
	}

	private void updateCustomField(List<?> voList, Class<?> voClass)
			throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			Rt1QueryException, JSONException, IOException, DocumentException {
		if (voList.size() == 0) {
			return;
		}

		OssCommand ossCommand = customFieldService.getOssCommand(voClass);
		if (ossCommand == null) {
			return;
		}

		LOG.info("add custom field to " + ossCommand);
		for (Iterator<Rpc> ossCommandIterator = ossCommand.getRpc().iterator(); ossCommandIterator.hasNext();) {
			Rpc rpc = (Rpc) ossCommandIterator.next();
			Map<String, Object> templateArgs = CustomFieldUtil.createRpcTemplateArgs(rpc, voList, voClass);
			String rpcRequest = netconfRpcBuilder.buildRpcRequest(rpc.getFile(), templateArgs);
			String rpcResponse = netconfService.sendRequest(rpcRequest);

			for (Iterator<?> voListIterator = voList.iterator(); voListIterator.hasNext();) {
				Object vo = (Object) voListIterator.next();
				Map<String, String> xPathPlaceholderMap = CustomFieldUtil.createXpathPlaceholderMap(vo, voClass);

				for (Iterator<Attribute> rpcAttributesIter = rpc.getAttributes().iterator(); rpcAttributesIter
						.hasNext();) {
					// <attribute name="ALIAS" xPath="..."/>
					Attribute rpcAttribute = (Attribute) rpcAttributesIter.next();
					String cfValue = CustomFieldUtil.parseCustomFieldValueFromResponse(rpcResponse, rpcAttribute,
							xPathPlaceholderMap);
					CustomFieldUtil.updateCustomerFieldValueToVo(rpcAttribute, cfValue, vo, voClass);
				}
			}
		}
	}

	private boolean needOutputField(Field field, Class<?> voClass) {
		NbiResponseDefinition annotation = field.getAnnotation(NbiResponseDefinition.class);
		if (annotation == null || annotation.ignored()) {
			return false;
		}

		// <ignore> <attribute name="TOPOLOC" /><attribute name="DSTAT" /> </ignore>
		OssCommand ossCommand = customFieldService.getOssCommand(voClass);
		if (ossCommand != null) {
			List<String> ignoreList = ossCommand.getIgnoreAttributes().stream().map(attribute -> attribute.getName())
					.collect(Collectors.toList());
			return !ignoreList.contains(annotation.fieldName());
		}

		String fieldSpec = annotation.nbiSpec();
		if (!StringUtils.hasText(fieldSpec)) {
			return true;
		} else {
			return fieldSpec.equalsIgnoreCase(nbiSpec);
		}
	}

	private String getMessage(String key, Object[] obj) {
		return messageSource.getMessage(key, obj);
	}

	private void sortVoList(List<?> voList) {
		Comparator<Object> comparator = new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				Comparable<Object> comparable1 = (Comparable<Object>) o1;
				Comparable<Object> comparable2 = (Comparable<Object>) o2;
				return comparable1.compareTo(comparable2);
			}
		};
		Collections.sort(voList, comparator);
	}
}
