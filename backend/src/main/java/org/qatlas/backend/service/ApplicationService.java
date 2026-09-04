package org.qatlas.backend.service;

import org.qatlas.backend.entity.Application;
import org.qatlas.backend.vo.ApplicationVO;

import java.util.List;

public interface ApplicationService {
    ApplicationVO create(ApplicationVO applicationVO);

    ApplicationVO get(Long id);

    List<ApplicationVO> getAll();

    ApplicationVO update(ApplicationVO applicationVO);

    void delete(Long id);

    ApplicationVO getByName(String name);

    Application getApplication(final Long id);
}
