
# Glimmerseed 服务器部署完整指南（最终版）

## 前置条件

确认以下内容已就绪：
- ✅ Java 21 已安装
- ✅ MySQL 8.0 已安装
- ✅ 知道 MySQL root 密码
- ✅ 有 SiliconFlow API Key

---

## 第一步：环境准备（3种方式）

### 方式 A：一键安装（推荐）
```batch
cd C:\Users\Administrator\Desktop\server-deploy\production
quick-install-all.bat
```

### 方式 B：分步安装
先安装 Maven：
```batch
cd C:\Users\Administrator\Desktop\server-deploy\production
install-maven.bat
```
[重要] 安装后关闭CMD，重新打开新窗口

然后继续：
```batch
quick-install-all.bat
```

### 方式 C：手动安装
1. 下载 Maven：https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip
2. 解压到：`C:\Program Files\Apache\maven`
3. 配置环境变量：
   - 添加 `MAVEN_HOME=C:\Program Files\Apache\maven`
   - 在 PATH 中添加 `%MAVEN_HOME%\bin`

---

## 第二步：初始化数据库

```batch
cd C:\Users\Administrator\Desktop\server-deploy
mysql -u root -p
```
输入密码后，执行：
```sql
source init_db.sql;
exit;
```

---

## 第三步：编译和启动

### 编译后端
```batch
cd C:\Users\Administrator\Desktop\server-deploy\backend
mvn clean package -DskipTests
```

### 启动服务
```batch
# 窗口1 - 启动 Nginx
cd C:\nginx
start nginx

# 窗口2 - 启动 Spring Boot
cd C:\Users\Administrator\Desktop\server-deploy\backend
start.bat
```

---

## 第四步：测试功能

### 在服务器本地测试
```batch
cd C:\Users\Administrator\Desktop\server-deploy\production
test-api.bat
```

### 在本地浏览器测试
访问：`https://8.134.80.158/actuator/health`

---

## 第五步：配置阿里云安全组

1. 登录阿里云控制台
2. 找到服务器实例
3. 安全组 -&gt; 配置规则
4. 添加入方向规则：
   - 端口范围：443/443
   - 授权对象：0.0.0.0/0
   - 描述：HTTPS

5. **重要**：删除或关闭 8080 端口的公网访问

---

## 第六步：Android 应用配置

确保 `app/src/main/java/com/example/glimmerseed/network/ApiClient.kt` 中是：
```kotlin
private const val BASE_URL = "https://8.134.80.158/api/"
```

---

## 故障排查

### Maven 命令无法识别
- 关闭并重新打开 CMD 窗口
- 检查环境变量是否生效

### 编译失败
- 检查网络连接（需要下载依赖）
- 运行 `mvn clean` 清理后重试

### 端口无法访问
- 检查 Windows 防火墙
- 检查阿里云安全组
- 检查服务是否正在运行

---

## 日常管理

### 启动服务
```batch
cd C:\nginx
start nginx
cd C:\Users\Administrator\Desktop\server-deploy\backend
start.bat
```

### 停止服务
```batch
cd C:\nginx
nginx -s quit
# 在 Spring Boot 窗口按 Ctrl+C
```

### 查看日志
- Nginx 日志：`C:\nginx\logs\`
- Spring Boot 日志：控制台输出

---

## 完成检查清单

部署后确认：
- [ ] Maven 命令可用（`mvn -version`）
- [ ] 后端编译成功
- [ ] Nginx 正在运行
- [ ] Spring Boot 正在运行
- [ ] 健康检查返回 UP
- [ ] 阿里云安全组开放 443
- [ ] Android 应用可以连接

