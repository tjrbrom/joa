package com.x.hhqs.joa.sa.api;

import com.x.hhqs.joa.common.rt1fwk.exception.OssCommandException;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.List;

public interface RequestExecutor {
    SbiResponse execute(List<SbiRequest> requestList) throws OssCommandException, TemplateException, IOException;
}
