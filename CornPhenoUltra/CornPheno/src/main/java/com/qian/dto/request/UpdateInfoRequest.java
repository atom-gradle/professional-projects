package com.qian.dto.request;

import lombok.Data;

@Data
public class UpdateInfoRequest {
    private String phone;
    private String username;
    private String enterprise;
    private String staffId;
}
