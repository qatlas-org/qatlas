package org.qatlas.backend.entity;

import org.qatlas.backend.enums.ExecutionStatus;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "TEST_CASE", schema = "reports_db")
public class TestCase implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(
        name = "TEST_SUITE_ID",
        foreignKey = @ForeignKey(
            name = "FK_TEST_CASE_SUITE_ID"
        ),
        nullable = false,
        updatable = false
    )
    private TestSuite testSuite;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "CREATED_TIME", nullable = false)
    private LocalDateTime executionStartTime;

    @Column(name = "UPDATED_TIME")
    private LocalDateTime executionEndTime;

    @Column(name = "EXECUTION_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private ExecutionStatus executionStatus;

    @Column(name = "COMMENTS")
    private String comments;

    @OneToMany(mappedBy = "testCase")
    private List<TestStep> testSteps;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestSuite getTestSuite() {
        return testSuite;
    }

    public void setTestSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public LocalDateTime getExecutionStartTime() {
        return executionStartTime;
    }

    public void setExecutionStartTime(LocalDateTime executionStartTime) {
        this.executionStartTime = executionStartTime;
    }

    public LocalDateTime getExecutionEndTime() {
        return executionEndTime;
    }

    public void setExecutionEndTime(LocalDateTime executionEndTime) {
        this.executionEndTime = executionEndTime;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<TestStep> getTestSteps() {
        return testSteps;
    }

    public void setTestSteps(List<TestStep> testSteps) {
        this.testSteps = testSteps;
    }

}
