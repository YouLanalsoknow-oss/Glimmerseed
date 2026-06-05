
# Glimmerseed 一键部署脚本
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Glimmerseed 服务器一键部署" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查管理员权限
if (-not ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "[!] 请以管理员身份运行此脚本！" -ForegroundColor Red
    pause
    exit
}

# 获取输入
Write-Host "[1/5] 收集配置信息..." -ForegroundColor Yellow
$mysqlRootPass = Read-Host "请输入 MySQL root 密码"
$siliconFlowApiKey = Read-Host "请输入 SiliconFlow API Key"
Write-Host ""

# 步骤 1: 初始化数据库
Write-Host "[2/5] 初始化数据库..." -ForegroundColor Yellow
$initDbScript = Get-Content "init_db.sql" -Raw
$mysqlCommand = "mysql -u root -p$mysqlRootPass -e `"$initDbScript`""
Invoke-Expression $mysqlCommand
if ($LASTEXITCODE -eq 0) {
    Write-Host "  [OK] 数据库初始化成功" -ForegroundColor Green
} else {
    Write-Host "  [X] 数据库初始化失败" -ForegroundColor Red
    pause
    exit
}
Write-Host ""

# 步骤 2: 生成配置文件
Write-Host "[3/5] 生成配置文件..." -ForegroundColor Yellow
$templatePath = Join-Path $PSScriptRoot "application.yml.template"
if (-not (Test-Path $templatePath)) {
    Write-Host "  [X] 配置文件模板不存在: $templatePath" -ForegroundColor Red
    pause
    exit
}
$templateContent = Get-Content $templatePath -Raw
$configContent = $templateContent -replace '\{\{DB_PASSWORD\}\}', $mysqlRootPass
$configContent = $configContent -replace '\{\{API_KEY\}\}', $siliconFlowApiKey
$configPath = "backend\src\main\resources\application.yml"
Set-Content -Path $configPath -Value $configContent
Write-Host "  [OK] 配置文件已生成" -ForegroundColor Green
Write-Host ""

# 步骤 3: 编译项目
Write-Host "[4/5] 编译后端项目..." -ForegroundColor Yellow
Push-Location backend
cmd /c "mvn clean package -DskipTests"
if ($LASTEXITCODE -eq 0) {
    Write-Host "  [OK] 编译成功" -ForegroundColor Green
} else {
    Write-Host "  [X] 编译失败" -ForegroundColor Red
    Pop-Location
    pause
    exit
}
Pop-Location
Write-Host ""

# 步骤 4: 开放防火墙端口
Write-Host "[5/5] 配置防火墙..." -ForegroundColor Yellow
netsh advfirewall firewall add rule name="Glimmerseed API" dir=in action=allow protocol=TCP localport=8080 | Out-Null
Write-Host "  [OK] 防火墙端口 8080 已开放" -ForegroundColor Green
Write-Host ""

# 完成
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  部署完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步：" -ForegroundColor Yellow
Write-Host "  1. 运行: .\backend\start.bat" -ForegroundColor White
Write-Host "  2. 在阿里云安全组开放 8080 端口" -ForegroundColor White
Write-Host "  3. 测试访问: http://8.134.80.158:8080" -ForegroundColor White
Write-Host ""
pause
