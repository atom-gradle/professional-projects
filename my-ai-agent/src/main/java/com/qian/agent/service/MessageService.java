package com.qian.agent.service;

import com.qian.agent.common.Constants;
import com.qian.agent.entity.ChatMessageEntity;
import com.qian.agent.entity.ChatSessionEntity;
import com.qian.agent.exception.BusinessException;
import com.qian.agent.model.ChatMessageDto;
import com.qian.agent.repository.ChatMessageRepository;
import com.qian.agent.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ChatMessageRepository messageRepository;
    private final ChatSessionRepository sessionRepository;

    @Transactional(readOnly = true)
    public List<ChatMessageDto> listBySession(Long sessionId) {
        ensureSessionExists(sessionId);
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ChatMessageEntity saveUserMessage(Long sessionId, String content) {
        return saveMessage(sessionId, Constants.ROLE_USER, content);
    }

    @Transactional
    public ChatMessageEntity saveAssistantMessage(Long sessionId, String content) {
        return saveMessage(sessionId, Constants.ROLE_ASSISTANT, content);
    }

    private ChatMessageEntity saveMessage(Long sessionId, String role, String content) {
        ensureSessionExists(sessionId);

        ChatMessageEntity message = new ChatMessageEntity();
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        return messageRepository.save(message);
    }

    private void ensureSessionExists(Long sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new BusinessException(404, "Session not found: " + sessionId);
        }
    }

    private ChatMessageDto toDto(ChatMessageEntity entity) {
        return ChatMessageDto.builder()
                .id(entity.getId())
                .sessionId(entity.getSessionId())
                .role(entity.getRole())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
