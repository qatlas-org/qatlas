package org.qatlas.backend.mapper;

import org.qatlas.backend.entity.Application;
import org.qatlas.backend.entity.Environment;
import org.qatlas.backend.entity.TestExecution;
import org.qatlas.backend.vo.TestExecutionVO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring")
@Component
public abstract class TestExecutionMapper {

    /**
     *
     * @param entity
     * @param application
     * @param environment
     */
    @AfterMapping
    protected void afterMapping(
            @MappingTarget final TestExecution entity,
            @Context Application application,
            @Context Environment environment) {
        entity.setApplication(application);
        entity.setEnvironment(environment);
    }

    public abstract TestExecution map(
            TestExecutionVO vo,
            @Context Application application,
            @Context Environment environment);

    @AfterMapping
    protected void afterMapping(
            final TestExecution entity,
            @MappingTarget
            final TestExecutionVO vo) {
        Environment environment = entity.getEnvironment();
        if (environment != null) {
            vo.setEnvironmentId(environment.getId());
            vo.setEnvironmentName(environment.getName());
        }

        Application application = entity.getApplication();
        if (application != null) {
            vo.setApplicationId(application.getId());
            vo.setApplicationName(application.getName());
        }
    }

    public abstract TestExecutionVO map(TestExecution entity);

    public abstract List<TestExecutionVO> map(List<TestExecution> entities);

}
