package com.interview.service.interview;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.dao.entity.Question;
import com.interview.dao.entity.Resume;
import com.interview.dao.mapper.ResumeMapper;
import com.interview.service.ai.AiClientService;
import com.interview.service.ai.PromptTemplate;
import com.interview.service.crawler.JobData;
import com.interview.service.crawler.RagStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 题目生成器
 * 根据简历和职位生成三类题目：
 * 1. 基础题：考察技能点掌握程度
 * 2. 项目题：考察项目经验和解决问题能力
 * 3. 场景题：考察实际工作场景应对能力
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionGenerator {

    private final AiClientService aiClientService;
    private final PromptTemplate promptTemplate;
    private final ResumeMapper resumeMapper;
    private final RagStorage ragStorage;
    private final ObjectMapper objectMapper;

    /**
     * 生成面试题目
     */
    public List<Question> generate(Long resumeId, Long jobId) {
        Resume resume = resumeMapper.selectById(resumeId);
        JobData job = ragStorage.getByIds(List.of(String.valueOf(jobId))).stream().findFirst().orElse(null);

        List<Question> questions = new ArrayList<>();

        // 1. 基础题：根据简历中的技能点
        questions.addAll(generateBasicQuestions(resume));

        // 2. 项目题：根据简历中的项目经历
        questions.addAll(generateProjectQuestions(resume));

        // 3. 场景题：根据职位要求
        questions.addAll(generateScenarioQuestions(job));

        return questions;
    }

    /**
     * 生成基础题
     */
    private List<Question> generateBasicQuestions(Resume resume) {
        try {
            // 从结构化简历中提取技能
            Map<String, Object> structured = objectMapper.readValue(resume.getStructuredContent(),
                    new TypeReference<Map<String, Object>>() {});
            List<String> skills = (List<String>) structured.get("skills");

            if (skills == null || skills.isEmpty()) {
                return new ArrayList<>();
            }

            String prompt = promptTemplate.buildBasicQuestionPrompt(skills);
            String response = aiClientService.chat(prompt);
            return parseQuestions(response);
        } catch (Exception e) {
            log.error("生成基础题失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 生成项目题
     */
    private List<Question> generateProjectQuestions(Resume resume) {
        try {
            Map<String, Object> structured = objectMapper.readValue(resume.getStructuredContent(),
                    new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> projects = (List<Map<String, Object>>) structured.get("projects");

            if (projects == null || projects.isEmpty()) {
                return new ArrayList<>();
            }

            String prompt = promptTemplate.buildProjectQuestionPrompt(objectMapper.writeValueAsString(projects));
            String response = aiClientService.chat(prompt);
            return parseQuestions(response);
        } catch (Exception e) {
            log.error("生成项目题失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 生成场景题
     */
    private List<Question> generateScenarioQuestions(JobData job) {
        try {
            String prompt = promptTemplate.buildScenarioQuestionPrompt(job.getRequirements());
            String response = aiClientService.chat(prompt);
            return parseQuestions(response);
        } catch (Exception e) {
            log.error("生成场景题失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 解析AI返回的题目
     */
    private List<Question> parseQuestions(String response) {
        try {
            // 提取JSON部分
            String json = extractJson(response);
            List<Map<String, Object>> items = objectMapper.readValue(json,
                    new TypeReference<List<Map<String, Object>>>() {});

            List<Question> questions = new ArrayList<>();
            for (Map<String, Object> item : items) {
                Question question = new Question();
                question.setContent((String) item.get("content"));
                question.setType(((Number) item.get("type")).intValue());
                question.setDifficulty(((Number) item.get("difficulty")).intValue());
                question.setSkillTag((String) item.get("skillTag"));
                question.setExpectedPoints(objectMapper.writeValueAsString(item.get("expectedPoints")));
                questions.add(question);
            }
            return questions;
        } catch (Exception e) {
            log.error("解析题目失败，response: {}", response, e);
            return new ArrayList<>();
        }
    }

    /**
     * 从响应中提取JSON
     */
    private String extractJson(String response) {
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }
}
