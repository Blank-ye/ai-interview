# 智能面试平台 - 设计方案

## 一、项目概述

### 1.1 项目简介

智能面试平台是一个基于AI的在线面试系统，通过解析简历、匹配职位、生成题目、实时追问、智能评分等能力，为企业和求职者提供智能化的面试体验。

### 1.2 核心功能

| 功能模块 | 说明 |
|----------|------|
| 简历解析 | 上传PDF/Word简历，AI提取结构化信息 |
| 职位匹配 | 根据简历内容，语义匹配最合适的职位 |
| 题目生成 | 结合简历和职位，AI生成个性化面试题 |
| 实时追问 | 根据回答质量，AI决定是否追问及追问内容 |
| 智能评分 | 多维度评估回答质量，生成面试报告 |

### 1.3 项目亮点（简历）

- 基于Spring AI Alibaba集成通义千问大模型，实现智能题目生成和实时追问
- 使用Qdrant向量数据库存储简历向量，实现语义匹配
- WebSocket实现实时面试交互，支持多轮对话
- 设计Prompt工程模板，优化AI生成质量

---

## 二、技术栈

### 2.1 后端技术

| 模块 | 技术 | 说明 |
|------|------|------|
| 核心框架 | Spring Boot 3 | 主流Java框架 |
| AI集成 | Spring AI Alibaba | 对接通义千问大模型 |
| 实时通信 | WebSocket | 面试实时交互 |
| 简历解析 | Apache POI + PDFBox | 解析Word/PDF |
| 消息队列 | RabbitMQ | 异步处理简历解析 |
| 数据库 | MySQL | 业务数据存储 |
| 缓存 | Redis | 热点数据缓存 |
| 向量数据库 | Qdrant | 简历/职位向量存储 |
| 认证 | Spring Security + JWT | 用户认证授权 |

### 2.2 前端技术

| 模块 | 技术 | 说明 |
|------|------|------|
| 框架 | Vue 3 | 主流前端框架 |
| UI库 | Element Plus | 组件库 |
| 状态管理 | Pinia | Vue 3状态管理 |
| HTTP客户端 | Axios | 接口调用 |
| WebSocket | 原生API | 实时通信 |

### 2.3 部署技术

| 模块 | 技术 | 说明 |
|------|------|------|
| 容器化 | Docker | 应用打包 |
| 编排 | Docker Compose | 多服务管理 |
| 反向代理 | Nginx | 负载均衡 |

---

## 三、项目结构

```
ai-interview/
├── ai-interview-api/                    # 接口层
│   └── src/main/java/com/interview/api/
│       ├── controller/
│       │   ├── AuthController.java      # 认证接口
│       │   ├── ResumeController.java    # 简历管理
│       │   ├── InterviewController.java # 面试管理
│       │   └── ReportController.java    # 报告查询
│       ├── dto/
│       │   ├── request/                 # 请求DTO
│       │   └── response/                # 响应DTO
│       └── config/
│           ├── WebMvcConfig.java
│           └── WebSocketConfig.java
│
├── ai-interview-service/                # 业务层
│   └── src/main/java/com/interview/service/
│       ├── resume/
│       │   ├── ResumeParseService.java      # 简历解析
│       │   ├── ResumeVectorService.java     # 向量化存储
│       │   └── impl/
│       ├── interview/
│       │   ├── InterviewEngine.java         # 面试引擎核心
│       │   ├── QuestionGenerator.java       # 题目生成
│       │   ├── FollowUpJudge.java           # 追问判断
│       │   ├── ScoringService.java          # 评分服务
│       │   └── impl/
│       ├── match/
│       │   ├── JobMatchService.java         # 职位匹配
│       │   └── impl/
│       └── ai/
│           ├── AiClientService.java         # AI调用封装
│           └── PromptTemplate.java          # Prompt模板管理
│
├── ai-interview-dao/                    # 数据层
│   └── src/main/java/com/interview/dao/
│       ├── mapper/
│       │   ├── UserMapper.java
│       │   ├── ResumeMapper.java
│       │   ├── InterviewMapper.java
│       │   └── QuestionMapper.java
│       ├── entity/
│       │   ├── User.java
│       │   ├── Resume.java
│       │   ├── Interview.java
│       │   ├── Question.java
│       │   └── Answer.java
│       └── repository/
│           └── VectorRepository.java    # 向量数据库操作
│
├── ai-interview-common/                 # 公共模块
│   └── src/main/java/com/interview/common/
│       ├── exception/
│       │   ├── BusinessException.java
│       │   └── GlobalExceptionHandler.java
│       ├── result/
│       │   ├── Result.java
│       │   └── ResultCode.java
│       ├── util/
│       │   ├── JwtUtil.java
│       │   └── FileUtil.java
│       └── constant/
│           └── InterviewConstant.java
│
├── ai-interview-web/                    # WebSocket模块
│   └── src/main/java/com/interview/web/
│       ├── InterviewWebSocket.java      # WebSocket处理器
│       ├── SessionManager.java          # 会话管理
│       └── MessageDispatcher.java       # 消息分发
│
└── pom.xml
```

---

## 四、数据库设计

### 4.1 用户表

```sql
CREATE TABLE user (
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
```

### 4.2 简历表

```sql
CREATE TABLE resume (
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
```

### 4.3 职位表

```sql
CREATE TABLE job (
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
```

### 4.4 面试记录表

```sql
CREATE TABLE interview (
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
```

### 4.5 题目表

```sql
CREATE TABLE question (
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
```

### 4.6 回答表

```sql
CREATE TABLE answer (
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
```

---

## 五、核心流程设计

### 5.1 简历解析流程

```
用户上传简历
     ↓
存储文件到本地/OSS
     ↓
发送消息到RabbitMQ
     ↓
异步消费消息
     ↓
┌────────────────────────────────────────────┐
│  1. 读取文件（PDF用PDFBox，Word用POI）      │
│  2. 提取纯文本                              │
│  3. 调用通义千问进行结构化解析              │
│     - 基本信息（姓名、手机、邮箱）          │
│     - 教育经历                              │
│     - 工作经历                              │
│     - 项目经历                              │
│     - 技能清单                              │
│  4. 使用BGE模型将简历内容向量化             │
│  5. 存储向量到Qdrant                        │
│  6. 更新MySQL简历状态为已解析               │
└────────────────────────────────────────────┘
```

### 5.2 职位匹配流程

```
用户请求匹配职位
     ↓
获取用户简历向量
     ↓
从Qdrant检索相似职位向量（Top N）
     ↓
对候选职位进行重排序
     ↓
返回匹配度最高的职位列表
```

### 5.3 面试流程

```
┌─────────────────────────────────────────────────────┐
│                    面试引擎核心流程                   │
├─────────────────────────────────────────────────────┤
│                                                     │
│  1. 初始化阶段                                      │
│     ├── 加载简历结构化数据                          │
│     ├── 加载职位要求                                │
│     ├── 语义匹配，找出匹配点和差距                  │
│     └── 生成面试大纲（基础→项目→场景）              │
│                                                     │
│  2. 题目生成阶段                                    │
│     ├── 基础题：根据技能清单生成                    │
│     ├── 项目题：根据项目经历生成                    │
│     ├── 场景题：根据职位要求生成                    │
│     └── 按难度梯度排列                              │
│                                                     │
│  3. 面试交互阶段（WebSocket）                       │
│     ├── 发送当前题目                                │
│     ├── 接收用户回答                                │
│     ├── 调用AI实时评估回答质量                      │
│     ├── AI决定是否追问                              │
│     │   ├── 回答不完整 → 追问细节                   │
│     │   ├── 回答太浅 → 追问深度                     │
│     │   └── 回答良好 → 进入下一题                   │
│     └── 记录所有问答到数据库                        │
│                                                     │
│  4. 评分阶段                                        │
│     ├── 单题评分（准确性、完整性、深度）            │
│     ├── 维度评分（基础知识、项目经验、解决问题能力）│
│     └── 生成综合评价和面试报告                      │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### 5.4 追问判断流程

```
用户提交回答
     ↓
AI评估回答质量
     ↓
┌─────────────────────────────────┐
│  判断是否需要追问               │
│  - 分数 < 60分 → 追问           │
│  - 完整性 < 50% → 追问          │
│  - 深度 < 30% → 追问            │
│  - 否则 → 进入下一题            │
└─────────────────────────────────┘
     ↓
生成追问内容
     ↓
发送追问给用户
```

---

## 六、核心类设计

### 6.1 面试引擎

```java
@Service
public class InterviewEngine {

    @Autowired
    private QuestionGenerator questionGenerator;
    @Autowired
    private FollowUpJudge followUpJudge;
    @Autowired
    private ScoringService scoringService;
    @Autowired
    private AiClientService aiClientService;

    /**
     * 初始化面试
     */
    public InterviewContext initInterview(Long userId, Long resumeId, Long jobId) {
        // 1. 加载简历和职位信息
        Resume resume = resumeService.getStructuredResume(resumeId);
        Job job = jobService.getJob(jobId);

        // 2. 分析匹配度
        MatchResult match = matchService.analyzeMatch(resume, job);

        // 3. 生成题目列表
        List<Question> questions = questionGenerator.generate(resume, job, match);

        // 4. 构建面试上下文
        return InterviewContext.builder()
            .resume(resume)
            .job(job)
            .match(match)
            .questions(questions)
            .currentQuestionIndex(0)
            .build();
    }

    /**
     * 处理用户回答
     */
    public InterviewMessage handleAnswer(InterviewContext context, String answer) {
        Question currentQuestion = context.getCurrentQuestion();

        // 1. 评估回答
        AnswerEvaluation eval = scoringService.evaluate(currentQuestion, answer);

        // 2. 决定是否追问
        if (followUpJudge.shouldFollowUp(eval)) {
            String followUp = followUpJudge.generateFollowUp(currentQuestion, answer, eval);
            return InterviewMessage.followUp(followUp);
        }

        // 3. 记录答案
        answerService.saveAnswer(currentQuestion.getId(), answer, eval);

        // 4. 进入下一题或结束
        if (context.hasMoreQuestions()) {
            context.nextQuestion();
            return InterviewMessage.nextQuestion(context.getCurrentQuestion());
        } else {
            return InterviewMessage.finish(generateReport(context));
        }
    }
}
```

### 6.2 题目生成器

```java
@Service
public class QuestionGenerator {

    @Autowired
    private AiClientService aiClientService;
    @Autowired
    private PromptTemplate promptTemplate;

    /**
     * 生成面试题目
     */
    public List<Question> generate(Resume resume, Job job, MatchResult match) {
        List<Question> questions = new ArrayList<>();

        // 1. 基础题：根据简历中的技能点
        questions.addAll(generateBasicQuestions(resume.getSkills()));

        // 2. 项目题：根据简历中的项目经历
        questions.addAll(generateProjectQuestions(resume.getProjects()));

        // 3. 场景题：根据职位要求
        questions.addAll(generateScenarioQuestions(job.getRequirements()));

        // 4. 补充题：针对匹配薄弱点
        questions.addAll(generateGapQuestions(match.getGaps()));

        return questions;
    }

    /**
     * 生成基础题
     */
    private List<Question> generateBasicQuestions(List<String> skills) {
        String prompt = promptTemplate.buildBasicQuestionPrompt(skills);
        String response = aiClientService.chat(prompt);
        return parseQuestions(response, QuestionType.BASIC);
    }
}
```

### 6.3 追问判断器

```java
@Service
public class FollowUpJudge {

    @Autowired
    private AiClientService aiClientService;
    @Autowired
    private PromptTemplate promptTemplate;

    /**
     * 判断是否需要追问
     */
    public boolean shouldFollowUp(AnswerEvaluation eval) {
        if (eval.getScore() < 60) {
            return true;
        }
        if (eval.getCompleteness() < 0.5) {
            return true;
        }
        if (eval.getDepth() < 0.3) {
            return true;
        }
        return false;
    }

    /**
     * 生成追问内容
     */
    public String generateFollowUp(Question question, String answer, AnswerEvaluation eval) {
        String prompt = promptTemplate.buildFollowUpPrompt(question, answer, eval);
        return aiClientService.chat(prompt);
    }
}
```

### 6.4 Prompt模板管理

```java
@Service
public class PromptTemplate {

    /**
     * 简历解析Prompt
     */
    public String buildResumeParsePrompt(String rawText) {
        return """
            请解析以下简历内容，提取结构化信息：

            简历内容：
            %s

            请按以下JSON格式输出：
            {
                "name": "姓名",
                "phone": "手机号",
                "email": "邮箱",
                "education": [
                    {
                        "school": "学校",
                        "major": "专业",
                        "degree": "学历",
                        "start_date": "开始时间",
                        "end_date": "结束时间"
                    }
                ],
                "experience": [
                    {
                        "company": "公司",
                        "position": "职位",
                        "start_date": "开始时间",
                        "end_date": "结束时间",
                        "description": "工作描述"
                    }
                ],
                "projects": [
                    {
                        "name": "项目名",
                        "role": "角色",
                        "description": "项目描述",
                        "tech_stack": ["技术栈"],
                        "responsibilities": ["职责"]
                    }
                ],
                "skills": ["技能1", "技能2"]
            }
            """.formatted(rawText);
    }

    /**
     * 题目生成Prompt
     */
    public String buildBasicQuestionPrompt(List<String> skills) {
        return """
            你是一位资深技术面试官，请根据以下技能点生成面试题：

            技能点：%s

            要求：
            1. 每个技能点生成1-2道题
            2. 题目难度分为：基础、进阶、高级
            3. 题目要具体，避免过于宽泛
            4. 输出JSON数组格式

            示例输出：
            [
                {
                    "content": "请解释Spring IOC的原理",
                    "type": "BASIC",
                    "difficulty": 1,
                    "skill": "Spring",
                    "expected_points": ["控制反转", "依赖注入", "Bean生命周期"]
                }
            ]
            """.formatted(String.join(", ", skills));
    }

    /**
     * 追问生成Prompt
     */
    public String buildFollowUpPrompt(Question question, String answer, AnswerEvaluation eval) {
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
            """.formatted(question.getContent(), answer, eval.getScore(),
                         eval.getCompleteness() * 100, eval.getDepth() * 100);
    }
}
```

### 6.5 AI调用封装

```java
@Service
public class AiClientService {

    @Autowired
    private ChatClient chatClient;

    /**
     * 单轮对话
     */
    public String chat(String prompt) {
        return chatClient.prompt(prompt).call().content();
    }

    /**
     * 多轮对话
     */
    public String chat(List<Message> messages) {
        return chatClient.prompt(messages).call().content();
    }

    /**
     * 流式对话（用于实时输出）
     */
    public Flux<String> chatStream(String prompt) {
        return chatClient.prompt(prompt).stream().content();
    }
}
```

---

## 七、WebSocket消息协议

### 7.1 消息类型

```java
public enum MessageType {
    // 客户端 -> 服务端
    START_INTERVIEW,    // 开始面试
    ANSWER,            // 提交答案
    NEXT_QUESTION,     // 请求下一题
    END_INTERVIEW,     // 结束面试

    // 服务端 -> 客户端
    QUESTION,          // 发送题目
    FOLLOW_UP,         // 追问
    EVALUATION,        // 评价
    REPORT,            // 面试报告
    ERROR              // 错误
}
```

### 7.2 消息结构

```java
@Data
public class WebSocketMessage {
    private MessageType type;
    private Long interviewId;
    private Object data;
    private Long timestamp;
}
```

### 7.3 消息示例

```json
// 开始面试
{
    "type": "START_INTERVIEW",
    "interviewId": 123456,
    "data": {
        "resumeId": 1,
        "jobId": 10
    },
    "timestamp": 1717584000000
}

// 发送题目
{
    "type": "QUESTION",
    "interviewId": 123456,
    "data": {
        "questionId": 1,
        "content": "请解释Spring IOC的原理",
        "type": "BASIC",
        "difficulty": 1
    },
    "timestamp": 1717584010000
}

// 提交答案
{
    "type": "ANSWER",
    "interviewId": 123456,
    "data": {
        "questionId": 1,
        "content": "Spring IOC是控制反转..."
    },
    "timestamp": 1717584060000
}

// 追问
{
    "type": "FOLLOW_UP",
    "interviewId": 123456,
    "data": {
        "questionId": 1,
        "content": "你提到了控制反转，能具体说说依赖注入的几种方式吗？"
    },
    "timestamp": 1717584070000
}
```

---

## 八、API设计

### 8.1 认证接口

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody RegisterRequest request);

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginRequest request);

    @PostMapping("/logout")
    public Result<Void> logout();
}
```

### 8.2 简历接口

```java
@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @PostMapping("/upload")
    public Result<ResumeVO> uploadResume(@RequestParam MultipartFile file);

    @GetMapping("/{id}/status")
    public Result<ResumeStatusVO> getStatus(@PathVariable Long id);

    @GetMapping("/{id}/structured")
    public Result<StructuredResumeVO> getStructuredResume(@PathVariable Long id);

    @GetMapping("/list")
    public Result<List<ResumeVO>> getResumeList();
}
```

### 8.3 面试接口

```java
@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    @PostMapping("/create")
    public Result<InterviewVO> createInterview(@RequestBody CreateInterviewRequest request);

    @GetMapping("/{id}")
    public Result<InterviewVO> getInterview(@PathVariable Long id);

    @GetMapping("/{id}/current-question")
    public Result<QuestionVO> getCurrentQuestion(@PathVariable Long id);

    @GetMapping("/{id}/report")
    public Result<ReportVO> getReport(@PathVariable Long id);

    @GetMapping("/list")
    public Result<List<InterviewVO>> getInterviewList();
}
```

### 8.4 职位接口

```java
@RestController
@RequestMapping("/api/job")
public class JobController {

    @GetMapping("/list")
    public Result<List<JobVO>> getJobList();

    @GetMapping("/{id}")
    public Result<JobVO> getJob(@PathVariable Long id);

    @GetMapping("/match")
    public Result<List<JobMatchVO>> matchJobs(@RequestParam Long resumeId);
}
```

---

## 九、配置文件

### 9.1 application.yml

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_interview?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

  data:
    redis:
      host: localhost
      port: 6379
      password: ${REDIS_PASSWORD}

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: ${RABBITMQ_PASSWORD}

  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
      chat:
        model: qwen-turbo

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.interview.dao.entity
  configuration:
    map-underscore-to-camel-case: true

# Qdrant配置
qdrant:
  host: localhost
  port: 6333
  collection-name: resumes

# JWT配置
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
```

---

## 十、开发计划

| 阶段 | 内容 | 时间 | 产出 |
|------|------|------|------|
| 第一阶段 | 项目搭建、数据库设计、用户认证 | 2天 | 可注册登录 |
| 第二阶段 | 简历上传、解析、向量化 | 3天 | 简历可解析 |
| 第三阶段 | 职位管理、匹配算法 | 2天 | 职位可匹配 |
| 第四阶段 | 面试引擎、题目生成 | 3天 | 可生成题目 |
| 第五阶段 | WebSocket实时交互 | 3天 | 可进行面试 |
| 第六阶段 | 评分系统、面试报告 | 2天 | 可生成报告 |
| 第七阶段 | 前端页面、联调测试 | 5天 | 完整可用 |
| 第八阶段 | 优化、部署 | 2天 | 可部署运行 |

**总计**：约3-4周

---

## 十一、简历写法

### 项目经历

**智能面试平台** | 独立开发 | 2026.xx - 2026.xx

基于Spring AI Alibaba的智能面试系统，实现简历解析、职位匹配、智能出题、实时追问、自动评分等功能。

**技术栈**：Spring Boot 3、Spring AI Alibaba、Qdrant、WebSocket、MySQL、Redis、RabbitMQ、Vue 3

**项目亮点**：
- 基于Spring AI Alibaba集成通义千问大模型，设计Prompt工程模板，实现高质量题目生成和智能追问
- 使用Qdrant向量数据库存储简历向量，通过语义匹配算法实现精准职位推荐
- WebSocket实现面试实时交互，支持多轮对话和状态管理
- 设计多维度评分体系，从准确性、完整性、深度等维度评估回答质量

**个人职责**：
- 负责整体架构设计和核心模块开发
- 实现简历解析、向量化、匹配算法
- 设计面试引擎和Prompt模板
- 开发WebSocket实时交互模块
