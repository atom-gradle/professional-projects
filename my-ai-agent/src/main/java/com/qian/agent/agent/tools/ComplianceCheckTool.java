package com.qian.agent.agent.tools;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ComplianceCheckTool {

    private static final Map<String, Set<String>> RESTRICTED_CATEGORIES = new ConcurrentHashMap<>();

    static {
        RESTRICTED_CATEGORIES.put("US", Set.of(
                "电子烟", "CBD产品", "未经批准的药品", "含铅玩具", "转基因食品"
        ));
        RESTRICTED_CATEGORIES.put("EU", Set.of(
                "转基因食品", "含特定化学物质的化妆品", "一次性塑料制品", "未经批准的保健品"
        ));
        RESTRICTED_CATEGORIES.put("CN", Set.of(
                "非法出版物", "管制刀具", "未经批准的保健品", "电子烟"
        ));
        RESTRICTED_CATEGORIES.put("JP", Set.of(
                "未经批准的药品", "特定外来物种", "含铅玩具"
        ));
        RESTRICTED_CATEGORIES.put("UK", Set.of(
                "高糖饮料", "未经批准的药品", "一次性塑料制品"
        ));
        RESTRICTED_CATEGORIES.put("AU", Set.of(
                "未经批准的药品", "转基因食品", "含特定化学物质的化妆品"
        ));
    }

    @Tool(description = "检查某个商品品类在目标国家是否合规，返回合规状态和限制说明")
    public ComplianceResult checkCompliance(String category, String targetCountry) {
        if (category == null || category.isBlank()) {
            return ComplianceResult.error("品类不能为空");
        }
        if (targetCountry == null || targetCountry.isBlank()) {
            return ComplianceResult.error("目标国家不能为空");
        }

        String countryCode = targetCountry.toUpperCase().trim();
        Set<String> restricted = RESTRICTED_CATEGORIES.getOrDefault(countryCode, Set.of());

        // 检查品类是否受限（模糊匹配）
        boolean isRestricted = restricted.stream()
                .anyMatch(keyword -> category.toLowerCase().contains(keyword.toLowerCase()) ||
                        keyword.toLowerCase().contains(category.toLowerCase()));

        return ComplianceResult.builder()
                .category(category)
                .country(countryCode)
                .compliant(!isRestricted)
                .reason(isRestricted ?
                        "⚠️ 该品类在 " + countryCode + " 受到限制，建议改选其他品类或申请特殊许可" :
                        "✅ 该品类在 " + countryCode + " 合规，可正常销售")
                .restrictedKeywords(isRestricted ? restricted : Set.of())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceResult {
        private String category;
        private String country;
        private boolean compliant;
        private String reason;
        private Set<String> restrictedKeywords;

        public static ComplianceResult error(String message) {
            return ComplianceResult.builder()
                    .compliant(false)
                    .reason("❌ " + message)
                    .build();
        }
    }
}