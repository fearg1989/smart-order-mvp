package tech.fearg.smartorder.application.port.out;

import tech.fearg.smartorder.domain.model.Client;
import tech.fearg.smartorder.domain.model.Order;

/**
 * Outbound Port (Secondary Port / Driven Port) — AI side.
 *
 * The application core declares WHAT it needs from an AI parser.
 * The concrete implementation (Groq adapter) lives in the infrastructure layer
 * and plugs into this interface — dependency points inward.
 */
public interface AiOrderParserPort {

    /**
     * Sends unstructured natural-language text to an AI model and returns
     * a fully structured domain Order.
     *
     * @param rawText  free-form order text (e.g., WhatsApp message, email body)
     * @param client   the B2B client initiating the order
     * @return a structured, in-memory domain Order (not yet persisted)
     */
    Order parse(String rawText, Client client);
}
