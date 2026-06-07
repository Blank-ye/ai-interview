package com.interview.service.agent;

import com.interview.dao.entity.Question;
import com.interview.dao.entity.Resume;
import com.interview.dao.mapper.ResumeMapper;
import com.interview.service.ai.AiClientService;
import com.interview.service.ai.PromptTemplate;
import com.interview.service.crawler.JobData;
import com.interview.service.crawler.RagStorage;
import com.interview.service.interview.AnswerEvaluation;
import com.interview.service.interview.InterviewContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 面试Agent
 * AI自主决策面试流程，而不是固定规则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewAgent {

    private final AiClientService aiClientService;
    private final PromptTemplate promptTemplate;
    private final AgentToolExecutor toolExecutor;
    private final AgentMemoryService memoryService;
    private final ResumeMapper resumeMapper;
    private final RagStorage ragStorage;

    // Agent决策缓存
    private final Map<Long, AgentState> stateCache = new ConcurrentHashMap<>();

    /**
     * 初始化Agent状态
     */
    public AgentState initState(Long interviewId, Resume resume, JobData job) {
        AgentState state = AgentState.builder()
                .interviewId(interviewId)
                .resume(resume)
                .job(job)
                .currentTopicIndex(0)
                .totalQuestions(0)
                .build();
        stateCache.put(interviewId, state);
        return state;
    }

    /**
     * Agent决策：根据回答决定下一步动作
     */
    public AgentAction decide(Long interviewId, Question question, String answer, AnswerEvaluation eval) {
        AgentState state = stateCache.get(interviewId);
        if (state == null) {
            return AgentAction.endInterview("面试状态不存在");
        }

        // 获取历史记忆
        String memory = memoryService.getMemory(state.getResume().getUserId());

        // 构建决策Prompt
        String prompt = buildDecisionPrompt(state, question, answer, eval, memory);

        // 调用AI决策
        String response = aiClientService.chat(prompt);

        // 解析决策结果
        AgentAction action = parseAction(response);

        // 保存到记忆
        memoryService.addMemory(state.getResume().getUserId(),
                String.format("问题：%s，回答得分：%.1f，决策：%s",
                        question.getContent(), eval.getScore(), action.getType()));

        log.info("Agent决策：interviewId={}, action={}, reason={}",
                interviewId, action.getType(), action.getReason());

        return action;
    }

    /**
     * 构建决策Prompt
     */
    private String buildDecisionPrompt(AgentState state, Question question,
                                        String answer, AnswerEvaluation eval, String memory) {
        return """
                你是一个智能面试官Agent，根据候选人的回答自主决定下一步动作。

                ## 候选人信息
                简历摘要：%s

                ## 应聘职位
                %s - %s

                ## 当前情况
                已问题目数：%d
                当前问题：%s
                候选人回答：%s

                ## 评估结果
                得分：%.1f
                完整性：%.1f%%
                深度：%.1f%%
                关键点命中：%s
                遗漏关键点：%s

                ## 历史表现
                %s

                ## 请决定下一步动作

                可选动作：
                1. ASK_QUESTION - 提问下一题（回答质量好，可以进入下一个话题）
                2. FOLLOW_UP - 追问（回答不完整或太浅，需要深入）
                3. DEEP_DIVE - 深入某个方向（发现候选人擅长点，想深入了解）
                4. SWITCH_TOPIC - 切换话题（当前话题问得差不多了，换个方向）
                5. END_INTERVIEW - 结束面试（已经问够了，或者候选人表现很差）

                请按以下JSON格式输出：
                {
                    "action": "ASK_QUESTION",
                    "content": "具体内容（如果是提问或追问）",
                    "reason": "决策理由"
                }
                """.formatted(
                truncate(state.getResume().getRawContent(), 500),
                state.getJob().getTitle(),
                state.getJob().getCompany(),
                state.getTotalQuestions(),
                question.getContent(),
                truncate(answer, 300),
                eval.getScore(),
                eval.getCompleteness() * 100,
                eval.getDepth() * 100,
                String.join(", ", eval.getKeyPointsHit()),
                String.join(", ", eval.getKeyPointsMiss()),
                memory.isEmpty() ? "无" : memory
        );
    }

    /**
     * 解析AI决策结果
     */
    private AgentAction parseAction(String response) {
        try {
            String json = extractJson(response);
            // 简单解析，实际应该用Jackson
            if (json.contains("FOLLOW_UP")) {
                String content = extractField(json, "content");
                String reason = extractField(json, "reason");
                return AgentAction.followUp(content, reason);
            } else if (json.contains("DEEP_DIVE")) {
                String content = extractField(json, "content");
                String reason = extractField(json, "reason");
                return AgentAction.deepDive(content, reason);
            } else if (json.contains("SWITCH_TOPIC")) {
                String content = extractField(json, "content");
                String reason = extractField(json, "reason");
                return AgentAction.switchTopic(content, reason);
            } else if (json.contains("END_INTERVIEW")) {
                String reason = extractField(json, "reason");
                return AgentAction.endInterview(reason);
            } else {
                String content = extractField(json, "content");
                String reason = extractField(json, "reason");
                return AgentAction.askQuestion(content, reason);
            }
        } catch (Exception e) {
            log.error("解析Agent决策失败", e);
            return AgentAction.askQuestion("请继续", "解析失败，默认提问");
        }
    }

    private String extractJson(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }

    private String extractField(String json, String field) {
        String pattern = "\"" + field + "\":\"";
        int start = json.indexOf(pattern);
        if (start < 0) return "";
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return "";
        return json.substring(start, end);
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "..." : text;
    }

    /**
     * Agent状态
     */
    @lombok.Data
    @lombok.Builder
    public static class AgentState {
        private Long interviewId;
        private Resume resume;
        private JobData job;
        private int currentTopicIndex;
        private int totalQuestions;
    }
}
