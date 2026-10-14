package com.x.hhqs.joa.in.handler;

import com.x.hhqs.joa.common.api.UserInfo;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdMeta;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.in.service.PassThroughService;
import com.x.hhqs.joa.in.util.OssCmdMetaParser;
import com.x.hhqs.joa.in.vo.passThrough.Result;
import com.x.hhqs.joa.in.vo.passThrough.ResultTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component("PASS-THROUGH")
public class PassThroughHandler extends AbstractHandler {

    @Autowired
    private PassThroughService passThroughService;

    private static final Logger LOG = LoggerFactory.getLogger(PassThroughHandler.class);

    @Override
    protected OssCmdResult executeRt1Msg(OssCmdMeta ossCmdMeta, UserInfo userinfo) throws Exception {
        if (isOperationCommand(ossCmdMeta)) {
            return new OssCmdResult(true);
        }

        ResultTemplate resultTemplate = passThroughService.generateResultTemplate(OssCmdMetaParser.code(ossCmdMeta));
        int randomMillis = ThreadLocalRandom.current().nextInt(resultTemplate.getMinSleepTime(), resultTemplate.getMaxSleepTime());
        Thread.sleep(randomMillis);

        List<String> paramNames = resultTemplate.getParamNameList().getParamNames();
        List<Result> results = resultTemplate.getResultList().getResults();
        String[][] rtrvArray = new String[paramNames.size()][results.size() + 1];

        for (int i = 0; i < paramNames.size(); i++) {
            // first line of table (OLTID,PONID,ONUNO ...)
            rtrvArray[i][0] = paramNames.get(i);
            for (int j = 0; j < results.size(); j++) {
                Result result = results.get(j);
                rtrvArray[i][j + 1] = result.getParamValues().get(i);
            }
        }
        return createOssCmdResult(resultTemplate.getTitle(), rtrvArray);
    }

    private boolean isOperationCommand(OssCmdMeta ossCmdMeta) {
        String commandName = OssCmdMetaParser.code(ossCmdMeta);
        if ("TEST-PPPOESIMULATION".equalsIgnoreCase(commandName) || "TEST-CALLEESIMULATION".equalsIgnoreCase(commandName)
                || "TEST-CALLERSIMULATION".equalsIgnoreCase(commandName)) {
            String action = OssCmdMetaParser.getValFromNbiMetadata(ossCmdMeta, "ACTION");
            return "Start".equalsIgnoreCase(action);
        }
        return false;
    }

}
