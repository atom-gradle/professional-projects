package com.qian.controller;

import com.qian.dto.response.AnalysisReportDetailDTO;
import com.qian.dto.response.AnalysisResultResponse;
import com.qian.common.Result;
import com.qian.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

/**
 * @version 1.0.0
 * @author Qian
 * @since 2.0.0
 */

@Slf4j
@Tag(name = "分析报告相关接口")
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisReportService;

    @Operation(
            summary = "提交分析请求",
            description = "为指定的captureId提交采集记录进行AI分析"
    )
    @GetMapping("/{captureId}/submit")
    public Result<?> submitAnalysis(@PathVariable String captureId) {
        log.info("收到分析请求: captureId={}", captureId);

        // 35s超时
        DeferredResult<Object> deferredResult = new DeferredResult<>(35000L);

        var future = analysisReportService.submitAnalysis(captureId);

        future.whenComplete((result, throwable) -> {
            if(throwable != null) {
                log.error("分析任务失败");
                deferredResult.setErrorResult(throwable.getMessage());
            } else {
                log.info("分析任务成功");
                deferredResult.setResult(result);
            }
        });

        return Result.success(deferredResult);
    }

    @Operation(
            summary = "获取分析结果",
            description = "获取指定captureId的分析报告"
    )
    @GetMapping("/{captureId}/result")
    public Result<AnalysisResultResponse> getAnalysisResult(@PathVariable String captureId) {
        AnalysisResultResponse result = analysisReportService.getAnalysisResult(captureId);
        return Result.success(result);
    }

    @Operation(
            summary = "分析报告列表",
            description = "获取所有分析报告的信息列表"
    )
    @GetMapping("/list")
    public Result<List<AnalysisReportDetailDTO>> listAllReports() {
        var results = analysisReportService.listAllReports();
        return Result.success(results);
    }

}
