package com.x.hhqs.joa.in.service;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.x.hhqs.joa.in.exception.ErrorKey;
import com.x.hhqs.joa.in.exception.Rt1QueryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import freemarker.template.Configuration;
import freemarker.template.Template;

@Component
public class NetconfRpcBuilder {

	@Autowired
	private Configuration freemarkerConfig;

	public String buildRpcRequest(String ftlName) throws Rt1QueryException {
		return buildRpcRequest(ftlName, new HashMap<String, Object>());
	}

	public String buildRpcRequest(String ftlName, Map<String, Object> templateArgs) throws Rt1QueryException {
		try {
			Template template = freemarkerConfig.getTemplate(ftlName);
			StringWriter writer = new StringWriter();
			template.process(templateArgs, writer);
			return writer.toString();
		} catch (Exception e) {
			throw new Rt1QueryException(ErrorKey.FTL_RENDER_ERROR, new String[] { ftlName }, e);
		}
	}

}
