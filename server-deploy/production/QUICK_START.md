
# Glimmerseed 生产环境快速开始

## 前置条件确认

在服务器上运行检查脚本：
```batch
cd C:\Users\Administrator\Desktop\server-deploy
1-check-server.bat
```

确保以下组件正常：
- ✅ Java 21
- ✅ Maven 3.9+
- ✅ MySQL 8.0

## 快速部署步骤

### 1. 确保后端已编译

```batch
cd C:\Users\Administrator\Desktop\server-deploy\backend
call mvn clean package -DskipTests
```

### 2. 安装 Nginx

```batch
cd C:\Users\Administrator\Desktop\server-deploy\production
install-nginx.bat
```

### 3. 配置防火墙

```batch
open-firewall-https.bat
```

### 4. 更新阿里云安全组

**只开放以下端口：**
- 443 (HTTPS)
- 3389 (远程桌面)

**关闭 8080 端口！**

### 5. 启动服务

**方式 A：直接启动（简单）**

```batch
# 启动 Nginx
cd C:\nginx
start nginx

# 启动 Spring Boot
cd C:\Users\Administrator\Desktop\server-deploy\backend
start.bat
```

**方式 B：安装成 Windows 服务（推荐）**

```batch
cd C:\Users\Administrator\Desktop\server-deploy\production
setup-windows-service.bat
net start GlimmerseedBackend
```

### 6. 测试访问

在本地浏览器访问：
```
https://8.134.80.158/actuator/health
```

### 7. 运行 Android 应用

确保 `ApiClient.kt` 中的 BASE_URL 是：
```kotlin
private const val BASE_URL = "https://8.134.80.158/api/"
```

## 常见问题

### 自签名证书警告

浏览器会显示"不安全"警告，这是正常的（因为我们使用自签名证书）。点击"继续访问"即可。

正式上线时可以申请 Let's Encrypt 免费证书。

### 端口检查

在服务器上运行：
```batch
netstat -ano | findstr "443 8080"
```

确保 443 和 8080 都在监听。

### 查看日志

- Nginx 日志: `C:\nginx\logs\
- Spring Boot 日志: 控制台输出或 `C:\nginx\logs\glimmerseed-stdout.log

## 下一步

- 配置数据库定时备份（任务计划程序）
- 配置监控告警
- 申请正式 SSL 证书（可选）

