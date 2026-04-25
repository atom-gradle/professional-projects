package com.qian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qian.config.RabbitMQConfig;
import com.qian.dto.AnalysisTaskRequest;
import com.qian.dto.response.AnalysisProgressResponse;
import com.qian.dto.response.AnalysisReportDetailDTO;
import com.qian.dto.response.AnalysisResultResponse;
import com.qian.exception.BusinessException;
import com.qian.mapper.CaptureRecordMapper;
import com.qian.mapper.MediaFileMapper;
import com.qian.pojo.AnalysisReport;
import com.qian.mapper.AnalysisReportMapper;
import com.qian.pojo.CaptureRecord;
import com.qian.pojo.MediaFile;
import com.qian.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class AnalysisServiceImpl extends ServiceImpl<AnalysisReportMapper, AnalysisReport>
        implements AnalysisService {

    private final CaptureRecordMapper captureRecordMapper;
    private final AnalysisReportMapper analysisReportMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ConcurrentHashMap<String, CompletableFuture<Object>> pendingTasks;
    private final MediaFileMapper mediaFileMapper;

    public AnalysisServiceImpl(CaptureRecordMapper captureRecordMapper, AnalysisReportMapper analysisReportMapper, RabbitTemplate rabbitTemplate, MediaFileMapper mediaFileMapper) {
        this.captureRecordMapper = captureRecordMapper;
        this.analysisReportMapper = analysisReportMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.pendingTasks = new ConcurrentHashMap<>();
        this.mediaFileMapper = mediaFileMapper;
    }

    /**
     * 发送异步分析任务
     */
    @Override
    @Async
    public CompletableFuture<Object> submitAnalysis(String captureId) {

        // 检查该采集记录是否已上传
        CaptureRecord captureRecord = captureRecordMapper.selectOne(new LambdaQueryWrapper<CaptureRecord>()
                .eq(CaptureRecord::getCaptureId, captureId));

        if(captureRecord != null) {
            if(captureRecord.getStatus() != CaptureRecord.STATUS.UPLOADED.getValue()) {
                throw new BusinessException("该采集记录未上传或已提交分析");
            }
        }

        // 查询采集记录对应的媒体文件url
        List<MediaFile> mediaFiles = mediaFileMapper.selectList(
                new LambdaQueryWrapper<MediaFile>()
                        .eq(MediaFile::getCaptureId, captureId));

        List<String> mediaFiles_ = List.of("https://tukuimg.bdstatic.com/cms/ff3c2cd7bca2ae399855b883dfe5cbcf.jpeg");
        AnalysisTaskRequest task = new AnalysisTaskRequest(captureId, mediaFiles_);

        CompletableFuture<Object> future = new CompletableFuture<>();
        pendingTasks.put(captureId, future);

        AnalysisReport report = new AnalysisReport();
        report.setCaptureId(10);
        report.setStatus(0);
        report.setCreateTime(LocalDateTime.now());
        report.setStartTime(LocalDateTime.now());
        this.save(report);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DIRECT_EXCHANGE,
                RabbitMQConfig.TASK_ROUTING_KEY,
                task
        );
        log.info("已发送任务到队列: captureId={}", captureId);

        // 设置30秒超时
        CompletableFuture.delayedExecutor(30, TimeUnit.SECONDS)
                .execute(() -> {
                    if (!future.isDone()) {
                        future.completeExceptionally(new RuntimeException("任务执行超时"));
                        pendingTasks.remove(captureId);
                    }
                });

        return future;
    }

    @Override
    public void completeTask(String taskId, Object result) {
        CompletableFuture<Object> future = pendingTasks.remove(taskId);
        if (future != null) {
            future.complete(result);
        }
    }

    @Override
    public void completeTaskExceptionally(String taskId, String errorMessage) {
        CompletableFuture<Object> future = pendingTasks.remove(taskId);
        if (future != null) {
            future.completeExceptionally(new RuntimeException(errorMessage));
        }
    }

    @Override
    public AnalysisProgressResponse getAnalysisProgress(String captureId) {
        AnalysisReport report = analysisReportMapper.selectOne(new LambdaQueryWrapper<AnalysisReport>()
                .eq(AnalysisReport::getCaptureId, captureId));

        if(report == null) {
            throw new BusinessException("没有找到指定的分析报告");
        }

        var response = new AnalysisProgressResponse();
        BeanUtils.copyProperties(report, response);

        return response;
    }

    @Override
    public AnalysisResultResponse getAnalysisResult(String captureId) {
        AnalysisReport analysisReport = this.getOne(new LambdaQueryWrapper<AnalysisReport>()
                .eq(AnalysisReport::getCaptureId, captureId));
        AnalysisResultResponse response = new AnalysisResultResponse();
        BeanUtils.copyProperties(analysisReport, response);
        return response;
    }

    @Override
    public List<AnalysisReportDetailDTO> listAllReports() {
        var analysisReports = this.list();
        List<AnalysisReportDetailDTO> analysisReportDetails = new ArrayList<>(analysisReports.size());

        for(var analysisReport : analysisReports) {
            AnalysisReportDetailDTO detail = new AnalysisReportDetailDTO();
            BeanUtils.copyProperties(analysisReport, detail);
            analysisReportDetails.add(detail);
        }

        return analysisReportDetails;
    }
}