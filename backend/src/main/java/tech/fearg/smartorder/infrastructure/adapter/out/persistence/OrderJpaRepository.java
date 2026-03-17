package tech.fearg.smartorder.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA Repository.
 * Lives entirely in the infrastructure layer — the domain never sees this.
 */
public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, UUID> {
}
