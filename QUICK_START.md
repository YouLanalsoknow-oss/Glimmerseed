# 🎯 Glimmerseed AI桌宠 - 快速部署指南

## 📦 已准备好的文件

### 核心文件
- ✅ **backend/** - Spring Boot后端完整项目源码
- ✅ **init_db.sql** - MySQL数据库初始化脚本
- ✅ **pom.xml** - Maven依赖配置
- ✅ **application.yml** - 应用配置文件

### 部署脚本
- ✅ **build.bat** - Windows一键构建脚本
- ✅ **start.bat** - Windows启动脚本
- ✅ **setup.sh** - Linux一键安装脚本
- ✅ **test-api.ps1** - API测试脚本

### 文档
- ✅ **README_WINDOWS.txt** - Windows详细部署指南
- ✅ **README_LINUX.txt** - Linux详细部署指南
- ✅ **MANUAL_GUIDE.md** - 综合部署指南
- ✅ **WINDOWS_DEPLOY.md** - Windows完整部署文档
- ✅ **DEPLOYMENT.md** - 通用部署文档

---

## 🚀 最快部署路径（推荐）

### 方式A：Windows Server

#### 第一步：安装软件
在服务器PowerShell (管理员) 中执行：

```powershell
# 安装Java 21
choco install openjdk21 -y

# 安装MySQL
choco install mysql -y

# 安装Maven
choco install maven -y

# 验证安装
java -version
mvn -version
```

#### 第二步：配置MySQL
打开MySQL Command Line Client：
```sql
CREATE DATABASE glimmerseed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'admin'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON glimmerseed.* TO 'admin'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### 第三步：上传并构建
1. 将backend文件夹上传到服务器 C:\glimmerseed\
2. 在PowerShell执行：
```powershell
cd C:\glimmerseed\backend
mvn clean package -DskipTests
```

#### 第四步：启动服务
```powershell
java -jar target\glimmerseed-backend-1.0.0.jar
```

#### 第五步：开放端口
阿里云控制台 → 安全组 → 添加规则：端口8080 TCP

---

### 方式B：Linux Server

#### 第一步：安装软件
通过SSH连接后执行：
```bash
# 安装Java 21
sudo dnf install -y java-21-openjdk-devel

# 安装MySQL
sudo dnf install -y mysql-server
sudo systemctl start mysqld
sudo systemctl enable mysqld
sudo mysql_secure_installation

# 安装Maven
sudo dnf install -y maven

# 验证
java -version
mvn -version
```

#### 第二步：配置MySQL
```bash
mysql -u root -p
```
```sql
CREATE DATABASE glimmerseed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'admin'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON glimmerseed.* TO 'admin'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### 第三步：上传并构建
```bash
# 从本地上传
scp -r backend/* root@8.134.80.158:/root/glimmerseed/

# 构建
cd /root/glimmerseed/backend
mvn clean package -DskipTests
```

#### 第四步：启动服务
```bash
java -jar target/glimmerseed-backend-1.0.0.jar
```

#### 第五步：开放端口
```bash
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

---

## ✅ 快速验证

部署成功后，浏览器访问：
```
http://8.134.80.158:8080/api/auth/login
```

测试账号：
- 邮箱: admin@example.com
- 密码: password

---

## 🔧 常用命令

### Windows
```powershell
# 启动
java -jar C:\glimmerseed\backend\target\glimmerseed-backend-1.0.0.jar

# 停止
Ctrl+C

# 查看日志
Get-Content C:\glimmerseed\backend\target\logs\*.log -Tail 50
```

### Linux
```bash
# 后台运行
nohup java -jar target/glimmerseed-backend-1.0.0.jar > /var/log/glimmerseed.log 2>&1 &

# 查看日志
tail -f /var/log/glimmerseed.log

# 创建服务
sudo nano /etc/systemd/system/glimmerseed.service
# 写入配置并启用
sudo systemctl enable glimmerseed
```

---

## 📱 Android应用配置

后端部署成功后，修改Android应用的API地址：

文件：`app/src/main/java/com/example/glimmerseed/network/ApiClient.kt`

```kotlin
private const val BASE_URL = "http://8.134.80.158:8080/api/"
```

重新构建运行即可！

---

## 🆘 常见问题

### Q1: MySQL连接失败
```powershell
# 检查服务状态
Get-Service MySQL80
# 或
sudo systemctl status mysqld
```

### Q2: 端口被占用
```powershell
netstat -ano | findstr :8080
# 或
sudo lsof -i :8080
```

### Q3: 外网无法访问
- 检查阿里云安全组是否开放8080端口
- 检查服务器防火墙是否允许8080端口

### Q4: 构建失败
```bash
# 清理并重试
mvn clean
mvn package -DskipTests
```

---

## 📞 获取帮助

遇到问题？请提供：
1. 错误信息
2. 服务器系统类型
3. 执行到哪一步

---

## 🎉 成功标志

看到以下输出表示部署成功：
```
Started Application in X.XXX seconds
Tomcat started on port(s): 8080
```

浏览器访问返回JSON响应：
```json
{"success":false,"message":"操作成功","data":null}
```

---

**下一步**：部署后端 → 配置Android应用 → 测试聊天功能 → 完成！🎊