package com.x.hhqs.joa.common.rt1fwk.impl;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.rt1fwk.api.*;
import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.common.rt1fwk.OssCommandManager;
import com.x.hhqs.joa.common.rt1fwk.pojo.Validation;
import com.x.hhqs.joa.common.util.CommonConstants;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import com.x.hhqs.joa.rt1message.RT1OutputMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.activation.CommandInfo;
import java.util.Set;

@Service
public class OssCommandServiceImpl implements OssCommandService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OssCommandServiceImpl.class);

    @Autowired
    private OssCommandManager manager;

    @Autowired
    private OssCommandValidator validator;

    @Autowired
    private JoaMessage i18n;

    @Override
    public Set<String> getCommandCodes() {
        return manager.getCommandList();
    }

    @Override
    public String execute(String inputMessage, UserInfo userInfo) {
        OutputMsgBuilder builder = manager.getGeneralBuilder();//Spec?
        RT1InputMessage rt1InputMessage = null;
        try {
            //1.parse string msg to object
            rt1InputMessage = parseInputMessage(inputMessage);
            LOGGER.info("## parse finish");

            //2.check command exist, skip as already checked in rt1-agent.
            String code = rt1InputMessage.getCommandCode();
            if (!this.getCommandCodes().contains(code)) {
                throw new OssCommandException(CommonConstants.SENS, i18n.getMessage(CommonConstants.ENDESC_PREFIX +
                        CommonConstants.SENS));
            }
            LOGGER.info("## check command finish");

            //3.validation
            Validation validation = manager.getValidation(code);
            validator.validate(rt1InputMessage, validation);

            LOGGER.info("## validation finish");

            //4.parse & query
            OssCommandParser parser = manager.getParser(code);

            OssCmdMeta cmdMeta = parser.parse(rt1InputMessage);

            LOGGER.info("## parse & query finish");

            //5.filter pap
            if (cmdMeta.getPap() != null) {
                LOGGER.debug("## Current User's PAPs =" + userInfo.getPaps());
                LOGGER.debug("## Current Device's PAP =" + cmdMeta.getPap());
                boolean passPap = false;
                Set<String> paplist = userInfo.getPaps();
                if(paplist != null && paplist.size() > 0) {
                    for (String pap : paplist) {
                        if (pap.equals("*")) {
                            passPap = true;
                            break;
                        } else if (cmdMeta.getPap().equals(pap)) {
                            passPap = true;
                            break;
                        }
                    }
                }
                if (!passPap) {
                    throw new OssCommandException(CommonConstants.SEOF, i18n.getMessage(CommonConstants.EADD_PREFIX +
                            "pap.check"));
                }
            }
            LOGGER.info("## filter pap finish");

            //6.handle message
            OssCommandHandler handler = manager.getHandler(code, cmdMeta);

            OssCmdResult ossCmdResult = handler.execute(cmdMeta, userInfo);

            LOGGER.info("## handle message finish");

            //7.build response

            RT1OutputMessage responseMessage = manager.getOutputMsgBuilder(code).buildResponseMessage(
                    rt1InputMessage, ossCmdResult);
            LOGGER.info("## build response finish");
            return responseMessage.toString();

        } catch (OssCommandException e) {
            LOGGER.error("OssCommandException occurred when ", e);
            return builder.buildResponseMessage(rt1InputMessage, new OssCmdResult(
                    false,
                    e.getErrCode(),
                    e.getErrDetail())).toString();
        } catch (Exception e) {
            LOGGER.error("Exception occurred when ", e);
            return builder.buildResponseMessage(rt1InputMessage, new OssCmdResult(
                    false,
                    CommonConstants.EEEH,
                    e.getMessage())).toString();
        }

    }

    private RT1InputMessage parseInputMessage(String inputMessage) throws OssCommandException {
        try {
            return RT1InputMessage.parseInputMessage(inputMessage);
        } catch (Exception e) {
            LOGGER.error("Error occurred when ", e);
            throw new OssCommandException(CommonConstants.IIPF, e.getMessage());
        }
    }
}
