package com.qian.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @version 1.0.0
 * @author Qian
 * @since 2.0.0
 * 采集记录表实体类
 */
@Data
@TableName("capture_record")
public class CaptureRecord {

    public enum STATUS {
        LOCAL(0),UPLOADED(1),ANALYZING(2),ANALYZED(3),FAILED(4);
        private int status;
        STATUS(int status) {
            this.status = status;
        }
        public int getValue() {
            return status;
        }
    }

    /**
     * 记录ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 采集编号(日期_时间_UUID)，唯一
     */
    @TableField("capture_id")
    private String captureId;

    /**
     * 采集人ID（外键）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 纬度（DECIMAL(10,7)）
     */
    @TableField("latitude")
    private BigDecimal latitude;

    /**
     * 经度（DECIMAL(10,7)）
     */
    @TableField("longitude")
    private BigDecimal longitude;

    /**
     * 位置名称
     */
    @TableField("location_name")
    private String locationName;

    /**
     * 样本类型：马齿型/硬粒型/半硬粒型/其他
     */
    @TableField("sample_type")
    private String sampleType;

    /**
     * 区块/小区编号
     */
    @TableField("block_id")
    private String blockId;

    /**
     * 品种名称（扩展字段）
     */
    @TableField("variety_name")
    private String varietyName;

    /**
     * 采集方式：single_image / image_sequence / video
     */
    @TableField("media_type")
    private String mediaType;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 状态：0本地 1已上传待分析 2分析中 3分析完成 4分析失败
     */
    @TableField("status")
    private Integer status;

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
CREATE TABLE `capture_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  `capture_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '采集编号(日期_时间_UUID)',
  `user_id` BIGINT NOT NULL COMMENT '采集人ID',
  `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
  `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
  `location_name` VARCHAR(256) DEFAULT NULL COMMENT '位置名称',
  `sample_type` VARCHAR(32) NOT NULL COMMENT '样本类型(马齿型/硬粒型/半硬粒型/爆裂型/糯质型/其他)',
  `block_id` VARCHAR(32) NOT NULL COMMENT '区块/小区编号',
  `variety_name` VARCHAR(64) DEFAULT NULL COMMENT '品种名称',
  `media_type` VARCHAR(20) NOT NULL COMMENT '采集方式(single_image/image_sequence/video)',
  `remark` TEXT DEFAULT NULL COMMENT '备注',
  `status` TINYINT DEFAULT 0 COMMENT '状态：0本地 1已上传 2分析中 3已完成 4失败',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_capture_time` (`capture_time`),
  INDEX `idx_status` (`status`),
  INDEX `idx_capture_id` (`capture_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采集记录表';
 */