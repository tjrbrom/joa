package com.x.hhqs.joa.alarm.handler;

import com.google.common.base.Strings;
import com.x.hhqs.joa.alarm.api.Command;
import com.x.hhqs.joa.alarm.api.CommandHandler;
import com.x.hhqs.joa.alarm.api.CommandHandlerRegistry;
import com.x.hhqs.joa.alarm.model.MessageType;
import com.x.hhqs.joa.alarm.model.OmcRequest;
import com.x.hhqs.joa.alarm.model.response.OmcAckMessage;
import com.x.hhqs.joa.common.core.contants.MessageConstant;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import com.x.hhqs.joa.alarm.property.Messages;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.MessageFormat;

@Slf4j
public abstract class RequestMessageHandler implements CommandHandler, InitializingBean, DisposableBean {

	@Autowired
	protected CommandHandlerRegistry handlerRegistry;

	public RequestMessageHandler() {
		super();
	}

	@Override
	public void execute(Channel ch, Command cmd) {
		try {
			executeRequest(ch, (OmcRequest) cmd);
		} catch (Exception e) {
			log.error("{}",e);
			fail(ch, (OmcRequest) cmd, Messages.getString("resDesc.executeFail"));
		}
	}

	private void fail(Channel ch, OmcRequest request, String resDesc) {
		MessageType responseType = request.getMessageType().getResponseType();
		OmcAckMessage ackMessage = OmcAckMessage.getAckMessage(responseType);

		String reqId = request.getValue("ReqId");
		if (Strings.isNullOrEmpty(reqId)) {
			reqId = "-1";
		}

		ackMessage.setValue("reqId", reqId);
		ackMessage.setValue("result", MessageConstant.FAIL);
		ackMessage.setValue("resDesc", resDesc);

		ch.writeAndFlush(ackMessage);
	}

	public abstract void executeRequest(Channel ch, OmcRequest request);

	
	@Override
	public void afterPropertiesSet() {
		register();
	}

	@Override
	public void destroy() {
		unregister();
	}
	
	private void register() {
		handlerRegistry.register(getCommandCode().toLowerCase(), this);
	}

	private void unregister() {
		handlerRegistry.unRegister(getCommandCode().toLowerCase());
	}

	@Override
	public String getCommandCode() {
		return getType().getMessageName();
	}

	protected OmcAckMessage getOmcAckMessage() {
		return OmcAckMessage.getAckMessage(getType().getResponseType());
	}

	public abstract MessageType getType();

	protected String invalidParamter(String parameter) {
		return MessageFormat.format(Messages.getString("resDesc.invalidParameter"), parameter);
	}

}
