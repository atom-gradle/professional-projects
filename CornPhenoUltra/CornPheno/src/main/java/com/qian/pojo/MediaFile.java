package com.qian.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 媒体文件实体类
 */
@Data
@TableName("media_file")
public class MediaFile {

    /**
     * 文件ID（主键，自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联采集记录ID（外键，级联删除）
     */
    @TableField("capture_id")
    private String captureId;

    /**
     * 原始文件名
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 文件访问URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 文件类型：image / video
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * MIME 类型（如：image/jpeg, video/mp4）
     */
    @TableField("mime_type")
    private String mimeType;

    /**
     * 序列号（用于 image_sequence 场景，默认为1）
     */
    @TableField("sequence_num")
    private Integer sequenceNum;

    /**
     * 视频时长（秒），仅视频有效
     */
    @TableField("duration")
    private Integer duration;

    /**
     * 缩略图路径（主要用于视频或大图预览）
     */
    @TableField("thumbnail_path")
    private String thumbnailPath;

    /**
     * 创建时间（自动填充）
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

/*
CREATE TABLE `media_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `capture_id` bigint NOT NULL COMMENT '关联 capture_record.id',
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_url` varchar(255) DEFAULT NULL COMMENT '文件访问URL',
  `file_type` varchar(20) NOT NULL COMMENT '文件类型(image/video)',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `mime_type` varchar(64) DEFAULT NULL COMMENT 'MIME类型',
  `sequence_num` int DEFAULT '1' COMMENT '序列号(时序图使用)',
  `duration` int DEFAULT NULL COMMENT '视频时长(秒)',
  `thumbnail_path` varchar(255) DEFAULT NULL COMMENT '缩略图路径',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_capture_fk` (`capture_id`),
  CONSTRAINT `fk_media_file_capture_record` FOREIGN KEY (`capture_id`) REFERENCES `capture_record` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='媒体文件表'
 */
