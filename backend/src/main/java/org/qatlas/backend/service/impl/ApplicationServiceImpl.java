package org.qatlas.backend.service.impl;

import org.qatlas.backend.entity.Application;
import org.qatlas.backend.exception.ApplicationNotFoundException;
import org.qatlas.backend.mapper.EntityVOMapper;
import org.qatlas.backend.repository.ApplicationRepository;
import org.qatlas.backend.service.ApplicationService;
import org.qatlas.backend.vo.ApplicationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private ApplicationRepository applicationRepository;

    private EntityVOMapper entityVOMapper;

    @Autowired
    public ApplicationServiceImpl(
            ApplicationRepository applicationRepository,
            EntityVOMapper entityVOMapper) {
        this.applicationRepository = applicationRepository;
        this.entityVOMapper = entityVOMapper;
    }

    @Override
    @Transactional
    public ApplicationVO create(final ApplicationVO applicationVO) {
        Application entity = entityVOMapper.map(applicationVO);
        entity = applicationRepository.save(entity);
        return entityVOMapper.map(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationVO get(final Long id) {
        Application application = getApplication(id);
        return entityVOMapper.map(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationVO> getAll() {
        List<Application> applications = applicationRepository.findAll();
        return entityVOMapper.mapApplications(applications);
    }

    @Override
    @Transactional
    public ApplicationVO update(final ApplicationVO applicationVO) {
        if (!applicationRepository.existsById(applicationVO.getId())) {
            throw new ApplicationNotFoundException(applicationVO.getId());
        }
        return entityVOMapper.map(
            applicationRepository.save(
                entityVOMapper.map(applicationVO)
            )
        );
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        applicationRepository
            .delete(getApplication(id));
    }

    @Override
    public ApplicationVO getByName(String name) {
        return entityVOMapper.map(
            applicationRepository.findByNameIgnoreCase(name).orElse(null)
        );
    }

    @Override
    public Application getApplication(final Long id) {
        return applicationRepository
            .findById(id)
            .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

}
