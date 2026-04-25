package com.qian.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisProgressResponse {

    private String captureRecordId;

    private Integer status;

    private Integer progress;

    private String reportUrl;

    private String errorMessage;
}
