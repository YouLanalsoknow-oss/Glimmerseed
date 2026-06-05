# Glimmerseed 后端 - Linux 服务器部署指南

## 服务器信息
- **IP**: 8.134.80.158
- **用户名**: root（或其他有sudo权限的用户）

## 连接方式

### 方式1: 使用 PuTTY (Windows)
1. 下载 PuTTY: https://www.putty.org/
2. 主机名填: 8.134.80.158
3. 端口: 22
4. 连接类型: SSH
5. 点击"Open"，使用服务器密码登录

### 方式2: 使用 Windows Terminal (Windows 10/11)
```powershell
ssh root@8.134.80.158
```

### 方式3: 使用 VS Code Remote SSH
1. 安装 Remote - SSH 扩展
2. 按 F1 → Remote-SSH: Connect to Host
3. 输入: root@8.134.80.158

## 快速安装（复制粘贴）

### 第一步：更新系统并安装 Java 21
```bash
sudo dnf update -y
sudo dnf install -y java-21-openjdk-devel
java -version
```

### 第二步：安装 MySQL 8.0
```bash
sudo dnf install -y mysql-server
sudo systemctl start mysqld
sudo systemctl enable mysqld
sudo mysql_secure_installation
```
按照提示设置root密码，其他选项全部选 Y

### 第三步：安装 Maven
```bash
sudo dnf install -y maven
mvn -v
```

### 第四步：创建数据库
```bash
sudo mysql -u root -p
```

在MySQL中执行：
```sql
CREATE DATABASE glimmerseed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'admin'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON glimmerseed.* TO 'admin'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 第五步：上传项目
```bash
# 在本地电脑执行，上传项目到服务器
scp -r /path/to/backend root@8.134.80.158:/root/glimmerseed
```

或者在服务器上使用git克隆：
```bash
cd /root
git clone <your-repo-url> glimmerseed
```

### 第六步：构建并运行
```bash
cd /root/glimmerseed/backend
mvn clean package -DskipTests
java -jar target/glimmerseed-backend-1.0.0.jar
```

### 第七步：开放端口
```bash
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

## 阿里云安全组配置
1. 登录阿里云控制台
2. 进入 ECS → 安全组
3. 添加入方向规则：
   - 协议: TCP
   - 端口: 8080
   - 来源: 0.0.0.0/0

## 验证部署
在浏览器访问：
```
http://8.134.80.158:8080/api/auth/login
```

## 创建系统服务（后台运行）

创建服务文件：
```bash
sudo nano /etc/systemd/system/glimmerseed.service
```

写入以下内容：
```ini
[Unit]
Description=Glimmerseed Backend Service
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
sudo systemctl daemon-reload
sudo systemctl enable glimmerseed
sudo systemctl start glimmerseed
sudo systemctl status glimmerseed
```

服务管理命令：
```bash
sudo systemctl start glimmerseed    # 启动
sudo systemctl stop glimmerseed     # 停止
sudo systemctl restart glimmerseed  # 重启
sudo systemctl status glimmerseed   # 状态
sudo journalctl -u glimmerseed -f   # 查看日志
```

## 测试 API

### 登录
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"password"}'
```

### 创建桌宠
```bash
curl -X POST http://localhost:8080/api/pets \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{"name":"小萌","appearance":"可爱猫咪","personality":"活泼开朗","color":"粉色"}'
```

### 发送消息
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -d '{"petId":1,"content":"你好"}'
```

## 常见问题

### 1. MySQL连接失败
```bash
# 检查MySQL状态
sudo systemctl status mysqld

# 检查端口
sudo netstat -tlnp | grep 3306
```

### 2. 端口被占用
```bash
# 查看8080端口占用
sudo netstat -tlnp | grep 8080

# 或修改application.yml中的端口
```

### 3. 外网无法访问
- 检查阿里云安全组是否开放8080端口
- 检查服务器防火墙状态
```bash
sudo firewall-cmd --list-all
```

### 4. 构建失败
- 检查Java版本: java -version
- 检查Maven: mvn -v
- 清理并重试: mvn clean

## 性能优化（可选）

### 设置Java内存
在启动命令中添加：
```bash
java -Xms512m -Xmx1024m -jar target/glimmerseed-backend-1.0.0.jar
```

### 使用Nginx反向代理
```bash
sudo dnf install -y nginx
sudo nano /etc/nginx/conf.d/glimmerseed.conf
```

写入配置：
```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

启用配置：
```bash
sudo systemctl enable nginx
sudo systemctl start nginx
```

## 数据备份

### 备份数据库
```bash
mysqldump -u admin -p glimmerseed > backup_$(date +%Y%m%d).sql
```

### 恢复数据库
```bash
mysql -u admin -p glimmerseed < backup_file.sql
```