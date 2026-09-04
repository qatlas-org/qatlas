package org.qatlas.backend.controller.advice;

import org.qatlas.backend.exception.ResourceNotFoundException;
import org.qatlas.backend.exception.ServiceException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;

@RestControllerAdvice
public class RestControllerExceptionHandlerAdvice {

    @ExceptionHandler(ConstraintViolationException.class)
    public void handleConstraintViolationException(
            final HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public void handleResourceNotFoundException(
            final HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @ExceptionHandler(ServiceException.class)
    public void handleServiceException(
            final HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

}
