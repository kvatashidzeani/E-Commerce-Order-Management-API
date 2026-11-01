package com.ecommerce.ordermanagement.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupBanner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupBanner.class);

    private final Environment environment;

    public StartupBanner(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        String port = environment.getProperty("server.port", "8080");
        String swaggerPath = environment.getProperty("springdoc.swagger-ui.path", "/swagger-ui.html");

        log.info("======================================================");
        log.info(" API running:     http://localhost:{}", port);
        log.info(" Swagger UI:      http://localhost:{}{}", port, swaggerPath);
        log.info(" Auth register:   http://localhost:{}/api/auth/register", port);
        log.info(" Admin login:     admin@ecommerce.local / Admin123!", port);
        log.info("======================================================");
    }
}
