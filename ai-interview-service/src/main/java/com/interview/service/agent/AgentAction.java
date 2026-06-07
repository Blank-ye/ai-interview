package com.interview.service.agent;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Agent动作枚举
 * AI根据面试情况自主决定下一步动作
 */
@Data
@AllArgsConstructor
public class AgentAction {

    private Action type;
    private String content;
    private String reason;  // 决策理由

    public enum Action {
        ASK_QUESTION,    // 提问（下一题）
        FOLLOW_UP,       // 追问
        DEEP_DIVE,       // 深入某个方向
        SWITCH_TOPIC,    // 切换话题
        END_INTERVIEW    // 结束面试
    }

    public static AgentAction askQuestion(String content, String reason) {
        return new AgentAction(Action.ASK_QUESTION, content, reason);
    }

    public static AgentAction followUp(String content, String reason) {
        return new AgentAction(Action.FOLLOW_UP, content, reason);
    }

    public static AgentAction deepDive(String content, String reason) {
        return new AgentAction(Action.DEEP_DIVE, content, reason);
    }

    public static AgentAction switchTopic(String content, String reason) {
        return new AgentAction(Action.SWITCH_TOPIC, content, reason);
    }

    public static AgentAction endInterview(String reason) {
        return new AgentAction(Action.END_INTERVIEW, "面试结束", reason);
    }
}
