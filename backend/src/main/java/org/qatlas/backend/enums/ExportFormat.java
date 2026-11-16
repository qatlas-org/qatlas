package org.qatlas.backend.enums;

/**
 * File formats supported by the tabular export endpoints.
 */
public enum ExportFormat {
    CSV("text/csv; charset=UTF-8", "csv"),
    XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");

    private final String contentType;
    private final String fileExtension;

    ExportFormat(String contentType, String fileExtension) {
        this.contentType = contentType;
        this.fileExtension = fileExtension;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}
