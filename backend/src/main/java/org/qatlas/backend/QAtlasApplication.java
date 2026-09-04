package org.qatlas.backend;

import org.qatlas.backend.config.properties.StorageProperties;
import org.qatlas.backend.repository.impl.CustomRepositoryImpl;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class})
@EnableJpaRepositories(repositoryBaseClass = CustomRepositoryImpl.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class QAtlasApplication {

    public static void main(final String[] args) {
        SpringApplication.run(QAtlasApplication.class, args);
    }

    @Bean
    public LocalValidatorFactoryBean validator(
            final MessageSource messageSource) {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }

    @Bean
    @ConfigurationProperties(prefix = "app.open-api")
    public OpenAPI openAPI() {
        return new OpenAPI();
    }

}
