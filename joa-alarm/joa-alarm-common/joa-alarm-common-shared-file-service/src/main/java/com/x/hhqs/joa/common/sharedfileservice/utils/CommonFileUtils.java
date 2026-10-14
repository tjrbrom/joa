package com.x.hhqs.joa.common.sharedfileservice.utils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CommonFileUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonFileUtils.class);
    public static final int CSV_BUFFER_SIZE = Integer.parseInt(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty("CSV_BUFFER_SIZE","52428800"));//50MB is default buffer size
    public CommonFileUtils() {

    }

    /**
     * Returns a zip file system
     *
     * @param zipFilename to construct the file system from
     * @return a zip file system
     * @throws IOException
     */
    public static FileSystem createZipFileSystem(String zipFilename)
            throws Exception {
        final Path path = Paths.get(zipFilename);
        Map<String, String> env = new HashMap<>();
        env.put("create", "false");
        return FileSystems.newFileSystem(path, env, null);
    }

    /**
     * This method used to copy the specific file from ZIP to targeted location
     *
     * @param zipFileLocation - Zip File Location
     * @param targetFilename  - Actual target file location in the zip
     * @param toCopyFilePath  - Copy file path
     * @throws Exception
     */
    public static void copySpecificFileFromZip(String zipFileLocation, String targetFilename, Path toCopyFilePath)
            throws Exception {
        try (FileSystem zipFileSystem = createZipFileSystem(zipFileLocation)) {
            final Path targetFilePath = zipFileSystem.getPath(targetFilename);
            if (Files.notExists(targetFilePath)) {
                throw new Exception(targetFilePath.getFileName().toString() + " file does not exist in the specified archive file");
            }
            Files.copy(targetFilePath, toCopyFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void unpackToSpecificDirectory(String zipFilename, String destDirname)
            throws Exception {

        final Path destDir = Paths.get(destDirname);
        //if the destination doesn't exist, create it
        if (Files.notExists(destDir)) {
            LOGGER.debug(" {} does not exist. Creating...", destDir);
            Files.createDirectories(destDir);
        }

        try (FileSystem zipFileSystem = createZipFileSystem(zipFilename)) {
            final Path root = zipFileSystem.getPath("/");

            //walk the zip file tree and copy files to the destination
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attrs) throws IOException {
                    final Path destFile = Paths.get(destDir.toString(),
                            file.toString());
                    LOGGER.debug("Extracting file {} to {}\n", file, destFile);
                    Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                                                         BasicFileAttributes attrs) throws IOException {
                    final Path dirToCreate = Paths.get(destDir.toString(),
                            dir.toString());
                    if (Files.notExists(dirToCreate)) {
                        LOGGER.debug("Creating directory : {}\n", dirToCreate);
                        Files.createDirectory(dirToCreate);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            LOGGER.debug("Unpacked file {} to {}\n", zipFilename, destDirname);
        } catch (Exception e) {
            LOGGER.error("Error occurred while unpacking ", e);
            throw e;
        }
    }

    public static void deleteDirectory(String destDirectory) {
        if (destDirectory == null || destDirectory.isEmpty()) {
            return;
        }

        try {
            FileUtils.deleteDirectory(new File(destDirectory));
        } catch (IOException e) {
            LOGGER.error(null, "Exception while deleting directory: {}", destDirectory, e);
        }
    }

    public static void concatAllCsvFilesIntoOne(String sourceCsvDir, String destinationCsvPath) throws Exception {
        String outputFilePath = destinationCsvPath;
        if(!destinationCsvPath.endsWith(".csv")) {
            outputFilePath = outputFilePath + ".csv";
        }
        File outputFile = new File(outputFilePath);
        File parentPath = new File(sourceCsvDir);
        String[] extensions = new String[] {"csv"};
        Collection<File> allCsvFiles = FileUtils.listFiles(parentPath, extensions, false);
        for(File file : allCsvFiles) {
            try {
                String fileContents = FileUtils.readFileToString(file);
                FileUtils.write(outputFile, fileContents, true);
                FileUtils.writeStringToFile(outputFile, "\n", true);
            } catch (Exception e) {
                LOGGER.error(null, "Error while combining all csv documents into single large document {} ",outputFilePath, e);
                throw e;
            }
        }
    }

    public static void createTarGZForSingleFile(String inputFilePath, String outputPath) throws IOException {

        File inputFile = new File(inputFilePath);
        File outputFile = new File(outputPath);

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             GzipCompressorOutputStream gzipOutputStream = new GzipCompressorOutputStream(bufferedOutputStream);
             TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(gzipOutputStream)) {

            tarArchiveOutputStream.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
            tarArchiveOutputStream.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            TarArchiveEntry tarEntry = new TarArchiveEntry(inputFile, inputFile.getName());
            tarEntry.setSize(inputFile.length());

            tarArchiveOutputStream.putArchiveEntry(tarEntry);
            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] buffer = new byte[CSV_BUFFER_SIZE]; //default 50MB buffer
            int numBytesRead = -1; //number of bytes read
            while((numBytesRead = inputStream.read(buffer)) > 0) {
                tarArchiveOutputStream.write(buffer, 0, numBytesRead);
            }
            tarArchiveOutputStream.closeArchiveEntry();
        }
    }
}
