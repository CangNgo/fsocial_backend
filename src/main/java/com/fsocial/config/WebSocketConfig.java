package com.fsocial.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

//@Configuration
//@EnableWebSocketSecurity
//public class WebSocketConfig  {
//    @Bean
//    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
//        messages.simpDestMatchers("/user/queue/errors").permitAll()
//                .simpDestMatchers("/admin/**").hasRole("ADMIN").anyMessage()
//                .authenticated();
//    }
//}
