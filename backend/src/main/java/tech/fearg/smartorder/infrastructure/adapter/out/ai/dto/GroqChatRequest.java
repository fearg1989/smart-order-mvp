package tech.fearg.smartorder.infrastructure.adapter.out.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class GroqChatRequest {

    private final String model;
    private final List<GroqMessage> messages;
    private final double temperature;

    /** Groq supports response_format to enforce JSON output. */
    @JsonProperty("response_format")
    private final Map<String, String> responseFormat;
}
