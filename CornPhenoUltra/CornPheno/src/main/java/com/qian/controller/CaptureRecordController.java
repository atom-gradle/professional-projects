package com.qian.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qian.dto.request.CaptureRecordCreateDTO;
import com.qian.dto.response.CaptureRecordDetailResponse;
import com.qian.common.Result;
import com.qian.pojo.CaptureRecord;
import com.qian.service.CaptureRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @version 3.0.0
 * @author Qian
 * @since 2.0.0
 */

@Tag(name = "采集记录相关接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/capture")
public class CaptureRecordController {

    private final CaptureRecordService captureRecordService;

    public CaptureRecordController(CaptureRecordService captureRecordService) {
        this.captureRecordService = captureRecordService;
    }

    @Operation(
            summary = "创建采集记录",
            description = "创建新的采集记录"
    )
    @PostMapping("/create")
    public Result<String> createCapture(@RequestBody @Valid CaptureRecordCreateDTO dto) {
        return Result.success(captureRecordService.createCapture(dto));
    }

    @Operation(
            summary = "获取记录列表",
            description = "分页查询指定用户的所有采集记录"
    )
    @GetMapping("/list")
    public Result<Page<CaptureRecord>> listCaptures(@RequestParam(name = "page") Integer page,
                                                    @RequestParam(name = "size") Integer size) {
        return Result.success(captureRecordService.listCaptures(page, size));
    }

    @Operation(
            summary = "获取记录详情",
            description = "获取单条记录详情"
    )
    @GetMapping("/{captureId}")
    public Result<CaptureRecordDetailResponse> getCaptureDetail(@PathVariable String captureId) {
        return Result.success(captureRecordService.getCaptureDetail(captureId));
    }

    @Operation(
            summary = "获取指定用户的所有待分析采集记录",
            description = "获取指定用户的所有待分析采集记录"
    )
    @GetMapping("/all-unanalyzed-records")
    public Result<List<CaptureRecordDetailResponse>> getAllUnanalyzedByUser() {
        return Result.success(captureRecordService.getAllUnanalyzedRecords());
    }

    @Operation(
            summary = "删除采集记录",
            description = "删除记录及关联文件"
    )
    @DeleteMapping("/{captureId}")
    public Result<?> deleteCapture(@PathVariable String captureId) {
        captureRecordService.deleteCapture(captureId);
        return Result.success();
    }
}