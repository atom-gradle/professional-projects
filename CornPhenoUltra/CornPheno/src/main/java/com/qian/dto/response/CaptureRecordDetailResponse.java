package com.qian.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CaptureRecordDetailResponse {
    private String captureId;
    private LocalDateTime captureTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationName;
    private String sampleType;
    private String blockId;
    private String varietyName;
    private String mediaType;
    private String remark;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}