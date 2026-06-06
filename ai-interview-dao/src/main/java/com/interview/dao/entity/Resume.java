package com.interview.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历实体
 * status: 0-待解析 1-已解析 2-解析失败
 * structuredContent: AI解析后的JSON，包含教育、工作、项目、技能
 * vectorId: Qdrant中的向量ID，用于语义匹配
 */
@Data
@TableName("resume")
public class Resume {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String fileName;
    private String filePath;
    private String rawContent;
    private String structuredContent;
    private String vectorId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
