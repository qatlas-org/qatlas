package org.qatlas.backend.service.impl;

import org.qatlas.backend.entity.Environment;
import org.qatlas.backend.exception.EnvironmentNotFoundException;
import org.qatlas.backend.mapper.EntityVOMapper;
import org.qatlas.backend.repository.EnvironmentRepository;
import org.qatlas.backend.service.EnvironmentService;
import org.qatlas.backend.vo.EnvironmentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EnvironmentServiceImpl implements EnvironmentService {

    private EnvironmentRepository environmentRepository;

    private EntityVOMapper entityVOMapper;

    @Autowired
    public EnvironmentServiceImpl(
            EnvironmentRepository environmentRepository,
            EntityVOMapper entityVOMapper) {
        this.environmentRepository = environmentRepository;
        this.entityVOMapper = entityVOMapper;
    }

    @Override
    @Transactional
    public EnvironmentVO create(final EnvironmentVO environmentVO) {
        return save(environmentVO);
    }

    @Override
    @Transactional(readOnly = true)
    public EnvironmentVO get(final Long id) {
        return entityVOMapper
            .map(getEnvironment(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnvironmentVO> getAll() {
        return entityVOMapper.mapEnvironments(
            environmentRepository.findAll()
        );
    }

    @Override
    @Transactional
    public EnvironmentVO update(final EnvironmentVO environmentVO) {
        if (environmentRepository.existsById(environmentVO.getId())) {
            throw new EnvironmentNotFoundException(environmentVO.getId());
        }
        return save(environmentVO);
    }

    @Override
    @Transactional
    public void delete(final Long id) {
        if (environmentRepository.existsById(id)) {
            throw new EnvironmentNotFoundException(id);
        }
        environmentRepository.deleteById(id);
    }

    @Override
    public EnvironmentVO getByName(String name) {
        return entityVOMapper.map(
            environmentRepository.getByNameIgnoreCase(name).orElse(null)
        );
    }

    @Override
    public Environment getEnvironment(Long id) {
        return environmentRepository
            .findById(id)
            .orElseThrow(() ->
                new EnvironmentNotFoundException(id)
            );
    }

    private EnvironmentVO save(final EnvironmentVO environmentVO) {
        return entityVOMapper.map(
            environmentRepository.save(
                entityVOMapper.map(environmentVO)
            )
        );
    }

}
