package org.qatlas.backend.exception;

public class ApplicationNotFoundException extends ResourceNotFoundException {
    public ApplicationNotFoundException(final Long id) {
        super("Could not find Application with ID: " + id);
    }
}
