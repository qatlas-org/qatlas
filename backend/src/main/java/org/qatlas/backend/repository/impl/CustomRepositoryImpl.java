package org.qatlas.backend.repository.impl;

import org.qatlas.backend.repository.CustomRepository;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.io.Serializable;

public class CustomRepositoryImpl<T, I extends Serializable>
        extends SimpleJpaRepository<T, I>
        implements CustomRepository<T, I> {

    private final EntityManager entityManager;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public CustomRepositoryImpl(
            final JpaEntityInformation<T, ?> entityInformation,
            final EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void refresh(final T t) {
        entityManager.refresh(t);
    }
}
