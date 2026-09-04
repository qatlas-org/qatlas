package org.qatlas.backend.entity;

import org.qatlas.backend.enums.AttachmentType;
import org.apache.commons.lang3.StringUtils;

import jakarta.persistence.*;
import java.io.File;
import java.io.Serializable;

@Entity
@Table(name = "TEST_STEP_ATTACHMENT", schema = "reports_db")
public class TestStepAttachment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(
        name = "TEST_STEP_ID",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "FK_TEST_STEP_ATTACHMENT_STEP_ID"
        ),
        updatable = false
    )
    private TestStep testStep;

    @Enumerated(EnumType.STRING)
    @Column(name = "ATTACHMENT_TYPE", nullable = false, updatable = false)
    private AttachmentType attachmentType;

    @Column(name = "ATTACHMENT_NAME", nullable = false, length = 50, updatable = false)
    private String fileName;

    @Transient
    public TestCase getTestCase() {
        return testStep.getTestCase();
    }

    @Transient
    public TestSuite getTestSuite() {
        return getTestCase().getTestSuite();
    }

    @Transient
    public TestExecution getTestExecution() {
        return getTestSuite().getTestExecution();
    }

    @Transient
    public String getAttachmentRelativePath() {
        if (StringUtils.isEmpty(fileName)) {
            return StringUtils.EMPTY;
        }
        return getTestExecution().getId()
            + File.separator
            + getTestSuite().getId()
            + File.separator
            + getTestCase().getId()
            + File.separator
            + testStep.getId()
            + File.separator
            + fileName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestStep getTestStep() {
        return testStep;
    }

    public void setTestStep(TestStep testStep) {
        this.testStep = testStep;
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
}
