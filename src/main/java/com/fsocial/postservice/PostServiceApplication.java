package com.fsocial.postservice;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PostServiceApplication {

    public static void main(String[] args) {
        Dotenv.configure().ignoreIfMissing().load()
                .entries()
                .forEach(entry -> {
                    if (System.getenv(entry.getKey()) == null) {
                        System.setProperty(entry.getKey(), entry.getValue());
                    }
                });
        SpringApplication.run(PostServiceApplication.class, args);
    }

}
