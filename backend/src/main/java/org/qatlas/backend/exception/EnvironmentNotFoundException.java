package org.qatlas.backend.exception;

public class EnvironmentNotFoundException extends ResourceNotFoundException {
    public EnvironmentNotFoundException(final Long id) {
        super("Could not find Environment with id: " + id);
    }
}
