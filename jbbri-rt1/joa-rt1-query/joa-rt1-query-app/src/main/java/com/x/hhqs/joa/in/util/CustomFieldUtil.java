package com.x.hhqs.joa.in.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.dom4j.DocumentException;

import com.x.hhqs.joa.in.consts.RpcConsts;
import com.x.hhqs.joa.in.exception.ErrorKey;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import com.x.hhqs.joa.in.vo.NbiResponseDefinition;
import com.x.hhqs.joa.in.vo.customField.Attribute;
import com.x.hhqs.joa.in.vo.customField.CustomField;
import com.x.hhqs.joa.in.vo.customField.Rpc;

public class CustomFieldUtil {

	private static final String RPC_TYPE_SINGLE = "single";

	private static final String RPC_TYPE_MULTIPLE = "multiple";

	/**
	 * <rpc file="customField/QueryDeviceMf.ftl" type="multiple" targetName="deviceTarget">
	 * create template arguments map for specified ftl
	 * 
	 * @return
	 *         <li>type:single, Map<deviceTarget, target value></li>
	 *         <li>type:multiple, Map<deviceTarget, target value list></li>
	 * 
	 */
	public static Map<String, Object> createRpcTemplateArgs(Rpc rpc, List<?> voList, Class<?> voClass)
			throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			Rt1QueryException {
		Map<String, Object> templateArgs = new HashMap<String, Object>();
		for (Field field : voClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(CustomField.class)) {
				CustomField annotation = field.getAnnotation(CustomField.class);
				if (rpc.getTargetName().equals(annotation.fieldName())) {
					if (RPC_TYPE_SINGLE.equals(rpc.getType())) {
						String targetVal = getFieldValueFromVo(voList.get(0), field.getName(), voClass);
						templateArgs.put(rpc.getTargetName(), targetVal);
					} else if (RPC_TYPE_MULTIPLE.equals(rpc.getType())) {
						List<String> targetList = new ArrayList<String>();
						for (Object vo : voList) {
							String targetVal = getFieldValueFromVo(vo, field.getName(), voClass);
							targetList.add(targetVal);
						}
						templateArgs.put(rpc.getTargetName(), targetList);
					} else {
						throw new Rt1QueryException(ErrorKey.INVALID_ARGUMENT, new String[] { rpc.getType() });
					}
				}
			}
		}
		return templateArgs;
	}

	/**
	 * create xPath placeholder map for specified VO field which has CustomField annotation. 
	 * key:@CustomField.fieldName, value:vo value
	 * 
	 */
	public static Map<String, String> createXpathPlaceholderMap(Object vo, Class<?> voClass)
			throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Map<String, String> rpcPlaceholderMap = new HashMap<>();
		for (Field field : voClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(CustomField.class)) {
				CustomField annotation = field.getAnnotation(CustomField.class);
				String value = getFieldValueFromVo(vo, field.getName(), voClass);
				rpcPlaceholderMap.put(annotation.fieldName(), value);
			}
		}
		return rpcPlaceholderMap;
	}

	public static String parseCustomFieldValueFromResponse(String rpcResponse, Attribute rpcAttribute,
			Map<String, String> xPathPlaceholderMap) throws DocumentException {
		String xPathExpr = rpcAttribute.getXPath();
		for (Map.Entry<String, String> entry : xPathPlaceholderMap.entrySet()) {
			xPathExpr = xPathExpr.replace("${" + entry.getKey() + "}", entry.getValue());
		}
		return getFirstValueByXPath(rpcResponse, xPathExpr);
	}

	public static void updateCustomerFieldValueToVo(Attribute rpcAttribute, String cfValue, Object vo, Class<?> voClass)
			throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Optional<Field> optionalField = Arrays.stream(voClass.getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(NbiResponseDefinition.class)
						&& rpcAttribute.getName().equals(field.getAnnotation(NbiResponseDefinition.class).fieldName()))
				.findFirst();
		if (optionalField.isPresent()) {
			Field field = optionalField.get();
			PropertyDescriptor prop = new PropertyDescriptor(field.getName(), voClass);
			prop.getWriteMethod().invoke(vo, cfValue);
		}
	}

	private static String getFieldValueFromVo(Object vo, String name, Class<?> voClass)
			throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		PropertyDescriptor prop = new PropertyDescriptor(name, voClass);
		return (String) prop.getReadMethod().invoke(vo, new Object[] {});
	}

	private static String getFirstValueByXPath(String rpcResponse, String xPathExpr) {
		try {
			return RpcUtil.getFirstValueByXPath(rpcResponse, xPathExpr, RpcConsts.MF_IBN_NS);
		} catch (Exception e) {
			return "";
		}
	}
}
