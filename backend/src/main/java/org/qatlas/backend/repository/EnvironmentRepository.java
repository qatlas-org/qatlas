package org.qatlas.backend.repository;

import org.qatlas.backend.entity.Environment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnvironmentRepository
        extends JpaRepository<Environment, Long> {
    Optional<Environment> getByNameIgnoreCase(final String name);
}
