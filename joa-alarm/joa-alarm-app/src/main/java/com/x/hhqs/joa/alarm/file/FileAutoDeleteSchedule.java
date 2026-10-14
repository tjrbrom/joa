package com.x.hhqs.joa.alarm.file;

import com.x.hhqs.joa.alarm.service.FileDeleteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class FileAutoDeleteSchedule {

    @Resource
    private FileDeleteService fileDeleteService;

    @Resource
    private DestinationNode destinationNode;

    @Resource
    private FileConfigLoader fileConfigLoader;


    @Scheduled(cron = "0 0 3 * * ?")
//    @Scheduled(cron = "0 */5 * * * *")
    public void deleteExpiredFilesOnDestinationNode() {
        log.info("start delete expired files schedule.");
        if (!destinationNode.isFileDelete()) {
            log.warn("AutoDelete function isn't enable, you can set <file-delete>true</file-delete> to enable it.");
            return;
        }

        FileDeleteStrategy strategy = fileConfigLoader.getFileDeleteStrategy();

        if (strategy == null) {
            log.warn("FileDeleteStrategy is invalid, please check config file.");
            return;
        }
        log.info("{}",strategy);

        try {
            if (strategy.getType().equals(FileDeleteStrategy.Type.ALIVE_TIME)) {
                fileDeleteService.deleteFilesByAliveTime(destinationNode, strategy.getAliveTime(), strategy.getTimeUnit());
            }

            else if (strategy.getType().equals(FileDeleteStrategy.Type.QUANTITY_LIMIT)) {
                fileDeleteService.deleteFilesByQuantityLimit(destinationNode, strategy.getMaxQuantity());
            }

        } catch (Exception e) {
            log.error("",e);
        }

        log.info("finish delete expired files schedule.");
    }


}
