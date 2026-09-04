package org.qatlas.backend.exception;

public class TestCaseNotFoundException extends ResourceNotFoundException {
    public TestCaseNotFoundException(final Long id) {
        super("Could not find Test Case with ID: " + id);
    }
}
