package com.interview.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket会话管理
 * 维护sessionId -> WebSocketSession和sessionId -> interviewId的映射
 * 支持并发访问
 */
@Slf4j
@Component
public class SessionManager {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionInterviewMap = new ConcurrentHashMap<>();

    public void addSession(WebSocketSession session) {
        sessions.put(session.getId(), session);
        log.info("添加会话，sessionId: {}, 当前会话数: {}", session.getId(), sessions.size());
    }

    public void removeSession(WebSocketSession session) {
        sessions.remove(session.getId());
        sessionInterviewMap.remove(session.getId());
        log.info("移除会话，sessionId: {}, 当前会话数: {}", session.getId(), sessions.size());
    }

    public void bindInterview(WebSocketSession session, Long interviewId) {
        sessionInterviewMap.put(session.getId(), interviewId);
        log.info("绑定面试，sessionId: {}, interviewId: {}", session.getId(), interviewId);
    }

    public void unbindInterview(WebSocketSession session) {
        sessionInterviewMap.remove(session.getId());
        log.info("解绑面试，sessionId: {}", session.getId());
    }

    public Long getInterviewId(WebSocketSession session) {
        return sessionInterviewMap.get(session.getId());
    }

    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
}
