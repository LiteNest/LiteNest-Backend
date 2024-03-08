package container.desktop.containerdesktopbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class CorsConfig {
    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;
    @Value("${cors.allowed-methods}")
    private List<String> allowedMethods;
    @Value("${cors.allowed-headers}")
    private List<String> allowedHeaders;
    @Value("${cors.allowed-credentials}")
    private boolean allowedCredentials;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(allowedMethods);
        config.setAllowedHeaders(allowedHeaders);
        config.setAllowCredentials(allowedCredentials);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
