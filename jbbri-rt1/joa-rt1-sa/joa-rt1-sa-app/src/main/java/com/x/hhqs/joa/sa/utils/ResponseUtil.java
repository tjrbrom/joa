package com.x.hhqs.joa.sa.utils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResponseUtil {

    private ResponseUtil() {
    }

    public static String getValueByXPath(String xml, String xpath) throws DocumentException {
        Document document = DocumentHelper.parseText(xml);

        Map<String, String> uris = new HashMap<>();
        uris.put("ibn", "http://www.x.com/management-solutions/ibn");
        uris.put("dmf", "http://www.x.com/management-solutions/device-mf");
        uris.put("fiber", "http://www.x.com/management-solutions/fiber");
        uris.put("ont", "http://www.x.com/management-solutions/ont");
        uris.put("l2infra", "http://www.x.com/management-solutions/l2-infra");
        uris.put("l2user", "http://www.x.com/management-solutions/l2-user");

        XPath xPath = document.createXPath(xpath);
        xPath.setNamespaceURIs(uris);
        Node node1 = xPath.selectSingleNode(document);
        if (node1 != null) {
            return node1.getText();
        }
        return null;
    }

    public static String getValueByRegex(String response, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
