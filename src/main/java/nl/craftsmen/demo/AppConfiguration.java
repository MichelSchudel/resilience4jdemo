package nl.craftsmen.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfiguration {

    @Bean
    RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
