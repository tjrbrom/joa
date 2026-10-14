package com.x.hhqs.joa.sa.template;

import com.x.hhqs.joa.sa.api.FreeMarkerTemplateService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.Map;

@Service
public class FreeMarkerTemplateServiceImpl implements FreeMarkerTemplateService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FreeMarkerTemplateServiceImpl.class);

    @Autowired
    private Configuration freeMarkerConfiguration;

    @Override
    public String renderTemplateUsingName(String templateName, Map<String, Object> model) throws IOException, TemplateException {
        Template template = freeMarkerConfiguration.getTemplate(templateName);
        String result = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
        LOGGER.info("render template: \n{}", result);
        return result;
    }
}
