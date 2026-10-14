package com.x.hhqs.joa.rt1agent.service;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.x.hhqs.joa.common.restclient.RestClient;
import com.x.hhqs.joa.common.restclient.processor.HttpRequestBaseProcessor;
import freemarker.template.Configuration;
import freemarker.template.Template;

@Service
public class AcRestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AcRestService.class);

    @Autowired(required = false)
    private RestClient restClient;

    @Autowired
    private Configuration freeMarkerConfig;

    public Map<String, Set<String>> getPapGroupMap() throws Exception {
        if (restClient == null) {
            throw new Exception("AC RestClient is required, please check CORE Rest Client Properties.");
        }

        try {
            String rpc = buildPapManagementRpc();
            LOGGER.debug(rpc);

            JSONObject jsonObject = restClient.processNetconfRequest(false, setHttpEntity(rpc));
            if (jsonObject.getBoolean("error")) {
                LOGGER.error("Error when fetch paps, {}", jsonObject.getString("errorMessage"));
                LOGGER.error("{}", jsonObject);
                throw new Exception("Error when fetch paps.");
            }
            return parsePapGroups(jsonObject.getString("response"));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    private HttpRequestBaseProcessor<HttpPost> setHttpEntity(String rpc) {
        return method -> method.setEntity(new StringEntity(rpc, ContentType.APPLICATION_XML));
    }

    protected String buildPapManagementRpc() throws Exception {
        Template template = freeMarkerConfig.getTemplate("getPaps.ftl");

        StringWriter writer = new StringWriter();
        template.process(new HashMap<>(), writer);

        return writer.toString();
    }

    protected Map<String, Set<String>> parsePapGroups(String xml) throws Exception {
        Map<String, Set<String>> result = new HashMap<>();

        SAXReader reader = new SAXReader();
        Map<String, String> nsMap = new HashMap<>();
        nsMap.put("pap", "http://www.x.com/management-solutions/x-sdnc-pap-management");
        reader.getDocumentFactory().setXPathNamespaceURIs(nsMap);

        Document document = reader.read(new ByteArrayInputStream(xml.getBytes()));
        List<Node> groupNodes = document.selectNodes("//pap:pap-group");

        for (Node groupNode : groupNodes) {
            String groupName = groupNode.selectSingleNode("pap:pap-group-name").getText();
            List<Node> listNodes = groupNode.selectNodes("pap:pap-list");
            Set<String> papList = new HashSet<>();
            for (Node listNode : listNodes) {
                papList.add(listNode.getText());
            }
            result.put(groupName.toUpperCase(), papList);
        }

        return result;
    }

}
