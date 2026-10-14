package com.x.hhqs.joa.alarm.noncodedplugs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class NonCodedPlugUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(NonCodedPlugUtils.class);

    public static final String SLASH = "/";
    public static final int ERROR_MSG_COLUMN_WIDTH = 1000;
    public static final String END_ERROR_MSG_EXCEED_LIMIT= "...";

    public static String getIntegratedYangsDirectory(String destDirname) {
        return destDirname + SLASH + PlugSpecificConstants.DEFAULT_YANG_PATH;
    }

    public static String getAlarmXmlPath(String destDirname) {
        return destDirname + PlugSpecificConstants.DEFAULT_ALARM_XML_PATH;
    }

    public static URL buildURLFromPath(String filePath) throws IOException {
        File file = new File(filePath);
        if(!file.exists()) {
            throw new IOException("File does not exist from path: " + filePath);
        }
        return file.toURI().toURL();
    }

    public static void closingInputStreams(List<InputStream> inputStreams) {
        inputStreams.forEach(inputStream -> {
            try {
                inputStream.close();
            } catch (IOException e) {
                LOGGER.debug("Cannot close inputstream", e);
            }
        });
    }

    /**
     * Trim error message and append '...' in end of the error message if it is more than 100 characters
     */
    public static String trimErrorMsg(String errorMsg) {
        if (errorMsg != null && errorMsg.length() > ERROR_MSG_COLUMN_WIDTH) {
            return new StringBuilder(errorMsg.substring(0,
                    ERROR_MSG_COLUMN_WIDTH - END_ERROR_MSG_EXCEED_LIMIT.length()))
                    .append(END_ERROR_MSG_EXCEED_LIMIT).toString();
        }
        return errorMsg;
    }
}
