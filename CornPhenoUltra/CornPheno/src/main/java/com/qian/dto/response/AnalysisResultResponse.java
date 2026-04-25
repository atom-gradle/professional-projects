package com.qian.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AnalysisResultResponse {

    private String captureRecordId;

    private String reportName;

    private Integer status;

    private Integer progress;

    private Integer kernelCount;

    private BigDecimal kernelWeight;

    private BigDecimal earLength;

    private BigDecimal earDiameter;

    private Integer rowCount;

    private Integer kernelPerRow;

    private String errorMessage;

    private Integer timeCost;

}
