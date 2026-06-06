package com.interview.service.interview;

import com.interview.dao.entity.Interview;
import com.interview.dao.entity.Question;
import com.interview.dao.entity.Resume;
import com.interview.dao.mapper.AnswerMapper;
import com.interview.dao.mapper.InterviewMapper;
import com.interview.dao.mapper.QuestionMapper;
import com.interview.service.ai.AiClientService;
import com.interview.service.ai.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 面试引擎核心
 * 负责面试全流程：创建面试 -> 出题 -> 评估回答 -> 追问 -> 生成报告
 * 使用内存缓存维护面试上下文，支持并发面试
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewEngine {

    private final InterviewMapper interviewMapper;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final QuestionGenerator questionGenerator;
    private final FollowUpJudge followUpJudge;
    private final ScoringService scoringService;
    private final AiClientService aiClientService;
    private final PromptTemplate promptTemplate;

    // 面试上下文缓存
    private final Map<Long, InterviewContext> contextCache = new ConcurrentHashMap<>();

    /**
     * 创建面试
     */
    public Interview createInterview(Long userId, Long resumeId, Long jobId) {
        Interview interview = new Interview();
        interview.setUserId(userId);
        interview.setResumeId(resumeId);
        interview.setJobId(jobId);
        interview.setStatus(0); // 进行中
        interview.setStartTime(LocalDateTime.now());
        interview.setCreateTime(LocalDateTime.now());
        interviewMapper.insert(interview);

        // 生成题目
        List<Question> questions = questionGenerator.generate(resumeId, jobId);
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            question.setInterviewId(interview.getId());
            question.setOrderNum(i + 1);
            questionMapper.insert(question);
        }

        // 初始化上下文
        InterviewContext context = InterviewContext.builder()
                .interviewId(interview.getId())
                .questions(questions)
                .currentQuestionIndex(0)
                .build();
        contextCache.put(interview.getId(), context);

        log.info("面试创建成功，interviewId: {}, 题目数量: {}", interview.getId(), questions.size());
        return interview;
    }

    /**
     * 获取当前题目
     */
    public Question getCurrentQuestion(Long interviewId) {
        InterviewContext context = contextCache.get(interviewId);
        if (context == null) {
            throw new RuntimeException("面试不存在或已结束");
        }
        return context.getCurrentQuestion();
    }

    /**
     * 处理回答
     */
    public InterviewMessage handleAnswer(Long interviewId, String answer) {
        InterviewContext context = contextCache.get(interviewId);
        if (context == null) {
            throw new RuntimeException("面试不存在或已结束");
        }

        Question currentQuestion = context.getCurrentQuestion();

        // 1. 评估回答
        AnswerEvaluation eval = scoringService.evaluate(currentQuestion, answer);

        // 2. 保存回答
        answerMapper.insert(scoringService.buildAnswer(currentQuestion, answer, eval));

        // 3. 判断是否追问
        if (followUpJudge.shouldFollowUp(eval) && context.getFollowUpCount() < 2) {
            String followUp = followUpJudge.generateFollowUp(currentQuestion, answer, eval);
            context.incrementFollowUpCount();
            return InterviewMessage.followUp(followUp);
        }

        // 4. 进入下一题或结束
        context.resetFollowUpCount();
        if (context.hasMoreQuestions()) {
            context.nextQuestion();
            return InterviewMessage.nextQuestion(context.getCurrentQuestion());
        } else {
            return finishInterview(interviewId, context);
        }
    }

    /**
     * 结束面试
     */
    private InterviewMessage finishInterview(Long interviewId, InterviewContext context) {
        // 1. 生成报告
        String report = generateReport(interviewId);

        // 2. 更新面试状态
        Interview interview = interviewMapper.selectById(interviewId);
        interview.setStatus(1); // 已完成
        interview.setEndTime(LocalDateTime.now());
        interviewMapper.updateById(interview);

        // 3. 清除上下文
        contextCache.remove(interviewId);

        return InterviewMessage.finish(report);
    }

    /**
     * 生成面试报告
     */
    public String generateReport(Long interviewId) {
        Interview interview = interviewMapper.selectById(interviewId);
        List<Question> questions = questionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Question>()
                        .eq("interview_id", interviewId)
                        .orderByAsc("order_num"));

        // 构建面试数据
        StringBuilder data = new StringBuilder();
        data.append("职位ID: ").append(interview.getJobId()).append("\n");
        data.append("题目数量: ").append(questions.size()).append("\n");

        for (Question question : questions) {
            data.append("\n题目: ").append(question.getContent());
            var answers = answerMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.interview.dao.entity.Answer>()
                            .eq("question_id", question.getId()));
            for (var answer : answers) {
                data.append("\n回答: ").append(answer.getContent());
                data.append("\n得分: ").append(answer.getScore());
            }
        }

        // 调用AI生成报告
        String prompt = promptTemplate.buildReportPrompt(data.toString());
        return aiClientService.chat(prompt);
    }
}
