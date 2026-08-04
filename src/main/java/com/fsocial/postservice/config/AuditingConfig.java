package com.fsocial.postservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditingConfig {
    @Bean
    public ApplicationAuditAware auditorAware (){
        return new ApplicationAuditAware();
    }
}
