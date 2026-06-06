package com.interview.web;

import com.interview.dao.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket消息协议
 * 客户端 -> 服务端：START_INTERVIEW, ANSWER, END_INTERVIEW
 * 服务端 -> 客户端：QUESTION, FOLLOW_UP, REPORT, ERROR
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketMessage {
    private MessageType type;
    private Long interviewId;
    private Object data;
    private Long timestamp;

    public enum MessageType {
        START_INTERVIEW,
        ANSWER,
        NEXT_QUESTION,
        END_INTERVIEW,
        QUESTION,
        FOLLOW_UP,
        EVALUATION,
        REPORT,
        ERROR
    }

    public static WebSocketMessage question(Long interviewId, Question question) {
        return new WebSocketMessage(MessageType.QUESTION, interviewId, question, System.currentTimeMillis());
    }

    public static WebSocketMessage question(Long interviewId, Long questionId, String content) {
        Question q = new Question();
        q.setId(questionId);
        q.setContent(content);
        return new WebSocketMessage(MessageType.QUESTION, interviewId, q, System.currentTimeMillis());
    }

    public static WebSocketMessage followUp(Long interviewId, String content) {
        return new WebSocketMessage(MessageType.FOLLOW_UP, interviewId, content, System.currentTimeMillis());
    }

    public static WebSocketMessage finish(Long interviewId, String report) {
        return new WebSocketMessage(MessageType.REPORT, interviewId, report, System.currentTimeMillis());
    }

    public static WebSocketMessage error(String message) {
        return new WebSocketMessage(MessageType.ERROR, null, message, System.currentTimeMillis());
    }
}
