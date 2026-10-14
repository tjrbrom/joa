package com.x.hhqs.joa.alarm.file.utils;


import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.util.Objects;

public class SimpleRemoteFileVisitor<T> implements RemoteFileVisitor<T> {

    protected SimpleRemoteFileVisitor() {
    }

    @Override
    public FileVisitResult preVisitDirectory(T dir, String parentPath) throws IOException {
        Objects.requireNonNull(dir);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(T file, String parentPath) throws IOException {
        Objects.requireNonNull(file);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(T file, String parentPath, IOException exc) throws IOException {
        Objects.requireNonNull(file);
        throw exc;
    }

    @Override
    public FileVisitResult postVisitDirectory(T dir, String parentPath, IOException exc) throws IOException {
        Objects.requireNonNull(dir);
        if (exc != null)
            throw exc;
        return FileVisitResult.CONTINUE;
    }
}