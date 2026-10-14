package com.x.hhqs.joa.sa.api;

import org.w3c.dom.Element;

import javax.xml.xpath.XPathExpressionException;

public interface ServiceTemplateService {

    String getValueUsingXPath(String xpath) throws XPathExpressionException;

    Element getElementUsingXPath(String xpath) throws XPathExpressionException;
}
