package nl.craftsmen.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Component
@Retry(name = "backendA")
@CircuitBreaker(name = "backendA")
public class HelloClient {

    @Autowired
    private RestTemplate restTemplate;

    public String getHello() {
        return restTemplate.getForObject("http://localhost:8080/backendA", String.class);
    }

    public String fallback(Throwable throwable) {
        return "Simple hello";
    }
}
