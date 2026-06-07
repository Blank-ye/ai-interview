package com.interview.service.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Agent工具执行器
 * 让AI可以调用外部工具获取信息
 */
@Slf4j
@Service
public class AgentToolExecutor {

    /**
     * 执行工具调用
     */
    public String executeTool(String toolName, Map<String, String> params) {
        log.info("执行工具：{}，参数：{}", toolName, params);

        return switch (toolName) {
            case "search_tech_doc" -> searchTechDoc(params.get("keyword"));
            case "get_job_requirements" -> getJobRequirements(params.get("jobId"));
            case "analyze_code" -> analyzeCode(params.get("code"));
            default -> "未知工具：" + toolName;
        };
    }

    /**
     * 搜索技术文档
     */
    private String searchTechDoc(String keyword) {
        // 实际可以调用外部API
        log.info("搜索技术文档：{}", keyword);
        return "技术文档搜索结果：" + keyword;
    }

    /**
     * 获取职位要求
     */
    private String getJobRequirements(String jobId) {
        // 从数据库获取
        log.info("获取职位要求：jobId={}", jobId);
        return "职位要求";
    }

    /**
     * 分析代码
     */
    private String analyzeCode(String code) {
        // 调用AI分析代码
        log.info("分析代码片段");
        return "代码分析结果";
    }

    /**
     * 获取可用工具列表（供AI参考）
     */
    public String getAvailableToolsDescription() {
        return """
                可用工具：
                1. search_tech_doc(keyword) - 搜索技术文档
                2. get_job_requirements(jobId) - 获取职位要求
                3. analyze_code(code) - 分析代码片段
                """;
    }
}
