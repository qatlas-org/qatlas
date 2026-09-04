package org.qatlas.backend.entity;

import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "TEST_EXECUTION", schema = "reports_db")
@SQLRestriction("IS_ARCHIVED = 0")
public class TestExecution implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", updatable = false)
    private Long id;

    @Column(name = "NAME", length = 100, updatable = false)
    private String name;

    @ManyToOne
    @JoinColumn(
            name = "APPLICATION_ID",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "FK_TEST_EXECUTION_APP_ID"
            ),
            updatable = false
    )
    private Application application;

    @Column(name = "APP_VERSION", length = 50, updatable = false)
    private String applicationVersion;

    @OneToOne
    @JoinColumn(
            name = "ENVIRONMENT_ID",
            foreignKey = @ForeignKey(
                    name = "FK_TEST_EXECUTION_ENV_ID"
            ),
            updatable = false
    )
    private Environment environment;

    @Column(name = "BROWSER_NAME", nullable = false, length = 100, updatable = false)
    private String browser;

    @Column(name = "OS_NAME", length = 100, updatable = false)
    private String operatingSystem;

    @Column(name = "SYSTEM_IP", nullable = false, length = 50, updatable = false)
    private String systemIp;

    @Column(name = "SYSTEM_NAME", nullable = false, length = 50, updatable = false)
    private String systemName;

    @Column(name = "EXECUTED_BY", length = 50, updatable = false)
    private String executedBy;

    @Column(name = "START_TIME", nullable = false, updatable = false)
    private LocalDateTime startTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

    @Column(name = "IS_ARCHIVED")
    private Boolean archived = false;

    @OneToMany(mappedBy = "testExecution")
    private List<TestSuite> testSuites;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getSystemIp() {
        return systemIp;
    }

    public void setSystemIp(String systemIp) {
        this.systemIp = systemIp;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(String executedBy) {
        this.executedBy = executedBy;
    }

    public List<TestSuite> getTestSuites() {
        return testSuites;
    }

    public void setTestSuites(List<TestSuite> testSuites) {
        this.testSuites = testSuites;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }
}
