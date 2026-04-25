package com.qian.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qian.dto.request.ExamineRequest;
import com.qian.exception.BusinessException;
import com.qian.mapper.*;
import com.qian.pojo.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    private final UserMapper userMapper;
    private final CaptureRecordMapper captureRecordMapper;
    private final AnalysisReportMapper analysisReportMapper;
    private final MediaFileMapper mediaFileMapper;

    public AdminService(UserMapper userMapper, CaptureRecordMapper captureRecordMapper, AnalysisReportMapper analysisReportMapper, MediaFileMapper mediaFileMapper) {
        this.userMapper = userMapper;
        this.captureRecordMapper = captureRecordMapper;
        this.analysisReportMapper = analysisReportMapper;
        this.mediaFileMapper = mediaFileMapper;
    }

    public List<User> listAllUsersTodo() {
        List<User> allUsers = userMapper.selectList(new LambdaQueryWrapper<User>()
                .eq(User::getAuditStatus, 1));
        return allUsers;
    }

    public List<User> listAllUsers() {
        return userMapper.selectList(null);
    }

    public void examineUser(ExamineRequest request) {
        User user = userMapper.selectById(request.getUserId());
        if(user == null) {
            throw new BusinessException("用户不存在，操作失败");
        }

        user.setAuditStatus(request.getAuditStatus());

        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);
    }

    public List<CaptureRecord> listAllRecords() {
        return captureRecordMapper.selectList(null);
    }

    public List<MediaFile> listAllFiles() {
        return mediaFileMapper.selectList(null);
    }
}
