package com.x.hhqs.joa.in.parser;

import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.rt1fwk.pojo.DeviceInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.api.DeviceQueryService;
import com.x.hhqs.joa.common.rt1fwk.api.OssCommandParser;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import com.x.hhqs.joa.common.util.CommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * OssOltCommandParserImpl is only used to parse single olt command(LST-CMD::
 * OLTID=olt-name:CTAG::;) and entire network command(LST-CMD:::CTAG::;)
 * 
 * @author lli021
 *
 */
@Component("OLT.parser")
public class OssOltCommandParserImpl implements OssCommandParser {

	@Autowired
	private DeviceQueryService queryService;

	@Autowired
	private JoaMessage i18n;

	@Override
	public OssCmdMeta parse(RT1InputMessage rt1InputMsg) throws OssCommandException {
		// get nbiMeta from rt1InputMsg
		OssCmdMeta metaData = OssCommandParser.super.parse(rt1InputMsg);
		Map<String, String> nbiMeta = metaData.getNbiMetadata();
		Map<String, String> sbiMeta = metaData.getSbiMetadata();

		// get oltType/oltVersion/sbiMeta from DeviceQueryService
		DeviceInfo devInfo;
		if (StringUtils.hasText(nbiMeta.get(CommonConstants.OLTID))
				&& StringUtils.isEmpty(nbiMeta.get(CommonConstants.PONID))
				&& StringUtils.isEmpty(nbiMeta.get(CommonConstants.ONUIDTYPE))
				&& StringUtils.isEmpty(nbiMeta.get(CommonConstants.ONUID))) {
			// OLT Query
			devInfo = queryService.queryOlt(nbiMeta.get(CommonConstants.OLTID));
			metaData.setOltType(devInfo.getOltType());
			metaData.setOltVersion(devInfo.getOltVersion());
			metaData.setServiceType(devInfo.getOnuType());
			metaData.setOltIP(devInfo.getOltIP());
			metaData.setOltName(devInfo.getOltName());
			metaData.setPap(devInfo.getPap());
			metaData.setPonType(devInfo.getPonType());

			sbiMeta.put(CommonConstants.OLT_TARGETNAME, devInfo.getOltTargetName());
		} else if (StringUtils.isEmpty(nbiMeta.get(CommonConstants.OLTID))
				&& StringUtils.isEmpty(nbiMeta.get(CommonConstants.PONID))
				&& StringUtils.isEmpty(nbiMeta.get(CommonConstants.ONUIDTYPE))
				&& StringUtils.isEmpty(nbiMeta.get(CommonConstants.ONUID))) {
			// entire network
			// do nothing
		} else {
			throw new OssCommandException(CommonConstants.IMP,
					i18n.getMessage(CommonConstants.ENDESC_PREFIX + CommonConstants.IMP));
		}
		return metaData;
	}
}
