package com.qian.agent.controller;

import com.qian.agent.common.Result;
import com.qian.agent.model.AgentRequest;
import com.qian.agent.model.AgentResponse;
import com.qian.agent.service.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
@Tag(name = "Agent", description = "AI Agent 对话接口")
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/sessions/{sessionId}/chat")
    @Operation(summary = "同步对话", description = "发送消息并等待完整回复")
    public Result<AgentResponse> chat(
            @PathVariable Long sessionId,
            @Valid @RequestBody AgentRequest request) {
        return Result.ok(agentService.chat(sessionId, request.getMessage()));
    }

    @PostMapping(value = "/sessions/{sessionId}/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式对话", description = "SSE 流式返回回复内容")
    public Flux<String> chatStream(
            @PathVariable Long sessionId,
            @Valid @RequestBody AgentRequest request) {
        return agentService.chatStream(sessionId, request.getMessage());
    }
}
