package com.qian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qian.dto.response.CaptureRecordStatDTO;
import com.qian.pojo.CaptureRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 采集记录 Mapper 接口
 */
@Mapper
public interface CaptureRecordMapper extends BaseMapper<CaptureRecord> {
    // 继承 BaseMapper 后，已自动拥有 insert, update, delete, selectById, selectList 等通用方法
    // 如需自定义 SQL，可在此添加方法 + XML 或使用 MP 条件构造器
    /**
     * 查询地块采集记录统计信息
     *
     * @param blockIds   地块ID列表
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @return 统计列表
     */
    List<CaptureRecordStatDTO> getCaptureStatistics(
            @Param("blockIds") List<String> blockIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
