package com.qian.agent.controller;

import com.qian.agent.common.Result;
import com.qian.agent.model.ChatMessageDto;
import com.qian.agent.model.CreateSessionRequest;
import com.qian.agent.model.SessionDto;
import com.qian.agent.service.ChatSessionService;
import com.qian.agent.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Session", description = "会话管理接口")
public class SessionController {

    private final ChatSessionService sessionService;
    private final MessageService messageService;

    @GetMapping
    @Operation(summary = "会话列表")
    public Result<List<SessionDto>> listSessions() {
        return Result.ok(sessionService.listSessions());
    }

    @PostMapping
    @Operation(summary = "创建会话")
    public Result<SessionDto> createSession(@RequestBody(required = false) CreateSessionRequest request) {
        return Result.ok(sessionService.createSession(request));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "会话详情")
    public Result<SessionDto> getSession(@PathVariable Long sessionId) {
        return Result.ok(sessionService.getSession(sessionId));
    }

    @GetMapping("/{sessionId}/messages")
    @Operation(summary = "会话消息历史")
    public Result<List<ChatMessageDto>> listMessages(@PathVariable Long sessionId) {
        return Result.ok(messageService.listBySession(sessionId));
    }
}
