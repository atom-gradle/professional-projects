package com.qian.agent.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class WeatherTool {

    private static final Map<String, String[]> CITY_WEATHER = Map.of(
            "北京", new String[]{"晴", "多云", "阴"},
            "上海", new String[]{"多云", "小雨", "晴"},
            "广州", new String[]{"晴", "雷阵雨", "多云"},
            "深圳", new String[]{"晴", "多云", "阵雨"},
            "杭州", new String[]{"多云", "小雨", "晴"}
    );

    @Tool(description = "查询指定城市的当前天气情况，包括温度、湿度和天气状况")
    public String getWeather(
            @ToolParam(description = "城市名称，例如：北京、上海") String city) {
        String[] conditions = CITY_WEATHER.getOrDefault(city, new String[]{"晴", "多云"});
        String condition = conditions[ThreadLocalRandom.current().nextInt(conditions.length)];
        int temperature = ThreadLocalRandom.current().nextInt(15, 35);
        int humidity = ThreadLocalRandom.current().nextInt(40, 90);

        return String.format("%s：%s，温度 %d°C，湿度 %d%%（Demo 模拟数据）",
                city, condition, temperature, humidity);
    }
}
