package com.interview.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职位实体
 * status: 0-关闭 1-开放
 * skills: JSON数组，如["Java", "Spring Boot", "MySQL"]
 * vectorId: Qdrant中的向量ID，用于语义匹配
 */
@Data
@TableName("job")
public class Job {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String skills;
    private String company;
    private String salaryRange;
    private String location;
    private String vectorId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
