package com.qian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qian.dto.response.MediaFileDetailDTO;
import com.qian.pojo.MediaFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaFileService extends IService<MediaFile> {

    String uploadMediaFile(String captureId, MultipartFile file, Integer sequenceNum);

    String uploadTestFile(MultipartFile file, String fileName);

    void deleteMediaFile(String fileName);

    List<MediaFileDetailDTO> getMediaFileDetails(String captureId);

    String getAnalysisReportUrl(String captureId);
}
