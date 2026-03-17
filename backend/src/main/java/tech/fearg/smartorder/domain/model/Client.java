package tech.fearg.smartorder.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Value Object representing a B2B Client.
 * @Value = immutable class (all fields final). @Builder gives a fluent construction API.
 */
@Value
@Builder
public class Client {

    String id;
    String name;
    String email;

    public Client(String id, String name, String email) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Client id must not be blank");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Client name must not be blank");
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
