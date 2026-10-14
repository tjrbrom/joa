package com.x.hhqs.joa.common.sharedfileservice.utils;

import com.github.sardine.Sardine;
import com.x.hhqs.joa.common.sharedfileservice.impl.WebdavSardineUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileServerUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileServerUtil.class);
    public static final String SEPARATOR = "/";
    public static final String COLON = ":";
    private static final Object LOCK = new Object();

    public static boolean createDirectoryIfNotExists(String currentPath, String directory, Sardine sardine) throws Exception {
        String[] dirs = directory.split(SEPARATOR);
        for (String dir : dirs) {
            if (!dir.isEmpty()) {
                if (currentPath.endsWith(SEPARATOR)) {
                    currentPath = currentPath + dir + SEPARATOR;
                } else {
                    currentPath = currentPath + SEPARATOR + dir + SEPARATOR;
                }
                LOGGER.info(null, "User {} is creating directory {}",
                        getCurrentUser(), currentPath);
//                        LOGGER.sensitiveData(getCurrentUser()), currentPath);
                if (!WebdavSardineUtil.exists(sardine, currentPath)) {
                    synchronized (LOCK) {
                        if (!WebdavSardineUtil.exists(sardine, currentPath)) {
                            WebdavSardineUtil.createDirectory(sardine, currentPath);
                        } else {
                            LOGGER.debug("Directory {} already exists", currentPath);
                        }
                    }
                } else {
                    LOGGER.debug("Directory {} already exists", currentPath);
                }
            }
        }
        return true;
    }

    public static String formatSeparatorPath(String... dirs) {
        if (dirs[0].endsWith(SEPARATOR)) {
            dirs[0] = dirs[0].substring(0, dirs[0].length() - 1);
        }
        String finalFormattedString = dirs[0];
        for (int i = 1; i < dirs.length - 1; i++) {
            if (dirs[i].startsWith(SEPARATOR) && !dirs[i].endsWith(SEPARATOR)) {
                dirs[i] = dirs[i] + SEPARATOR;
            } else if (!dirs[i].startsWith(SEPARATOR) && dirs[i].endsWith(SEPARATOR)) {
                dirs[i] = SEPARATOR + dirs[i];
            } else if (!dirs[i].startsWith(SEPARATOR) && !dirs[i].endsWith(SEPARATOR)) {
                dirs[i] = SEPARATOR + dirs[i] + SEPARATOR;
            }
            finalFormattedString = finalFormattedString + dirs[i];
        }
        // The last string parameter is the file name, so do not build the SEPARATOR for it
        String fileName = dirs[dirs.length - 1];
        finalFormattedString = finalFormattedString + fileName;
        return finalFormattedString;
    }

    public static String getCurrentUser() {
        return getCurrentLoggedInUser();
    }

    public static String getCurrentLoggedInUser() {
        String username = "";
        return username;
    }
}