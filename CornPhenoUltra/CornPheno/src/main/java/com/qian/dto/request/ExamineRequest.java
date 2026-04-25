package com.qian.dto.request;

import lombok.Data;

@Data
public class ExamineRequest {
    private Integer userId;
    private Integer auditStatus;
    private String rejectionRemark;
}
