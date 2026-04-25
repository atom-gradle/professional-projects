package com.qian.controller;

import com.qian.common.Result;
import com.qian.dto.response.MediaFileDetailDTO;
import com.qian.service.MediaFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @version 3.0.0
 * @author Qian
 * @since 2.0.0
 */

@Slf4j
@Tag(name = "媒体文件相关接口")
@RestController
@RequestMapping("/api/v1/file")
public class MediaFileController {

    private final MediaFileService mediaFileService;

    public MediaFileController(MediaFileService mediaFileService) {
        this.mediaFileService = mediaFileService;
    }

    @Operation(
            summary = "上传媒体文件",
            description = "为指定的采集记录上传媒体文件"
    )
    @PostMapping("/upload/{captureId}")
    public Result<String> uploadFile(@PathVariable String captureId,
                                     @RequestParam("file") MultipartFile file,
                                     @RequestParam(required = false) Integer sequenceNum) {

        String fileUrl = mediaFileService.uploadMediaFile(captureId, file, sequenceNum);
        return Result.success(fileUrl);
    }

    @Operation(
            summary = "删除媒体文件",
            description = "为指定的采集记录删除单个媒体文件"
    )
    @DeleteMapping("/{fileName}")
    public Result<?> deleteFile(@PathVariable String fileName) {

        mediaFileService.deleteMediaFile(fileName);
        return Result.success();
    }

    @Operation(
            summary = "获取媒体文件信息列表",
            description = "获取指定的采集记录中，所有已上传的媒体文件的信息列表"
    )
    @GetMapping("/{captureId}/detail")
    public Result<List<MediaFileDetailDTO>> getFileDetails(@PathVariable String captureId) {
        log.info("查询的captureId为:{}", captureId);

        return Result.success(mediaFileService.getMediaFileDetails(captureId));
    }

    @Operation(
            summary = "获取分析PDF报告",
            description = "获取指定的分析报告"
    )
    @GetMapping("/{captureId}/report")
    public Result<String> getAnalysisReport(@PathVariable String captureId) {
        log.info("captureId:{}", captureId);
        return Result.success(mediaFileService.getAnalysisReportUrl(captureId));
    }

    @PostMapping("/test-upload")
    public Result<String> uploadTestFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam String fileName) {

        String fileUrl = mediaFileService.uploadTestFile(file, fileName);
        return Result.success(fileUrl);
    }
}
