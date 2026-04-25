package com.qian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qian.common.Result;
import com.qian.dto.response.AnalysisProgressResponse;
import com.qian.dto.response.AnalysisReportDetailDTO;
import com.qian.dto.response.AnalysisResultResponse;
import com.qian.pojo.AnalysisReport;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AnalysisService extends IService<AnalysisReport> {
    CompletableFuture<Object> submitAnalysis(String captureId);
    AnalysisProgressResponse getAnalysisProgress(String captureId);
    AnalysisResultResponse getAnalysisResult(String captureId);
    List<AnalysisReportDetailDTO> listAllReports();
    void completeTask(String captureId, Object result);
    void completeTaskExceptionally(String captureId, String errorMessage);
}
