package org.qatlas.backend.exception;

public class TestStepNotFoundException extends ResourceNotFoundException {
    public TestStepNotFoundException(final Long id) {
        super("Could not find Test Step with ID: " + id);
    }
}
