package com.qian.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AnalysisResultMessage implements Serializable {

    private String taskId;
    private String resultUrl;
    private LocalDateTime completeTime;
    private Long elapsedTime;

}