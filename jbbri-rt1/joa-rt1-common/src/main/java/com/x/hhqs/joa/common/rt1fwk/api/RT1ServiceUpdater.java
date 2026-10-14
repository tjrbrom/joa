package com.x.hhqs.joa.common.rt1fwk.api;


import java.util.Set;

public interface RT1ServiceUpdater {
    /**
     * update metadata for nacos service instance
     * @param cmdCode the command code need to update to Nacos; like ADD-ONU,DEL-ONU,etc
     * @param addOrDelete   0: add,means cmdCode need to be added into current metadata
     *                      1:delete, means cmdCode need to be removed from current metadata
     * invoke namingService.registerInstance to update metadata for all the instance under the specific nacos service
     */
    public void updateOssCmd(String cmdCode, int addOrDelete);

    /**
     * invoked by OSSCommandManager.init()
     * @param cmdCodeSet
     * get supported command list and put them into NacosDiscoveryProperties metadata
     */
    public void initOssCmds(Set<String> cmdCodeSet);
}
