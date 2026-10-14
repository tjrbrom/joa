package com.x.hhqs.joa.sa.model;

import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.util.CommonConstants;
import com.x.hhqs.joa.sa.netconf.NetconfRequest;
import com.x.hhqs.joa.sa.utils.ResponseUtil;
import com.x.hhqs.joa.sa.utils.SystemConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component("regexSelector")
public class RegexResultSelector implements ResultSelector {

    @Autowired
    private JoaMessage joaMessage;

    @Override
    public void select(NetconfRequest.Selector selector, String rpcResponse) throws OssCommandException {
        String key = selector.getKey();
        String expr = selector.getExpr();
        String result = ResponseUtil.getValueByRegex(rpcResponse, expr);
        if (result == null) {
            if (Boolean.TRUE.equals(selector.getOptional())) {
                return;
            }
            throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.REGEX_NOT_MATCHED, new Object[]{expr, key}));
        }
        ModelDataContainer.getSharedData().getContext().put(key, result);
    }
}
