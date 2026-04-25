package com.qian.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qian.dto.request.CaptureRecordCreateDTO;
import com.qian.dto.response.CaptureRecordDetailResponse;
import com.qian.dto.response.CaptureRecordStatDTO;
import com.qian.pojo.CaptureRecord;

import java.time.LocalDateTime;
import java.util.List;

public interface CaptureRecordService {
    String createCapture(CaptureRecordCreateDTO dto);
    Page<CaptureRecord> listCaptures(Integer page, Integer size);
    CaptureRecordDetailResponse getCaptureDetail(String captureId);
    List<CaptureRecordDetailResponse> getAllUnanalyzedRecords();
    void deleteCapture(String captureId);
    //获取采集记录统计信息
    List<CaptureRecordStatDTO> getStatistics(List<String> blockIds, LocalDateTime startTime, LocalDateTime endTime);
}
