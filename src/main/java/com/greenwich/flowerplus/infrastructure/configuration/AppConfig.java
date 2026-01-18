package com.greenwich.flowerplus.infrastructure.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class AppConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowCredentials(true)
                        .allowedOrigins(
                                "http://localhost:3000",
                                "http://localhost:5173",
                                "https://thang.tail704409.ts.net",
                                "http://localhost:8080"
                        )
                        .allowedMethods("*")
                        .allowedHeaders("*");
            }
        };
    }
}
