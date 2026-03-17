package tech.fearg.smartorder.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Inbound REST DTO. A record is ideal here: pure data carrier,
 * immutable, no business logic.
 */
public record OrderRequest(

        @NotBlank(message = "rawText must not be blank")
        @Size(max = 4000, message = "rawText must not exceed 4000 characters")
        String rawText,

        @NotBlank(message = "clientId must not be blank")
        @Size(max = 100, message = "clientId must not exceed 100 characters")
        String clientId,

        @Size(max = 100, message = "clientName must not exceed 100 characters")
        String clientName,

        @Email(message = "clientEmail must be a valid email address")
        @Size(max = 200, message = "clientEmail must not exceed 200 characters")
        String clientEmail
) {}
