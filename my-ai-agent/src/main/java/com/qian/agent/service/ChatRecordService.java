package com.qian.agent.service;

import com.qian.agent.redis.SessionCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ChatRecordService {

    private final MessageService messageService;
    private final ChatSessionService sessionService;
    private final SessionCacheRepository sessionCacheRepository;

    @Transactional
    public void recordAssistantReply(Long sessionId, String userMessage, String reply) {
        if (!StringUtils.hasText(reply)) {
            return;
        }
        messageService.saveAssistantMessage(sessionId, reply);
        sessionCacheRepository.cacheLastReply(sessionId, reply);
        sessionService.touchSession(sessionId);

        if (shouldAutoTitle(sessionId, userMessage)) {
            sessionService.updateTitle(sessionId, truncateTitle(userMessage));
        }
    }

    private boolean shouldAutoTitle(Long sessionId, String userMessage) {
        return messageService.listBySession(sessionId).size() <= 2
                && StringUtils.hasText(userMessage);
    }

    private String truncateTitle(String userMessage) {
        String trimmed = userMessage.trim();
        return trimmed.length() <= 30 ? trimmed : trimmed.substring(0, 30) + "...";
    }
}
