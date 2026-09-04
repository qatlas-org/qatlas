package org.qatlas.backend.exception;

public class TestSuiteNotFoundException extends ResourceNotFoundException {
    public TestSuiteNotFoundException(final Long id) {
        super("Could not find Test Suite with ID: " + id);
    }
}
