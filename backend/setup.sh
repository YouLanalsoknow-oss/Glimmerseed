#!/bin/bash

echo "=========================================="
echo "  Glimmerseed 后端一键安装脚本"
echo "=========================================="
echo ""

# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then
    echo "请使用 root 权限运行此脚本"
    echo "运行: sudo ./setup.sh"
    exit 1
fi

echo "[步骤1/5] 更新系统..."
dnf update -y
echo "✓ 系统更新完成"
echo ""

echo "[步骤2/5] 安装 Java 21..."
dnf install -y java-21-openjdk-devel
echo "✓ Java 21 安装完成"
java -version
echo ""

echo "[步骤3/5] 安装 MySQL 8.0..."
dnf install -y mysql-server
systemctl start mysqld
systemctl enable mysqld
echo "✓ MySQL 8.0 安装完成"
echo ""

echo "[步骤4/5] 配置 MySQL..."
echo "请设置 MySQL root 密码:"
mysql_secure_installation
echo "✓ MySQL 配置完成"
echo ""

echo "[步骤5/5] 安装 Maven..."
dnf install -y maven
echo "✓ Maven 安装完成"
mvn -v
echo ""

echo "=========================================="
echo "  环境安装完成！"
echo "=========================================="
echo ""
echo "下一步操作："
echo "1. 创建数据库"
echo "2. 上传后端项目"
echo "3. 启动服务"
echo ""
echo "请参考 README_LINUX.txt 获取详细步骤"