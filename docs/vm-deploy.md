# 虚拟机部署指南

## 一、虚拟机环境准备

### 1. 安装Docker

```bash
# Ubuntu/Debian
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER

# CentOS
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install -y docker-ce docker-ce-cli containerd.io
sudo systemctl start docker
sudo systemctl enable docker
```

### 2. 安装Docker Compose

```bash
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 3. 开放端口

```bash
# 防火墙开放端口
sudo firewall-cmd --permanent --add-port=3306/tcp
sudo firewall-cmd --permanent --add-port=6379/tcp
sudo firewall-cmd --permanent --add-port=5672/tcp
sudo firewall-cmd --permanent --add-port=15672/tcp
sudo firewall-cmd --permanent --add-port=6333/tcp
sudo firewall-cmd --reload

# 或者关闭防火墙（不推荐生产环境）
sudo systemctl stop firewalld
sudo systemctl disable firewalld
```

## 二、部署服务

### 1. 上传文件到虚拟机

```bash
# 将docker-compose.yml和sql目录上传到虚拟机
scp -r docker-compose.yml sql/ user@192.168.1.100:/home/user/ai-interview/
```

### 2. 启动服务

```bash
# SSH登录虚拟机
ssh user@192.168.1.100

# 进入目录
cd /home/user/ai-interview

# 启动服务
docker-compose up -d

# 查看状态
docker-compose ps
```

### 3. 验证服务

```bash
# 检查MySQL
mysql -h 127.0.0.1 -u root -p123456 -e "SHOW DATABASES;"

# 检查Redis
redis-cli ping

# 检查RabbitMQ
curl http://localhost:15672

# 检查Qdrant
curl http://localhost:6333
```

## 三、本地开发配置

### 1. 复制环境变量文件

```bash
cp .env.example .env
```

### 2. 修改.env文件

将 `192.168.1.100` 改为你的虚拟机IP：

```properties
MYSQL_HOST=192.168.1.100
REDIS_HOST=192.168.1.100
RABBITMQ_HOST=192.168.1.100
QDRANT_HOST=192.168.1.100
DASHSCOPE_API_KEY=your_api_key
```

### 3. 运行项目

```bash
# 加载环境变量（Linux/Mac）
source .env

# Windows PowerShell
Get-ChildItem .env | ForEach-Object {
    $name = $_.Name
    $value = $_.Value
    [Environment]::SetEnvironmentVariable($name, $value, "Process")
}

# 或者直接设置环境变量
set MYSQL_HOST=192.168.1.100
set REDIS_HOST=192.168.1.100
set RABBITMQ_HOST=192.168.1.100
set QDRANT_HOST=192.168.1.100
set DASHSCOPE_API_KEY=your_api_key

# 编译运行
mvn clean package -DskipTests
java -jar ai-interview-api/target/ai-interview-api-1.0.0.jar
```

## 四、常见问题

### 1. 连接被拒绝

检查虚拟机防火墙是否开放端口：
```bash
sudo firewall-cmd --list-ports
```

### 2. MySQL连接失败

检查MySQL是否允许远程连接：
```bash
docker exec -it ai-interview-mysql mysql -u root -p123456
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY '123456';
FLUSH PRIVILEGES;
```

### 3. Qdrant连接失败

检查Qdrant是否正常运行：
```bash
docker logs ai-interview-qdrant
```

## 五、服务访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| MySQL | 192.168.1.100:3306 | 数据库 |
| Redis | 192.168.1.100:6379 | 缓存 |
| RabbitMQ | 192.168.1.100:15672 | 管理界面 |
| Qdrant | 192.168.1.100:6333 | 向量数据库 |
