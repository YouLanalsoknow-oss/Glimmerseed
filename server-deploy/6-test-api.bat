
@echo off
chcp 65001 &gt;nul
echo ========================================
echo   [Step 6/6] Test API
echo ========================================
echo.

echo Waiting for server to start...
timeout /t 5 /nobreak &gt;nul
echo.

echo [1] Testing health check...
curl -s http://localhost:8080/actuator/health
echo.
echo.

echo [2] Testing register endpoint...
curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d "{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"123456\"}"
echo.
echo.

echo ========================================
echo   Test complete!
echo ========================================
echo.
pause
