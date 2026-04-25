package com.qian.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UserInfoDTO {
    private Long userId;
    private Integer auditStatus;
    private String phone;
    private String username;
    private String enterprise;
    private String staffId;
    private String rejectionRemark;
}
