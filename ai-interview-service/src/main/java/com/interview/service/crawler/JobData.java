package com.interview.service.crawler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 职位数据（RAG文档结构）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobData {
    private String jobId;           // 职位唯一标识
    private String title;           // 职位标题
    private String company;         // 公司名称
    private String salary;          // 薪资范围
    private String location;        // 工作地点
    private String description;     // 职位描述
    private String requirements;    // 职位要求
    private List<String> skills;    // 技能要求
    private LocalDateTime crawlTime; // 爬取时间
}
