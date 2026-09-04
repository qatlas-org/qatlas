package org.qatlas.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface CustomRepository<T, I extends Serializable>
    extends JpaRepository<T, I> {
    void refresh(T t);
}
