package org.qatlas.backend.repository;

import org.qatlas.backend.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository
    extends JpaRepository<Application, Long> {
    Optional<Application> findByNameIgnoreCase(final String name);
}
