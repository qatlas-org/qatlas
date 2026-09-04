package org.qatlas.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Requires a valid admin token ONLY on the execution archive/delete endpoint.
 * Every other endpoint (all reads, creates from test-runner agents, etc.) is
 * intentionally left untouched so nothing else in the app changes behavior.
 */
@Component
@Order(1)
public class AdminOnlyFilter extends HttpFilter {

    private final AdminTokenService tokenService;

    public AdminOnlyFilter(final AdminTokenService tokenService) {
        this.tokenService = tokenService;
    }

    private static boolean isProtected(final HttpServletRequest request) {
        final String method = request.getMethod();
        final String path = request.getRequestURI();
        return "PUT".equalsIgnoreCase(method) && path != null && path.contains("/test-execution/archive/");
    }

    @Override
    protected void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        if (!isProtected(request)) {
            chain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring("Bearer ".length())
                : null;

        if (token == null || tokenService.validate(token).isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Admin authentication required to delete executions\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
