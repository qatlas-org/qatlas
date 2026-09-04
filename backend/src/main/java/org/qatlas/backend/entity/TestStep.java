package org.qatlas.backend.entity;

import org.qatlas.backend.enums.ExecutionStatus;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TEST_STEP", schema = "reports_db")
public class TestStep implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(
        name = "TEST_CASE_ID",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "FK_TEST_STEP_CASE_ID"
        ),
        updatable = false
    )
    private TestCase testCase;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "OBJECT_NAME") // FIXME check with BAs
    private String objectName;

    @Column(name = "OPERATION") // FIXME check with BAs
    private String operation;

    @Column(name = "RESULT") // FIXME check with BAs
    private String result;

     // FIXME WHAT ARE THE UNITS OF
    @Column(name = "EXECUTION_TIME")
    private Long executionTime;

    @Column(name = "EXECUTION_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExecutionStatus executionStatus;

    @OneToMany(
        mappedBy = "testStep",
        orphanRemoval = true,
        cascade = CascadeType.PERSIST // FIXME why we need PERSIST
    )
    private List<TestStepAttachment> attachments = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    public List<TestStepAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<TestStepAttachment> attachments) {
        this.attachments = attachments;
    }
}
