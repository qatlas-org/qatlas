package org.qatlas.backend.service.impl;

import org.qatlas.backend.entity.*;
import org.qatlas.backend.enums.ExecutionStatus;
import org.qatlas.backend.exception.TestExecutionNotFoundException;
import org.qatlas.backend.mapper.TestExecutionMapper;
import org.qatlas.backend.repository.TestCaseRepository;
import org.qatlas.backend.repository.TestExecutionRepository;
import org.qatlas.backend.repository.TestSuiteRepository;
import org.qatlas.backend.service.ApplicationService;
import org.qatlas.backend.service.EnvironmentService;
import org.qatlas.backend.service.TestCaseService;
import org.qatlas.backend.service.TestExecutionService;
import org.qatlas.backend.service.TestStepAttachmentService;
import org.qatlas.backend.service.TestStepService;
import org.qatlas.backend.service.TestSuiteService;
import org.qatlas.backend.vo.TestCaseVO;
import org.qatlas.backend.vo.TestExecutionVO;
import org.qatlas.backend.vo.TestStepAttachmentVO;
import org.qatlas.backend.vo.TestStepVO;
import org.qatlas.backend.vo.TestSuiteVO;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class TestExecutionServiceImpl implements TestExecutionService {

    private Logger logger = LoggerFactory.getLogger(TestExecutionServiceImpl.class);

    private TestExecutionRepository testExecutionRepository;

    private TestCaseRepository testCaseRepository;

    private TestSuiteRepository testSuiteRepository;

    private TestSuiteService testSuiteService;

    private TestCaseService testCaseService;

    private TestStepService testStepService;

    private StaticReportHtmlGenerator staticReportHtmlGenerator;

    private ApplicationService applicationService;

    private EnvironmentService environmentService;

    private TestExecutionMapper testExecutionMapper;

    private TestStepAttachmentService testStepAttachmentService;

    private FileSystemStorageService fileSystemStorageService;

    @Autowired
    public TestExecutionServiceImpl(
            TestExecutionRepository testExecutionRepository,
            TestCaseRepository testCaseRepository,
            TestSuiteRepository testSuiteRepository,
            TestSuiteService testSuiteService,
            TestCaseService testCaseService,
            TestStepService testStepService,
            StaticReportHtmlGenerator staticReportHtmlGenerator,
            ApplicationService applicationService,
            EnvironmentService environmentService,
            TestExecutionMapper testExecutionMapper,
            TestStepAttachmentService testStepAttachmentService,
            FileSystemStorageService fileSystemStorageService) {
        this.testExecutionRepository = testExecutionRepository;
        this.testCaseRepository = testCaseRepository;
        this.testSuiteRepository = testSuiteRepository;
        this.testSuiteService = testSuiteService;
        this.testCaseService = testCaseService;
        this.testStepService = testStepService;
        this.staticReportHtmlGenerator = staticReportHtmlGenerator;
        this.applicationService = applicationService;
        this.environmentService = environmentService;
        this.testExecutionMapper = testExecutionMapper;
        this.testStepAttachmentService = testStepAttachmentService;
        this.fileSystemStorageService = fileSystemStorageService;
    }

    @Override
    @Transactional
    public TestExecutionVO create(final TestExecutionVO testExecutionVO) {
        Application application = applicationService
                .getApplication(testExecutionVO.getApplicationId());
        Environment environment = environmentService
                .getEnvironment(testExecutionVO.getEnvironmentId());
        return testExecutionMapper.map(
            testExecutionRepository.save(
                testExecutionMapper.map(
                    testExecutionVO, application, environment)
            )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TestExecutionVO getById(final Long id) {
        TestExecutionVO testExecutionVO = testExecutionMapper.map(get(id));
        populateCounts(List.of(testExecutionVO));
        return testExecutionVO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestExecutionVO> getAllActive(final int limit) {
        List<TestExecutionVO> executions = testExecutionMapper.map(
            testExecutionRepository.findByOrderByIdDesc(
                PageRequest.of(0, limit)
            )
        );
        populateCounts(executions);
        return executions;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestExecutionVO> getAllByApplicationId(Long applicationId) {
        List<TestExecutionVO> executions = testExecutionMapper
            .map(
                testExecutionRepository
                    .findByApplicationIdOrderByIdDesc(applicationId)
            );
        populateCounts(executions);
        return executions;
    }

    /**
     * Populates test-case status counts (targeted/executed/passed/failed/
     * inProgress) and the endTime fallback on each VO using a small,
     * fixed number of aggregate GROUP BY queries against the given
     * executions' IDs - regardless of how many test suites/cases/steps
     * sit underneath them. This replaces the previous approach of lazily
     * loading the full testSuites -> testCases entity graph per execution
     * and counting in Java (an N+1 query fan-out on every call).
     */
    private void populateCounts(final List<TestExecutionVO> executions) {
        if (CollectionUtils.isEmpty(executions)) {
            return;
        }
        List<Long> executionIds = executions
            .stream()
            .map(TestExecutionVO::getId)
            .collect(Collectors.toList());

        Map<Long, Map<ExecutionStatus, Long>> statusCountsByExecution = new HashMap<>();
        for (Object[] row : testCaseRepository.countByExecutionIdsGroupedByStatus(executionIds)) {
            Long executionId = (Long) row[0];
            ExecutionStatus status = (ExecutionStatus) row[1];
            Long count = (Long) row[2];
            statusCountsByExecution
                .computeIfAbsent(executionId, id -> new EnumMap<>(ExecutionStatus.class))
                .put(status, count);
        }

        Map<Long, Long> plannedCountByExecution = new HashMap<>();
        for (Object[] row : testSuiteRepository.sumPlannedTestCaseCountByExecutionIds(executionIds)) {
            // SUM() over an Integer column can come back as Long or
            // BigDecimal depending on JPA provider/dialect - Number covers both.
            plannedCountByExecution.put((Long) row[0], ((Number) row[1]).longValue());
        }

        Map<Long, LocalDateTime> maxEndTimeByExecution = new HashMap<>();
        for (Object[] row : testCaseRepository.maxExecutionEndTimeByExecutionIds(executionIds)) {
            maxEndTimeByExecution.put((Long) row[0], (LocalDateTime) row[1]);
        }

        for (TestExecutionVO execution : executions) {
            Map<ExecutionStatus, Long> statusCounts = statusCountsByExecution
                .getOrDefault(execution.getId(), Map.of());

            long passed = statusCounts.getOrDefault(ExecutionStatus.PASSED, 0L);
            long failed = statusCounts.getOrDefault(ExecutionStatus.FAILED, 0L);
            long warning = statusCounts.getOrDefault(ExecutionStatus.WARNING, 0L);
            long inProgress = statusCounts.getOrDefault(ExecutionStatus.PROGRESS, 0L);
            long executed = passed + failed + warning;
            long targeted = plannedCountByExecution.getOrDefault(execution.getId(), 0L);

            execution.setTargetedTestCaseCount((int) targeted);
            execution.setExecutedTestCaseCount((int) executed);
            execution.setPassedTestCaseCount((int) passed);
            execution.setFailedTestCaseCount((int) failed);
            // Skipped count has never had a real definition (see original
            // TODO on TestSuiteVO.getSkippedTestCaseCount) - kept at 0 to
            // match prior behavior.
            execution.setSkippedTestCaseCount(0);
            execution.setInProgressTestCaseCount((int) inProgress);

            //TODO: Fix the above temporary fix - same caveat as before,
            // just moved server-side instead of computed per-request.
            if (execution.getEndTime() == null && targeted <= executed) {
                execution.setEndTime(maxEndTimeByExecution.get(execution.getId()));
            }
        }
    }

    @Override
    @Transactional
    public TestExecutionVO update(Long id, LocalDateTime executionEndTime) {
        TestExecution testExecution = get(id);
        testExecution.setEndTime(executionEndTime);
        return testExecutionMapper
            .map(
                testExecutionRepository
                    .save(testExecution)
            );
    }

    @Override
    @Transactional
    public void archive(List<Long> executionIds, boolean deleteAttachmentsOnly) {
        if(CollectionUtils.isNotEmpty(executionIds)) {
            testStepAttachmentService.deleteTestStepAttachments(executionIds);
            if(!deleteAttachmentsOnly) {
                testExecutionRepository.archive(executionIds);
            }
        }
    }

    @Override
    public void downloadAttachments(List<Long> executionIds, OutputStream outputStream) throws IOException {
        if(CollectionUtils.isNotEmpty(executionIds)) {
            try(ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(outputStream))) {
                for(TestExecution e : testExecutionRepository.findAllById(executionIds)) {
                    addTestExecutionToZip(e, zipOut);
                }
            }
        }
    }

    private void addTestExecutionToZip(TestExecution testExecution, ZipOutputStream zipOut) throws IOException {
        File executionFolder = fileSystemStorageService.getRootLocation().resolve(String.valueOf(testExecution.getId())).toFile();
        String folderName = "TestExecution_" + executionFolder.getName() + "/";
        zipOut.putNextEntry(new ZipEntry(folderName));
        zipOut.closeEntry();
        for(TestSuite s : testExecution.getTestSuites()) {
            addTestSuiteFolderToZip(s, folderName, zipOut);
        }
    }

    private void addTestSuiteFolderToZip(TestSuite testSuite, String executionFolder, ZipOutputStream zipOut) throws IOException {
        String suiteFolder = executionFolder + "TestSuite_" + testSuite.getId() + "/";
        zipOut.putNextEntry(new ZipEntry(suiteFolder));
        zipOut.closeEntry();
        for(TestCase c : testSuite.getTestCases()) {
            addTestCaseFolderToZip(c, suiteFolder, zipOut);
        }
    }

    private void addTestCaseFolderToZip(TestCase testCase, String suiteFolder, ZipOutputStream zipOut) throws IOException {
        String testCaseFolder = suiteFolder + "TestCase_" + testCase.getId() + "/";
        zipOut.putNextEntry(new ZipEntry(testCaseFolder));
        zipOut.closeEntry();
        for(TestStep step : testCase.getTestSteps()) {
            addTestStepAttachmentsToZip(step, testCaseFolder, zipOut);
        }
    }

    private void addTestStepAttachmentsToZip(TestStep testStep, String testCaseFolder, ZipOutputStream zipOut) throws IOException {
        for(TestStepAttachment a : testStep.getAttachments()) {
            File attachment = fileSystemStorageService.getRootLocation()
                .resolve(a.getAttachmentRelativePath())
                .toFile();
            try {
                FileInputStream fis = new FileInputStream(attachment);
                ZipEntry zipEntry = new ZipEntry(testCaseFolder + testStep.getId() + "_" + a.getId() + "_" + attachment.getName());
                zipOut.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            } catch (FileNotFoundException e) {
                logger.error("Could not find file with path " + attachment.getPath());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void exportStaticReport(final Long executionId, final OutputStream outputStream) throws IOException {
        TestExecutionVO execution = getById(executionId);
        List<TestSuiteVO> testSuites = testSuiteService.getByTestExecutionId(executionId);
        for (TestSuiteVO suite : testSuites) {
            List<TestCaseVO> testCases = testCaseService.getAll(suite.getId(), null, null);
            for (TestCaseVO testCase : testCases) {
                testCase.setTestSteps(testStepService.getByTestCase(testCase.getId()));
            }
            suite.setTestCases(testCases);
        }

        String html = staticReportHtmlGenerator.build(execution, testSuites);

        try (ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(outputStream))) {
            zipOut.putNextEntry(new ZipEntry("report.html"));
            zipOut.write(html.getBytes(StandardCharsets.UTF_8));
            zipOut.closeEntry();

            for (TestSuiteVO suite : testSuites) {
                for (TestCaseVO testCase : suite.getTestCases()) {
                    for (TestStepVO step : testCase.getTestSteps()) {
                        for (TestStepAttachmentVO attachment : step.getAttachments()) {
                            addAttachmentToZip(attachment, zipOut);
                        }
                    }
                }
            }
        }
    }

    private void addAttachmentToZip(
            final TestStepAttachmentVO attachment,
            final ZipOutputStream zipOut) throws IOException {
        String path = attachment.getAttachmentRelativePath();
        if (path == null) {
            return;
        }
        // Strip the "../" the mapper adds for the live app's relative-URL
        // resolution - the static export lays attachments out fresh under
        // its own "attachments/" folder next to report.html.
        String rawPath = path.replaceFirst("^\\.\\./attachments/", "");
        File file = fileSystemStorageService.getRootLocation().resolve(rawPath).toFile();
        try (FileInputStream fis = new FileInputStream(file)) {
            zipOut.putNextEntry(new ZipEntry("attachments/" + rawPath));
            byte[] bytes = new byte[4096];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            zipOut.closeEntry();
        } catch (FileNotFoundException e) {
            logger.error("Could not find attachment file for static export: " + file.getPath());
        }
    }

    private TestExecution get(final Long id) {
        return testExecutionRepository
            .findById(id)
            .orElseThrow(() ->
                new TestExecutionNotFoundException(id)
            );
    }

}
