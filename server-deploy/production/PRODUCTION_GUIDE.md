
# Glimmerseed 生产环境部署完整指南

## 架构概览

```
┌─────────────┐
│   用户    │
└──────┬────┘
       │ HTTPS (443)
       ▼
┌───────────────────────┐
│   Nginx (反向代理)    │
│   - SSL 终止         │
│   - 静态文件缓存      │
└──────┬────────────────┘
       │ HTTP (localhost:8080)
       ▼
┌──────────────────────────┐
│  Spring Boot 后端      │
│  - 业务逻辑            │
│  - API 接口            │
└──────┬───────────────────┘
       │ JDBC (localhost:3306)
       ▼
┌───────────────────────┐
│   MySQL 数据库        │
└───────────────────────┘
```

## 部署步骤

### 第一步：安装 Nginx

在服务器上运行：
```batch
cd C:\Users\Administrator\Desktop\server-deploy\production
install-nginx.bat
```

### 第二步：配置防火墙

```batch
open-firewall-https.bat
```

### 第三步：配置阿里云安全组

**只开放以下端口到公网：**
- 443 (HTTPS)
- 3389 (远程桌面)

**关闭 8080 端口！

### 第四步：配置 Windows 服务（可选）

```batch
setup-windows-service.bat
```

### 第五步：配置数据库定时备份

使用「任务计划程序」每天定时运行 `backup-database.bat`

### 第六步：更新 Android 应用配置

修改 `ApiClient.kt`：
```kotlin
private const val BASE_URL = "https://8.134.80.158/api/"
```

## 服务管理

### 启动所有服务

```batch
# 启动 Nginx
cd C:\nginx
start nginx

# 启动 Spring Boot（如果没有安装服务）
cd C:\Users\Administrator\Desktop\server-deploy\backend
start.bat

# 或者启动 Spring Boot 服务（如果已安装）
net start GlimmerseedBackend
```

### 停止所有服务

```batch
# 停止 Nginx
cd C:\nginx
nginx -s quit

# 停止 Spring Boot 服务
net stop GlimmerseedBackend
```

### 重启 Nginx 配置

```batch
cd C:\nginx
nginx -s reload
```

## 监控和维护

### 查看日志

- Nginx 访问日志: `C:\nginx\logs\glimmerseed_access.log
- Nginx 错误日志: `C:\nginx\logs\glimmerseed_error.log
- Spring Boot 日志: `C:\nginx\logs\glimmerseed-stdout.log`

### 备份位置

- 数据库备份: `C:\backups\`

## 安全检查清单

部署完成后检查：
- [ ] 只有 443 和 3389 端口开放
- [ ] 8080 端口不开放到公网
- [ ] 数据库有定时备份
- [ ] 服务配置自动启动
- [ ] SSL 证书有效
- [ ] Android 应用使用 HTTPS
- [ ] 日志轮转已配置

