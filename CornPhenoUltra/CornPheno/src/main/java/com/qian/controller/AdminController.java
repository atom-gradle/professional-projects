package com.qian.controller;

import com.qian.common.Result;
import com.qian.dto.request.ExamineRequest;
import com.qian.dto.response.CaptureRecordStatDTO;
import com.qian.pojo.CaptureRecord;
import com.qian.pojo.MediaFile;
import com.qian.pojo.User;
import com.qian.service.AdminService;
import com.qian.service.CaptureRecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "管理端相关接口")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;
    private final CaptureRecordService captureRecordService;

    public AdminController(AdminService adminService, CaptureRecordService captureRecordService) {
        this.adminService = adminService;
        this.captureRecordService = captureRecordService;
    }

    @GetMapping("/statistics")
    public Result<List<CaptureRecordStatDTO>> getStatistics(
            @RequestParam(required = false) List<String> blockIds,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        // 设置默认时间范围
        if (startTime == null && endTime == null) {
            // 两个都为空，默认查询最近一年
            startTime = LocalDateTime.now().minusYears(1);
            endTime = LocalDateTime.now();
        } else if (startTime == null) {
            // 只有结束时间，默认从一年前开始
            startTime = endTime.minusYears(1);
        } else if (endTime == null) {
            // 只有开始时间，默认到当前时间
            endTime = LocalDateTime.now();
        }

        List<CaptureRecordStatDTO> data = captureRecordService.getStatistics(blockIds, startTime, endTime);
        return Result.success(data);
    }

    @GetMapping("/user/list")
    public Result<List<User>> listAllUsers() {
        return Result.success(adminService.listAllUsers());
    }

    @GetMapping("/user/all-todo")
    public Result<List<User>> listAllUsersTodo() {
        return Result.success(adminService.listAllUsersTodo());
    }

    @PostMapping("/user/examine")
    public Result<?> examineUser(ExamineRequest request) {
        adminService.examineUser(request);
        return Result.success();
    }

    @GetMapping("/capture/all-records")
    public Result<List<CaptureRecord>> listAllRecords() {
        return Result.success(adminService.listAllRecords());
    }

    @GetMapping("/file/list")
    public Result<List<MediaFile>> listAllFiles() {
        return Result.success(adminService.listAllFiles());
    }
}
