package com.qian.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CaptureRecordCreateDTO {
    @NotNull(message = "采集时间不能为空")
    private LocalDateTime captureTime;

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String locationName;

    @NotNull(message = "样本类型不能为空")
    private String sampleType; // 马齿型/硬粒型/半硬粒型/其他

    @NotNull(message = "区块编号不能为空")
    private String blockId;

    private String varietyName;

    @NotNull(message = "采集方式不能为空")
    private String mediaType; // single_image / image_sequence / video

    private String remark;
}