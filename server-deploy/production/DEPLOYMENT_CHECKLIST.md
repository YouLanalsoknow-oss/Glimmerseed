
# Glimmerseed 部署检查清单

## 📋 部署前检查

- [ ] Java 21 已安装？
- [ ] Maven 3.9+ 已安装？
- [ ] MySQL 8.0 已安装？
- [ ] MySQL root 密码已知？
- [ ] 服务器防火墙允许端口？

## 🚀 部署步骤

### 阶段一：文件复制
- [ ] `server-deploy` 文件夹已复制到服务器桌面？

### 阶段二：数据库初始化
- [ ] 已创建数据库 `glimmerseed`？
- [ ] `init_db.sql` 已执行？

### 阶段三：编译后端
- [ ] `mvn clean package -DskipTests` 执行成功？
- [ ] 生成了 `target/glimmerseed-backend-1.0.0.jar`？

### 阶段四：配置 Nginx
- [ ] Nginx 已安装？
- [ ] `nginx.conf` 已复制到 `C:\nginx\conf\`？
- [ ] SSL 证书已生成？

### 阶段五：启动服务
- [ ] Spring Boot 服务已启动？
- [ ] Nginx 服务已启动？

### 阶段六：端口与防火墙
- [ ] Windows 防火墙开放 443？
- [ ] 阿里云安全组开放 443？
- [ ] 阿里云安全组开放 3389？
- [ ] 阿里云安全组**关闭** 8080？

## ✅ 功能测试

### 测试 1：健康检查
```
访问：https://8.134.80.158/actuator/health
预期：{"status":"UP"}
```

### 测试 2：用户注册
```
POST https://8.134.80.158/api/auth/register
Body: {"username":"test","email":"test@test.com","password":"123456"}
预期：返回成功和token
```

### 测试 3：创建宠物
```
POST https://8.134.80.158/api/pets
Header: X-User-Id: 1
Body: {"name":"小橘子","species":"cat","personality":"friendly"}
预期：返回宠物信息
```

### 测试 4：聊天功能
```
POST https://8.134.80.158/api/chat
Header: X-User-Id: 1
Body: {"petId":1,"message":"你好"}
预期：返回AI回复
```

## 📱 Android 应用测试

- [ ] `ApiClient.kt` 中的 BASE_URL 是 `https://8.134.80.158/api/`？
- [ ] 应用可以正常启动？
- [ ] 注册功能正常？
- [ ] 登录功能正常？
- [ ] 创建宠物功能正常？
- [ ] 聊天功能正常？

## 🔧 故障排查

### 问题：无法连接服务器
- 检查安全组是否开放 443
- 检查 Nginx 是否运行

### 问题：数据库连接失败
- 检查 MySQL 是否运行
- 检查 `application.yml` 中的密码是否正确

### 问题：AI 回复超时
- 检查 SiliconFlow API Key 是否正确
- 检查网络连接

## 📊 运行后检查

- [ ] 检查日志：`C:\nginx\logs\access.log`
- [ ] 检查日志：`C:\nginx\logs\error.log`
- [ ] 检查数据库：数据是否正常保存？
- [ ] 检查备份：定时备份是否正常？

