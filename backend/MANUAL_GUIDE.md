# 阿里云ECS手动部署指南

## ⚠️ 重要说明
由于远程连接受限，请按照以下步骤在服务器上手动操作。

## 第一步：连接服务器

### 方法A：使用Windows远程桌面 (RDP)
1. 按 `Win + R`，输入 `mstsc`，回车
2. 计算机填: `8.134.80.158`
3. 用户名: `Administrator`
4. 密码: `5-iKje5Q.Dt48fw`

### 方法B：使用手机App
下载"阿里云"App，使用手机管理服务器

## 第二步：检查服务器系统

打开"命令提示符"或"PowerShell"，执行：
```cmd
systeminfo
```

查看"OS 名称"字段：
- 如果显示 **"Microsoft Windows Server"** → Windows系统
- 如果显示 **"Alibaba Cloud Linux"** 或 **"CentOS"** → Linux系统

---

## Windows Server 部署步骤

### 1. 安装 Chocolatey (包管理器)
在PowerShell (管理员) 中执行：
```powershell
Set-ExecutionPolicy Bypass -Scope Process -Force
iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))
```

### 2. 使用Chocolatey安装Java
```powershell
choco install openjdk21 -y
```

### 3. 安装MySQL
```powershell
choco install mysql -y
```

安装完成后启动MySQL服务：
```powershell
Start-Service MySQL80
```

### 4. 安装Maven
```powershell
choco install maven -y
```

### 5. 验证安装
```powershell
java -version
mvn -version
```

### 6. 配置MySQL
打开MySQL Command Line Client，执行：
```sql
CREATE DATABASE glimmerseed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'admin'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON glimmerseed.* TO 'admin'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 7. 上传项目
使用以下任一方式：
- **FileZilla**: SFTP连接，上传到 C:\glimmerseed\
- **阿里云文件管理**: 网页上传

### 8. 构建项目
打开PowerShell (管理员)：
```powershell
cd C:\glimmerseed\backend
mvn clean package -DskipTests
```

### 9. 启动服务
```powershell
java -jar target\glimmerseed-backend-1.0.0.jar
```

### 10. 开放端口
**Windows防火墙**：
```powershell
New-NetFirewallRule -DisplayName "Glimmerseed" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow
```

**阿里云安全组**：
1. 登录阿里云控制台
2. ECS → 安全组
3. 添加入方向规则：
   - 协议: TCP
   - 端口: 8080
   - 来源: 0.0.0.0/0

---

## Linux Server 部署步骤

### 1. SSH连接
使用PuTTY或Windows Terminal连接：
```bash
ssh root@8.134.80.158
```

### 2. 更新系统
```bash
dnf update -y
```

### 3. 安装Java 21
```bash
dnf install -y java-21-openjdk-devel
java -version
```

### 4. 安装MySQL
```bash
dnf install -y mysql-server
systemctl start mysqld
systemctl enable mysqld
mysql_secure_installation
```

按提示设置root密码，其他选Y

### 5. 配置MySQL
```bash
mysql -u root -p
```

执行：
```sql
CREATE DATABASE glimmerseed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'admin'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON glimmerseed.* TO 'admin'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 6. 安装Maven
```bash
dnf install -y maven
mvn -v
```

### 7. 上传项目
在本地电脑执行：
```powershell
scp -r C:\Users\YouLa\AndroidStudioProjects\Glimmerseed\backend root@8.134.80.158:/root/glimmerseed
```

### 8. 构建项目
```bash
cd /root/glimmerseed/backend
mvn clean package -DskipTests
```

### 9. 启动服务
```bash
java -jar target/glimmerseed-backend-1.0.0.jar
```

### 10. 开放端口
```bash
firewall-cmd --permanent --add-port=8080/tcp
firewall-cmd --reload
```

---

## 创建后台服务 (Linux)

创建服务文件：
```bash
nano /etc/systemd/system/glimmerseed.service
```

写入内容：
```ini
[Unit]
Description=Glimmerseed Backend
After=network.target mysql.service

[Service]
Type=simple
User=root
WorkingDirectory=/root/glimmerseed/backend
ExecStart=/usr/bin/java -jar target/glimmerseed-backend-1.0.0.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

启用服务：
```bash
systemctl daemon-reload
systemctl enable glimmerseed
systemctl start glimmerseed
systemctl status glimmerseed
```

---

## 验证部署

在本地浏览器访问：
```
http://8.134.80.158:8080/api/auth/login
```

应该看到JSON响应

## 测试API

使用curl测试：
```bash
curl -X POST http://8.134.80.158:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"password"}'
```

预期响应：
```json
{"success":true,"message":"操作成功","token":"...","userId":1}
```

---

## 常见问题解决

### 问题1：连接被拒绝
- 检查服务是否启动: `netstat -tlnp | grep 8080`
- 检查防火墙: `firewall-cmd --list-all` (Linux) 或 `netsh advfirewall firewall show rule name=all` (Windows)

### 问题2：MySQL连接失败
- 检查MySQL状态: `systemctl status mysqld` (Linux) 或 `Get-Service MySQL80` (Windows)
- 验证密码是否正确

### 问题3：内存不足
```bash
# Linux查看内存
free -h

# Windows查看内存
Get-Process | Sort-Object WorkingSet -Descending | Select -First 10
```

### 问题4：端口被占用
```bash
# Linux查找占用端口的进程
lsof -i :8080

# Windows查找占用端口的进程
netstat -ano | findstr :8080
```

---

## 服务管理命令

### Linux
```bash
systemctl start glimmerseed    # 启动
systemctl stop glimmerseed     # 停止
systemctl restart glimmerseed  # 重启
systemctl status glimmerseed   # 状态
journalctl -u glimmerseed -f   # 日志
```

### Windows
```powershell
Start-Service Glimmerseed      # 启动
Stop-Service Glimmerseed      # 停止
Restart-Service Glimmerseed   # 重启
Get-Service Glimmerseed       # 状态
```

---

## 安全建议

1. **修改默认密码**: 立即修改MySQL的admin密码
2. **配置SSL**: 生产环境建议启用HTTPS
3. **限制IP访问**: 阿里云安全组只开放必要端口
4. **定期备份**: 设置数据库自动备份
5. **监控系统**: 使用监控工具跟踪服务状态

---

## 下一步

部署成功后，更新Android应用的API地址为：
```
http://8.134.80.158:8080/api/
```

然后在Android Studio中重新构建并运行应用！