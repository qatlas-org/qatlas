package org.qatlas.backend.mapper;

import org.qatlas.backend.entity.Application;
import org.qatlas.backend.entity.Environment;
import org.qatlas.backend.vo.ApplicationVO;
import org.qatlas.backend.vo.EnvironmentVO;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring")
@Component
public interface EntityVOMapper {

    Application map(ApplicationVO vo);

    ApplicationVO map(Application entity);

    List<ApplicationVO> mapApplications(List<Application> entities);

    Environment map(EnvironmentVO vo);

    EnvironmentVO map(Environment entity);

    List<EnvironmentVO> mapEnvironments(List<Environment> entities);

}
