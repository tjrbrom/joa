package com.x.hhqs.joa.mds.sync;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.x.hhqs.joa.common.restclient.processor.HttpRequestBaseProcessor;
import com.x.hhqs.joa.mds.emsclient.ac.MdsRestClient;
import com.x.hhqs.joa.mds.model.Device;
import com.x.hhqs.joa.mds.model.Manager;
import com.x.hhqs.joa.mds.model.ManagerNode;
import com.x.hhqs.joa.mds.model.ManagerType;
import freemarker.template.Configuration;
import freemarker.template.Template;

@Service
public class NavSyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NavSyncService.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MdsRestClient restClient;

    @Autowired
    private Configuration freeMarkerConfig;

    @Autowired
    private SyncTaskConfig taskConfig;

    public void syncDevices(ManagerNode context) {
        LOGGER.info("Starting device synchronization for Manager {}", context.getManager());

        try {
            Manager manager = context.getManager();
            manager.setLastSyncTime(DATE_TIME_FORMATTER.format(LocalDateTime.now()));

            String rpc = buildRpc(manager.getName());
            JSONObject jsonObject = restClient.processNetconfRequest(setHttpEntity(rpc));

            if (jsonObject.getBoolean("error")) {
                LOGGER.error("Error when fetch devices, {}", jsonObject.getString("errorMessage"));
                LOGGER.error("{}", jsonObject);
                return;
            }

            List<Device> devices = parseDevices(jsonObject.getString("response"))
                .values().stream()
                .map(d -> {
                    d.setManagerName(manager.getName());
                    d.setManagerType(ManagerType.NAV);
                    return d;
                })
                .collect(Collectors.toList());

            context.setDevices(devices);
            LOGGER.info("Fetched {} devices from Core.", devices.size());

            manager.setLastSuccessSyncTime(DATE_TIME_FORMATTER.format(LocalDateTime.now()));
            LOGGER.info("Device synchronization for {} completed.", context.getManager());
        } catch (Exception e) {
            LOGGER.error("Device synchronization failed for Manager:" + context.getManager().getName(), e);
        }

    }

    public static Map<String, Device> parseDevices(String xml) throws Exception {
        Map<String, Device> result = new LinkedHashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();//NOSONAR
        factory.setNamespaceAware(true);
        Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes()));

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(new CustomNamespaceContext());

        NodeList intents = (NodeList) xpath.evaluate("//ibn:intent", doc, XPathConstants.NODESET);

        for (int i = 0; i < intents.getLength(); i++) {
            Device device = new Device();

            Node intent = intents.item(i);
            String target = xpath.evaluate("ibn:target", intent);
            device.setName(target);

            String ipAddress = extractField(xpath, intent, "ip-address");
            String deviceVersion = extractField(xpath, intent, "device-version");
            String hardwareType = extractField(xpath, intent, "hardware-type");

            device.setIpAddress(ipAddress);
            device.setDeviceVersion(deviceVersion);
            device.setHardwareType(hardwareType);
            device.setFamilyTypeRelease(hardwareType + "-" + deviceVersion);

            result.put(target, device);
        }

        return result;
    }

    static String extractField(XPath xpath, Node intent, String fieldName) throws Exception {
        String expression = String.format(".//*[local-name()='%s']", fieldName);
        String value = xpath.evaluate(expression, intent);
        if (value == null || value.isBlank()) {
            LOGGER.error(nodeToString(intent));
            throw new IllegalArgumentException("Missing required field: " + fieldName + " in intent node: " +
                xpath.evaluate("ibn:target", intent));
        }
        return value;
    }

    static String nodeToString(Node node) {
        try {
            StringWriter writer = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            return node.toString();
        }
    }

    static class CustomNamespaceContext implements NamespaceContext {
        private final Map<String, String> ns = Map.of(
            "ibn", "http://www.x.com/management-solutions/ibn");

        public String getNamespaceURI(String prefix) {
            return ns.getOrDefault(prefix, XMLConstants.NULL_NS_URI);
        }

        public String getPrefix(String uri) {
            return ns.entrySet().stream()
                .filter(e -> e.getValue().equals(uri))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
        }

        public Iterator<String> getPrefixes(String uri) {
            return ns.keySet().iterator();
        }
    }

    private HttpRequestBaseProcessor<HttpPost> setHttpEntity(String rpc) {
        return method -> method.setEntity(new StringEntity(rpc, ContentType.APPLICATION_XML));
    }

    String buildRpc(String deviceManager) throws Exception {
        Template template = freeMarkerConfig.getTemplate("getDevices.ftl");

        Map<String, Object> model = new HashMap<>();
        model.put("intentTypes", taskConfig.getIntentTypes());
        model.put("deviceManager", deviceManager);

        StringWriter writer = new StringWriter();
        template.process(model, writer);

        return writer.toString();
    }

}
