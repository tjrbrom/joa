package com.x.hhqs.joa.common.rt1fwk.impl;

import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.rt1fwk.api.DeviceQueryService;
import com.x.hhqs.joa.common.rt1fwk.api.OssCommandParser;
import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.rt1fwk.pojo.DeviceInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.util.CommonConstants;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * as a common parser, it can parse Onu/Pon/Olt
 * if ONUID exists, then invoke queryOnu
 * else if PONPORTID exists, then invoke queryPon
 * else invoke queryOlt
 * for each process, construct different sbimetadata
 */
@Component("COMMON.parser")
public class OssCommandParserImpl implements OssCommandParser {

    @Autowired
    private DeviceQueryService queryService;

    @Autowired
    private JoaMessage i18n;


    private static final Pattern PTN = Pattern.compile("^[A-Z]{2}\\d+\\.[A-Z]{3}\\d+$");

    @Override
    public OssCmdMeta parse(RT1InputMessage rt1InputMsg) throws OssCommandException {

        //get nbiMeta from rt1InputMsg
        OssCmdMeta metaData = OssCommandParser.super.parse(rt1InputMsg);
        Map<String, String> nbiMeta = metaData.getNbiMetadata();
        Map<String, String> sbiMeta = metaData.getSbiMetadata();
        //get oltType/oltVersion/sbiMeta from DeviceQueryService
        DeviceInfo devInfo;

        if (notBank(nbiMeta.get(CommonConstants.OLTID)) && notBank(nbiMeta.get(CommonConstants.PONID)) &&
                notBank(nbiMeta.get(CommonConstants.ONUIDTYPE)) && notBank(nbiMeta.get(CommonConstants.ONUID))) {
            //ONU Query
            devInfo = queryService.queryOnu(nbiMeta.get(CommonConstants.OLTID), nbiMeta.get(CommonConstants.PONID),
                    nbiMeta.get(CommonConstants.ONUIDTYPE), nbiMeta.get(CommonConstants.ONUID));

        } else if (notBank(nbiMeta.get(CommonConstants.OLTID)) && notBank(nbiMeta.get(CommonConstants.PONID))) {
            //PON Query
            devInfo = queryService.queryPon(nbiMeta.get(CommonConstants.OLTID), nbiMeta.get(CommonConstants.PONID));

        } else if (notBank(nbiMeta.get(CommonConstants.OLTID))) {
            //OLT Query
            devInfo = queryService.queryOlt(nbiMeta.get(CommonConstants.OLTID));

        } else if(notBank(nbiMeta.get(CommonConstants.ONUIP))) {
            //ONU ip address or ONU name
            devInfo = queryService.queryOnu(nbiMeta.get(CommonConstants.ONUIP));

        } else {
            throw new OssCommandException(CommonConstants.IMP, i18n.getMessage(CommonConstants.EADD_PREFIX +
                    "oltid.notexist"));
        }
        metaData.setOltType(devInfo.getOltType());
        metaData.setOltVersion(devInfo.getOltVersion());
        metaData.setServiceType(devInfo.getOnuType());

        metaData.setOltIP(devInfo.getOltIP());
        metaData.setOltName(devInfo.getOltName());
        metaData.setPap(devInfo.getPap());

        metaData.setPonType(devInfo.getPonType());
        if(isValid(devInfo.getPonId())) {
            metaData.setSlotNo(devInfo.getPonId().split("\\.")[0].substring(2));
            metaData.setPonNo(devInfo.getPonId().split("\\.")[1].substring(3));
        }

        metaData.setOnuNo(devInfo.getOnuAid());
        metaData.setOnuName(devInfo.getOnuName());
        metaData.setOnuIP(devInfo.getOnuIP());
        metaData.setOnuSerNum(devInfo.getOnuSerNum());
        metaData.setOnuSlid(devInfo.getOnuSlid());
        metaData.setOnuLoid(devInfo.getOnuLoid());

        sbiMeta.put(CommonConstants.OLT_TARGETNAME, devInfo.getOltTargetName());
        if(devInfo.getPonTargetName() != null) {
            sbiMeta.put(CommonConstants.PON_TARGETNAME, devInfo.getPonTargetName());
        }
        if(devInfo.getOnuTargetName() != null) {
            sbiMeta.put(CommonConstants.ONU_TARGETNAME, devInfo.getOnuTargetName());
        }


        return metaData;

    }

    private boolean isValid(String ponId) {

        return ponId != null && PTN.matcher(ponId).matches();
    }
}
