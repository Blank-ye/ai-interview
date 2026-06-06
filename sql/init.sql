CREATE DATABASE IF NOT EXISTS ai_interview DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_interview;

-- 用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(500),
    role TINYINT DEFAULT 0 COMMENT '0-求职者 1-面试官 2-管理员',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 简历表
CREATE TABLE IF NOT EXISTS resume (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(200),
    file_path VARCHAR(500),
    raw_content TEXT COMMENT '解析后的原始文本',
    structured_content JSON COMMENT '结构化内容',
    vector_id VARCHAR(100) COMMENT '向量数据库中的ID',
    status TINYINT DEFAULT 0 COMMENT '0-待解析 1-已解析 2-解析失败',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 职位表
CREATE TABLE IF NOT EXISTS job (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    requirements TEXT,
    skills JSON COMMENT '技能要求',
    company VARCHAR(100),
    salary_range VARCHAR(50),
    location VARCHAR(100),
    vector_id VARCHAR(100) COMMENT '向量ID，用于匹配',
    status TINYINT DEFAULT 1 COMMENT '0-关闭 1-开放',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 面试记录表
CREATE TABLE IF NOT EXISTS interview (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0-进行中 1-已完成 2-已取消',
    total_score DECIMAL(5,2) COMMENT '总分',
    dimension_scores JSON COMMENT '各维度分数',
    start_time DATETIME,
    end_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 题目表
CREATE TABLE IF NOT EXISTS question (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    interview_id BIGINT NOT NULL,
    type TINYINT NOT NULL COMMENT '1-基础题 2-项目题 3-场景题 4-追问',
    content TEXT NOT NULL,
    difficulty TINYINT DEFAULT 1 COMMENT '1-简单 2-中等 3-困难',
    expected_points JSON COMMENT '预期关键点',
    skill_tag VARCHAR(50) COMMENT '技能标签',
    order_num INT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_interview_id (interview_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 回答表
CREATE TABLE IF NOT EXISTS answer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    interview_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    score DECIMAL(5,2),
    evaluation TEXT COMMENT 'AI评价',
    key_points_hit JSON COMMENT '命中关键点',
    key_points_miss JSON COMMENT '遗漏关键点',
    follow_up_count INT DEFAULT 0 COMMENT '追问次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_interview_id (interview_id),
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入测试职位数据
INSERT INTO job (title, description, requirements, skills, company, salary_range, location, status) VALUES
('Java后端开发工程师', '负责公司核心业务系统的开发和维护', '1. 本科及以上学历\n2. 3年以上Java开发经验\n3. 熟悉Spring Boot、MyBatis等主流框架', '["Java", "Spring Boot", "MySQL", "Redis", "MyBatis"]', '科技有限公司', '15-25K', '北京', 1),
('全栈开发工程师', '负责前后端开发，参与产品设计', '1. 本科及以上学历\n2. 熟悉Vue、React等前端框架\n3. 熟悉Node.js或Java', '["JavaScript", "Vue", "React", "Node.js", "Java"]', '互联网公司', '20-35K', '上海', 1),
('AI工程师', '负责AI模型训练和应用开发', '1. 硕士及以上学历\n2. 熟悉Python、PyTorch\n3. 有大模型相关经验优先', '["Python", "PyTorch", "TensorFlow", "NLP", "大模型"]', 'AI科技公司', '30-50K', '深圳', 1);
