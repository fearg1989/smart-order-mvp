package tech.fearg.smartorder.application.port.in;

import lombok.Builder;
import lombok.Value;

/**
 * Input DTO (Command) for the ProcessUnstructuredOrderUseCase.
 * @Value = immutable; @Builder = fluent construction from the REST adapter.
 */
@Value
@Builder
public class ProcessOrderCommand {

    String rawText;
    String clientId;
    String clientName;
    String clientEmail;

    public ProcessOrderCommand(String rawText, String clientId, String clientName, String clientEmail) {
        if (rawText == null || rawText.isBlank())
            throw new IllegalArgumentException("rawText must not be blank");
        if (clientId == null || clientId.isBlank())
            throw new IllegalArgumentException("clientId must not be blank");
        this.rawText = rawText;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientEmail = clientEmail;
    }
}
