package org.qatlas.backend.service;

import org.qatlas.backend.vo.TestStepAttachmentVO;

import java.util.List;

public interface TestStepAttachmentService {
    void create(TestStepAttachmentVO testStepAttachmentVO);
    void deleteTestStepAttachments(final List<Long> executionIds);
}
