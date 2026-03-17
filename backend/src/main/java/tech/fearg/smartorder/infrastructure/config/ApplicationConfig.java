package tech.fearg.smartorder.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import tech.fearg.smartorder.application.port.in.ProcessUnstructuredOrderUseCase;
import tech.fearg.smartorder.application.port.out.AiOrderParserPort;
import tech.fearg.smartorder.application.port.out.SaveOrderPort;
import tech.fearg.smartorder.application.service.OrderProcessingService;

/**
 * Spring Configuration — wires the framework-agnostic application core
 * to its adapters via constructor injection.
 *
 * This is the ONLY place where Spring "knows" about the concrete service.
 * All other classes depend on the interface (port), not the implementation.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Exposes OrderProcessingService as the use-case bean.
     * The controller only sees ProcessUnstructuredOrderUseCase (the port).
     */
    @Bean
    public ProcessUnstructuredOrderUseCase processUnstructuredOrderUseCase(
            AiOrderParserPort aiOrderParserPort,
            SaveOrderPort saveOrderPort) {
        return new OrderProcessingService(aiOrderParserPort, saveOrderPort);
    }

    /**
     * Shared RestClient bean, configured once and reused by all HTTP adapters.
     */
    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    /**
     * ObjectMapper with JavaTimeModule so LocalDateTime serializes as ISO-8601,
     * not as a numeric timestamp array.
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
