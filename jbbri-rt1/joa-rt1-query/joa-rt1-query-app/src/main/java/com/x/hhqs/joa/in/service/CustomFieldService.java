package com.x.hhqs.joa.in.service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.x.hhqs.joa.in.vo.customField.CustomField;
import com.x.hhqs.joa.in.vo.customField.OssCommand;
import com.x.hhqs.joa.in.vo.customField.OssCommands;

@Service
public class CustomFieldService extends FileAlterationListenerAdaptor {

	@Value("${service.config-path}")
	private String configPath;

	private static final String CUSTOM_FIELD_XML = "customField.xml";

	private Map<String, OssCommand> ossCommandMap = new HashMap<>();

	private static final Logger LOG = LoggerFactory.getLogger(CustomFieldService.class);

	@PostConstruct
	public void init() throws Exception {
		parseXML();
		monitorTemplate();
	}

	public OssCommand getOssCommand(Class<?> voClass) {
		if (voClass.isAnnotationPresent(CustomField.class)) {
			String ossName = voClass.getAnnotation(CustomField.class).ossName();
			return ossCommandMap.get(ossName);
		} else {
			return null;
		}
	}

	private void parseXML() {
		try {
			ossCommandMap.clear();
			File file = new File(configPath, CUSTOM_FIELD_XML);
			JAXBContext jaxbContext = JAXBContext.newInstance(OssCommands.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			OssCommands ossCommands = (OssCommands) jaxbUnmarshaller.unmarshal(file);
			for (OssCommand command : ossCommands.getOssCommands()) {
				ossCommandMap.put(command.getName(), command);
			}
		} catch (Exception e) {
			LOG.error("parseXML " + CUSTOM_FIELD_XML + " error", e);
		}
	}

	private void monitorTemplate() throws Exception {
		IOFileFilter filter = FileFilterUtils.and(FileFilterUtils.fileFileFilter(),
				FileFilterUtils.nameFileFilter(CUSTOM_FIELD_XML));
		FileAlterationObserver observer = new FileAlterationObserver(new File(configPath), filter);
		observer.addListener(this);
		new FileAlterationMonitor(TimeUnit.SECONDS.toMillis(10), observer).start();
	}

	@Override
	public void onFileChange(File file) {
		LOG.info("onFileChange " + file);
		parseXML();
	}

	@Override
	public void onFileCreate(File file) {
		LOG.info("onFileCreate " + file);
		parseXML();
	}

	@Override
	public void onFileDelete(File file) {
		LOG.info("onFileDelete " + file);
		ossCommandMap.clear();
	}

}
