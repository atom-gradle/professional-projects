package com.qian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qian.dto.response.MediaFileDetailDTO;
import com.qian.exception.BusinessException;
import com.qian.mapper.AnalysisReportMapper;
import com.qian.mapper.CaptureRecordMapper;
import com.qian.mapper.MediaFileMapper;
import com.qian.pojo.AnalysisReport;
import com.qian.pojo.CaptureRecord;
import com.qian.pojo.MediaFile;
import com.qian.service.MediaFileService;
import com.qian.utils.CurrentHolder;
import com.qian.utils.FFmpegUtil;
import com.qian.utils.OSSUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class MediaFileServiceImpl extends ServiceImpl<MediaFileMapper, MediaFile> implements MediaFileService {

    private final OSSUtil ossUtil;
    private final FFmpegUtil fFmpegUtil;

    private final MediaFileMapper mediaFileMapper;
    private final CaptureRecordMapper captureRecordMapper;
    private final AnalysisReportMapper analysisReportMapper;

    public MediaFileServiceImpl(OSSUtil ossUtil, FFmpegUtil fFmpegUtil, MediaFileMapper mediaFileMapper, CaptureRecordMapper captureRecordMapper, AnalysisReportMapper analysisReportMapper) {
        this.ossUtil = ossUtil;
        this.fFmpegUtil = fFmpegUtil;
        this.mediaFileMapper = mediaFileMapper;
        this.captureRecordMapper = captureRecordMapper;
        this.analysisReportMapper = analysisReportMapper;
    }

    @Override
    @Transactional
    public String uploadMediaFile(String captureId, MultipartFile file, Integer sequenceNum) {

        CaptureRecord record = captureRecordMapper.selectOne(new LambdaQueryWrapper<CaptureRecord>().eq(CaptureRecord::getCaptureId, captureId));

        Long userId = CurrentHolder.getCurrentId();

        if (record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("采集记录为空或无权限");
        }

        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }

        // 1.存储到OSS
        String newFileName = ossUtil.generateFileName(file);
        String savedUrl = ossUtil.upload(newFileName, file);
        String fileExt = newFileName.substring(newFileName.lastIndexOf(".") + 1);
        String fileType = OSSUtil.getFileType(fileExt.toLowerCase());

        if (StringUtils.isBlank(savedUrl)) {
            throw new BusinessException("文件保存失败");
        }

        // 2. 构建 MediaFile对象,保存文件详细信息到MySQL
        MediaFile mediaFile = new MediaFile();
        mediaFile.setCaptureId(captureId);
        mediaFile.setFileName(newFileName);
        mediaFile.setFileUrl(savedUrl);
        mediaFile.setFileType(fileType);
        mediaFile.setFileSize(file.getSize());
        mediaFile.setMimeType(file.getContentType());
        mediaFile.setSequenceNum(sequenceNum != null ? sequenceNum : 1); // 默认值
        mediaFile.setCreateTime(LocalDateTime.now());

        if ("video".equals(fileType)) {
            // TODO: 可集成 FFmpeg 获取视频时长（此处暂设为 null 或 0）
            mediaFile.setDuration(null);
            // 同步生成缩略图（可异步处理）
            //fFmpegUtil.createVideoThumbnail(newFileName, newFileName);
        }

        mediaFileMapper.insert(mediaFile);

        // 更新采集记录状态为“已上传”
        record.setStatus(1);
        //updateById(record);
        captureRecordMapper.update(record,
                new LambdaQueryWrapper<CaptureRecord>().eq(CaptureRecord::getCaptureId, captureId));

        //或
//        captureRecordMapper.update(
//                null, // 实体为 null
//                new LambdaUpdateWrapper<CaptureRecord>()
//                        .set(CaptureRecord::getStatus, 1)
//                        .eq(CaptureRecord::getCaptureId, captureId)
//        );
        return savedUrl;
    }

    @Override
    @Transactional
    public String uploadTestFile(MultipartFile file, String fileName) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件为空");
        }

        String savedUrl = ossUtil.uploadAnalysisReport(fileName, file);

        if (StringUtils.isBlank(savedUrl)) {
            throw new BusinessException("文件保存失败");
        }

        return savedUrl;
    }

    @Override
    public void deleteMediaFile(String fileName) {
        ossUtil.deleteFile(fileName);
    }

    @Override
    public List<MediaFileDetailDTO> getMediaFileDetails(String captureId) {
        var record = captureRecordMapper.selectOne(new LambdaQueryWrapper<CaptureRecord>().eq(CaptureRecord::getCaptureId, captureId));

        Long userId = CurrentHolder.getCurrentId();

        if(record == null || !record.getUserId().equals(userId)) {
            throw new BusinessException("采集记录为空或无权限");
        }

        List<MediaFile> mediaFiles = mediaFileMapper.selectList(new LambdaQueryWrapper<MediaFile>()
                .eq(MediaFile::getCaptureId, captureId));
        List<MediaFileDetailDTO> mediaFileDetailDTOS = new ArrayList<>(mediaFiles.size());
        for (var mediaFile : mediaFiles) {
            MediaFileDetailDTO dto = new MediaFileDetailDTO();
            dto.setId(mediaFile.getId());
            dto.setFileName(mediaFile.getFileName());
            dto.setFileUrl(mediaFile.getFileUrl());
            dto.setFileType(mediaFile.getFileType());
            dto.setMimeType(mediaFile.getMimeType());
            dto.setFileSize(mediaFile.getFileSize());
            dto.setDuration(mediaFile.getDuration());
            dto.setThumbnailPath(mediaFile.getThumbnailPath());
            dto.setSequenceNum(mediaFile.getSequenceNum());
            dto.setCreateTime(mediaFile.getCreateTime());
            mediaFileDetailDTOS.add(dto);
        }

        return mediaFileDetailDTOS;
    }

    @Override
    public String getAnalysisReportUrl(String captureId) {
        CaptureRecord record = captureRecordMapper.selectOne(new LambdaQueryWrapper<CaptureRecord>().eq(CaptureRecord::getCaptureId, captureId));
        Long currentUserId = CurrentHolder.getCurrentId();

        if(record == null) {
            throw new BusinessException("采集记录为空");
        }

        if(!record.getStatus().equals(2)) {
            throw new BusinessException("分析未完成");
        }

        var analysisReport = analysisReportMapper.selectOne(new LambdaQueryWrapper<AnalysisReport>().eq(AnalysisReport::getCaptureId, captureId));
        return analysisReport.getReportUrl();
    }
}