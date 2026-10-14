package com.x.hhqs.joa.sa.api;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;

/**
 * checkTemplateUsingName() is needed for validation
 */
public interface FreeMarkerTemplateService {
    String renderTemplateUsingName(String templateName, Map<String, Object> model) throws IOException, TemplateException;
}
