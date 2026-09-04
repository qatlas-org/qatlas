package org.qatlas.backend.service;

import org.qatlas.backend.entity.Environment;
import org.qatlas.backend.vo.EnvironmentVO;

import java.util.List;

public interface EnvironmentService {
    EnvironmentVO create(EnvironmentVO environmentVO);

    EnvironmentVO get(Long id);

    List<EnvironmentVO> getAll();

    EnvironmentVO update(EnvironmentVO environmentVO);

    void delete(Long id);

    EnvironmentVO getByName(String name);

    Environment getEnvironment(final Long id);
}
