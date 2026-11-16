package com.dinsaren.springbootjwtapi.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("public", "uploads").toAbsolutePath();
        String uploadPath = uploadDir.toUri().toString();
        if (!uploadPath.endsWith("/")) {
            uploadPath += "/";
        }

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/api/swagger-ui/index.html", "/swagger-ui/index.html");
        registry.addRedirectViewController("/api/swagger-ui.html", "/swagger-ui.html");
        registry.addRedirectViewController("/api/swagger-ui", "/swagger-ui.html");
        registry.addRedirectViewController("/swagger", "/swagger-ui.html");
    }
}
