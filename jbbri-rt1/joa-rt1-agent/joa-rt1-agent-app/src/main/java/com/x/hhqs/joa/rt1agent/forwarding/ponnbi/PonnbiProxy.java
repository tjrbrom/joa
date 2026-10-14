package com.x.hhqs.joa.rt1agent.forwarding.ponnbi;

import com.x.hhqs.joa.rt1agent.command.EmptyResponse;
import com.x.hhqs.joa.rt1message.RT1InputMessage;

public class PonnbiProxy {

    private PonnbiClientManager ponnbiClientManager;

    private PonnbiProperties ponnbiProperties;

    public PonnbiProxy(PonnbiClientManager ponnbiClientManager, PonnbiProperties ponnbiProperties) {
        this.ponnbiClientManager = ponnbiClientManager;
        this.ponnbiProperties = ponnbiProperties;
    }

    public Object sendMessage(RT1InputMessage message, String sessionId) throws Exception {
        PonnbiClient client = ponnbiClientManager.getClient(sessionId);
        client.write(message);

        return EmptyResponse.Instance;
    }

    public boolean isEnable() {
        return ponnbiProperties.isEnabled();
    }

}
