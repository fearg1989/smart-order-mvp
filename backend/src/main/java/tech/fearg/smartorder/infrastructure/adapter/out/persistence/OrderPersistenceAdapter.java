package tech.fearg.smartorder.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.fearg.smartorder.application.port.out.SaveOrderPort;
import tech.fearg.smartorder.domain.model.Order;

/**
 * Outbound Adapter — JPA Persistence.
 *
 * Implements the SaveOrderPort. The domain never sees Spring Data;
 * the OrderMapper handles the translation between worlds.
 */
@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements SaveOrderPort {

    private final OrderJpaRepository repository;
    private final OrderMapper mapper;

    @Override
    @Transactional
    public Order save(Order order) {
        OrderJpaEntity entity = mapper.toJpaEntity(order);
        OrderJpaEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }
}
