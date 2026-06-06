package com.interview.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.service.interview.InterviewEngine;
import com.interview.service.interview.InterviewMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 面试WebSocket处理器
 * 处理客户端消息：开始面试、提交答案、结束面试
 * 维护WebSocket会话和面试ID的绑定关系
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewWebSocket extends TextWebSocketHandler {

    private final InterviewEngine interviewEngine;
    private final ObjectMapper objectMapper;
    private final SessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket连接建立，sessionId: {}", session.getId());
        sessionManager.addSession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            WebSocketMessage msg = objectMapper.readValue(message.getPayload(), WebSocketMessage.class);
            log.info("收到消息，type: {}, interviewId: {}", msg.getType(), msg.getInterviewId());

            switch (msg.getType()) {
                case START_INTERVIEW -> handleStartInterview(session, msg);
                case ANSWER -> handleAnswer(session, msg);
                case END_INTERVIEW -> handleEndInterview(session, msg);
                default -> sendMessage(session, WebSocketMessage.error("未知消息类型"));
            }
        } catch (Exception e) {
            log.error("处理消息失败", e);
            sendMessage(session, WebSocketMessage.error("消息处理失败：" + e.getMessage()));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket连接关闭，sessionId: {}", session.getId());
        sessionManager.removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket传输错误，sessionId: {}", session.getId(), exception);
        sessionManager.removeSession(session);
    }

    /**
     * 处理开始面试
     */
    private void handleStartInterview(WebSocketSession session, WebSocketMessage msg) {
        Map<String, Object> data = (Map<String, Object>) msg.getData();
        Long userId = ((Number) data.get("userId")).longValue();
        Long resumeId = ((Number) data.get("resumeId")).longValue();
        Long jobId = ((Number) data.get("jobId")).longValue();

        var interview = interviewEngine.createInterview(userId, resumeId, jobId);
        var question = interviewEngine.getCurrentQuestion(interview.getId());

        sessionManager.bindInterview(session, interview.getId());
        sendMessage(session, WebSocketMessage.question(interview.getId(), question));
    }

    /**
     * 处理回答
     */
    private void handleAnswer(WebSocketSession session, WebSocketMessage msg) {
        Long interviewId = msg.getInterviewId();
        Map<String, Object> data = (Map<String, Object>) msg.getData();
        String answer = (String) data.get("content");

        InterviewMessage response = interviewEngine.handleAnswer(interviewId, answer);

        switch (response.getType()) {
            case QUESTION -> sendMessage(session, WebSocketMessage.question(interviewId,
                    response.getQuestionId(), response.getContent()));
            case FOLLOW_UP -> sendMessage(session, WebSocketMessage.followUp(interviewId,
                    response.getContent()));
            case FINISH -> sendMessage(session, WebSocketMessage.finish(interviewId,
                    response.getData()));
            case ERROR -> sendMessage(session, WebSocketMessage.error(response.getContent()));
        }
    }

    /**
     * 处理结束面试
     */
    private void handleEndInterview(WebSocketSession session, WebSocketMessage msg) {
        Long interviewId = msg.getInterviewId();
        String report = interviewEngine.generateReport(interviewId);
        sendMessage(session, WebSocketMessage.finish(interviewId, report));
        sessionManager.unbindInterview(session);
    }

    private void sendMessage(WebSocketSession session, WebSocketMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("发送消息失败", e);
        }
    }
}
