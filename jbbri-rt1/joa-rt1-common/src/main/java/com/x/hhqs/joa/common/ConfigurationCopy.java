package com.x.hhqs.joa.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@Component
public class ConfigurationCopy {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationCopy.class);

    private static final String JOA_DEFAULT = "/joa/default";
    private static final String JOA_CONFIG = "/joa/config";
    @PostConstruct
    public void init() {
        try {
            if ("container".equals(System.getenv("ENV"))) {
                copyDirectory(Paths.get(JOA_DEFAULT), Paths.get(JOA_CONFIG));
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while initialize.", e);
        }
    }

    private void copyDirectory(Path source, Path destination) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path relativePath = source.relativize(dir);
                Path targetDir = destination.resolve(relativePath);
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                    LOGGER.info("Create directory: {}", targetDir);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = source.relativize(file);
                Path targetFile = destination.resolve(relativePath);
                if (!Files.exists(targetFile)) {
                    Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Copy file: {}", targetFile);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
