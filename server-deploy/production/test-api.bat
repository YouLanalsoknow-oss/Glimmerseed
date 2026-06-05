
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   Glimmerseed API 功能测试
echo ========================================
echo.
echo [提示] 确保服务已启动后再运行此脚本
echo.

echo ========================================
echo 1. 测试健康检查端点
echo ========================================
curl -s http://localhost:8080/actuator/health
echo.
echo.

echo ========================================
echo 2. 测试用户注册
echo ========================================
curl -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"test_user\",\"email\":\"test@glimmerseed.com\",\"password\":\"123456\"}"
echo.
echo.

echo ========================================
echo 3. 测试用户登录
echo ========================================
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"test_user\",\"password\":\"123456\"}"
echo.
echo.

echo ========================================
echo 4. 测试创建宠物
echo ========================================
curl -X POST http://localhost:8080/api/pets ^
  -H "Content-Type: application/json" ^
  -H "X-User-Id: 1" ^
  -d "{\"name\":\"测试小猫\",\"species\":\"cat\",\"personality\":\"playful\"}"
echo.
echo.

echo ========================================
echo 5. 测试获取宠物列表
echo ========================================
curl -X GET http://localhost:8080/api/pets ^
  -H "X-User-Id: 1"
echo.
echo.

echo ========================================
echo 测试完成！
echo ========================================
pause

