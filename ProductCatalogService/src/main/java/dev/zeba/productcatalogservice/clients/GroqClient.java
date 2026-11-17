package dev.zeba.productcatalogservice.clients;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import dev.zeba.productcatalogservice.dto.GroqPitchResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class GroqClient {
    private final RestClient restClient;

    public GroqClient(RestClient.Builder builder,
                      @Value("${groq.url}") String url,
                      @Value("${groq.api-key}") String apiKey) {
        this.restClient = builder.baseUrl(url)
                .defaultHeaders(headers -> headers.setBearerAuth(apiKey))
                .build();
    }

    @CircuitBreaker(name = "groq")
    public GroqPitchResponseDto chat(Map<String, Object> body) {
        return restClient.post()
                .body(body)
                .retrieve()
                .body(GroqPitchResponseDto.class);
    }
}
