package org.qatlas.backend.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.qatlas.backend.enums.AttachmentType;
import org.qatlas.backend.validator.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

@Schema(name = "TestStepAttachment")
public class TestStepAttachmentVO {

    @Null(
        groups = ValidationGroups.Create.class,
        message = "Test Step Attachment ID should be empty."
    )
    private Long id;

    @Null(
        groups = ValidationGroups.Create.class,
        message = "Test Step ID should be empty."
    )
    private Long testStepId;

    @NotNull(
        groups = ValidationGroups.Create.class,
        message = "Test Step Attachment Type should not be empty."
    )
    @Schema(enumAsRef = true)
    private AttachmentType attachmentType;

    @NotNull(message = "Test Step Attachment File Name should not be empty.")
    @Size(
        max = 50,
        message = "Test Step Attachment File Name should not exceed 50 characters."
    )
    private String fileName;

    @NotNull(message = "Test Step Attachment Content should not be empty.")
    @Schema(format = "byte", type = "string")
    private byte[] fileContent;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean fileContentBase64Encoded;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String attachmentRelativePath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTestStepId() {
        return testStepId;
    }

    public void setTestStepId(Long testStepId) {
        this.testStepId = testStepId;
    }

    public AttachmentType getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(AttachmentType attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public boolean isFileContentBase64Encoded() {
        return fileContentBase64Encoded;
    }

    public void setFileContentBase64Encoded(boolean fileContentBase64Encoded) {
        this.fileContentBase64Encoded = fileContentBase64Encoded;
    }

    public String getAttachmentRelativePath() {
        return attachmentRelativePath;
    }

    public void setAttachmentRelativePath(String attachmentRelativePath) {
        this.attachmentRelativePath = attachmentRelativePath;
    }
}
