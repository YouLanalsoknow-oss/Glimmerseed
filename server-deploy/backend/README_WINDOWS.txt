# Glimmerseed 后端 Windows Server 部署检查清单

## 部署前准备

### ✅ 服务器信息
- **公网IP**: 8.134.80.158
- **用户名**: Administrator
- **密码**: 5-iKje5Q.Dt48fw

### ✅ 必需软件
- [ ] Java 21 JDK
  下载: https://download.oracle.com/java/21/latest/jdk-21_windows-x64_bin.exe
  验证: 运行 `java -version`

- [ ] MySQL 8.0
  下载: https://dev.mysql.com/downloads/mysql/
  验证: 运行 `mysql --version`

- [ ] Maven 3.8+
  下载: https://maven.apache.org/download.cgi
  验证: 运行 `mvn -version`

## 部署步骤

### 步骤1: 配置MySQL数据库
1. 打开 MySQL Command Line Client
2. 执行以下SQL命令:
```sql
CREATE DATABASE glimmerseed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'admin'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON glimmerseed.* TO 'admin'@'localhost';
FLUSH PRIVILEGES;
USE glimmerseed;
SOURCE C:\glimmerseed\backend\init_db.sql;
EXIT;
```

### 步骤2: 上传项目
将 backend 文件夹上传到服务器: C:\glimmerseed\

### 步骤3: 构建项目
打开 PowerShell (管理员):
```powershell
cd C:\glimmerseed\backend
.\build.bat
```

### 步骤4: 配置数据库连接
编辑: C:\glimmerseed\backend\src\main\resources\application.yml
确认数据库密码为: password

重新构建:
```powershell
.\build.bat
```

### 步骤5: 启动服务
方式1 - 直接运行:
```powershell
cd C:\glimmerseed\backend
java -jar target\glimmerseed-backend-1.0.0.jar
```

方式2 - Windows服务 (推荐):
1. 下载 WinSW: https://github.com/winsw/winsw/releases
2. 将 WinSW.exe 重命名为 glimmerseed.exe
3. 复制到 C:\glimmerseed\backend\target\
4. 运行 service-install.bat

### 步骤6: 开放端口
#### 阿里云安全组:
1. 登录阿里云控制台
2. ECS → 安全组 → 配置规则
3. 添加入方向规则:
   - 协议: TCP
   - 端口: 8080
   - 来源: 0.0.0.0/0

#### Windows防火墙:
```powershell
New-NetFirewallRule -DisplayName "Glimmerseed Backend" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow
```

## 验证部署

### 本地测试
打开浏览器访问:
```
http://localhost:8080/api/auth/login
```

### 远程测试
```
http://8.134.80.158:8080/api/auth/login
```

### 运行测试脚本
```powershell
cd C:\glimmerseed\backend
.\test-api.ps1
```

## 常见问题

### Q1: MySQL连接失败
A: 检查MySQL服务是否运行:
```powershell
Get-Service MySQL80
```

### Q2: 端口8080被占用
A: 查找占用进程:
```powershell
netstat -ano | findstr :8080
```
或修改 application.yml 中的端口

### Q3: 外网无法访问
A: 检查阿里云安全组是否开放8080端口

### Q4: 构建失败
A: 确保所有依赖下载完成，可能需要配置Maven镜像

## 服务管理

### 查看服务状态
```powershell
cd C:\glimmerseed\backend\target
.\glimmerseed.exe status
```

### 重启服务
```powershell
cd C:\glimmerseed\backend\target
.\glimmerseed.exe restart
```

### 查看日志
```powershell
Get-Content C:\glimmerseed\backend\target\logs\glimmerseed.log -Tail 50 -Wait
```

## API 接口

### 基础URL
```
http://8.134.80.158:8080/api
```

### 认证接口
- POST /auth/register - 注册
- POST /auth/login - 登录

### 桌宠接口
- POST /pets - 创建桌宠
- GET /pets - 获取列表
- GET /pets/{id} - 获取详情
- DELETE /pets/{id} - 删除

### 聊天接口
- POST /chat - 发送消息
- GET /chat/{petId} - 获取记录

## 测试账户

- **邮箱**: admin@example.com
- **密码**: password