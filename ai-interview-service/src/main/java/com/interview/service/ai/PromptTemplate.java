package com.interview.service.ai;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Prompt模板管理
 * 集中管理所有AI交互的Prompt，便于调优和维护
 * 包括：简历解析、题目生成、回答评估、追问生成、报告生成
 */
@Component
public class PromptTemplate {

    /**
     * 简历解析Prompt
     */
    public String buildResumeParsePrompt(String rawText) {
        return """
                请解析以下简历内容，提取结构化信息：

                简历内容：
                %s

                请严格按以下JSON格式输出，不要添加其他文字：
                {
                    "name": "姓名",
                    "phone": "手机号",
                    "email": "邮箱",
                    "education": [
                        {
                            "school": "学校",
                            "major": "专业",
                            "degree": "学历",
                            "startDate": "开始时间",
                            "endDate": "结束时间"
                        }
                    ],
                    "experience": [
                        {
                            "company": "公司",
                            "position": "职位",
                            "startDate": "开始时间",
                            "endDate": "结束时间",
                            "description": "工作描述"
                        }
                    ],
                    "projects": [
                        {
                            "name": "项目名",
                            "role": "角色",
                            "description": "项目描述",
                            "techStack": ["技术栈"],
                            "responsibilities": ["职责"]
                        }
                    ],
                    "skills": ["技能1", "技能2"]
                }
                """.formatted(rawText);
    }

    /**
     * 基础题生成Prompt
     */
    public String buildBasicQuestionPrompt(List<String> skills) {
        return """
                你是一位资深技术面试官，请根据以下技能点生成面试题。

                技能点：%s

                要求：
                1. 每个技能点生成1-2道题
                2. 题目难度分为：1-简单、2-中等、3-困难
                3. 题目要具体，避免过于宽泛
                4. 输出JSON数组格式，不要添加其他文字

                示例输出：
                [
                    {
                        "content": "请解释Spring IOC的原理",
                        "type": 1,
                        "difficulty": 1,
                        "skillTag": "Spring",
                        "expectedPoints": ["控制反转", "依赖注入", "Bean生命周期"]
                    }
                ]
                """.formatted(String.join(", ", skills));
    }

    /**
     * 项目题生成Prompt
     */
    public String buildProjectQuestionPrompt(String projectJson) {
        return """
                你是一位资深技术面试官，请根据以下项目经历生成面试题。

                项目信息：
                %s

                要求：
                1. 针对项目的技术选型、架构设计、难点解决等方面提问
                2. 生成3-5道题
                3. 输出JSON数组格式，不要添加其他文字

                输出格式：
                [
                    {
                        "content": "问题内容",
                        "type": 2,
                        "difficulty": 2,
                        "skillTag": "相关技术",
                        "expectedPoints": ["关键点1", "关键点2"]
                    }
                ]
                """.formatted(projectJson);
    }

    /**
     * 场景题生成Prompt
     */
    public String buildScenarioQuestionPrompt(String requirements) {
        return """
                你是一位资深技术面试官，请根据以下职位要求生成场景面试题。

                职位要求：
                %s

                要求：
                1. 生成实际工作场景中可能遇到的问题
                2. 生成3-5道题
                3. 输出JSON数组格式，不要添加其他文字

                输出格式：
                [
                    {
                        "content": "场景描述和问题",
                        "type": 3,
                        "difficulty": 2,
                        "skillTag": "相关技术",
                        "expectedPoints": ["解决思路", "关键技术", "注意事项"]
                    }
                ]
                """.formatted(requirements);
    }

    /**
     * 回答评估Prompt
     */
    public String buildEvaluatePrompt(String question, List<String> expectedPoints, String answer) {
        return """
                你是一位资深技术面试官，请评估候选人的回答。

                问题：%s

                预期关键点：%s

                候选人回答：%s

                请按以下JSON格式输出评估结果，不要添加其他文字：
                {
                    "score": 85,
                    "completeness": 0.8,
                    "depth": 0.7,
                    "evaluation": "评价内容",
                    "keyPointsHit": ["命中的关键点"],
                    "keyPointsMiss": ["遗漏的关键点"]
                }
                """.formatted(question, String.join(", ", expectedPoints), answer);
    }

    /**
     * 追问生成Prompt
     */
    public String buildFollowUpPrompt(String question, String answer, double score, double completeness, double depth) {
        return """
                你是一位资深技术面试官，候选人回答了以下问题，但回答不够完整或深入，请生成追问。

                原问题：%s

                候选人回答：%s

                评估结果：得分%.1f，完整性%.1f%%，深度%.1f%%

                请生成一个追问，要求：
                1. 针对回答中的薄弱点
                2. 引导候选人深入思考
                3. 语气友好专业

                只输出追问内容，不要其他文字。
                """.formatted(question, answer, score, completeness * 100, depth * 100);
    }

    /**
     * 面试报告生成Prompt
     */
    public String buildReportPrompt(String interviewData) {
        return """
                你是一位资深技术面试官，请根据以下面试数据生成面试报告。

                面试数据：
                %s

                请按以下JSON格式输出报告，不要添加其他文字：
                {
                    "totalScore": 85,
                    "dimensionScores": {
                        "基础知识": 80,
                        "项目经验": 85,
                        "解决问题能力": 90,
                        "沟通表达": 85
                    },
                    "summary": "整体评价",
                    "strengths": ["优势1", "优势2"],
                    "weaknesses": ["不足1", "不足2"],
                    "suggestion": "改进建议"
                }
                """.formatted(interviewData);
    }
}
