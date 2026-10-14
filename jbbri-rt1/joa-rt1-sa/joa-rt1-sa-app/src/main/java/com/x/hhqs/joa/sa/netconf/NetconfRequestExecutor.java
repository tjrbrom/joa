package com.x.hhqs.joa.sa.netconf;

import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.restclient.RestClient;
import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.sa.api.FreeMarkerTemplateService;
import com.x.hhqs.joa.sa.model.ConditionEvaluator;
import com.x.hhqs.joa.sa.model.ModelDataContainer;
import com.x.hhqs.joa.sa.model.ModelDataProcessor;
import com.x.hhqs.joa.sa.model.ResultSelector;
import com.x.hhqs.joa.sa.utils.SystemConstants;
import com.x.hhqs.joa.sa.api.RequestExecutor;
import com.x.hhqs.joa.sa.api.SbiRequest;
import com.x.hhqs.joa.sa.api.SbiResponse;
import freemarker.template.TemplateException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NetconfRequestExecutor implements RequestExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetconfRequestExecutor.class);

    @Autowired
    private RestClient restClient;

    @Autowired
    private FreeMarkerTemplateService freeMarkerTemplateService;

    @Autowired
    private Map<String, ModelDataProcessor> processorMap;

    @Autowired
    private Map<String, ResultSelector> selectorMap;

    @Autowired
    private JoaMessage joaMessage;

    @Autowired
    private ConditionEvaluator conditionEvaluator;

    @Override
    public SbiResponse execute(List<SbiRequest> requestList) throws OssCommandException, TemplateException, IOException {
        StopWatch stopWatch = new StopWatch("NetconfRequestExecutor");
        try {
            for (SbiRequest request : requestList) {
                NetconfRequest netconfRequest = (NetconfRequest) request;
                if (!conditionEvaluator.evaluate(netconfRequest.getCondition())) {
                    continue;
                }
                stopWatch.start(netconfRequest.getTemplate());
                Map<String, Object> model = new HashMap<>();
                for (NetconfRequest.Data data : netconfRequest.getModels()) {
                    ModelDataProcessor processor = processorMap.get(data.getType());
                    if (processor == null) {
                        return new NetconfResponse(true, joaMessage.getMessage(SystemConstants.UNSUPPORTED_MODEL_DATA_TYPE, new Object[]{data.getType()}));
                    }
                    try {
                        processor.process(data, model);
                    } catch (OssCommandException e) {
                        LOGGER.error("Error occurred while processing model data ", e);
                        return new NetconfResponse(true, e.getErrDetail());
                    }
                }
                String rpc = freeMarkerTemplateService.renderTemplateUsingName(netconfRequest.getTemplate(), model);
                JSONObject response = restClient.processNetconfRequest(netconfRequest.isHistory(), method ->
                        ((HttpPost) method).setEntity(new StringEntity(rpc, ContentType.APPLICATION_JSON)));
                LOGGER.info("Response: {}", response);
                if (response.getBoolean(SystemConstants.ERROR)) {
                    stopWatch.stop();
                    return new NetconfResponse(response.getBoolean(SystemConstants.ERROR),
                            response.getString(SystemConstants.ERROR_MESSAGE),
                            response.getString(SystemConstants.REQUEST_TYPE),
                            response.getString(SystemConstants.RESPONSE));
                }
                if (SystemConstants.HTTP_GET.equals(response.getString(SystemConstants.REQUEST_TYPE))) {
                    String rpcResponse = response.getString(SystemConstants.RESPONSE);
                    LOGGER.info("RPC Response: {}", rpcResponse);
                    if (netconfRequest.getSelectors() != null) {
                        for (NetconfRequest.Selector selector : netconfRequest.getSelectors()) {
                            ResultSelector resultSelector = selectorMap.get(selector.getType() + "Selector");
                            if (resultSelector == null) {
                                stopWatch.stop();
                                return new NetconfResponse(true, joaMessage.getMessage(SystemConstants.UNSUPPORTED_SELECTOR_TYPE, new Object[]{selector.getType()}));
                            }
                            try {
                                resultSelector.select(selector, rpcResponse);
                            } catch (OssCommandException e) {
                                stopWatch.stop();
                                LOGGER.error("Error occurred while selecting result data ", e);
                                return new NetconfResponse(true, e.getErrDetail());
                            }
                        }
                    }
                }
                stopWatch.stop();
            }
        } finally {
            // only for test
            Map<String, String> nbiMeta = ModelDataContainer.getSharedData().getNbiMeta();
            LOGGER.info("## NBI metadata: {}", nbiMeta);
            Map<String, String> sbiMeta = ModelDataContainer.getSharedData().getSbiMeta();
            LOGGER.info("## SBI metadata: {}", sbiMeta);
            Map<String, String> context = ModelDataContainer.getSharedData().getContext();
            LOGGER.info("## Context data: {}", context);
            Map<String, String> onuInfo = ModelDataContainer.getSharedData().getOnuInfo();
            LOGGER.info("## ONU info: {}", onuInfo);
            Map<String, String> userInfo = ModelDataContainer.getSharedData().getUserInfo();
            LOGGER.info("## User info: {}", userInfo);
            String prettyPrint = stopWatch.prettyPrint();
            LOGGER.info("## \n {}", prettyPrint);
        }
        return new NetconfResponse(false, null, SystemConstants.HTTP_POST, SystemConstants.SUCCESS);
    }
}


















