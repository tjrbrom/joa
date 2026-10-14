package com.x.hhqs.joa.sa.model;

import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.util.CommonConstants;
import com.x.hhqs.joa.sa.netconf.NetconfRequest;
import com.x.hhqs.joa.sa.utils.ResponseUtil;
import com.x.hhqs.joa.sa.utils.SystemConstants;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("xpathSelector")
public class XPathResultSelector implements ResultSelector {

    @Autowired
    private JoaMessage joaMessage;

    @Override
    public void select(NetconfRequest.Selector selector, String rpcResponse) throws OssCommandException {
        String key = selector.getKey();
        String expr = selector.getExpr();
        String result;
        try {
            result = ResponseUtil.getValueByXPath(rpcResponse, expr);
        } catch (DocumentException e) {
            throw new OssCommandException(CommonConstants.SEOF, e.getMessage());
        }
        if (result == null) {
            if (selector.getOptional()) {
                return;
            }
            throw new OssCommandException(CommonConstants.SEOF, joaMessage.getMessage(SystemConstants.XPATH_NOT_MATCHED, new Object[]{expr, key}));
        }
        ModelDataContainer.getSharedData().getContext().put(key, result);
    }
}
