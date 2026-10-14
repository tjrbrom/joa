package com.x.hhqs.joa.rt1agent.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.rt1fwk.api.OutputMsgBuilder;
import com.x.hhqs.joa.common.rt1fwk.impl.OutputMsgBuilderImpl;

@Configuration
public class MessageBuilderConfiguration {

  @Autowired
  private JoaMessage joaMessage;

  @Bean
  @RefreshScope
  public OutputMsgBuilder outputMsgBuilder() {
    return new OutputMsgBuilderImpl();
  }

  @Bean
  public RT1AgentResponseBuilder rt1AgentResponseBuilder() {
    return new RT1AgentResponseBuilder(outputMsgBuilder(), joaMessage);
  }

}
