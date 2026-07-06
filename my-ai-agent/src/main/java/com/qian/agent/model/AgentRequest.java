package com.qian.agent.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentRequest {

    @NotBlank(message = "message must not be blank")
    private String message;
}
