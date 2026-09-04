package org.qatlas.backend.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "TEST_SUITE", schema = "reports_db")
public class TestSuite implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(
        name = "TEST_EXECUTION_ID",
        nullable = false,
        foreignKey = @ForeignKey(
            name = "FK_TEST_SUITE_EXECUTION_ID"
        ),
        updatable = false
    )
    private TestExecution testExecution;

    @Column(name = "NAME", nullable = false, length = 100)
    private String testSuiteName;

    @Column(name = "CREATED_TIME", nullable = false, updatable = false)
    private LocalDateTime executionStartTime;

    @Column(name = "UPDATED_TIME")
    private LocalDateTime executionEndTime;

    @Column(name = "PLANNED_TEST_CASES_CNT", nullable = false)
    private Integer plannedTestCaseCount;

    @OneToMany(mappedBy = "testSuite")
    private List<TestCase> testCases;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestExecution getTestExecution() {
        return testExecution;
    }

    public void setTestExecution(TestExecution testExecution) {
        this.testExecution = testExecution;
    }

    public String getTestSuiteName() {
        return testSuiteName;
    }

    public void setTestSuiteName(String testSuiteName) {
        this.testSuiteName = testSuiteName;
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

    public Integer getPlannedTestCaseCount() {
        return plannedTestCaseCount;
    }

    public void setPlannedTestCaseCount(Integer plannedTestCaseCount) {
        this.plannedTestCaseCount = plannedTestCaseCount;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }
}
