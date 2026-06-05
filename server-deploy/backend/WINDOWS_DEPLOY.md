# Windows Server 部署指南

## 环境要求

- Windows Server 2019/2022
- Java 21+
- MySQL 8.0+
- Maven 3.8+

## 第一步：安装依赖

### 1.1 安装 Java 21

下载链接：https://download.oracle.com/java/21/latest/jdk-21_windows-x64_bin.exe

安装后配置环境变量：
```powershell
# 系统环境变量中添加
JAVA_HOME = C:\Program Files\Java\jdk-21
Path中添加 %JAVA_HOME%\bin
```

### 1.2 安装 MySQL 8.0

下载链接：https://dev.mysql.com/downloads/mysql/

选择 MySQL Installer → Windows (x86, 64-bit), MSI Installer

安装选项：选择 "Full" 安装

记住 root 密码！

### 1.3 安装 Maven

下载链接：https://maven.apache.org/download.cgi

下载 apache-maven-3.9.x-bin.zip

解压到 C:\Maven

配置环境变量：
```powershell
MAVEN_HOME = C:\Maven
Path中添加 %MAVEN_HOME%\bin
```

## 第二步：配置 MySQL

### 2.1 启动 MySQL 服务

打开 "服务" (services.msc)，找到 MySQL80，确保状态为"运行中"

### 2.2 创建数据库和用户

打开 MySQL Command Line Client 或使用 PowerShell：

```powershell
mysql -u root -p
```

输入 root 密码后执行：

```sql
CREATE DATABASE glimmerseed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'admin'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON glimmerseed.* TO 'admin'@'localhost';
FLUSH PRIVILEGES;
USE glimmerseed;
SOURCE C:\glimmerseed\backend\init_db.sql;
EXIT;
```

## 第三步：上传项目

将整个 backend 文件夹上传到服务器，例如：C:\glimmerseed\

## 第四步：修改配置文件

编辑 C:\glimmerseed\backend\src\main\resources\application.yml

确保数据库连接信息正确：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/glimmerseed?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: admin
    password: password
```

## 第五步：构建项目

打开 PowerShell：

```powershell
cd C:\glimmerseed\backend
mvn clean package -DskipTests
```

成功后会生成：C:\glimmerseed\backend\target\glimmerseed-backend-1.0.0.jar

## 第六步：运行服务

### 方式一：直接运行（测试用）

```powershell
cd C:\glimmerseed\backend
java -jar target\glimmerseed-backend-1.0.0.jar
```

### 方式二：创建 Windows 服务（推荐）

下载 WinSW：https://github.com/winsw/winsw/releases

将 WinSW.exe 复制到 C:\glimmerseed\backend\target\ 并重命名为 glimmerseed.exe

创建 glimmerseed.xml 配置文件：

```xml
<service>
  <id>glimmerseed</id>
  <name>Glimmerseed Backend</name>
  <description>AI桌宠聊天应用后端服务</description>
  <executable>java</executable>
  <arguments>-jar glimmerseed-backend-1.0.0.jar</arguments>
  <logmode>rotate</logmode>
</service>
```

安装服务：
```powershell
cd C:\glimmerseed\backend\target
.\glimmerseed.exe install
.\glimmerseed.exe start
```

## 第七步：配置防火墙

### Windows 防火墙

打开 "高级安全 Windows Defender 防火墙"

入站规则 → 新建规则：
- 端口：8080
- TCP
- 允许连接

### 阿里云安全组

1. 登录阿里云控制台
2. 进入 ECS → 安全组
3. 添加规则：
   - 协议：TCP
   - 端口：8080
   - 来源：0.0.0.0/0

## 第八步：测试

在浏览器中访问：
```
http://8.134.80.158:8080/api/auth/login
```

应该返回 JSON 响应

## 常用命令

```powershell
# 启动服务
.\glimmerseed.exe start

# 停止服务
.\glimmerseed.exe stop

# 重启服务
.\glimmerseed.exe restart

# 查看状态
.\glimmerseed.exe status

# 卸载服务
.\glimmerseed.exe uninstall
```

## 注意事项

1. 确保 MySQL 服务正在运行
2. 确保端口 8080 未被占用
3. 如果外网无法访问，检查阿里云安全组配置
4. 查看日志：C:\glimmerseed\backend\target\logs\

## API 测试

使用 Postman 或 curl 测试：

### 注册
```powershell
Invoke-RestMethod -Uri "http://8.134.80.158:8080/api/auth/register" -Method POST -ContentType "application/json" -Body '{"email":"test@example.com","password":"123456","username":"测试用户"}'
```

### 登录
```powershell
Invoke-RestMethod -Uri "http://8.134.80.158:8080/api/auth/login" -Method POST -ContentType "application/json" -Body '{"email":"admin@example.com","password":"password"}'
```

### 创建桌宠
```powershell
Invoke-RestMethod -Uri "http://8.134.80.158:8080/api/pets" -Method POST -ContentType "application/json" -Headers @{"X-User-Id"="1"} -Body '{"name":"小萌","appearance":"可爱猫咪","personality":"活泼开朗","color":"粉色"}'
```

### 发送消息
```powershell
Invoke-RestMethod -Uri "http://8.134.80.158:8080/api/chat" -Method POST -ContentType "application/json" -Headers @{"X-User-Id"="1"} -Body '{"petId":1,"content":"你好呀"}'
```