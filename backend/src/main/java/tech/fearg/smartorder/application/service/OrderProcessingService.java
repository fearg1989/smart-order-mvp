package tech.fearg.smartorder.application.service;

import tech.fearg.smartorder.application.port.in.ProcessOrderCommand;
import tech.fearg.smartorder.application.port.in.ProcessUnstructuredOrderUseCase;
import tech.fearg.smartorder.application.port.out.AiOrderParserPort;
import tech.fearg.smartorder.application.port.out.SaveOrderPort;
import tech.fearg.smartorder.domain.model.Client;
import tech.fearg.smartorder.domain.model.Order;

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

    private final AiOrderParserPort aiOrderParserPort;
    private final SaveOrderPort saveOrderPort;

    public OrderProcessingService(AiOrderParserPort aiOrderParserPort, SaveOrderPort saveOrderPort) {
        this.aiOrderParserPort = aiOrderParserPort;
        this.saveOrderPort = saveOrderPort;
    }

    @Override
    public Order process(ProcessOrderCommand command) {
        Client client = new Client(
                command.getClientId(),
                command.getClientName(),
                command.getClientEmail()
        );

        Order parsedOrder = aiOrderParserPort.parse(command.getRawText(), client);

        return saveOrderPort.save(parsedOrder);
    }
}
