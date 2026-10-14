package com.x.hhqs.joa.sa.utils;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * 1. Consider changing to class tools in the future;
 * 2. The modification takes effect in real time.
 */
public class XPathReader implements Closeable {

    private InputStream inputStream;
    private Document document;


    public void load(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
        this.inputStream = inputStream;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        this.document = builder.parse(inputStream);
    }

    public String query(String exp) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile(exp);
        return (String) expr.evaluate(document, XPathConstants.STRING);
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }

}
