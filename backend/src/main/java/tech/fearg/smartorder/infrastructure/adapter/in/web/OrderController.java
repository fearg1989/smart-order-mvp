package tech.fearg.smartorder.infrastructure.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;
import tech.fearg.smartorder.application.port.in.ProcessOrderCommand;
import tech.fearg.smartorder.application.port.in.ProcessUnstructuredOrderUseCase;
import tech.fearg.smartorder.domain.model.Order;
import tech.fearg.smartorder.infrastructure.adapter.in.web.dto.OrderRequest;
import tech.fearg.smartorder.infrastructure.adapter.in.web.dto.OrderResponse;

/**
 * Inbound Adapter (REST).
 *
 * Depends on the Inbound Port interface only — never on the concrete service.
 * @CrossOrigin required by Phase 3 (Astro frontend on a different origin).
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final ProcessUnstructuredOrderUseCase processUnstructuredOrderUseCase;

    @PostMapping("/ai-ingest")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse ingestOrder(@Valid @RequestBody OrderRequest request) {
        ProcessOrderCommand command = ProcessOrderCommand.builder()
                .rawText(request.rawText())
                .clientId(request.clientId())
                .clientName(request.clientName())
                .clientEmail(request.clientEmail())
                .build();

        Order order = processUnstructuredOrderUseCase.process(command);
        return OrderResponse.fromDomain(order);
    }
}
