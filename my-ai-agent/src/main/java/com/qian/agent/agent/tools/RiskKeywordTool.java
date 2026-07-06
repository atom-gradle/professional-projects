package com.qian.agent.agent.tools;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RiskKeywordTool {

    private static final List<String> RISK_KEYWORDS = Arrays.asList(
            "最好", "第一", "顶级", "最有效", "绝对安全", "100%",
            "保证治愈", "根治", "神效", "全网最低", "销量第一",
            "国家级", "世界级", "最先进", "唯一", "首选",
            "零风险", "无副作用", "永久", "立即见效", "彻底"
    );

    @Tool(description = "检测宣传文案中是否含风险关键词，返回风险等级和发现的关键词列表")
    public RiskResult detectRisk(String content) {
        if (content == null || content.isBlank()) {
            return RiskResult.builder()
                    .riskLevel(RiskLevel.LOW)
                    .foundKeywords(List.of())
                    .suggestion("内容为空，无需检测")
                    .build();
        }

        List<String> found = RISK_KEYWORDS.stream()
                .filter(content::contains)
                .collect(Collectors.toList());

        RiskLevel level;
        String suggestion;

        if (found.isEmpty()) {
            level = RiskLevel.LOW;
            suggestion = "✅ 未发现风险关键词，宣传用语合规";
        } else if (found.size() <= 2) {
            level = RiskLevel.MEDIUM;
            suggestion = "⚠️ 发现 " + found.size() + " 个风险关键词，建议修改：" + String.join("、", found);
        } else {
            level = RiskLevel.HIGH;
            suggestion = "🚨 发现 " + found.size() + " 个风险关键词，存在较高违规风险，强烈建议重新撰写！";
        }

        return RiskResult.builder()
                .riskLevel(level)
                .foundKeywords(found)
                .suggestion(suggestion)
                .build();
    }

    public enum RiskLevel {
        LOW("低风险"),
        MEDIUM("中风险"),
        HIGH("高风险");

        private final String label;

        RiskLevel(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskResult {
        private RiskLevel riskLevel;
        private List<String> foundKeywords;
        private String suggestion;
    }
}