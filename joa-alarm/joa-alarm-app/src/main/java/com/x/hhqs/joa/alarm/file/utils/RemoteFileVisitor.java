package com.x.hhqs.joa.alarm.file.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;

public interface RemoteFileVisitor<T> {

    FileVisitResult preVisitDirectory(T dir, String parentPath)
            throws IOException;

    FileVisitResult visitFile(T file, String parentPath)
            throws IOException;

    FileVisitResult visitFileFailed(T file, String parentPath, IOException e)
            throws IOException;

    FileVisitResult postVisitDirectory(T dir, String parentPath, IOException e)
            throws IOException;
}
