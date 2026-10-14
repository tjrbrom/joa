package com.x.hhqs.joa.common.rt1fwk.impl;

import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.rt1message.*;
import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.rt1fwk.api.OutputMsgBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Component("COMMON.outputMsgBuilder")
@RefreshScope
public class OutputMsgBuilderImpl implements OutputMsgBuilder {

    @Value("${core.host:127.0.0.1}")
    private String host;

    @Value("${paging.page_enable:false}")
    private boolean isPaging;

    @Value("${paging.page_max_num:20}")
    private int pageMaxNum;

    @Autowired
    private JoaMessage i18n; 

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isPaging() {
        return isPaging;
    }

    public void setPaging(boolean paging) {
        isPaging = paging;
    }

    public int getPageMaxNum() {
        return pageMaxNum;
    }

    public void setPageMaxNum(int pageMaxNum) {
        this.pageMaxNum = pageMaxNum;
    }

    @Override
    public RT1OutputMessage buildResponseMessage(RT1InputMessage inputMessage, OssCmdResult ossCmdResult) {
        String ctag = "0";
        if (inputMessage != null) {
            ctag = inputMessage.getCTag();
            if (ctag.length() == 0)
                ctag = "0";
        }

        if(ossCmdResult.isSuccess() && ossCmdResult.getRtrvTitle() != null) {
            //succeed LST commands
            return this.buildQueryResponseMessage(ctag, ossCmdResult.getRtrvTitle(),
                    ossCmdResult.getRtrvArray());
        }

        RT1ResponseMessage responseMessage = new RT1ResponseMessage();
        responseMessage.setHeader(new RT1Header("ASB_" + host, this.currentDataTime()));

        RT1ResponseID responseID = new RT1ResponseID();
        if (ossCmdResult.isSuccess()) {
            responseID.setCompletionCode("COMPLD");
        } else {
            responseID.setCompletionCode("DENY");
        }

        responseID.setCTag(ctag);
        responseMessage.setResponseID(responseID);

        RT1TextBlock textBlock = new RT1TextBlock();
        RT1Line rt1lineErrCode = new RT1Line();
        if (ossCmdResult.isSuccess()) {
            rt1lineErrCode.setLineContent("EN=0   ENDESC=\"\"");
        } else {
            rt1lineErrCode.setLineContent("EN=" + ossCmdResult.getErrorCode() + "   "
                    + "ENDESC=\"" + i18n.getMessage(ossCmdResult.getErrorDesc()) + "\"   EADD=\""
                    + (ossCmdResult.getErrorDetail() != null ? ossCmdResult.getErrorDetail() : "") + "\"");
        }
        rt1lineErrCode.setLineType(RT1Line.UNQUOTED_LINE);
        textBlock.addRT1Line(rt1lineErrCode);
        responseMessage.setTextBlock(textBlock);

        return responseMessage;

    }

    private String currentDataTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }



    private  RT1QueryResponseMessage buildQueryResponseMessage(String cTag, String testTitle, String[][] rtrvArray) {
//        RT1QueryResponseMessage rt1Resp = new RT1QueryResponseMessage();
//        rt1Resp.setHeader(new RT1Header("ASB_" + getHost(), this.currentDataTime()));
//        rt1Resp.setResponseID(new RT1ResponseID(cTag, CompletionCode.COMPLD.getCode()));
//        rt1Resp.setResponseBlock(buildRT1QueryBlock(testTitle, rtrvArray));
//        return rt1Resp;

        RT1QueryResponseMessage rt1Resp = new RT1QueryResponseMessage();

        rt1Resp.setPaging(isPaging);

        if (isPaging) {
            RT1QueryResponseBlockWithPaging responseBlockWithPaging = new RT1QueryResponseBlockWithPaging();
            responseBlockWithPaging.setHeader(new RT1Header("ASB_" + getHost(), this.currentDataTime()));
            responseBlockWithPaging.setResponseID(new RT1ResponseID(cTag, CompletionCode.COMPLD.getCode()));
            responseBlockWithPaging.setRt1QueryResultList(buildRT1QueryResult(rtrvArray));
            responseBlockWithPaging.setLine(testTitle);
            responseBlockWithPaging.setPageMaxNum(pageMaxNum);
            rt1Resp.setResponseBlockWithPaging(responseBlockWithPaging);
        } else {
            RT1QueryResponseBlock responseBlock = new RT1QueryResponseBlock();
            responseBlock.setHeader(new RT1Header("ASB_" + getHost(), this.currentDataTime()));
            responseBlock.setResponseID(new RT1ResponseID(cTag, CompletionCode.COMPLD.getCode()));
            responseBlock.setRt1QueryResultList(buildRT1QueryResult(rtrvArray));
            responseBlock.setLine(testTitle);
            rt1Resp.setResponseBlock(responseBlock);
        }
        return rt1Resp;
    }

    private ArrayList<RT1QueryResult> buildRT1QueryResult(String[][] rtrvArray) {
        ArrayList<RT1QueryResult> queryRstlist = new ArrayList<RT1QueryResult>();
        int size = rtrvArray.length;
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                String[] records = rtrvArray[i];
                String key = records[0];
                String[] value = new String[records.length - 1];
                for (int j = 0; j < value.length; j++) {
                    String val = records[j + 1];
                    value[j] = (!StringUtils.hasText(val)) ? "--" : val;
                }
                queryRstlist.add(new RT1QueryResult(key, value));
            }
        }

        return queryRstlist;
//        RT1QueryResponseBlock responseBlock = new RT1QueryResponseBlock();
//        responseBlock.setRt1QueryResultList(queryRstlist);
//        responseBlock.setLine(testTitle);
//        return responseBlock;
    }
}
