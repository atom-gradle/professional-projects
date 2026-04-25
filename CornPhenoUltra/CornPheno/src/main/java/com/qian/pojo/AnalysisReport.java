package com.qian.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分析报告实体类
 */
@Data
@TableName("analysis_report")
public class AnalysisReport {

    /**
     * 报告ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联采集记录ID（唯一，外键）
     */
    @TableField("capture_id")
    private Integer captureId;

    /**
     * 报告名称
     */
    @TableField("report_name")
    private String reportName;

    /**
     * 报告文件路径（本地存储路径）
     * 保留！
     */
    @TableField("report_path")
    private String reportPath;

    /**
     * 报告下载URL（OSS地址）
     */
    @TableField("report_url")
    private String reportUrl;

    /**
     * 分析状态：
     * 0 - 待分析
     * 1 - 分析中
     * 2 - 成功
     * 3 - 失败
     */
    @TableField("status")
    private Integer status;

    /**
     * 分析进度 0-100
     */
    @TableField("progress")
    private Integer progress;

    /**
     * 分析结果JSON（MyBatis-Plus 默认支持 JSON 字段映射为 String 或 JSONObject）
     * 建议使用 String 类型配合 Jackson/Gson 序列化处理
     */
    @TableField("analysis_result")
    private String analysisResult; // 可替换为 com.fasterxml.jackson.databind.JsonNode 等，但需自定义 TypeHandler

    /**
     * 籽粒数量
     */
    @TableField("kernel_count")
    private Integer kernelCount;

    /**
     * 百粒重(g)，保留两位小数
     */
    @TableField("kernel_weight")
    private BigDecimal kernelWeight;

    /**
     * 穗长(cm)
     */
    @TableField("ear_length")
    private BigDecimal earLength;

    /**
     * 穗粗(cm)
     */
    @TableField("ear_diameter")
    private BigDecimal earDiameter;

    /**
     * 穗行数
     */
    @TableField("row_count")
    private Integer rowCount;

    /**
     * 行粒数
     */
    @TableField("kernel_per_row")
    private Integer kernelPerRow;

    /**
     * 失败错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 分析开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 分析完成时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

/*
CREATE TABLE `analysis_report` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
  `capture_id` bigint NOT NULL UNIQUE COMMENT '关联capture_record.id',
  `report_name` VARCHAR(256) COMMENT '报告名称',
  `report_url` VARCHAR(512) DEFAULT NULL COMMENT '报告文件下载URL',
  `status` TINYINT DEFAULT 0 COMMENT '分析状态：0待分析 1分析中 2成功 3失败',
  `progress` INTEGER DEFAULT 0 COMMENT '分析进度：0-100',
  `analysis_result` JSON DEFAULT NULL COMMENT '分析结果JSON（扩展字段）',
  `kernel_count` INT DEFAULT NULL COMMENT '籽粒数量',
  `kernel_weight` DECIMAL(10,2) DEFAULT NULL COMMENT '百粒重(g)',
  `ear_length` DECIMAL(10,2) DEFAULT NULL COMMENT '穗长(cm)',
  `ear_diameter` DECIMAL(10,2) DEFAULT NULL COMMENT '穗粗(cm)',
  `row_count` INT DEFAULT NULL COMMENT '穗行数',
  `kernel_per_row` INT DEFAULT NULL COMMENT '行粒数',
  `error_message` TEXT DEFAULT NULL COMMENT '失败错误信息',
  `start_time` DATETIME DEFAULT NULL COMMENT '分析开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '分析完成时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_capture_record_id` (`capture_record_id`),
  INDEX `idx_analysis_status` (`status`),
  FOREIGN KEY (`capture_record_id`) REFERENCES `capture_record`(`capture_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分析报告表';
 */