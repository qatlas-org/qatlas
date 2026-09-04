package org.qatlas.backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.zip.ZipOutputStream;

public interface StorageService {

    void store(MultipartFile file, String path);

    void deleteFolder(String folderPath);

    byte[] readFileContent(String filePath);

    void upload(byte[] data, String path, boolean base64Encoded);

    void zipFolder(final String folderPath, ZipOutputStream zipOutputStream) throws IOException;
}
