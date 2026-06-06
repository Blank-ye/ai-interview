package com.interview.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 回答实体
 * score: AI评分（0-100）
 * keyPointsHit/Miss: JSON数组，命中/遗漏的关键点
 * followUpCount: 追问次数，最多2次
 */
@Data
@TableName("answer")
public class Answer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private Long interviewId;
    private String content;
    private BigDecimal score;
    private String evaluation;
    private String keyPointsHit;
    private String keyPointsMiss;
    private Integer followUpCount;
    private LocalDateTime createTime;
}
