package com.x.hhqs.joa.rt1agent.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import com.x.hhqs.joa.rt1agent.command.EaddCode;
import com.x.hhqs.joa.rt1agent.command.FailResultException;
import com.x.hhqs.joa.rt1message.RT1InputMessage;
import com.x.hhqs.joa.rt1message.RT1Param;
import com.x.hhqs.joa.rt1message.RT1PayloadBlock;

public class Rt1MessageUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(Rt1MessageUtil.class);

    public static Properties generateParameters(RT1InputMessage rt1msg) {
        Properties params = new Properties();
        ArrayList<RT1PayloadBlock> blockList = rt1msg.getPayloadBlock();
        if (blockList.size() >= 0) {
            RT1PayloadBlock block = blockList.get(0);
            ArrayList<RT1Param> paramList = block.getParam();
            for (RT1Param param : paramList) {
                params.setProperty(param.getParamName().trim(), param.getParamValue().trim());
            }
        }
        return params;
    }

    public static Map<String, String> parseAidToMap(String aid) throws FailResultException {
        if (!StringUtils.hasText(aid)) {
            return new HashMap<>(0);
        }

        Map<String, String> map = new HashMap<>();

        String[] params = aid.split(",");
        for (String param : params) {
            String[] condition = param.split("=", 2);
            String key = condition[0].trim().toUpperCase();
            if (!StringUtils.hasText(key)) {
                LOGGER.warn("Invalid aid, blank key: " + aid);
                continue;
            }

            if (condition.length == 1) {
                LOGGER.warn("Invalid aid: " + aid);
                map.put(key, "");
                continue;
            }

            if (map.containsKey(key)) {
                LOGGER.warn("Invalid aid, duplicate key: " + aid);
                if ("OLTID".equalsIgnoreCase(key)) {
                    throw new FailResultException(EaddCode.MULTIPLE_OLTID_IN_AID);
                }
            }

            map.put(key, condition[1].trim());
        }

        return map;
    }
}
