package com.x.hhqs.joa.sa.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.rt1fwk.api.OssCommandHandler;
import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.common.util.CommonConstants;
import com.x.hhqs.joa.sa.api.ServiceTemplateService;
import com.x.hhqs.joa.sa.model.ModelDataContainer;
import com.x.hhqs.joa.sa.api.RequestExecutor;
import com.x.hhqs.joa.sa.api.SbiRequest;
import com.x.hhqs.joa.sa.api.SbiResponse;
import com.x.hhqs.joa.sa.netconf.NetconfRequest;
import com.x.hhqs.joa.sa.netconf.NetconfResponse;
import com.x.hhqs.joa.sa.template.ServiceTemplateIndex;
import com.x.hhqs.joa.sa.utils.BeanUtil;
import com.x.hhqs.joa.sa.utils.ResponseUtil;
import com.x.hhqs.joa.sa.utils.SystemConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class GeneralNetconfHandler implements OssCommandHandler {

    private static final String DIRECT_ASSIGNMENT = "directAssignment";
    private static final String NBI_META_DATA = "nbiMetaData";
    private static final String SBI_META_DATA = "sbiMetaData";
    private static final String CONTEXT_DATA = "contextData";
    private static final String PLACEHOLDER = "placeholder";
    private static final String ONU_INFO = "onuInfo";
    private static final String USER_INFO = "userInfo";

    @Autowired
    private ServiceTemplateService serviceTemplateService;

    @Autowired
    private RequestExecutor requestExecutor;

    @Autowired
    private JoaMessage joaMessage;

    @Override
    public OssCmdResult execute(OssCmdMeta metaData, UserInfo userInfo) throws OssCommandException {
        try {
            ModelDataContainer.SharedData sharedData = ModelDataContainer.getSharedData();
            sharedData.setNbiMeta(metaData.getNbiMetadata());
            sharedData.setSbiMeta(metaData.getSbiMetadata());
            sharedData.setOnuInfo(BeanUtil.toStringMap(metaData));
            sharedData.setUserInfo(BeanUtil.toStringMap(userInfo));
            ServiceTemplateIndex templateIndex = getTemplateIndex(metaData);
            List<SbiRequest> sbiRequestList = getSbiRequests(templateIndex);
            SbiResponse sbiResponse = requestExecutor.execute(sbiRequestList);
            return buildOssCmdResult(sbiResponse);
        } catch (Exception e) {
            throw new OssCommandException(CommonConstants.SEOF, e.getMessage(), e);
        } finally {
            ModelDataContainer.clearSharedData();
        }
    }

    private OssCmdResult buildOssCmdResult(SbiResponse sbiResponse) {
        NetconfResponse netconfResponse = (NetconfResponse) sbiResponse;
        if (netconfResponse.hasError()) {
            String errorMessage = netconfResponse.getErrorMessage();
            String response = netconfResponse.getResponse();
            if (response != null) {
                errorMessage = errorMessage + ": " + ResponseUtil.getValueByRegex(response, "<error-message>(.*?)</error-message>");
            }
            return new OssCmdResult(false, CommonConstants.SEOF, errorMessage);
        } else {
            return new OssCmdResult(true);
        }
    }

    private List<SbiRequest> getSbiRequests(ServiceTemplateIndex templateIndex) throws OssCommandException {
        int index = 1;
        List<SbiRequest> sbiRequestList = new ArrayList<>();
        while (true) {
            String rpcPath = String.format(SystemConstants.RPC_PATH, templateIndex.getOltType(), templateIndex.getPonType(),
                    templateIndex.getOnuType(), templateIndex.getCommandCode(), index);
            Element rpcElement;
            try {
                rpcElement = serviceTemplateService.getElementUsingXPath(rpcPath);
            } catch (XPathExpressionException e) {
                throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.XPATH_EXPRESSION_EXCEPTION, new Object[]{rpcPath}), e);
            }
            if (rpcElement == null) {
                break;
            }
            submitRequestList(rpcElement, sbiRequestList);
            index++;
        }
        if (sbiRequestList.isEmpty()) {
            throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.TEMPLATE_INDEX_NOT_FOUND, new Object[]{templateIndex}));
        }
        return sbiRequestList;
    }

    private void submitRequestList(Element rpcElement, List<SbiRequest> sbiRequestList) {
        NetconfRequest request = new NetconfRequest();
        request.setTemplate(rpcElement.getAttribute("file"));
        request.setCondition(rpcElement.getAttribute("condition"));
        NodeList model = rpcElement.getElementsByTagName("data");
        if (model.getLength() != 0) {
            List<NetconfRequest.Data> dataList = new ArrayList<>();
            for (int i = 0; i < model.getLength(); i++) {
                Element data = (Element) model.item(i);
                String name = data.getAttribute("name");
                String value = data.getAttribute("value");
                String type = getDataType(data.getAttribute("value"));
                Boolean optional = getOptional(data.getAttribute("optional"));
                Boolean toContext = getToContext(data.getAttribute("to-context"));
                dataList.add(new NetconfRequest.Data(name, value, type, optional, toContext));
            }
            request.setModels(dataList);
        }
        NodeList selectors = rpcElement.getElementsByTagName("selector");
        if (selectors.getLength() != 0) {
            List<NetconfRequest.Selector> selectorList = new ArrayList<>();
            for (int i = 0; i < selectors.getLength(); i++) {
                Element selector = (Element) selectors.item(i);
                selectorList.add(new NetconfRequest.Selector(selector.getAttribute("key"), selector.getAttribute("type"),
                        selector.getTextContent().trim(), Boolean.valueOf(selector.getAttribute("optional"))));
            }
            request.setSelectors(selectorList);
        }
        sbiRequestList.add(request);
    }

    private Boolean getToContext(String toContext) {
        return Boolean.valueOf(toContext);
    }

    private Boolean getOptional(String optional) {
        return Boolean.valueOf(optional);
    }

    private String getDataType(String value) {
        if (value != null) {
            if (value.matches("^\\$\\{nbiMetaData:\\w+}$")) {
                return NBI_META_DATA;
            } else if (value.matches("^\\$\\{sbiMetaData:\\w+}$")) {
                return SBI_META_DATA;
            } else if (value.matches("^\\$\\{context:\\w+}$")) {
                return CONTEXT_DATA;
            } else if (value.matches("^\\$\\{onuInfo:\\w+}$")) {
                return ONU_INFO;
            } else if (value.matches("^\\$\\{userInfo:\\w+}$")) {
                return USER_INFO;
            } else if (value.matches(".*\\$\\{(nbiMetaData|sbiMetaData|context|onuInfo|userInfo):\\w+}.*")) {
                return PLACEHOLDER;
            } else {
                return DIRECT_ASSIGNMENT;
            }
        }
        return DIRECT_ASSIGNMENT;
    }

    private ServiceTemplateIndex getTemplateIndex(OssCmdMeta metaData) {
        String oltType = metaData.getOltType();
        if (oltType == null) {
            oltType = "";
        }
        String ponType = metaData.getPonType();
        if (ponType == null) {
            ponType = "";
        }
        String onuType = metaData.getServiceType();
        Map<String, String> nbiMetadata = metaData.getNbiMetadata();
        if (onuType == null) {
            onuType = nbiMetadata.getOrDefault(CommonConstants.ONU_TYPE, "");
        }
        String commandCode = nbiMetadata.get(CommonConstants.CODE);
        if (commandCode == null) {
            commandCode = "";
        }
        String onuPort = nbiMetadata.get(CommonConstants.ONUPORT);
        if (onuPort != null && !onuPort.isEmpty()) {
            nbiMetadata.put(CommonConstants.ONUPORTID, onuPort.substring(onuPort.lastIndexOf("-") + 1));
        }
        return new ServiceTemplateIndex(oltType, ponType, onuType, commandCode);
    }
}
