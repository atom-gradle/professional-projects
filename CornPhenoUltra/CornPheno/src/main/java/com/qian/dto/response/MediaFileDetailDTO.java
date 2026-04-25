package com.qian.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MediaFileDetailDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private String mimeType;
    private Integer duration;
    private String fileUrl;
    private Long fileSize;
    private String thumbnailPath;
    private Integer sequenceNum;
    private LocalDateTime createTime;
}
