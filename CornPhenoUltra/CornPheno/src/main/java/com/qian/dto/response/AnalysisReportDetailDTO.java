package com.qian.dto.response;

import lombok.Data;

@Data
public class AnalysisReportDetailDTO {
    private String captureRecordId;

    private String reportName;

    private String reportUrl;

    private Integer status;

    private Integer progress;

    private String errorMessage;

    private Integer timeCost;
}
