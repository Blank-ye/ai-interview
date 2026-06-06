package com.interview.service.interview;

import com.interview.dao.entity.Question;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 面试消息
 * 服务端返回给客户端的消息类型：
 * - QUESTION: 下一题
 * - FOLLOW_UP: 追问
 * - FINISH: 面试结束，附带报告
 * - ERROR: 错误
 */
@Data
@AllArgsConstructor
public class InterviewMessage {
    private MessageType type;
    private Long questionId;
    private String content;
    private String data;

    public enum MessageType {
        QUESTION, FOLLOW_UP, FINISH, ERROR
    }

    public static InterviewMessage nextQuestion(Question question) {
        return new InterviewMessage(MessageType.QUESTION, question.getId(), question.getContent(), null);
    }

    public static InterviewMessage followUp(String content) {
        return new InterviewMessage(MessageType.FOLLOW_UP, null, content, null);
    }

    public static InterviewMessage finish(String report) {
        return new InterviewMessage(MessageType.FINISH, null, "面试结束", report);
    }

    public static InterviewMessage error(String message) {
        return new InterviewMessage(MessageType.ERROR, null, message, null);
    }
}
