package nl.craftsmen.demo;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import io.vavr.collection.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = DemoApplication.class)
@DirtiesContext
public class CircuitBreakerTest {

    private static final String BACKEND_A = "backendA";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    public void shouldOpenAndCloseBackendACircuitBreaker() throws InterruptedException {
        // When
        Stream.rangeClosed(1,5).forEach((count) -> produceFailure(BACKEND_A));

        // Then
        checkHealthStatus(BACKEND_A + "CircuitBreaker", Status.DOWN);

        Thread.sleep(2000);

        // When
        Stream.rangeClosed(1,3).forEach((count) -> produceSuccess(BACKEND_A));

        checkHealthStatus(BACKEND_A + "CircuitBreaker", Status.UP);
    }

    private void checkHealthStatus(String circuitBreakerName, Status status) {
        ResponseEntity<HealthResponse> healthResponse = testRestTemplate.getForEntity("/actuator/health", HealthResponse.class);
        assertThat(healthResponse.getBody()).isNotNull();
        assertThat(healthResponse.getBody().getDetails()).isNotNull();
        Map<String, Object> backendACircuitBreakerDetails = healthResponse.getBody().getDetails().get(circuitBreakerName);
        assertThat(backendACircuitBreakerDetails).isNotNull();
        assertThat(backendACircuitBreakerDetails.get("status")).isEqualTo(status.toString());
    }

    private void produceFailure(String backend) {
        Mockito.reset(restTemplate);
        when(restTemplate.getForObject("http://localhost:8080/backendA", String.class)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        ResponseEntity<String> response = testRestTemplate.getForEntity("/hello", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void produceSuccess(String backend) {
        Mockito.reset(restTemplate);
        when(restTemplate.getForObject("http://localhost:8080/backendA", String.class)).thenReturn("hello, world!");
        ResponseEntity<String> response = testRestTemplate.getForEntity("/hello", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


}
