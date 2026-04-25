package com.qian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qian.dto.request.*;
import com.qian.dto.response.CaptureRecordDetailResponse;
import com.qian.dto.response.CaptureRecordStatDTO;
import com.qian.exception.BusinessException;
import com.qian.pojo.*;
import com.qian.mapper.*;
import com.qian.service.CaptureRecordService;
import com.qian.utils.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CaptureRecordServiceImpl extends ServiceImpl<CaptureRecordMapper, CaptureRecord>
        implements CaptureRecordService {

    private final MediaFileMapper mediaFileMapper;

    private final CaptureRecordMapper captureRecordMapper;

    private final OSSUtil ossUtil;

    public CaptureRecordServiceImpl(MediaFileMapper mediaFileMapper, CaptureRecordMapper captureRecordMapper, OSSUtil ossUtil) {
        this.mediaFileMapper = mediaFileMapper;
        this.captureRecordMapper = captureRecordMapper;
        this.ossUtil = ossUtil;
    }

    @Override
    @Transactional
    public String createCapture(CaptureRecordCreateDTO dto) {
        CaptureRecord record = new CaptureRecord();
        BeanUtils.copyProperties(dto, record);

        String captureId = Util.generateCaptureId();
        record.setCaptureId(captureId);
        record.setUserId(CurrentHolder.getCurrentId());
        record.setStatus(0); // 本地
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        save(record);
        return record.getCaptureId();
    }

    @Override
    public Page<CaptureRecord> listCaptures(Integer page_,Integer size) {
        Page<CaptureRecord> page = new Page<>(page_, size);

        Long userId = CurrentHolder.getCurrentId();

        LambdaQueryWrapper<CaptureRecord> wrapper = new LambdaQueryWrapper<CaptureRecord>()
                .eq(CaptureRecord::getUserId, userId)
                .orderByDesc(CaptureRecord::getCreateTime);
        /*
        if (query.getStatus() != null) {
            wrapper.eq(CaptureRecord::getStatus, query.getStatus());
        }
         */
        return page(page, wrapper);
    }

    @Override
    public CaptureRecordDetailResponse getCaptureDetail(String captureId) {
        //CaptureRecord record = getById(id);
        CaptureRecord record = captureRecordMapper.selectOne(new LambdaQueryWrapper<CaptureRecord>().eq(CaptureRecord::getCaptureId, captureId));
        if (record == null) {
            throw new BusinessException("记录不存在");
        }

        CaptureRecordDetailResponse dto = new CaptureRecordDetailResponse();
        BeanUtils.copyProperties(record, dto);

        // 查询关联媒体文件
        List<MediaFile> mediaFiles = mediaFileMapper.selectList(
                new LambdaQueryWrapper<MediaFile>().eq(MediaFile::getCaptureId, captureId)
        );
        //dto.setMediaFiles(mediaFiles);
        return dto;
    }

    @Override
    public List<CaptureRecordDetailResponse> getAllUnanalyzedRecords() {
        List<CaptureRecord> captureRecords = captureRecordMapper
                .selectList(new LambdaQueryWrapper<CaptureRecord>()
                        .eq(CaptureRecord::getStatus, CaptureRecord.STATUS.UPLOADED.getValue()));
        List<CaptureRecordDetailResponse> records = new ArrayList<>(captureRecords.size());
        for (var captureRecord : captureRecords) {
            CaptureRecordDetailResponse response = new CaptureRecordDetailResponse();
            BeanUtils.copyProperties(captureRecord, response);
            records.add(response);
        }

        return records;
    }

    @Override
    @Transactional
    public void deleteCapture(String captureId) {
        //CaptureRecord record = getById(captureId);
        CaptureRecord record = captureRecordMapper.selectOne(new LambdaQueryWrapper<CaptureRecord>().eq(CaptureRecord::getCaptureId, captureId));
        if (record == null) {
            throw new BusinessException("记录不存在");
        }

        // 查询所有关联媒体文件，并调用OSSUtil删除
        List<MediaFile> mediaFilesToDelete = mediaFileMapper.selectList(
                new LambdaQueryWrapper<MediaFile>()
                        .eq(MediaFile::getCaptureId, captureId));

        if(mediaFilesToDelete != null && !mediaFilesToDelete.isEmpty()) {
            for (var mediafile : mediaFilesToDelete) {
                ossUtil.deleteFile(mediafile.getFileName());
            }
        }

        // 删除主记录（关联媒体文件由数据库自动级联处理）
        //removeById(id);
        captureRecordMapper.delete(new LambdaQueryWrapper<CaptureRecord>().eq(CaptureRecord::getCaptureId, captureId));
    }

    @Override
    public List<CaptureRecordStatDTO> getStatistics(List<String> blockIds, LocalDateTime startTime, LocalDateTime endTime) {
        // 这里可以添加额外的业务逻辑校验，例如时间范围检查等
        return captureRecordMapper.getCaptureStatistics(blockIds, startTime, endTime);
    }

}