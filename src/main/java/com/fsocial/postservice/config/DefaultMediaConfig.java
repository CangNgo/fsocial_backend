package com.fsocial.postservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.defaults")
@Getter
@Setter
public class DefaultMediaConfig {

    private List<String> avatars = List.of();
    private List<String> backgrounds = List.of();
}
