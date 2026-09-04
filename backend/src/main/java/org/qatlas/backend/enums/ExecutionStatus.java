package org.qatlas.backend.enums;

public enum ExecutionStatus {
    PLANNED,
    PROGRESS,
    PASSED,
    FAILED,
    WARNING;

    public boolean isExecuted() {
        return this != PROGRESS
            && this != PLANNED;
    }
}
