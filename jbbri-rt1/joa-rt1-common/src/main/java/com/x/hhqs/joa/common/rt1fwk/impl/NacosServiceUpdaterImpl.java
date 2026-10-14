package com.x.hhqs.joa.common.rt1fwk.impl;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.x.hhqs.joa.common.rt1fwk.api.RT1ServiceUpdater;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;


@Component
public class NacosServiceUpdaterImpl implements RT1ServiceUpdater {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(NacosServiceUpdaterImpl.class);

    @Autowired
    private NacosServiceManager nacosServiceManager;
    @Autowired
    private NacosDiscoveryProperties discoveryProperties;


    /**
     * update metadata for nacos service instance
     * @param cmdCode the command code need to update to Nacos; like ADD-ONU,DEL-ONU,etc
     * @param addOrDelete   0: add,means cmdCode need to be added into current metadata
     *                      1:delete, means cmdCode need to be removed from current metadata
     */
    public void updateOssCmd(String cmdCode, int addOrDelete)  {


        String cmdList = discoveryProperties.getMetadata().get("command-code");

        String metadataValue;
        cmdList = cmdList + ",";
        //some codes are similar, like CFG-LANPORT and CFG-LANPORTVLAN,
        //in order not to mix them ,add , in cmdCode to compare
        if(cmdList.contains(cmdCode + ",") && addOrDelete == 1){

            metadataValue = cmdList.replace(cmdCode + ",", "");
            if(metadataValue.endsWith(",")){
                metadataValue = metadataValue.substring(0, metadataValue.lastIndexOf(","));
            }

        }else if(!cmdList.contains(cmdCode + ",") && addOrDelete == 0){
            metadataValue = cmdList + cmdCode;
        }else{
            LOGGER.warn("No need to update nacos service instance. Current cmdCode=" + cmdCode +
                    ";NACOS metadata command-code=" + cmdList + ";addOrDelete=" + addOrDelete);
            return;
        }
        LOGGER.info("Start update metadata=" + metadataValue);
        discoveryProperties.getMetadata().put("command-code", metadataValue);

        NamingService namingService = nacosServiceManager.getNamingService(discoveryProperties.getNacosProperties());
        List<Instance> instanceList = null;
        try {
            instanceList = namingService.getAllInstances(discoveryProperties.getService());
            for (Instance instance : instanceList) {
                if(instance.getIp().equals(discoveryProperties.getIp())){
                    instance.getMetadata().put("command-code", metadataValue);
                    namingService.registerInstance(discoveryProperties.getService(), instance);
                }
            }
        } catch (NacosException e) {
            LOGGER.error("Update metadata failed:", e);
        }
        LOGGER.info("update command-code for nacos service " + discoveryProperties.getService() + " done");
    }

    @Override
    public void initOssCmds(Set<String> cmdSet) {

        String metadataValue = "";
        for(String cmdCode : cmdSet){
            metadataValue = metadataValue + cmdCode + ",";
        }
        if(metadataValue.endsWith(",")){
            metadataValue = metadataValue.substring(0, metadataValue.length() - 1);
        }
        LOGGER.info("Collected command list:" + metadataValue);
        discoveryProperties.getMetadata().put("command-code", metadataValue);

        LOGGER.info("init command-code for nacos service " + discoveryProperties.getService() + " done");

    }

}
