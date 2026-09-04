package org.qatlas.backend.service.impl;

import org.qatlas.backend.entity.TestStep;
import org.qatlas.backend.entity.TestStepAttachment;
import org.qatlas.backend.exception.StorageFileNotFoundException;
import org.qatlas.backend.exception.TestStepNotFoundException;
import org.qatlas.backend.mapper.TestStepAttachmentMapper;
import org.qatlas.backend.repository.TestStepAttachmentRepository;
import org.qatlas.backend.repository.TestStepRepository;
import org.qatlas.backend.service.TestStepAttachmentService;
import org.qatlas.backend.vo.TestStepAttachmentVO;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TestStepAttachmentServiceImpl
        implements TestStepAttachmentService {

    private Logger logger = LoggerFactory.getLogger(TestStepAttachmentServiceImpl.class);

    private TestStepAttachmentRepository testStepAttachmentRepository;

    private TestStepRepository testStepRepository;

    private TestStepAttachmentMapper testStepAttachmentMapper;

    private FileSystemStorageService fileSystemStorageService;

    @Autowired
    public TestStepAttachmentServiceImpl(
            TestStepAttachmentRepository testStepAttachmentRepository,
            TestStepRepository testStepRepository,
            TestStepAttachmentMapper testStepAttachmentMapper,
            FileSystemStorageService fileSystemStorageService) {
        this.testStepAttachmentRepository = testStepAttachmentRepository;
        this.testStepRepository = testStepRepository;
        this.testStepAttachmentMapper = testStepAttachmentMapper;
        this.fileSystemStorageService = fileSystemStorageService;
    }

    @Override
    @Transactional
    @Async("threadPoolAttachmentUploadTaskExecutor")
    public void create(
            final TestStepAttachmentVO testStepAttachmentVO) {
        logger.debug(
            "Creating Attachment {} of type {} for Test Step with ID: {}",
            testStepAttachmentVO.getFileName(),
            testStepAttachmentVO.getAttachmentType().getName(),
            testStepAttachmentVO.getTestStepId()
        );

        TestStep testStep = getTestStep(testStepAttachmentVO.getTestStepId());
        TestStepAttachment attachment = testStepAttachmentRepository.save(
            testStepAttachmentMapper.map(testStepAttachmentVO, testStep)
        );
        logger
            .debug(
                "Created {} Attachment {} for Test Step {} - {}",
                attachment.getAttachmentType().getName(),
                attachment.getFileName(),
                attachment.getTestStep().getId(),
                attachment.getTestStep().getDescription()
            );
        fileSystemStorageService
            .upload(
                testStepAttachmentVO.getFileContent(),
                attachment.getAttachmentRelativePath(),
                testStepAttachmentVO.isFileContentBase64Encoded()
            );
    }

    @Override
    public void deleteTestStepAttachments(List<Long> executionIds) {
        if(CollectionUtils.isNotEmpty(executionIds)) {
            testStepAttachmentRepository.deleteByTestStepTestCaseTestSuiteTestExecutionIdIn(executionIds);
            deleteAttachmentFolder(executionIds);
        }
    }

    private TestStep getTestStep(Long testStepId) {
        return testStepRepository
            .findById(testStepId)
            .orElseThrow(() ->
                new TestStepNotFoundException(testStepId)
            );
    }

    private void deleteAttachmentFolder(final List<Long> executionIds) {
        if(CollectionUtils.isNotEmpty(executionIds)) {
            for (Long executionId : executionIds) {
                try {
                    fileSystemStorageService.deleteFolder(String.valueOf(executionId));
                } catch (StorageFileNotFoundException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }
}
