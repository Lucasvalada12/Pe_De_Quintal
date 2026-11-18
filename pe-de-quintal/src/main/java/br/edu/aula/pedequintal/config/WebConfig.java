package br.edu.aula.pedequintal.config;

import br.edu.aula.pedequintal.service.FotoService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Publica a pasta local de fotos em /uploads/** (ex.: /uploads/abc.jpg). */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FotoService fotoService;

    public WebConfig(FotoService fotoService) {
        this.fotoService = fotoService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String local = fotoService.getPasta().toUri().toString();
        if (!local.endsWith("/")) {
            local = local + "/";
        }
        registry.addResourceHandler("/uploads/**").addResourceLocations(local);
    }
}
