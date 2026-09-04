package org.qatlas.backend.service.impl;

import org.qatlas.backend.config.properties.StorageProperties;
import org.qatlas.backend.exception.StorageException;
import org.qatlas.backend.exception.StorageFileNotFoundException;
import org.qatlas.backend.service.StorageService;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
public class FileSystemStorageService implements StorageService {

    private Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);

    private final Path rootLocation;

    public Path getRootLocation() {
        return rootLocation;
    }

    @Autowired
    public FileSystemStorageService(final StorageProperties properties) {
        rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void store(final MultipartFile file, final String path) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new StorageException(
                    "Failed to store empty file " + filename
                );
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException(
                    "Cannot store file with relative path"
                    + " outside current directory "
                    + filename);
            }
            Path folderPath = path != null
                ? rootLocation.resolve(path) : rootLocation;
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream,
                    folderPath.resolve(filename),
                    REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public void deleteFolder(final String folderPath) {
        File file = rootLocation.resolve(folderPath).toFile();
        if (!file.isDirectory() || !FileSystemUtils.deleteRecursively(file)) {
            throw new StorageFileNotFoundException(
                "Could not find folder with path " + file.getPath()
            );
        }
    }

    @Override
    public byte[] readFileContent(final String filePath) {
        try {
            return Files.readAllBytes(rootLocation.resolve(filePath));
        } catch (IOException e) {
            throw new StorageFileNotFoundException(
                "Could not fine folder: " + filePath,
                e
            );
        }
    }

    @Override
    @Validated
    public void upload(
            @NotNull
            final byte[] data,
            @NotNull
            final String path,
            final boolean base64Encoded) {
        String filename = rootLocation
            .resolve(path)
            .getFileName()
            .toString();
        try {
            Path folderPath = rootLocation
                .resolve(
                    path.replace(
                        filename,
                        org.apache.commons.lang3.StringUtils.EMPTY
                    )
                );
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }
            Path filePath = folderPath.resolve(filename);
            if(base64Encoded) {
                Files.write(filePath, Base64.decodeBase64(data));
            } else {
                Files.write(filePath, data);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public void zipFolder(String folderPath, ZipOutputStream zipOutputStream) throws IOException {
        File file = rootLocation.resolve(folderPath).toFile();
        zipFile(file, file.getName(), zipOutputStream);
    }

    private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        try {
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        } catch (FileNotFoundException e) {
            logger.error("Could not find folder/file with path " + fileToZip.getPath());
        }
    }

}
