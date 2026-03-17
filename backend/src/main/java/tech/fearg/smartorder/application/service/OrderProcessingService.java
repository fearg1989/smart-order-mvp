package tech.fearg.smartorder.application.service;

import tech.fearg.smartorder.application.port.in.ProcessOrderCommand;
import tech.fearg.smartorder.application.port.in.ProcessUnstructuredOrderUseCase;
import tech.fearg.smartorder.application.port.out.AiOrderParserPort;
import tech.fearg.smartorder.application.port.out.SaveOrderPort;
import tech.fearg.smartorder.domain.model.Client;
import tech.fearg.smartorder.domain.model.Order;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Application Service — orchestrates the use case flow.
 *
 * This class is the only implementation of the inbound port. It:
 *   1. Builds a Client value object from the incoming command.
 *   2. Delegates AI parsing to the AiOrderParserPort (outbound).
 *   3. Delegates persistence to the SaveOrderPort (outbound).
 *
 * No business rules live here; it is a pure orchestrator.
 * No Spring annotations intentionally — the core is framework-agnostic.
 * Spring wiring is handled in the infrastructure configuration layer (Phase 2).
 */
public class OrderProcessingService implements ProcessUnstructuredOrderUseCase {

    private static final List<Pattern> BILLING_NAME_PATTERNS = List.of(
            Pattern.compile("(?im)^\\s*(?:bill\\s*to|billing\\s*to|invoice\\s*to)\\s*[:\\-]?\\s*(.+?)\\s*$"),
            Pattern.compile("(?im)^\\s*(?:facturar\\s*a(?:\\s+nombre\\s+de)?|factura\\s*a(?:\\s+nombre\\s+de)?|a\\s+nombre\\s+de)\\s*[:\\-]?\\s*(.+?)\\s*$")
    );

    private final AiOrderParserPort aiOrderParserPort;
    private final SaveOrderPort saveOrderPort;

    public OrderProcessingService(AiOrderParserPort aiOrderParserPort, SaveOrderPort saveOrderPort) {
        this.aiOrderParserPort = aiOrderParserPort;
        this.saveOrderPort = saveOrderPort;
    }

    @Override
    public Order process(ProcessOrderCommand command) {
        String resolvedClientName = resolveClientName(command);

        Client client = new Client(
                command.getClientId(),
            resolvedClientName,
                command.getClientEmail()
        );

        Order parsedOrder = aiOrderParserPort.parse(command.getRawText(), client);

        return saveOrderPort.save(parsedOrder);
    }

    private String resolveClientName(ProcessOrderCommand command) {
        String extractedFromText = extractClientNameFromText(command.getRawText());
        if (extractedFromText != null && !extractedFromText.isBlank()) {
            return extractedFromText;
        }

        if (command.getClientName() != null && !command.getClientName().isBlank()) {
            return command.getClientName().trim();
        }

        return command.getClientId();
    }

    private String extractClientNameFromText(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return null;
        }

        for (Pattern pattern : BILLING_NAME_PATTERNS) {
            Matcher matcher = pattern.matcher(rawText);
            if (matcher.find()) {
                return sanitizeClientName(matcher.group(1));
            }
        }

        return null;
    }

    private String sanitizeClientName(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().replaceAll("[\\s.;,]+$", "");
        if (normalized.isEmpty()) {
            return null;
        }

        return normalized.length() > 100 ? normalized.substring(0, 100).trim() : normalized;
    }
}
