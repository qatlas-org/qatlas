package org.qatlas.backend.exception;

public class TestSuiteExecutionNotFoundException
        extends ResourceNotFoundException {

    public TestSuiteExecutionNotFoundException(final Long id) {
        super("Could not find test suite execution with id: " + id);
    }

}
