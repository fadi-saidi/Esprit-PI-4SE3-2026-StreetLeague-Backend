package tn.esprit.pi.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String productUploadDir;

    @Value("${app.upload.sponsors.dir}")
    private String sponsorUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path productPath = Paths.get(productUploadDir).toAbsolutePath();
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations("file:" + productPath + "/");

        Path sponsorPath = Paths.get(sponsorUploadDir).toAbsolutePath();
        registry.addResourceHandler("/uploads/sponsors/**")
                .addResourceLocations("file:" + sponsorPath + "/");
    }
}
