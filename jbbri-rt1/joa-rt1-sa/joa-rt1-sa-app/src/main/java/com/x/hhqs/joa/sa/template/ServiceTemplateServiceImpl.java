package com.x.hhqs.joa.sa.template;

import com.x.hhqs.joa.sa.api.ServiceTemplateService;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@DependsOn("configurationCopy")
public class ServiceTemplateServiceImpl extends FileAlterationListenerAdaptor implements ServiceTemplateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceTemplateServiceImpl.class);

    private Document document;

    private static final String SERVICE_XML = "service.xml";

    @Value("${service.config-path}")
    private String configPath;

    public void setDocument(Document document) {
        this.document = document;
    }

    @Override
    public String getValueUsingXPath(String xpath) throws XPathExpressionException {
        return XPathFactory
                .newInstance()
                .newXPath()
                .compile(xpath)
                .evaluate(document);
    }

    @Override
    public Element getElementUsingXPath(String xpath) throws XPathExpressionException {
        return (Element) XPathFactory
            .newInstance()
            .newXPath()
            .evaluate(xpath, document, XPathConstants.NODE);
    }

    @PostConstruct
    public void init() {
        try {
            document = parseTemplateContext(new File(configPath, SERVICE_XML));
            LOGGER.info("Load service template: {}/{}", configPath, SERVICE_XML);
            monitorTemplate();
            LOGGER.info("Start monitor template: {}/{}", configPath, SERVICE_XML);
        } catch (Exception e) {
            LOGGER.error("Error occurred while ServiceTemplateServiceImpl.init()", e);
        }
    }

    private void monitorTemplate() throws Exception {
        IOFileFilter filter = FileFilterUtils.and(FileFilterUtils.fileFileFilter(),FileFilterUtils.nameFileFilter(SERVICE_XML));
        FileAlterationObserver observer = new FileAlterationObserver(new File(configPath), filter);
        observer.addListener(this);
        new FileAlterationMonitor(TimeUnit.SECONDS.toMillis(10), observer).start();
    }

    private Document parseTemplateContext(File configFile) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(configFile);
    }

    @Override
    public void onFileChange(File file) {
        try {
            document = parseTemplateContext(file);
            LOGGER.info("Service template modified: {}", file.getAbsoluteFile());
        } catch (Exception e) {
            LOGGER.error("Error occurred while ServiceTemplateServiceImpl.onFileChange()", e);
        }
    }
}
