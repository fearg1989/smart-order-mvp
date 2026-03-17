package tech.fearg.smartorder.infrastructure.adapter.out.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tech.fearg.smartorder.application.port.out.AiOrderParserPort;
import tech.fearg.smartorder.domain.exception.OrderProcessingException;
import tech.fearg.smartorder.domain.model.Client;
import tech.fearg.smartorder.domain.model.Order;
import tech.fearg.smartorder.domain.model.OrderItem;
import tech.fearg.smartorder.infrastructure.adapter.out.ai.dto.*;

import java.util.List;
import java.util.Map;

/**
 * Outbound Adapter — Groq AI.
 *
 * Implements AiOrderParserPort using Spring's RestClient (Spring 6.1+).
 * The prompt forces a strict JSON response so the ObjectMapper can parse it
 * reliably without extra cleanup.
 */
@Component
public class GroqAiOrderParserAdapter implements AiOrderParserPort {

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    private static final String SYSTEM_PROMPT = """
            You are a B2B order parsing assistant.
            Extract order items from the unstructured text provided by the user.
            Return ONLY a valid JSON object — no markdown, no explanation, no code block.
            Use this exact structure:
            {
              "items": [
                {"productName": "string", "quantity": integer, "unitPrice": decimal}
              ]
            }
            If a unit price is not mentioned, estimate a reasonable placeholder value such as 0.00.
            """;

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GroqAiOrderParserAdapter(
            RestClient restClient,
            ObjectMapper objectMapper,
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.model}") String model) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public Order parse(String rawText, Client client) {
        GroqChatRequest request = GroqChatRequest.builder()
                .model(model)
                .messages(List.of(
                        new GroqMessage("system", SYSTEM_PROMPT),
                        new GroqMessage("user", rawText)))
                .temperature(0.0)
                .responseFormat(Map.of("type", "json_object"))
                .build();

        GroqChatResponse response = callGroq(request);
        ParsedOrderDto parsedOrder = parseContent(extractContent(response));
        List<OrderItem> items = mapItems(parsedOrder);

        return Order.newPending(client, items, rawText);
    }

    // --- private helpers ---

    private GroqChatResponse callGroq(GroqChatRequest request) {
        try {
            return restClient.post()
                    .uri(GROQ_API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(request)
                    .retrieve()
                    .body(GroqChatResponse.class);
        } catch (Exception e) {
            throw new OrderProcessingException("Failed to communicate with Groq API", e);
        }
    }

    private String extractContent(GroqChatResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new OrderProcessingException("Empty response received from Groq API");
        }
        return response.getChoices().get(0).getMessage().getContent();
    }

    private ParsedOrderDto parseContent(String json) {
        try {
            return objectMapper.readValue(json, ParsedOrderDto.class);
        } catch (Exception e) {
            throw new OrderProcessingException("Failed to parse JSON response from Groq: " + json, e);
        }
    }

    private List<OrderItem> mapItems(ParsedOrderDto dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new OrderProcessingException("AI could not identify any order items in the provided text");
        }
        return dto.getItems().stream()
                .map(i -> new OrderItem(i.getProductName(), i.getQuantity(), i.getUnitPrice()))
                .toList();
    }
}
