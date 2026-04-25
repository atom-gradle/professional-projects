package com.qian.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@AllArgsConstructor
@Getter
@Setter
public class AnalysisTaskRequest {

    private String captureId;

    private List<String> mediaFileUrls;
}
