package com.x.hhqs.joa.rmuid.model;

public class Uni {

    private String type;

    private int port;
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "Uni [type=" + type + ", port=" + port + "]";
    }

    /*-
    LAN1     -> 0
    LAN24    -> 23
    TEL1     -> 24
    TEL24    -> 47
    VEIP     -> 48
    VOIP-SIP -> 49
    GE10G    -> 50
     */
    public int toCode() {
        switch (this.type) {
            case "LAN":
                if (this.port < 1 || this.port > 24) {
                    throw new IllegalArgumentException("LAN out of range (1-24)");
                }
                return this.port - 1;
            case "TEL":
                if (this.port < 1 || this.port > 24) {
                    throw new IllegalArgumentException("TEL out of range (1-24)");
                }
                return this.port + 24 - 1;
            case "VEIP":
                return 48;
            case "VOIP-SIP":
                return 49;
            case "GE10G":
                return 50;
            default:
                String portPart = this.port == -1 ? "" : String.valueOf(this.port);
                throw new IllegalArgumentException("Invalid UNI: " + this.type + portPart);
        }
    }

    public static String toUni(int code) {
        if (code < 0 || code > 63) {
            throw new IllegalArgumentException("LAN out of range (0-63)");
        }
        if (code < 24) {
            return "LAN-" + (code + 1);
        }
        if (code < 48) {
            return  "TEL" + (code - 23);
        }
        if (code == 49) {
            return "VOIP-SIP";
        }
        if (code == 50) {
            return "GE10G";
        }
        throw new IllegalArgumentException("Invalid UNI code: " + code);
    }
}
