package org.qatlas.backend.exception;

public class TestExecutionNotFoundException extends ResourceNotFoundException {
    public TestExecutionNotFoundException(final Long id) {
        super("Could not find Test Execution with ID: " + id);
    }
}
