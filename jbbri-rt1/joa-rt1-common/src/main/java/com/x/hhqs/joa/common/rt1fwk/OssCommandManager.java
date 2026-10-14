package com.x.hhqs.joa.common.rt1fwk;

import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.rt1fwk.api.*;
import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.rt1fwk.pojo.*;
import com.x.hhqs.joa.common.util.CommonConstants;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * It should be a daemon to load/reload OSS xml files in runtime
 * return command list (that is supported by the rt1 service)
 * reflecting instance based by command name, to process the command request.
 */
@Service
@DependsOn("configurationCopy")
public class OssCommandManager extends FileAlterationListenerAdaptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OssCommandManager.class);

    private Map<String, OssCommand> ossCommandMap = new HashMap<>();

    @Autowired
    private Map<String, OssCommandParser> parsers;

    @Autowired
    private Map<String, OssCommandHandler> handlers;

    @Autowired
    private Map<String, OutputMsgBuilder> builders;

    @Autowired
    private JoaMessage i18n;

    @Autowired
    private RT1ServiceUpdater updater;

    @Value("${command.file-path}")
    private String commandFilePath;

    @PostConstruct
    public void init() throws Exception {

        File directory = new File(commandFilePath);

        // check dir exists
        if (!directory.exists() || !directory.isDirectory()) {
            LOGGER.error("No xml found under {} directory.", commandFilePath);
            throw new Exception(commandFilePath + " is not exist, please check!");
        }

        findOssCommandTemplatesRecursively(directory, false);

        updater.initOssCmds(ossCommandMap.keySet());
        monitorOssCommandPath();
    }
    private void monitorOssCommandPath() throws Exception {
        IOFileFilter filter = FileFilterUtils.and(FileFilterUtils.fileFileFilter(),FileFilterUtils.suffixFileFilter(".xml"));
        FileAlterationObserver observer = new FileAlterationObserver(new File(commandFilePath), filter);
        observer.addListener(this);
        new FileAlterationMonitor(TimeUnit.SECONDS.toMillis(10), observer).start();
    }
    public void onFileCreate(File file) {
        LOGGER.info("File is added: " + file.getName());
        this.findOssCommandTemplatesRecursively(file, true);
    }

    public void onFileChange(File file) {
        LOGGER.info("File is modified:" + file.getName());
        String commandCode = file.getName().substring(0, file.getName().lastIndexOf("."));
        ossCommandMap.remove(commandCode);
        this.findOssCommandTemplatesRecursively(file, false);
    }

    public void onFileDelete(File file) {

        LOGGER.info("File is deleted:" + file.getName());
        String commandCode = file.getName().substring(0, file.getName().lastIndexOf("."));
        this.ossCommandMap.remove(commandCode);

        updater.updateOssCmd(commandCode, 1);
    }


    private void findOssCommandTemplatesRecursively(File directory, boolean needNotify) {
//        File[] files = directory.listFiles();

        if(directory.isDirectory()){
            File[] files = directory.listFiles();
            if(files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // if dir, recursive
                        findOssCommandTemplatesRecursively(file, needNotify);
                    } else if (file.isFile() && file.getName().toLowerCase().endsWith(".xml")) {
                        // if it is xml file and can be parsed, add it into the ossCommandMap
                        LOGGER.info("Load OSS Command file " + file.getName());
                        OssCommand ossCommand = generateOssCommand(file);
                        if(ossCommand != null) {
                            ossCommandMap.put(ossCommand.getCmdName(), ossCommand);
                            if(needNotify) {
                                updater.updateOssCmd(ossCommand.getCmdName(), 0);
                            }
                        }
                    }
                }
            }

        } else  {
            if (directory.isFile() && directory.getName().toLowerCase().endsWith(".xml")) {
                // if it is xml file and can be parsed, add it into the ossCommandMap
                LOGGER.info("Load OSS Command file " + directory.getName());
                OssCommand ossCommand = generateOssCommand(directory);
                if(ossCommand != null) {
                    ossCommandMap.put(ossCommand.getCmdName(), ossCommand);
                    if(needNotify) {
                        updater.updateOssCmd(ossCommand.getCmdName(), 0);
                    }
                }
            }

        }

    }


    private OssCommand generateOssCommand(File file) {


        try {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(file);
            Element rootEle = doc.getRootElement();

            OssCommand ossCommand = new OssCommand();

            //1. get cmdName
            ossCommand.setCmdName(file.getName().substring(0, file.getName().lastIndexOf(".")));

            //2. get validator
            if(rootEle.element("validation") == null) {
                throw new Exception("validation not found");
            }
            Element valEle = rootEle.element("validation");
            Validation valid = new Validation();
            valid.setEnabled(Boolean.valueOf(valEle.attributeValue("enabled")));
            for (Iterator<Element> it = valEle.elementIterator(); it.hasNext(); ) {
                Element element = it.next();
                String name = element.getName();
                switch (name) {
                    case "tid" :
                        valid.setTid(new ValidAttribute(element.getName(), element.attributeValue(
                                "regexpr"), Boolean.valueOf(element.attributeValue("required"))));
                        break;
                    case "aid":
                        List<Map<String, ValidAttribute>> aidGroups = new ArrayList<>();
                        for (Iterator<Element> groupIt = element.elementIterator(); groupIt.hasNext(); ) {
                            Element groupEle = groupIt.next();
                            Map<String, ValidAttribute> map = new HashMap<>();
                            for (Iterator<Element> attrIt = groupEle.elementIterator(); attrIt.hasNext(); ) {
                                Element attrEle = attrIt.next();
                                map.put(attrEle.attributeValue("name"), new ValidAttribute(attrEle.attributeValue("name"),
                                        attrEle.attributeValue("regexpr"), Boolean.valueOf(attrEle.attributeValue("required"))));
                            }
                            aidGroups.add(map);
                        }
                        valid.setAidGroups(aidGroups);
                        break;
                    case "ctag" :
                        valid.setCtag(new ValidAttribute(element.getName(), element.attributeValue(
                                "regexpr"), Boolean.valueOf(element.attributeValue("required"))));
                        break;
                    case "param" :
                        List<Map<String, ValidAttribute>> paramGroups = new ArrayList<>();
                        for (Iterator<Element> blockIt = element.elementIterator(); blockIt.hasNext(); ) {
                            Element blockEle = blockIt.next();
                            Map<String, ValidAttribute> map = new HashMap<>();
                            for (Iterator<Element> attrIt = blockEle.elementIterator(); attrIt.hasNext(); ) {
                                Element attrEle = attrIt.next();
                                map.put(attrEle.attributeValue("name"), new ValidAttribute(attrEle.attributeValue("name"),
                                        attrEle.attributeValue("regexpr"), Boolean.valueOf(attrEle.attributeValue("required"))));
                            }
                            paramGroups.add(map);
                        }

                        valid.setParamBlocks(paramGroups);
                        break;
                    default:
                        LOGGER.warn("Invalid name " + name + " in validation:" + file.getName());
                }

            }
            ossCommand.setValidation(valid);

            //3. get parser
            String id = rootEle.element("parser").attributeValue("id");
            ossCommand.setParser(parsers.get(id));

            //4. get handlers
            Map<DeviceInfo, OssCommandHandler> handlerMap =  new HashMap<>();
            for(Iterator<Element> handlerIter = rootEle.element("handlers").elementIterator("handler");
                handlerIter.hasNext();) {
                Element handler = handlerIter.next();
                id = handler.attributeValue("id");
                OssCommandHandler handlerInstance = handlers.get(id);

                DeviceInfo deviceInfo = new DeviceInfo();
                for(Iterator<Element> conditionIter = handler.elementIterator("condition");
                    conditionIter.hasNext();) {
                    Element condition = conditionIter.next();
                    String name =  condition.attributeValue("name");
                    String value = condition.attributeValue("value");

                    switch(name){
                        case "olttype" :
                            deviceInfo.setOltType(value);
                            break;
                        case "oltversion" :
                            deviceInfo.setOltVersion(value);
                            break;
                        default:
                            LOGGER.warn("Invalid name " + name + " in handler:" + file.getName());
                    }

                }

                handlerMap.put(deviceInfo, handlerInstance);
            }
            ossCommand.setHandlerMap(handlerMap);

            //5 get output msg builder
            Element builderEle = rootEle.element("outputMsgBuilder");
            id = builderEle.attributeValue("id");
            //TODO how about title?
            ossCommand.setBuilder(builders.get(id));

            return ossCommand;
        } catch (Exception e) {
            LOGGER.error("Parse oss command file " + file.getName() + " failed:", e);
            return null;
        }
    }

    public Set<String> getCommandList() {

        return ossCommandMap.keySet();
    }

    public Validation getValidation(String cmdName) throws OssCommandException {
        OssCommand ossCommand = ossCommandMap.get(cmdName);
        if(ossCommand != null && ossCommand.getValidation() != null) {
            return ossCommand.getValidation();
        }
        throw new OssCommandException(CommonConstants.SEOF, i18n.getMessage(CommonConstants.EADD_PREFIX +
                        "validator.notfound"));
    }

    public OssCommandParser getParser(String cmdName) throws OssCommandException {

        OssCommand ossCommand = ossCommandMap.get(cmdName);
        if(ossCommand != null && ossCommand.getParser() != null) {
            return ossCommand.getParser();
        }
        throw new OssCommandException(CommonConstants.SEOF, i18n.getMessage(CommonConstants.EADD_PREFIX +
                "parser.notfound"));
    }

    public OssCommandHandler getHandler(String cmdName, OssCmdMeta deviceInfo) throws OssCommandException {
        String oltType = deviceInfo.getOltType();
        String oltVersion =  deviceInfo.getOltVersion();
        // to avoid nullPointerException, set "" if it is null
        oltType = setEmptyString(oltType);
        oltVersion = setEmptyString(oltVersion);
        OssCommand ossCommand = ossCommandMap.get(cmdName);
        if(ossCommand != null) {
            Map<DeviceInfo, OssCommandHandler> map = ossCommand.getHandlerMap();
            for (Map.Entry<DeviceInfo, OssCommandHandler> entry : map.entrySet()) {
                DeviceInfo key = entry.getKey();
                OssCommandHandler value = entry.getValue();
                boolean isOltType = key.getOltType() == null || key.getOltType().equals(oltType);
                boolean isOltVersion = key.getOltVersion() == null ||  key.getOltVersion().equals(oltVersion);

                if(isOltType && isOltVersion && value != null) {
                    return value;
                }
            }
        }
        throw new OssCommandException(CommonConstants.SEOF, i18n.getMessage(CommonConstants.EADD_PREFIX +
                "handler.notfound"));
    }

    private String setEmptyString(String type) {
        if(type == null) {
            type = "";
        }
        return type;
    }

    public OutputMsgBuilder getOutputMsgBuilder(String cmdName) throws OssCommandException {
        OssCommand ossCommand = ossCommandMap.get(cmdName);
        if(ossCommand != null && ossCommand.getBuilder() != null) {
            return ossCommand.getBuilder();
        }
        throw new OssCommandException(CommonConstants.SEOF, i18n.getMessage(CommonConstants.EADD_PREFIX +
                "builder.notfound"));
    }

    public OutputMsgBuilder getGeneralBuilder() {
        return builders.get("COMMON.outputMsgBuilder");
    }


}
