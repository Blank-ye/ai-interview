package com.interview.service.interview;

import com.interview.dao.entity.Question;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 面试上下文
 * 维护单次面试的状态：题目列表、当前题目索引、追问次数
 * 存储在内存中，面试结束后清除
 */
@Data
@Builder
public class InterviewContext {
    private Long interviewId;
    private List<Question> questions;
    private int currentQuestionIndex;
    private int followUpCount;

    public Question getCurrentQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            return null;
        }
        return questions.get(currentQuestionIndex);
    }

    public boolean hasMoreQuestions() {
        return currentQuestionIndex < questions.size() - 1;
    }

    public void nextQuestion() {
        currentQuestionIndex++;
    }

    public void incrementFollowUpCount() {
        followUpCount++;
    }

    public void resetFollowUpCount() {
        followUpCount = 0;
    }
}
