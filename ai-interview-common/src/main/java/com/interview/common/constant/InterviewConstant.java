package com.interview.common.constant;

/**
 * 常量定义
 * 集中管理状态码、类型码等常量，避免魔法数字
 */
public class InterviewConstant {
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    // 面试状态
    public static final int INTERVIEW_STATUS_ONGOING = 0;
    public static final int INTERVIEW_STATUS_COMPLETED = 1;
    public static final int INTERVIEW_STATUS_CANCELLED = 2;

    // 简历状态
    public static final int RESUME_STATUS_PENDING = 0;
    public static final int RESUME_STATUS_PARSED = 1;
    public static final int RESUME_STATUS_FAILED = 2;

    // 题目类型
    public static final int QUESTION_TYPE_BASIC = 1;
    public static final int QUESTION_TYPE_PROJECT = 2;
    public static final int QUESTION_TYPE_SCENARIO = 3;
    public static final int QUESTION_TYPE_FOLLOW_UP = 4;

    // 题目难度
    public static final int DIFFICULTY_EASY = 1;
    public static final int DIFFICULTY_MEDIUM = 2;
    public static final int DIFFICULTY_HARD = 3;
}
