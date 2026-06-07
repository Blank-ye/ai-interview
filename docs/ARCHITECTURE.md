# 智能面试平台 - 项目架构与学习指南

## 一、项目概述

这是一个基于Spring Boot 3的智能面试平台，集成了AI大模型（通义千问）和向量数据库（Qdrant），实现简历解析、职位匹配、智能出题、实时追问、自动评分等功能。

### 技术栈

| 类型 | 技术 | 说明 |
|------|------|------|
| 框架 | Spring Boot 3 | Java后端框架 |
| AI | Spring AI Alibaba | 对接通义千问大模型 |
| 数据库 | MySQL | 业务数据存储 |
| 缓存 | Redis | 热点数据缓存 |
| 向量库 | Qdrant | 语义匹配 |
| 实时通信 | WebSocket | 面试交互 |
| 认证 | JWT | 用户认证 |

---

## 二、项目结构

```
ai-interview/
├── ai-interview-api/           # 接口层（Controller）
│   ├── controller/             # 控制器
│   ├── dto/                    # 数据传输对象
│   ├── config/                 # 配置类
│   └── interceptor/            # 拦截器
│
├── ai-interview-service/       # 业务层（Service）
│   ├── ai/                     # AI调用封装
│   ├── interview/              # 面试引擎
│   ├── resume/                 # 简历服务
│   ├── job/                    # 职位服务
│   └── match/                  # 匹配服务
│
├── ai-interview-dao/           # 数据层（Mapper/Repository）
│   ├── entity/                 # 实体类
│   ├── mapper/                 # MyBatis Mapper
│   └── repository/             # Qdrant操作
│
├── ai-interview-common/        # 公共模块
│   ├── exception/              # 异常处理
│   ├── result/                 # 统一响应
│   ├── util/                   # 工具类
│   └── constant/               # 常量
│
├── ai-interview-web/           # WebSocket模块
│   └── InterviewWebSocket.java # WebSocket处理器
│
└── sql/                        # 数据库脚本
```

---

## 三、分层架构详解

### 3.1 Controller层（接口层）

**职责**：接收HTTP请求，调用Service，返回响应。

**示例**：AuthController.java

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // 注册接口
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody RegisterRequest request) {
        // 1. 接收参数
        // 2. 调用Service
        User user = userService.register(request);
        // 3. 返回结果
        return Result.success(userVO);
    }
}
```

**关键注解**：
- `@RestController`：标记为Controller，返回JSON
- `@RequestMapping("/api/auth")`：URL路径前缀
- `@PostMapping("/register")`：POST请求映射
- `@RequestBody`：接收JSON参数
- `@RequestParam`：接收查询参数

### 3.2 Service层（业务层）

**职责**：处理业务逻辑，调用Mapper操作数据库。

**示例**：ResumeParseService.java

```java
@Service
public class ResumeParseService {

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private AiClientService aiClientService;

    // 解析简历
    public void parseResume(Long resumeId) {
        // 1. 从数据库查询简历
        Resume resume = resumeMapper.selectById(resumeId);

        // 2. 提取文本
        String rawText = extractText(resume.getFilePath());

        // 3. 调用AI解析
        String structuredJson = aiClientService.chat(prompt);

        // 4. 向量化存储
        String vectorId = resumeVectorService.vectorizeAndStore(resumeId, rawText);

        // 5. 更新数据库
        resume.setStatus(1);
        resumeMapper.updateById(resume);
    }
}
```

**关键注解**：
- `@Service`：标记为Service层
- `@Autowired`：自动注入依赖

### 3.3 DAO层（数据层）

**职责**：操作数据库，提供数据访问接口。

**Mapper示例**：UserMapper.java

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper提供了基本的CRUD方法
    // selectById() - 根据ID查询
    // insert() - 插入
    // updateById() - 更新
    // deleteById() - 删除
}
```

**Repository示例**：VectorRepository.java

```java
@Repository
public class VectorRepository {

    private final QdrantClient qdrantClient;

    // 存储向量
    public String storeVector(String collectionName, List<Float> vector, Map payload) {
        // 调用Qdrant API存储向量
    }

    // 搜索相似向量
    public List<ScoredPoint> searchSimilar(String collectionName, List<Float> vector, int limit) {
        // 调用Qdrant API搜索
    }
}
```

### 3.4 Entity层（实体类）

**职责**：对应数据库表的Java对象。

**示例**：User.java

```java
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;           // 主键，自增
    private String username;   // 用户名
    private String password;   // 密码
    private String email;      // 邮箱
    private Integer role;      // 角色
    private LocalDateTime createTime;  // 创建时间
    private LocalDateTime updateTime;  // 更新时间
}
```

**关键注解**：
- `@Data`：Lombok注解，自动生成getter/setter
- `@TableName("user")`：对应数据库表名
- `@TableId(type = IdType.AUTO)`：主键自增

---

## 四、核心流程

### 4.1 用户认证流程

```
客户端                    服务端
  │                        │
  │ POST /api/auth/login   │
  │ {username, password}   │
  │───────────────────────>│
  │                        │
  │                        │ 1. 查询数据库
  │                        │ 2. 验证密码
  │                        │ 3. 生成JWT Token
  │                        │
  │ {token, userId}        │
  │<───────────────────────│
  │                        │
  │ 后续请求携带Token       │
  │ Authorization: Token   │
  │───────────────────────>│
  │                        │
  │                        │ 4. 拦截器验证Token
  │                        │ 5. 解析userId存入ThreadLocal
```

### 4.2 简历解析流程

```
上传简历 → 保存文件 → 异步解析
                      ↓
              ┌──────────────────┐
              │ 1. 提取文本       │
              │    PDF → PDFBox  │
              │    Word → POI    │
              └──────────────────┘
                      ↓
              ┌──────────────────┐
              │ 2. AI结构化解析   │
              │    调用通义千问    │
              │    提取技能/经历   │
              └──────────────────┘
                      ↓
              ┌──────────────────┐
              │ 3. 向量化存储     │
              │    文本 → 向量    │
              │    存入Qdrant     │
              └──────────────────┘
                      ↓
              ┌──────────────────┐
              │ 4. 更新数据库     │
              │    status = 1    │
              └──────────────────┘
```

### 4.3 面试流程

```
创建面试 → 生成题目 → 开始面试
                      ↓
              ┌──────────────────┐
              │ 发送第1题         │
              └──────────────────┘
                      ↓
              ┌──────────────────┐
              │ 用户回答          │
              └──────────────────┘
                      ↓
              ┌──────────────────┐
              │ AI评估回答        │
              │ - 得分            │
              │ - 完整性          │
              │ - 深度            │
              └──────────────────┘
                      ↓
              ┌──────────────────┐
              │ 判断是否追问      │
              │ 得分<60 → 追问    │
              │ 否则 → 下一题     │
              └──────────────────┘
                      ↓
              ┌──────────────────┐
              │ 所有题目完成      │
              │ 生成面试报告      │
              └──────────────────┘
```

---

## 五、关键类说明

### 5.1 配置类

| 类 | 作用 |
|------|------|
| SecurityConfig | Spring Security配置，CORS跨域 |
| WebSocketConfig | WebSocket端点配置 |
| WebMvcConfig | 拦截器注册 |
| QdrantConfig | Qdrant客户端配置 |
| QdrantInitConfig | 启动时初始化集合和向量化 |

### 5.2 拦截器和ThreadLocal

| 类 | 作用 |
|------|------|
| JwtInterceptor | JWT验证，解析Token |
| UserContext | ThreadLocal存储当前用户信息 |

**流程**：
```
请求 → JwtInterceptor → 验证Token → UserContext.setUserId() → Controller → UserContext.getUserId()
```

### 5.3 AI相关

| 类 | 作用 |
|------|------|
| AiClientService | 封装AI调用（单轮/多轮/流式） |
| PromptTemplate | 管理所有Prompt模板 |

### 5.4 面试引擎

| 类 | 作用 |
|------|------|
| InterviewEngine | 面试核心逻辑 |
| QuestionGenerator | 题目生成 |
| FollowUpJudge | 追问判断 |
| ScoringService | 评分服务 |
| InterviewContext | 面试上下文（内存） |

---

## 六、数据库设计

### 6.1 表结构

| 表 | 说明 | 核心字段 |
|------|------|----------|
| user | 用户表 | username, password, role |
| resume | 简历表 | user_id, file_path, raw_content, vector_id |
| job | 职位表 | title, company, requirements, vector_id |
| interview | 面试记录 | user_id, resume_id, job_id, status, total_score |
| question | 题目表 | interview_id, type, content, difficulty |
| answer | 回答表 | question_id, content, score, evaluation |

### 6.2 状态码

**简历状态**：
- 0：待解析
- 1：已解析
- 2：解析失败

**面试状态**：
- 0：进行中
- 1：已完成
- 2：已取消

**题目类型**：
- 1：基础题
- 2：项目题
- 3：场景题
- 4：追问

---

## 七、API接口

### 7.1 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 注册 |
| POST | /api/auth/login | 登录 |

### 7.2 简历接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/resume/upload | 上传简历 |
| GET | /api/resume/{id} | 获取简历 |
| GET | /api/resume/list | 简历列表 |

### 7.3 职位接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/job | 添加职位 |
| PUT | /api/job/{id} | 更新职位 |
| DELETE | /api/job/{id} | 删除职位 |
| GET | /api/job/list | 职位列表 |
| GET | /api/job/{id} | 职位详情 |
| GET | /api/job/match | 匹配职位 |

### 7.4 面试接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/interview/{id} | 获取面试 |
| GET | /api/interview/{id}/report | 获取报告 |
| GET | /api/interview/list | 面试列表 |
| WebSocket | /ws/interview | 面试交互 |

---

## 八、学习建议

### 8.1 阅读顺序

```
1. 实体类（理解数据结构）
   └── User.java, Resume.java, Job.java

2. Mapper（理解数据访问）
   └── UserMapper.java, ResumeMapper.java

3. Controller（理解接口）
   └── AuthController.java

4. Service（理解业务逻辑）
   └── ResumeParseService.java

5. 配置（理解框架配置）
   └── application.yml, SecurityConfig.java
```

### 8.2 重点理解

| 概念 | 说明 | 文件 |
|------|------|------|
| 分层架构 | Controller → Service → Mapper | 整个项目 |
| 依赖注入 | @Autowired自动注入 | Service类 |
| ORM映射 | 实体类对应数据库表 | Entity类 |
| 统一响应 | Result封装返回结果 | Result.java |
| 异常处理 | GlobalExceptionHandler | GlobalExceptionHandler.java |
| 拦截器 | 请求预处理 | JwtInterceptor.java |
| ThreadLocal | 线程级变量存储 | UserContext.java |

### 8.3 常见问题

**Q: 为什么分层？**
A: 职责分离，便于维护和测试。

**Q: @Autowired是什么？**
A: Spring自动注入依赖对象。

**Q: Mapper接口没有实现类，怎么工作的？**
A: MyBatis-Plus自动生成实现类。

**Q: ThreadLocal的作用？**
A: 在同一线程内共享数据，如当前用户ID。

---

## 九、项目亮点（简历写法）

### 技术亮点

1. **AI集成**：Spring AI Alibaba对接通义千问，实现智能题目生成和实时追问
2. **向量匹配**：Qdrant存储简历/职位向量，实现语义匹配
3. **实时交互**：WebSocket实现面试实时对话
4. **Prompt工程**：设计多场景Prompt模板，优化AI生成质量

### 架构亮点

1. **分层清晰**：Controller-Service-Mapper标准分层
2. **统一异常**：GlobalExceptionHandler统一处理
3. **JWT认证**：拦截器+ThreadLocal实现无状态认证
4. **异步处理**：简历解析异步执行，不阻塞响应
