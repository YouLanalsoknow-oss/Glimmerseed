# Glimmerseed 后端部署指南

## 环境要求

- Java 21+
- MySQL 8.0+
- Maven 3.8+

## 部署步骤

### 1. 安装依赖

```bash
# 安装 Java 21
sudo apt update
sudo apt install openjdk-21-jdk -y

# 安装 MySQL
sudo apt install mysql-server -y

# 安装 Maven
sudo apt install maven -y
```

### 2. 配置数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库和用户
CREATE DATABASE glimmerseed;
CREATE USER 'admin'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON glimmerseed.* TO 'admin'@'localhost';
FLUSH PRIVILEGES;

# 退出 MySQL
exit

# 导入初始化脚本
mysql -u admin -p glimmerseed < init_db.sql
```

### 3. 构建项目

```bash
cd backend
mvn clean package -DskipTests
```

### 4. 运行服务

```bash
# 开发环境运行
mvn spring-boot:run

# 或运行打包后的 Jar
java -jar target/glimmerseed-backend-1.0.0.jar
```

### 5. 配置 Nginx 反向代理（可选）

```bash
# 安装 Nginx
sudo apt install nginx -y

# 配置反向代理
sudo nano /etc/nginx/sites-available/glimmerseed

# 添加以下内容
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}

# 启用配置
sudo ln -s /etc/nginx/sites-available/glimmerseed /etc/nginx/sites-enabled/
sudo systemctl restart nginx
```

## API 接口

### 认证接口

- `POST /api/auth/register` - 注册
- `POST /api/auth/login` - 登录

### 桌宠接口

- `POST /api/pets` - 创建桌宠
- `GET /api/pets` - 获取桌宠列表
- `GET /api/pets/{petId}` - 获取单个桌宠
- `DELETE /api/pets/{petId}` - 删除桌宠

### 聊天接口

- `POST /api/chat` - 发送消息
- `GET /api/chat/{petId}` - 获取聊天记录

## 阿里云 ECS 配置

### 安全组规则

确保以下端口已开放：
- 80 (HTTP)
- 8080 (应用端口)
- 3306 (MySQL)

### 启动脚本

创建 `start.sh`:

```bash
#!/bin/bash
cd /path/to/backend
nohup java -jar target/glimmerseed-backend-1.0.0.jar > /var/log/glimmerseed.log 2>&1 &
echo $! > /var/run/glimmerseed.pid
```

### 停止脚本

创建 `stop.sh`:

```bash
#!/bin/bash
kill $(cat /var/run/glimmerseed.pid)
rm /var/run/glimmerseed.pid
```