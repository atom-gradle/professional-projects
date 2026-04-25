package com.qian.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 采集记录统计 DTO
 */
@Data
public class CaptureRecordStatDTO {
    private String blockId;           // 地块ID
    private String varietyName;       // 品种名称
    private String sampleType;        // 样本类型
    private Long sampleCount;         // 样本数量
    private LocalDateTime firstCollectTime; // 最早采集时间
    private LocalDateTime lastUpdateTime;   // 最晚更新时间
}