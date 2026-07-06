package com.qian.agent.service;

import com.qian.agent.common.Constants;
import com.qian.agent.entity.ChatSessionEntity;
import com.qian.agent.exception.BusinessException;
import com.qian.agent.model.CreateSessionRequest;
import com.qian.agent.model.SessionDto;
import com.qian.agent.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository sessionRepository;

    @Transactional(readOnly = true)
    public List<SessionDto> listSessions() {
        return sessionRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionDto getSession(Long sessionId) {
        ChatSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(404, "Session not found: " + sessionId));
        return toDto(session);
    }

    @Transactional
    public SessionDto createSession(CreateSessionRequest request) {
        ChatSessionEntity session = new ChatSessionEntity();
        session.setTitle(resolveTitle(request));
        session.setModel(Constants.DEFAULT_MODEL);
        return toDto(sessionRepository.save(session));
    }

    @Transactional
    public void touchSession(Long sessionId) {
        ChatSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(404, "Session not found: " + sessionId));
        sessionRepository.save(session);
    }

    @Transactional
    public void updateTitle(Long sessionId, String title) {
        ChatSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(404, "Session not found: " + sessionId));
        session.setTitle(title);
        sessionRepository.save(session);
    }

    private String resolveTitle(CreateSessionRequest request) {
        if (request != null && StringUtils.hasText(request.getTitle())) {
            return request.getTitle().trim();
        }
        return Constants.DEFAULT_SESSION_TITLE;
    }

    private SessionDto toDto(ChatSessionEntity entity) {
        return SessionDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .model(entity.getModel())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
