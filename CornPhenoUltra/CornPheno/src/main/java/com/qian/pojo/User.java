package com.qian.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * @since 2.0.0
 * @author Qian
 */

@Data
@Accessors(chain = true)
@TableName("user")
@Schema(description = "用户实体")
public class User {

    @Schema(description = "用户ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "微信openId")
    @TableField("openid")
    private String openid;

    @Schema(description = "微信unionId")
    @TableField("unionid")
    private String unionid;

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "手机号")
    @TableField("phone")
    private String phone;

    @Schema(description = "职工号")
    @TableField("staffid")
    private String staffId;

    @Schema(description = "单位")
    @TableField("enterprise")
    private String enterprise;

    @Schema(description = "角色：0普通用户 1管理员")
    @TableField("role")
    private Byte role;

    @Schema(description = "状态：0未登录 1已登录")
    @TableField("login_status")
    private Byte loginStatus;

    @Schema(description = "状态：0未注册 1已注册待审核 2审核通过 3审核驳回")
    @TableField("audit_status")
    private Integer auditStatus;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}


/*
CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `openid` VARCHAR(64) NOT NULL UNIQUE COMMENT '微信openId',
  `unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信unionId',
  `username` VARCHAR(20) DEFAULT NULL COMMENT '用户名',
  `enterprise` VARCHAR(30) DEFAULT NULL COMMENT '单位',
  `staffid` VARCHAR(30) DEFAULT NULL COMMENT '学号/工号',
  `phone` VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
  `role` TINYINT DEFAULT 0 COMMENT '角色：0普通用户 1管理员',
  `login_status` TINYINT DEFAULT 0 COMMENT '状态：0未登录 1已登陆',
  `audit_status` INTEGER DEFAULT 0 COMMENT '状态：0未注册 1已注册待审核 2审核通过 3审核驳回',
  `rejection_remark` VARCHAR(256) DEFAULT NULL COMMENT '驳回原因',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_openid` (`openid`),
  INDEX `idx_phone` (`phone`),
  INDEX `idx_enterprise_staff` (`enterprise`, `staffid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
 */
