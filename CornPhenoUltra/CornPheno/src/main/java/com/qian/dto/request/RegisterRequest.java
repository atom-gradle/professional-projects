package com.qian.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String phone;
    private String enterprise;
    private String staffId;
}
