package com.qian.agent.agent.tools;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TaxEstimateTool {

    // 税率配置：国家 -> (品类 -> 税率百分比)
    private static final Map<String, Map<String, BigDecimal>> TAX_RATES = new ConcurrentHashMap<>();

    // 默认税率（未配置的品类使用）
    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("10.0");

    static {
        // US 税率
        Map<String, BigDecimal> usRates = new ConcurrentHashMap<>();
        usRates.put("电子产品", new BigDecimal("5.0"));
        usRates.put("服装", new BigDecimal("10.0"));
        usRates.put("食品", new BigDecimal("8.0"));
        usRates.put("玩具", new BigDecimal("6.0"));
        usRates.put("化妆品", new BigDecimal("7.0"));
        usRates.put("家具", new BigDecimal("9.0"));
        usRates.put("珠宝", new BigDecimal("5.0"));
        TAX_RATES.put("US", usRates);

        // EU 税率
        Map<String, BigDecimal> euRates = new ConcurrentHashMap<>();
        euRates.put("电子产品", new BigDecimal("10.0"));
        euRates.put("服装", new BigDecimal("15.0"));
        euRates.put("食品", new BigDecimal("12.0"));
        euRates.put("玩具", new BigDecimal("10.0"));
        euRates.put("化妆品", new BigDecimal("12.0"));
        euRates.put("家具", new BigDecimal("14.0"));
        euRates.put("珠宝", new BigDecimal("10.0"));
        TAX_RATES.put("EU", euRates);

        // CN 税率
        Map<String, BigDecimal> cnRates = new ConcurrentHashMap<>();
        cnRates.put("电子产品", new BigDecimal("8.0"));
        cnRates.put("服装", new BigDecimal("12.0"));
        cnRates.put("食品", new BigDecimal("10.0"));
        cnRates.put("玩具", new BigDecimal("9.0"));
        cnRates.put("化妆品", new BigDecimal("11.0"));
        cnRates.put("家具", new BigDecimal("13.0"));
        cnRates.put("珠宝", new BigDecimal("8.0"));
        TAX_RATES.put("CN", cnRates);

        // JP 税率
        Map<String, BigDecimal> jpRates = new ConcurrentHashMap<>();
        jpRates.put("电子产品", new BigDecimal("6.0"));
        jpRates.put("服装", new BigDecimal("11.0"));
        jpRates.put("食品", new BigDecimal("9.0"));
        jpRates.put("玩具", new BigDecimal("7.0"));
        jpRates.put("化妆品", new BigDecimal("9.0"));
        jpRates.put("家具", new BigDecimal("10.0"));
        jpRates.put("珠宝", new BigDecimal("6.0"));
        TAX_RATES.put("JP", jpRates);

        // UK 税率
        Map<String, BigDecimal> ukRates = new ConcurrentHashMap<>();
        ukRates.put("电子产品", new BigDecimal("9.0"));
        ukRates.put("服装", new BigDecimal("13.0"));
        ukRates.put("食品", new BigDecimal("11.0"));
        ukRates.put("玩具", new BigDecimal("9.0"));
        ukRates.put("化妆品", new BigDecimal("11.0"));
        ukRates.put("家具", new BigDecimal("12.0"));
        ukRates.put("珠宝", new BigDecimal("9.0"));
        TAX_RATES.put("UK", ukRates);

        // AU 税率
        Map<String, BigDecimal> auRates = new ConcurrentHashMap<>();
        auRates.put("电子产品", new BigDecimal("7.0"));
        auRates.put("服装", new BigDecimal("12.0"));
        auRates.put("食品", new BigDecimal("10.0"));
        auRates.put("玩具", new BigDecimal("8.0"));
        auRates.put("化妆品", new BigDecimal("10.0"));
        auRates.put("家具", new BigDecimal("11.0"));
        auRates.put("珠宝", new BigDecimal("7.0"));
        TAX_RATES.put("AU", auRates);
    }

    @Tool(description = "估算商品在目标国家的跨境综合税费，输入品类、目标国家和商品价格")
    public TaxResult estimateTax(String category, String targetCountry, BigDecimal price) {
        if (category == null || category.isBlank()) {
            return TaxResult.error("品类不能为空");
        }
        if (targetCountry == null || targetCountry.isBlank()) {
            return TaxResult.error("目标国家不能为空");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return TaxResult.error("商品价格必须大于 0");
        }

        String countryCode = targetCountry.toUpperCase().trim();
        Map<String, BigDecimal> countryRates = TAX_RATES.getOrDefault(countryCode, Map.of());

        // 查找税率，找不到则使用默认税率
        BigDecimal taxRate = countryRates.entrySet().stream()
                .filter(entry -> category.contains(entry.getKey()) || entry.getKey().contains(category))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(DEFAULT_TAX_RATE);

        // 计算税费
        BigDecimal taxAmount = price.multiply(taxRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal totalPrice = price.add(taxAmount);

        String complianceTip;
        if (taxRate.compareTo(new BigDecimal("12")) > 0) {
            complianceTip = "⚠️ 该品类综合税率较高，建议对比不同国家的税负差异";
        } else if (taxRate.compareTo(new BigDecimal("8")) < 0) {
            complianceTip = "✅ 该品类综合税率较低，具有税收竞争优势";
        } else {
            complianceTip = "✅ 该品类综合税率处于中等水平";
        }

        return TaxResult.builder()
                .category(category)
                .country(countryCode)
                .price(price)
                .taxRate(taxRate)
                .taxAmount(taxAmount)
                .totalPrice(totalPrice)
                .complianceTip(complianceTip)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxResult {
        private String category;
        private String country;
        private BigDecimal price;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
        private BigDecimal totalPrice;
        private String complianceTip;

        public static TaxResult error(String message) {
            return TaxResult.builder()
                    .complianceTip("❌ " + message)
                    .build();
        }
    }
}