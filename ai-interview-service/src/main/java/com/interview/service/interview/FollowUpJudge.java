package com.interview.service.interview;

import com.interview.dao.entity.Question;
import com.interview.service.ai.AiClientService;
import com.interview.service.ai.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 追问判断器
 * 根据回答质量决定是否追问，最多追问2次
 * 追问条件：得分<60 或 完整性<50% 或 深度<30%
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowUpJudge {

    private final AiClientService aiClientService;
    private final PromptTemplate promptTemplate;

    /**
     * 判断是否需要追问
     */
    public boolean shouldFollowUp(AnswerEvaluation eval) {
        if (eval.getScore() < 60) {
            return true;
        }
        if (eval.getCompleteness() < 0.5) {
            return true;
        }
        if (eval.getDepth() < 0.3) {
            return true;
        }
        return false;
    }

    /**
     * 生成追问内容
     */
    public String generateFollowUp(Question question, String answer, AnswerEvaluation eval) {
        String prompt = promptTemplate.buildFollowUpPrompt(
                question.getContent(),
                answer,
                eval.getScore(),
                eval.getCompleteness(),
                eval.getDepth()
        );
        return aiClientService.chat(prompt);
    }
}
