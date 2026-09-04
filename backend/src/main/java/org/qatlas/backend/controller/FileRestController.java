package org.qatlas.backend.controller;

import org.qatlas.backend.exception.StorageFileNotFoundException;
import org.qatlas.backend.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.qatlas.backend.Constants.SLASH;

//@RestController
public class FileRestController {

    private static final String BASE_PATH = SLASH + "file";

    private StorageService storageService;

    @Autowired
    public FileRestController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping(BASE_PATH)
    public String handleFileUpload(
            @RequestParam("file")
            final MultipartFile file,
            @RequestParam(value = "path", required = false)
            final String path) {
        storageService.store(file, path);
        return String.format(
            "%s uploaded successfully!",
            file.getOriginalFilename()
        );
    }

    @PostMapping(BASE_PATH + "/bytes")
    public String handleFileUpload(
            @RequestBody
            final byte[] data,
            @RequestParam(value = "path")
            final String path) {
        storageService.upload(data, path, true);
        return String.format(
            "%s uploaded successfully!",
            StringUtils.cleanPath(path)
        );
    }

    @DeleteMapping(BASE_PATH + "/folder")
    public String handleDeleteFolder(
            @RequestParam("path")
            final String folderPath) {
        storageService.deleteFolder(folderPath);
        return "Folder deleted successfully!";
    }

    @GetMapping(BASE_PATH)
    public byte[] getFileContent(
            @RequestParam("filePath")
            final String filePath) {
        return storageService.readFileContent(filePath);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<Object> handleStorageFileNotFound(
            final StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
