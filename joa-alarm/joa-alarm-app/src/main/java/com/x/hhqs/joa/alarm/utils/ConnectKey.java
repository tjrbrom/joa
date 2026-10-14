package com.x.hhqs.joa.alarm.utils;


import com.x.hhqs.joa.alarm.model.ClientType;

public class ConnectKey {

    private final String username;

    private final ClientType clientType;

    private final int port;

    private ConnectKey(String username, ClientType clientType, int port) {
        this.username = username;
        this.clientType = clientType;
        this.port = port;
    }

    public static ConnectKey getConnectKey(String username, ClientType clientType, int port) {
        return new ConnectKey(username, clientType, port);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((clientType == null) ? 0 : clientType.hashCode());
        result = prime * result + port;
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConnectKey other = (ConnectKey) obj;
        if (clientType != other.clientType)
            return false;
        if (port != other.port)
            return false;
        if (username == null) {
            return other.username == null;
        } else return username.equals(other.username);
    }
}
