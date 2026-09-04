package org.qatlas.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.qatlas.backend.Constants.REST_CONTROLLERS_BASE_PKG_NAME;
import static org.qatlas.backend.Constants.REST_CONTROLLERS_PREFIX;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(final PathMatchConfigurer configurer) {
        configurer
            .addPathPrefix(
                REST_CONTROLLERS_PREFIX,
                HandlerTypePredicate
                    .forBasePackage(REST_CONTROLLERS_BASE_PKG_NAME)
            );
    }
}
