package nl.craftsmen.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = DemoApplication.class)
@DirtiesContext
public class RetryTest {

	private static final String BACKEND_A = "backendA";

	@Autowired
	private TestRestTemplate testRestTemplate;

	@MockBean
	private RestTemplate restTemplate;


	@Test
	public void shouldRetryThreeTimes() {
		// When
		produceFailure(BACKEND_A);

		checkMetrics("failed_with_retry", BACKEND_A, "1.0");
	}

	@Test
	public void shouldSucceedWithoutRetry() {
		produceSuccess(BACKEND_A);

		checkMetrics("successful_without_retry", BACKEND_A, "1.0");
	}

	private void checkMetrics(String kind, String backend, String count) {
		ResponseEntity<String> metricsResponse = testRestTemplate.getForEntity("/actuator/prometheus", String.class);
		assertThat(metricsResponse.getBody()).isNotNull();
		String response = metricsResponse.getBody();
		assertThat(response).contains("resilience4j_retry_calls{application=\"demo\",kind=\"" + kind + "\",name=\"" +  backend + "\",} " + count);
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
