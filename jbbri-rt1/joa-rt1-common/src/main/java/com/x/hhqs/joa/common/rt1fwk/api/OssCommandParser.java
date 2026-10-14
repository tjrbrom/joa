package com.x.hhqs.joa.common.rt1fwk.api;

import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import com.x.hhqs.joa.rt1message.RT1Param;
import com.x.hhqs.joa.rt1message.RT1PayloadBlock;
import com.x.hhqs.joa.common.util.CommonConstants;

import java.util.ArrayList;
import java.util.List;


/**
 get TID, AID, params from RT1InputMessage;
 find OLT/ONU, and retrieve metadata of OLT or ONU
 */
public interface OssCommandParser {
    default OssCmdMeta parse(RT1InputMessage message) throws OssCommandException {
        OssCmdMeta metaData = new OssCmdMeta();
        if (notBank(message.getCommandCode())) {
            metaData.getNbiMetadata().put(CommonConstants.CODE,message.getCommandCode());
        }
        if (notBank(message.getAid())) {
            parseString(message.getAid(), metaData);
        }
        if (notBank(message.getTid())) {
            parseString(message.getTid(), metaData);
        }
        if (notBank(message.getCTag())) {
            metaData.getNbiMetadata().put(CommonConstants.CTAG, message.getCTag());
        }
        if (notBank(message.getGeneralBlock())) {
            parseString(message.getGeneralBlock(), metaData);
        }
        if (notEmpty(message.getPayloadBlock())) {
            for (RT1PayloadBlock payloadBlock : message.getPayloadBlock()) {
                parseBlock(payloadBlock, metaData);
            }
        }
        return metaData;
    }

//   default OssCmdMeta splitAID(OssCmdMeta metaData) {
//        //split  PONID to SLOTID and PONPORTID
//        String ponId = metaData.getNbiMetadata().get(CommonConstants.PONID);
//        if(notBank(ponId) && ponId.split("-").length == 4){
//            metaData.getNbiMetadata().put(CommonConstants.SLOTID, ponId.split("-")[2]);
//            metaData.getNbiMetadata().put(CommonConstants.PONPORTID, ponId.split("-")[3]);
//        }
//        //split ONUPORT to ONUCARD and ONUPORTID
//        String onuPort = metaData.getNbiMetadata().get(CommonConstants.ONUPORT);
//        if(notBank(onuPort) && onuPort.split("-").length == 4){
//            metaData.getNbiMetadata().put(CommonConstants.ONUCARD, onuPort.split("-")[2]);
//            metaData.getNbiMetadata().put(CommonConstants.ONUPORTID, onuPort.split("-")[3]);
//        }
//        //split PORTID to ONUCARD and ONUPORTID
//        String portId = metaData.getNbiMetadata().get(CommonConstants.PORTID);
//        if(notBank(portId) && portId.split("-").length == 4){
//            metaData.getNbiMetadata().put(CommonConstants.ONUCARD, portId.split("-")[2]);
//            metaData.getNbiMetadata().put(CommonConstants.ONUPORTID, portId.split("-")[3]);
//        }
//
//        //TODO whether need to remove PONID and ONUPORT? KEEP THEM
//        return metaData;
//    }

    default boolean notBank(String str) {
        return str != null && str.trim().length() != 0;
    }

    default boolean notEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

    default void parseBlock(RT1PayloadBlock payloadBlock, OssCmdMeta metaData) {
        ArrayList<RT1Param> params = payloadBlock.getParam();
        if (notEmpty(params)) {
            for (RT1Param rt1Param : params) {
                metaData.getNbiMetadata().put(rt1Param.getParamName().trim(), rt1Param.getParamValue().trim());
            }
        }
    }

    default void parseString(String string, OssCmdMeta metaData) {
        //TODO delete ""
        if (string.contains(",")) {
            String[] splits = string.split(",", -1);
            for (String split : splits) {
                if (notBank(split)) {
                    if (split.contains("=")) {
                        String[] internSplit = split.split("=", -1);
                        metaData.getNbiMetadata().put(internSplit[0].trim(), internSplit[1].trim());
                    } else {
                        metaData.getNbiMetadata().put(split.trim(), "");
                    }
                }
            }
        } else {
            if(string.contains("=")) {
                String[] internSplit = string.split("=", -1);
                metaData.getNbiMetadata().put(internSplit[0].trim(), internSplit[1].trim());
            } else {
                metaData.getNbiMetadata().put(string.trim(), "");
            }

        }
    }
}
