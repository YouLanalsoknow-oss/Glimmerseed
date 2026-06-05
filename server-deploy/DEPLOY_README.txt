
================================================================================
                       Glimmerseed 服务器部署指南
================================================================================

第一步：将文件复制到服务器
---------------------------
1. 通过远程桌面连接到服务器 (8.134.80.158)
2. 将整个 server-deploy 文件夹复制到服务器的 C:\ 盘
3. 将 backend 文件夹也复制到 C:\server-deploy\
4. 最终目录结构：
   C:\server-deploy\
   ├── 1-check-server.bat
   ├── 2-setup-mysql.bat
   ├── 3-build-backend.bat
   ├── 4-configure.bat
   ├── 5-start-server.bat
   ├── 6-test-api.bat
   ├── DEPLOY_README.txt
   ├── init_db.sql
   └── backend\
       ├── pom.xml
       ├── build.bat
       ├── start.bat
       └── ...

第二步：按顺序运行脚本
-----------------------
在服务器上打开命令提示符（CMD），进入 C:\server-deploy\，依次运行：

1-check-server.bat     - 检查 Java/Maven/MySQL 是否安装
2-setup-mysql.bat      - 初始化数据库
3-build-backend.bat    - 编译项目
4-configure.bat        - 配置数据库密码和 API Key
5-start-server.bat     - 启动服务
6-test-api.bat         - 测试 API

第三步：配置防火墙
-------------------
如果需要，开放 Windows 防火墙的 8080 端口：
  netsh advfirewall firewall add rule name="Glimmerseed API" dir=in action=allow protocol=TCP localport=8080

第四步：配置阿里云安全组
-------------------------
登录阿里云控制台，开放安全组的 8080 端口

================================================================================
