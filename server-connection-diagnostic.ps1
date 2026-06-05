
# 服务器连接诊断脚本
# 服务器: 8.134.80.158

$SERVER_IP = "8.134.80.158"
$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  服务器连接诊断工具" -ForegroundColor Cyan
Write-Host "  目标服务器: $SERVER_IP" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. Ping测试
Write-Host "[1/6] 测试 ICMP Ping..." -ForegroundColor Yellow
try {
    $pingResult = Test-Connection -ComputerName $SERVER_IP -Count 2 -ErrorAction Stop
    Write-Host "  ✓ Ping 成功！" -ForegroundColor Green
    Write-Host "  延迟: $($pingResult.ResponseTime)ms" -ForegroundColor Gray
} catch {
    Write-Host "  ✗ Ping 失败: $_" -ForegroundColor Red
}
Write-Host ""

# 2. 测试常见端口
Write-Host "[2/6] 测试常用端口..." -ForegroundColor Yellow
$ports = @(3389, 5985, 5986, 445, 139, 80, 8080, 3306)
foreach ($port in $ports) {
    try {
        $tcp = New-Object System.Net.Sockets.TcpClient
        $tcp.ReceiveTimeout = 2000
        $tcp.SendTimeout = 2000
        $connect = $tcp.BeginConnect($SERVER_IP, $port, $null, $null)
        $wait = $connect.AsyncWaitHandle.WaitOne(2000, $false)
        if ($wait) {
            $tcp.EndConnect($connect)
            Write-Host "  ✓ 端口 $port 开放" -ForegroundColor Green
        } else {
            Write-Host "  ✗ 端口 $port 关闭或超时" -ForegroundColor Red
        }
        $tcp.Close()
    } catch {
        Write-Host "  ✗ 端口 $port 错误: $_" -ForegroundColor Red
    }
}
Write-Host ""

# 3. RDP连接说明
Write-Host "[3/6] RDP (远程桌面) 说明" -ForegroundColor Yellow
Write-Host "  如果端口 3389 开放的话，可以使用远程桌面连接" -ForegroundColor Gray
Write-Host "  命令: mstsc /v:$SERVER_IP" -ForegroundColor Gray
Write-Host ""

# 4. SMB文件共享说明
Write-Host "[4/6] SMB 文件共享说明" -ForegroundColor Yellow
Write-Host "  端口 445/139 用于文件共享" -ForegroundColor Gray
Write-Host "  尝试访问: \\$SERVER_IP" -ForegroundColor Gray
Write-Host ""

# 5. PowerShell Remoting测试
Write-Host "[5/6] PowerShell Remoting 测试" -ForegroundColor Yellow
Write-Host "  尝试测试 WinRM 连接..." -ForegroundColor Gray
try {
    $testSession = New-PSSession -ComputerName $SERVER_IP -ErrorAction SilentlyContinue
    if ($testSession) {
        Write-Host "  ✓ WinRM 会话创建成功！" -ForegroundColor Green
        Remove-PSSession $testSession
    } else {
        Write-Host "  ✗ WinRM 连接失败" -ForegroundColor Red
        Write-Host "  可能的原因:" -ForegroundColor Gray
        Write-Host "    - 安全组未开放 5985/5986 端口" -ForegroundColor Gray
        Write-Host "    - 服务器未启用 WinRM" -ForegroundColor Gray
        Write-Host "    - 认证问题" -ForegroundColor Gray
    }
} catch {
    Write-Host "  ✗ WinRM 错误: $_" -ForegroundColor Red
}
Write-Host ""

# 6. 建议的连接方式
Write-Host "[6/6] 推荐的连接方式" -ForegroundColor Yellow
Write-Host ""
Write-Host "  方式 1: 使用远程桌面 (RDP) - 最简单" -ForegroundColor Cyan
Write-Host "    按 Win+R，输入: mstsc /v:$SERVER_IP" -ForegroundColor Gray
Write-Host ""
Write-Host "  方式 2: 先在服务器上本地运行脚本" -ForegroundColor Cyan
Write-Host "    1. 通过 RDP 登录服务器" -ForegroundColor Gray
Write-Host "    2. 在服务器上创建文件夹" -ForegroundColor Gray
Write-Host "    3. 运行之前的 list-files.bat" -ForegroundColor Gray
Write-Host ""
Write-Host "  方式 3: 配置 WinRM (需要先在服务器上配置)" -ForegroundColor Cyan
Write-Host "    在服务器上运行:" -ForegroundColor Gray
Write-Host "    Enable-PSRemoting -Force" -ForegroundColor Gray
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  诊断完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
