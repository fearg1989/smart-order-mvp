package tech.fearg.smartorder.application.service;

import org.junit.jupiter.api.Test;
import tech.fearg.smartorder.application.port.in.ProcessOrderCommand;
import tech.fearg.smartorder.application.port.out.AiOrderParserPort;
import tech.fearg.smartorder.application.port.out.SaveOrderPort;
import tech.fearg.smartorder.domain.model.Client;
import tech.fearg.smartorder.domain.model.Order;
import tech.fearg.smartorder.domain.model.OrderItem;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderProcessingServiceTest {

    @Test
    void shouldUseBillingNameWhenTextContainsBillTo() {
        OrderProcessingService service = new OrderProcessingService(new FakeParserPort(), new FakeSavePort());

        ProcessOrderCommand command = ProcessOrderCommand.builder()
                .rawText("Dear sales team\nBill to: UK Fashion Retailers Ltd.\nPlease confirm ETA")
                .clientId("client-001")
                .clientName("Acme Corp")
                .clientEmail("orders@acme.com")
                .build();

        Order order = service.process(command);

        assertEquals("UK Fashion Retailers Ltd", order.getClient().getName());
    }

    @Test
    void shouldUseSpanishBillingNameWhenTextContainsFacturarANombreDe() {
        OrderProcessingService service = new OrderProcessingService(new FakeParserPort(), new FakeSavePort());

        ProcessOrderCommand command = ProcessOrderCommand.builder()
                .rawText("Necesitamos reposicion\nFacturar a nombre de Calzados Levante S.L.\nEntrega urgente")
                .clientId("client-001")
                .clientName("Acme Corp")
                .clientEmail("orders@acme.com")
                .build();

        Order order = service.process(command);

        assertEquals("Calzados Levante S.L", order.getClient().getName());
    }

    @Test
    void shouldUseSpanishBillingNameWhenMarkerAppearsMidLine() {
        OrderProcessingService service = new OrderProcessingService(new FakeParserPort(), new FakeSavePort());

        ProcessOrderCommand command = ProcessOrderCommand.builder()
                .rawText("Envíalo por SEUR como siempre. Facturar a Calzados Levante S.L.")
                .clientId("client-001")
                .clientName("Web Client")
                .clientEmail("noreply@smart-order.local")
                .build();

        Order order = service.process(command);

        assertEquals("Calzados Levante S.L", order.getClient().getName());
    }

    @Test
    void shouldKeepProvidedClientNameWhenNoBillingMarkerExists() {
        OrderProcessingService service = new OrderProcessingService(new FakeParserPort(), new FakeSavePort());

        ProcessOrderCommand command = ProcessOrderCommand.builder()
                .rawText("Necesito 10 cajas y 2 grapadoras")
                .clientId("client-001")
                .clientName("Acme Corp")
                .clientEmail("orders@acme.com")
                .build();

        Order order = service.process(command);

        assertEquals("Acme Corp", order.getClient().getName());
    }

    private static class FakeParserPort implements AiOrderParserPort {
        @Override
        public Order parse(String rawText, Client client) {
            return Order.newPending(
                    client,
                    List.of(new OrderItem("test-item", 1, BigDecimal.ONE)),
                    rawText
            );
        }
    }

    private static class FakeSavePort implements SaveOrderPort {
        @Override
        public Order save(Order order) {
            return order;
        }
    }
}
