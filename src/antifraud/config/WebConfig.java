package antifraud.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // This setting allows controllers mapped to "/path"
        // to also handle requests to "/path/"
        configurer.setUseTrailingSlashMatch(true);
    }
}