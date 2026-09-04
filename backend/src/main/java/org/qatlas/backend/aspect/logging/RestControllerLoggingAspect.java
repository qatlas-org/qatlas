package org.qatlas.backend.aspect.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RestControllerLoggingAspect {

    private static final Logger logger = LoggerFactory
            .getLogger(RestControllerLoggingAspect.class);

    @Before(value =
            "execution(* org.qatlas.backend.controller.*.*(..))")
    public void beforeAdvice(JoinPoint joinPoint) {
        logger.debug("Executing rest controller operation: {}",
                joinPoint.getSignature().toShortString());
    }

}
