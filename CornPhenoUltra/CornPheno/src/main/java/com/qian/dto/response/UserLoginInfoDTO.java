package com.qian.dto.response;

import lombok.Data;

@Data
public class UserLoginInfoDTO {

    private Long userId;

    private Integer auditStatus;

    private String token;

}
