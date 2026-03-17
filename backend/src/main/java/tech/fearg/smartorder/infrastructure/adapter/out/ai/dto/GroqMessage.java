package tech.fearg.smartorder.infrastructure.adapter.out.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroqMessage {
    private String role;
    private String content;
}
