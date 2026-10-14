package com.x.hhqs.joa.common.rt1fwk.api;

import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import com.x.hhqs.joa.common.rt1fwk.pojo.Validation;
import com.x.hhqs.joa.rt1message.RT1InputMessage;

public interface OssCommandValidator {
    void validate(RT1InputMessage rt1InputMsg, Validation validation) throws OssCommandException;
}
