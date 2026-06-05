
# 服务器部署操作清单

## 在远程桌面上操作的步骤

### 第一步：传输文件到服务器

在远程桌面连接窗口中：
1. 点击「显示选项」→「本地资源」→「详细信息」
2. 勾选你的本地驱动器（如 C:）
3. 连接服务器后，在服务器的「此电脑」中可以看到你的本地磁盘
4. 将 **整个 `server-deploy` 文件夹**从你的电脑复制到服务器的 `C:\`

### 第二步：在服务器上运行脚本

在服务器上打开 CMD，进入 `C:\server-deploy\`，按顺序运行：

| # | 脚本 | 说明 |
|---|------|------|
| 1 | `1-check-server.bat` | 检查 Java/Maven/MySQL 是否安装 |
| 2 | `2-setup-mysql.bat` | 初始化数据库（需要 MySQL root 密码） |
| 3 | `3-build-backend.bat` | 编译后端项目 |
| 4 | `4-configure.bat` | 配置数据库密码和 API Key |
| 5 | `5-start-server.bat` | 启动后端服务 |
| 6 | `6-test-api.bat` | 测试 API 是否正常工作 |

### 第三步：配置阿里云安全组（同时进行）

在你的电脑上登录阿里云控制台：
1. 开放 8080 端口（详见 ALIYUN_SECURITY_GROUP_GUIDE.md）

### 第四步：开放 Windows 防火墙（如果需要）

在服务器 CMD 中运行：
```cmd
netsh advfirewall firewall add rule name="Glimmerseed API" dir=in action=allow protocol=TCP localport=8080
```

---

## 需要的信息

运行脚本时准备好：
- MySQL root 密码
- SiliconFlow API Key（你之前提供过：sk-psahinqeszjuyqlerjswclddalhepyjheenidxrkuvawsyxav）

---

## 完成后

部署成功后，你的 Android 应用就可以连接到 `http://8.134.80.158:8080/api/` 了！

