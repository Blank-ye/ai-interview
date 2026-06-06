# 智能面试平台

基于Spring AI Alibaba的智能面试系统，实现简历解析、职位匹配、智能出题、实时追问、自动评分等功能。

## 技术栈

### 后端
- Spring Boot 3
- Spring AI Alibaba (通义千问)
- WebSocket
- MySQL
- Redis
- RabbitMQ
- Qdrant (向量数据库)

### 前端
- Vue 3
- Element Plus

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.8+
- Docker (可选)

### 2. 启动基础服务

```bash
docker-compose up -d
```

### 3. 配置环境变量

```bash
export DASHSCOPE_API_KEY=your_api_key
```

### 4. 编译运行

```bash
mvn clean package -DskipTests
java -jar ai-interview-api/target/ai-interview-api-1.0.0.jar
```

### 5. 访问服务

- API: http://localhost:8080
- RabbitMQ管理: http://localhost:15672
- Qdrant管理: http://localhost:6333/dashboard

## API接口

### 认证
- POST /api/auth/register - 注册
- POST /api/auth/login - 登录

### 简历
- POST /api/resume/upload - 上传简历
- GET /api/resume/{id} - 获取简历
- GET /api/resume/list - 简历列表

### 职位
- GET /api/job/list - 职位列表
- GET /api/job/{id} - 获取职位
- GET /api/job/match - 匹配职位

### 面试
- WebSocket /ws/interview - 面试连接
- GET /api/interview/{id} - 获取面试
- GET /api/interview/{id}/report - 获取报告
- GET /api/interview/list - 面试列表

## 项目结构

```
ai-interview/
├── ai-interview-api        # 接口层
├── ai-interview-service    # 业务层
├── ai-interview-dao        # 数据层
├── ai-interview-common     # 公共模块
├── ai-interview-web        # WebSocket模块
├── sql                     # 数据库脚本
└── docker-compose.yml      # Docker配置
```

## 开发计划

- [x] 项目搭建
- [x] 用户认证
- [x] 简历上传解析
- [x] 职位管理
- [x] 职位匹配
- [x] 面试引擎
- [x] 题目生成
- [x] 实时交互
- [x] 评分系统
- [ ] 前端页面
