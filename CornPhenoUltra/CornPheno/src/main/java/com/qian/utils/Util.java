package com.qian.utils;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class Util {

    // 生成 capture_id: 日期_时间_UUID
    public static String generateCaptureId() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

}

/**
 * -- 查询某个地块在时间段内的所有采集记录，按品种统计
 SELECT
 cr.block_id,
 cr.variety_name,
 cr.sample_type,
 COUNT(*) as sample_count,
 MIN(cr.create_time) as first_collect_time,
 MAX(cr.update_time) as last_update_time
 FROM capture_record cr
 LEFT JOIN media_file mf ON mf.capture_id = cr.id
 WHERE cr.status between 1 and 3  -- 已完成分析的
 AND cr.block_id IN ('BLOCK_D01', 'BLOCK_B01', 'BLOCK_E01')
 AND cr.create_time BETWEEN '2026-01-01 00:00:00' AND '2026-12-31 00:00:00'
 GROUP BY cr.block_id, cr.variety_name, cr.sample_type
 ORDER BY cr.block_id, sample_count DESC;
 */
// 约1.15s -> 稳定0.37s
//  KEY `idx_cover_group` (`block_id`,`create_time`,`status`,`variety_name`,`sample_type`,`update_time`)
