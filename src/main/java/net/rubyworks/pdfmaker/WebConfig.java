package net.rubyworks.pdfmaker;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;

import net.rubyworks.pdfmaker.support.ErrAttr;
import net.rubyworks.pdfmaker.support.ErrHandler;
import net.rubyworks.pdfmaker.support.WebfluxTraceFilter;

@Configuration
public class WebConfig implements WebFluxConfigurer {
    @Value("${app_props.allowedOriginPatterns}") private String[] allowedOriginPatterns;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
        .allowedOriginPatterns(allowedOriginPatterns)
        .allowedMethods("*")
        .allowedHeaders("*")
        .allowCredentials(true)
        .maxAge(3600L);
    }

    @Bean
    WebfluxTraceFilter traceFilter() {
        return new WebfluxTraceFilter();
    }

    @Bean
    ErrAttr errAttr() {
        return new ErrAttr();
    }

    @Bean
    @Order(-2)
    ErrHandler errHandler(ErrAttr attr, ApplicationContext applicationContext, ServerCodecConfigurer serverCodecConfigurer) {
        return new ErrHandler(attr, applicationContext, serverCodecConfigurer);
    }

    @Bean
    RouterFunction<?> commandRouter(AppHandler handler) {
        return route()
                .POST("/pdf/preview", handler::preview)
                .build();
    }
}
