package com.x.hhqs.joa.rt1message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ResponseMessageUtil {
	private static final String EMPTY = "";
	private static final String TIME_FORMART = "yyyy-MM-dd HH:mm:ss";

	public static RT1ResponseMessage commandNotSupport(String cTag) {
		return ResponseMessageUtil.buildErrorMessage(cTag, CompletionCode.DENY, "EEEH", "Command is not support.");
	}

	public static RT1ResponseMessage userHasNotLogin(String cTag) {
		return ResponseMessageUtil.buildErrorMessage(cTag, CompletionCode.DENY, "DDOF",
				"device operation failed!user hasn't logined");
	}

	public static RT1ResponseMessage userHasNoRole(String cTag) {
		return ResponseMessageUtil.buildErrorMessage(cTag, CompletionCode.DENY, "EEEH",
				"user has no rights to use this cmd");
	}

	/*-
		<header><response_id>[<response_block>]<terminator>
		header::=<cr><lf><lf>^^^<sid>^<year>-<month>-<day>^<hour>:<minute>:<second>
		response_id::=<cr><lf>M^^<ctag>^<completion code>
		response_block::=((<cr><lf>^^^<EN=error-code>^^^<ENDESC=error-description>)
		terminator::=<cr><lf>(;|>)
	 */

	public static RT1ResponseMessage buildSuccessMessage(String cTag) {
		RT1ResponseMessage rt11ResponseMessage = new RT1ResponseMessage();
		rt11ResponseMessage.setHeader(buildRT1Header());
		rt11ResponseMessage.setResponseID(buildRT1ResponseID(cTag, CompletionCode.COMPLD));
		rt11ResponseMessage.setTextBlock(buildRT1TextBlock());

		return rt11ResponseMessage;
	}

	public static RT1ResponseMessage buildErrorMessage(String cTag, CompletionCode completionCode, String errorCode,
			String errorDescription) {
		RT1ResponseMessage rt11ResponseMessage = new RT1ResponseMessage();
		rt11ResponseMessage.setHeader(buildRT1Header());
		rt11ResponseMessage.setResponseID(buildRT1ResponseID(cTag, completionCode));
		rt11ResponseMessage.setTextBlock(buildRT1TextBlock(errorCode, errorDescription));

		return rt11ResponseMessage;
	}
	
	public static RT1QueryResponseMessage buildQueryResponseMessage(String cTag, String testTitle, String[][] rtrvArray) {
		RT1QueryResponseMessage rt1Resp = new RT1QueryResponseMessage();

		rt1Resp.setResponseBlock(buildRT1QueryBlock(cTag, testTitle, rtrvArray));
		return rt1Resp;
	}

	private static RT1TextBlock buildRT1TextBlock() {
		return buildRT1TextBlock(EMPTY, EMPTY);
	}

	private static RT1TextBlock buildRT1TextBlock(String errorCode, String errorDescription) {
		RT1TextBlock textBlock = new RT1TextBlock();

		RT1Line rt1Line = new RT1Line();
		rt1Line.setLineContent(String.format("EN=%s   ENDESC=\"%s\"", errorCode, errorDescription));
		rt1Line.setLineType(RT1Line.UNQUOTED_LINE);
		textBlock.addRT1Line(rt1Line);

		return textBlock;
	}
	
	private static RT1QueryResponseBlock buildRT1QueryBlock(String cTag, String testTitle, String[][] rtrvArray) {
		ArrayList<RT1QueryResult> queryRstlist = new ArrayList<RT1QueryResult>();
		int size = rtrvArray.length;
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				String[] records = rtrvArray[i];
				String key = records[0];
				String[] value = new String[records.length - 1];
				for (int j = 0; j < value.length; j++) {
					String val = records[j + 1];
					if(val == null || val.isEmpty()) {
						val = "--";
					}
					//value[j] = (!StringUtils.hasText(val)) ? "--" : val;
					value[j] = val;
				}
				queryRstlist.add(new RT1QueryResult(key, value));
			}
		}

		RT1QueryResponseBlock responseBlock = new RT1QueryResponseBlock();
		responseBlock.setHeader(buildRT1Header());
		responseBlock.setResponseID(buildRT1ResponseID(cTag, CompletionCode.COMPLD));
		responseBlock.setRt1QueryResultList(queryRstlist);
		responseBlock.setLine(testTitle);
		return responseBlock;
	}

	private static RT1ResponseID buildRT1ResponseID(String cTag, CompletionCode completionCode) {
		if (cTag.trim().length() == 0) {
			cTag = "0";
		}

		return new RT1ResponseID(cTag, completionCode.getCode());
	}

	private static RT1Header buildRT1Header() {
		RT1Header header = new RT1Header();
		header.setSID(getSID());
		header.setTime(getCurrentTime());

		return header;
	}

	private static String getSID() {
		return "ASB_" + System.getProperty("EMS_PONNBI_IP", "");
	}

	private static String getCurrentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMART);
		return sdf.format(new Date());
	}
	
	public static RT1ResponseMessage mergeMessages(List<RT1ResponseMessage> messages) {
		if (messages == null || messages.isEmpty())
			return null;

		RT1ResponseMessage result = null;
		for (RT1ResponseMessage message : messages) {
			result = mergeMessages(result, message);
		}

		return result;
	}

	private static RT1ResponseMessage mergeMessages(RT1ResponseMessage previousMessage, RT1ResponseMessage rt1Msg) {
		if (previousMessage == null)
			return rt1Msg;

		ArrayList<RT1Line> quotedLineList = previousMessage.getTextBlock().getQuotedLineList();
		ArrayList<RT1Line> rt1LineList = previousMessage.getTextBlock().getRT1LineList();

		quotedLineList.addAll(rt1Msg.getTextBlock().getQuotedLineList());
		rt1LineList.addAll(rt1Msg.getTextBlock().getRT1LineList());

		rt1Msg.getTextBlock().setQuotedLineList(quotedLineList);
		rt1Msg.getTextBlock().setRT1LineList(rt1LineList);

		return rt1Msg;
	}

}
