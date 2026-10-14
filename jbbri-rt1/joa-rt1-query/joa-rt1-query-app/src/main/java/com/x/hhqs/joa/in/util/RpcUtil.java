package com.x.hhqs.joa.in.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.x.hhqs.joa.in.exception.ErrorKey;
import com.x.hhqs.joa.in.exception.Rt1QueryException;

public class RpcUtil {
	public static String getFirstValueByRegex(String xml, String regex) throws DocumentException, Rt1QueryException {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(xml);
		while (matcher.find()) {
			return matcher.group(1);
		}
		throw new Rt1QueryException(ErrorKey.XML_PARSE_ERROR, new String[] { regex });
	}

	public static String getFirstValueByXPath(String xml, String xPathExpr, Map<String, String> nsUris)
			throws DocumentException {
		BiFunction<XPath, Document, String> fun = (xPath, document) -> xPath.selectSingleNode(document).getText();
		return getResultByXPath(xml, xPathExpr, nsUris, fun);
	}

	public static List<String> getValuesByXPath(String xml, String xPathExpr, Map<String, String> nsUris)
			throws DocumentException {
		BiFunction<XPath, Document, List<String>> fun = (xPath, document) -> xPath.selectNodes(document).stream()
				.map(node -> node.getText()).collect(Collectors.toList());
		return getResultByXPath(xml, xPathExpr, nsUris, fun);
	}

	public static Element getFirstElementByXPath(String xml, String xPathExpr, Map<String, String> nsUris)
			throws DocumentException {
		BiFunction<XPath, Document, Element> fun = (xPath, document) -> (Element) xPath.selectSingleNode(document);
		return getResultByXPath(xml, xPathExpr, nsUris, fun);
	}

	public static List<Element> getElementsByXPath(String xml, String xPathExpr, Map<String, String> nsUris)
			throws DocumentException {
		BiFunction<XPath, Document, List<Element>> fun = (xPath, document) -> xPath.selectNodes(document).stream()
				.map(node -> (Element) node).collect(Collectors.toList());
		return getResultByXPath(xml, xPathExpr, nsUris, fun);
	}

	public static String formatXml(String original) throws IOException, DocumentException {
		Document document = DocumentHelper.parseText(original);
		OutputFormat format = OutputFormat.createPrettyPrint();
		StringWriter writer = new StringWriter();
		XMLWriter xmlWriter = new XMLWriter(writer, format);
		xmlWriter.write(document);
		xmlWriter.close();
		return writer.toString();
	}

	private static <T> T getResultByXPath(String xml, String xPathExpr, Map<String, String> nsUris,
			BiFunction<XPath, Document, T> fun) throws DocumentException {
		Document document = DocumentHelper.parseText(xml);
		XPath xPath = document.createXPath(xPathExpr);
		xPath.setNamespaceURIs(nsUris);
		return fun.apply(xPath, document);
	}

}
