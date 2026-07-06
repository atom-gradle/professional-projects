package com.qian.agent.service;

import com.qian.agent.model.AgentResponse;
import com.qian.agent.redis.SessionCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final ChatClient chatClient;
    private final MessageService messageService;
    private final ChatSessionService sessionService;
    private final ChatRecordService chatRecordService;
    private final SessionCacheRepository sessionCacheRepository;

    public AgentResponse chat(Long sessionId, String userMessage) {
        sessionService.getSession(sessionId);
        messageService.saveUserMessage(sessionId, userMessage);

        String reply = chatClient.prompt()
                .user(userMessage)
                .advisors(spec -> spec.param("chat_memory_conversation_id", sessionId.toString()))
                .call()
                .content();

        chatRecordService.recordAssistantReply(sessionId, userMessage, reply);

        return AgentResponse.builder()
                .sessionId(sessionId)
                .reply(reply)
                .build();
    }

    public Flux<String> chatStream(Long sessionId, String userMessage) {
        sessionService.getSession(sessionId);
        messageService.saveUserMessage(sessionId, userMessage);

        StringBuilder replyBuilder = new StringBuilder();

        return chatClient.prompt()
                .user(userMessage)
                .advisors(spec -> spec.param("chat_memory_conversation_id", sessionId.toString()))
                .stream()
                .content()
                .doOnNext(replyBuilder::append)
                .doOnComplete(() -> Schedulers.boundedElastic().schedule(() ->
                        chatRecordService.recordAssistantReply(sessionId, userMessage, replyBuilder.toString())))
                .doOnError(error -> log.error("Stream chat failed for session {}", sessionId, error));
    }

    public String getCachedReply(Long sessionId) {
        return sessionCacheRepository.getLastReply(sessionId).orElse(null);
    }
}
