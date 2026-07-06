package com.qian.agent.agent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

@Component
public class CalculatorTool {

    private final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    @Tool(description = "计算数学表达式，支持加减乘除、括号和幂运算")
    public String calculate(
            @ToolParam(description = "数学表达式，例如：2 + 3 * 4 或 (10 + 5) / 3") String expression) {
        try {
            Object result = engine.eval(sanitize(expression));
            return String.valueOf(result);
        } catch (ScriptException e) {
            return "计算失败：" + e.getMessage();
        }
    }

    private String sanitize(String expression) {
        if (!expression.matches("[0-9+\\-*/().\\s^]+")) {
            throw new IllegalArgumentException("表达式包含非法字符");
        }
        return expression.replace("^", "**");
    }
}
