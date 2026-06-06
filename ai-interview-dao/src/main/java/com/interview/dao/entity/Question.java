package com.interview.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目实体
 * type: 1-基础题 2-项目题 3-场景题 4-追问
 * difficulty: 1-简单 2-中等 3-困难
 * expectedPoints: JSON数组，AI评估时参考的关键点
 */
@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long interviewId;
    private Integer type;
    private String content;
    private Integer difficulty;
    private String expectedPoints;
    private String skillTag;
    private Integer orderNum;
    private LocalDateTime createTime;
}
