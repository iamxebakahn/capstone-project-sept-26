package dev.zeba.productcatalogservice.service;

import dev.zeba.productcatalogservice.dto.GroqPitchResponseDto;
import dev.zeba.productcatalogservice.model.Product;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j //In prod good practise log i/p, intermediate process variable if any, o/p : not every DTO n all, coz prod cost increases! PII is never logged GDPR
public class PitchService {
    private static final String GROQ_URL= "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL="openai/gpt-oss-20b";
    private static final String SYSTEM_PROMPT="You are a marketing copywriter for ShopNest, an online store. You write short push notifications that make people want to open the app.";
    private final RestTemplate restTemplate;
    private final String apiKey;

    public PitchService(RestTemplateBuilder restTemplateBuilder, @Value("${groq.api-key}") String apiKey) {
        this.restTemplate = restTemplateBuilder.build();
        this.apiKey = apiKey;
    }


    //headers to take care of authorisation and content type
    public String generatePitch(Product product){
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            //checked the request payload, map is perfect data
            Map<String, Object> body = Map.of("model",MODEL,
                    "reasoning_effort","low",
                    "max_completion_tokens",300,
                    "messages", List.of(
                        Map.of("role","system","content",SYSTEM_PROMPT),
                        Map.of("role","user","content",buildPrompt(product))
                    ));

            ResponseEntity<GroqPitchResponseDto> response = restTemplate.postForEntity(GROQ_URL,new HttpEntity<>(body,headers), GroqPitchResponseDto.class);
            if(response.getStatusCode().is2xxSuccessful() && response.hasBody())
                return response.getBody().getChoices().get(0).getMessage().getContent();

            //return "New arrival:"+product.getName()+" for just $"+product.getPrice()+". Check it out!";
            throw new IllegalArgumentException("Pitch couldn't be generated");
        }
        catch (Exception e){
            //System.out.println(e.getMessage());
            log.warn("Error in LLM call"+ e.getMessage());
            return "New arrival:"+product.getName()+" for just $"+product.getPrice()+". Check it out!";
        }
    }

    private String buildPrompt(Product product){
        return """
                Write ONE push notification for this product.
                Rules:
                -Maximum 25 words
                -Mention the price
                -At most one emoji
                -No hashtags
                -Reply with only the notification text
                
                Product: %s
                Category: %s
                Price: $%s
                Details: %s""".formatted(product.getName(),product.getCategory().getName(),product.getPrice(),product.getDescription());
    }
}
