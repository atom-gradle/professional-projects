package com.qian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qian.pojo.MediaFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 媒体文件 Mapper 接口
 */
@Mapper
public interface MediaFileMapper extends BaseMapper<MediaFile> {

    /**
     * 根据多个采集记录ID批量查询媒体文件（用于优化 N+1 查询）
     */
    List<MediaFile> selectByCaptureRecordIds(java.util.List<Long> captureRecordIds);
}