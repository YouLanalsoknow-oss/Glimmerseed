$server = "8.134.80.158"
$username = "Administrator"
$password = "5-iKje5Q.Dt48fw"

Write-Host "========================================"
Write-Host "  服务器连接测试"
Write-Host "========================================"
Write-Host ""

Write-Host "[1] 测试网络连接..."
$ping = Test-Connection -ComputerName $server -Count 2 -Quiet
if ($ping) {
    Write-Host "  ✓ 服务器在线" -ForegroundColor Green
} else {
    Write-Host "  ✗ 无法Ping通服务器" -ForegroundColor Red
}

Write-Host ""
Write-Host "[2] 测试RDP端口(3389)..."
$rdp = Test-NetConnection -ComputerName $server -Port 3389 -WarningAction SilentlyContinue
Write-Host "  TCP测试: $($rdp.TcpTestSucceeded)" -ForegroundColor $(if($rdp.TcpTestSucceeded){'Green'}else{'Red'})

Write-Host ""
Write-Host "[3] 测试SMB端口(445)..."
$smb = Test-NetConnection -ComputerName $server -Port 445 -WarningAction SilentlyContinue
Write-Host "  TCP测试: $($smb.TcpTestSucceeded)" -ForegroundColor $(if($smb.TcpTestSucceeded){'Green'}else{'Red'})

Write-Host ""
Write-Host "[4] 测试WinRM端口(5985)..."
$winrm = Test-NetConnection -ComputerName $server -Port 5985 -WarningAction SilentlyContinue
Write-Host "  TCP测试: $($winrm.TcpTestSucceeded)" -ForegroundColor $(if($winrm.TcpTestSucceeded){'Green'}else{'Red'})

Write-Host ""
Write-Host "[5] 尝试访问共享文件夹..."
try {
    $null = New-PSDrive -Name "RemoteShare" -PSProvider FileSystem -Root "\\$server\C$" -Credential (New-Object System.Management.Automation.PSCredential("$server\Administrator", (ConvertTo-SecureString $password -AsPlainText -Force))) -ErrorAction Stop
    Write-Host "  ✓ 共享访问成功" -ForegroundColor Green

    Write-Host ""
    Write-Host "[6] 检查Downloads目录..."
    if (Test-Path "RemoteShare:\Users\Administrator\Downloads") {
        Write-Host "  目录存在，文件列表:" -ForegroundColor Green
        Get-ChildItem "RemoteShare:\Users\Administrator\Downloads" | Select-Object Name, @{Name='Size(MB)';Expression={[math]::Round($_.Length/1MB, 2)}}, LastWriteTime | Format-Table -AutoSize
    } else {
        Write-Host "  Downloads目录不存在" -ForegroundColor Yellow
        Write-Host "  检查Users目录:" -ForegroundColor Yellow
        Get-ChildItem "RemoteShare:\Users" -ErrorAction SilentlyContinue | Format-Table
    }
} catch {
    Write-Host "  ✗ 共享访问失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================"