package com.interview.service.interview;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.dao.entity.Answer;
import com.interview.dao.entity.Question;
import com.interview.service.ai.AiClientService;
import com.interview.service.ai.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 评分服务
 * 调用AI评估回答质量，输出：
 * - score: 总分（0-100）
 * - completeness: 完整性（0-1）
 * - depth: 深度（0-1）
 * - keyPointsHit/Miss: 关键点命中情况
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringService {

    private final AiClientService aiClientService;
    private final PromptTemplate promptTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 评估回答
     */
    public AnswerEvaluation evaluate(Question question, String answer) {
        try {
            // 解析预期关键点
            List<String> expectedPoints = objectMapper.readValue(
                    question.getExpectedPoints(),
                    new TypeReference<List<String>>() {}
            );

            // 调用AI评估
            String prompt = promptTemplate.buildEvaluatePrompt(
                    question.getContent(),
                    expectedPoints,
                    answer
            );
            String response = aiClientService.chat(prompt);

            // 解析评估结果
            return parseEvaluation(response);
        } catch (Exception e) {
            log.error("评估回答失败", e);
            // 返回默认评估
            return AnswerEvaluation.builder()
                    .score(50.0)
                    .completeness(0.5)
                    .depth(0.5)
                    .evaluation("评估失败，请重试")
                    .build();
        }
    }

    /**
     * 构建回答实体
     */
    public Answer buildAnswer(Question question, String content, AnswerEvaluation eval) {
        Answer answer = new Answer();
        answer.setQuestionId(question.getId());
        answer.setInterviewId(question.getInterviewId());
        answer.setContent(content);
        answer.setScore(BigDecimal.valueOf(eval.getScore()));
        answer.setEvaluation(eval.getEvaluation());
        try {
            answer.setKeyPointsHit(eval.getKeyPointsHit() != null ?
                    objectMapper.writeValueAsString(eval.getKeyPointsHit()) : null);
        } catch (JsonProcessingException e) {
            log.error("");
        }
        try {
            answer.setKeyPointsMiss(eval.getKeyPointsMiss() != null ?
                    objectMapper.writeValueAsString(eval.getKeyPointsMiss()) : null);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        answer.setFollowUpCount(0);
        answer.setCreateTime(LocalDateTime.now());
        return answer;
    }

    /**
     * 解析评估结果
     */
    private AnswerEvaluation parseEvaluation(String response) {
        try {
            String json = extractJson(response);
            Map<String, Object> data = objectMapper.readValue(json,
                    new TypeReference<Map<String, Object>>() {});

            return AnswerEvaluation.builder()
                    .score(toDouble(data.get("score")))
                    .completeness(toDouble(data.get("completeness")))
                    .depth(toDouble(data.get("depth")))
                    .evaluation((String) data.get("evaluation"))
                    .keyPointsHit((List<String>) data.get("keyPointsHit"))
                    .keyPointsMiss((List<String>) data.get("keyPointsMiss"))
                    .build();
        } catch (Exception e) {
            log.error("解析评估结果失败，response: {}", response, e);
            return AnswerEvaluation.builder()
                    .score(50.0)
                    .completeness(0.5)
                    .depth(0.5)
                    .evaluation("解析失败")
                    .build();
        }
    }

    private Double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    private String extractJson(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }
}
