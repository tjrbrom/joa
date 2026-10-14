package com.x.hhqs.joa.alarm.file;

public enum NodeType {

//    LOCAL(0),
//    FTP(21),
    SFTP(22),

    UNKNOWN(-1);

    private int port = -1;

    NodeType(int port) {
        this.port = port;
    }

    public static NodeType getNodeType(String value) {
/*
        if(value == null || value.isEmpty()) {
            return LOCAL;
        }

        if(value.equalsIgnoreCase("local") || value.equalsIgnoreCase("file")) {
            return LOCAL;
        }

        if(value.equalsIgnoreCase("ftp")) {
            return FTP;
        }
*/

        if(value.equalsIgnoreCase("sftp")) {
            return SFTP;
        }

        return UNKNOWN;
    }

    public int getPort() {
        return port;
    }

}
