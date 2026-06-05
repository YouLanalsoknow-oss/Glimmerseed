# 服务器连接信息
$server = "8.134.80.158"
$username = "Administrator"
$password = "5-iKje5Q.Dt48fw"

Write-Host "========================================"
Write-Host "  连接到服务器"
Write-Host "========================================"
Write-Host ""

# 转换密码为SecureString
$securePassword = ConvertTo-SecureString $password -AsPlainText -Force
$credential = New-Object System.Management.Automation.PSCredential("$server\$username", $securePassword)

try {
    Write-Host "[1] 尝试连接共享文件夹..."
    $drive = New-PSDrive -Name "ServerC" -PSProvider FileSystem -Root "\\$server\C$" -Credential $credential -ErrorAction Stop
    Write-Host "    ✓ 连接成功" -ForegroundColor Green

    Write-Host ""
    Write-Host "[2] 检查Users目录..."
    if (Test-Path "ServerC:\Users") {
        Write-Host "    Users目录内容:" -ForegroundColor Cyan
        Get-ChildItem "ServerC:\Users" | Select-Object Name | Format-Table -AutoSize
    }

    Write-Host ""
    Write-Host "[3] 检查Administrator目录..."
    if (Test-Path "ServerC:\Users\Administrator") {
        Write-Host "    Administrator目录内容:" -ForegroundColor Cyan
        Get-ChildItem "ServerC:\Users\Administrator" -ErrorAction SilentlyContinue | Select-Object Name | Format-Table -AutoSize
    }

    Write-Host ""
    Write-Host "[4] 检查Downloads目录..."
    $downloadPath = "ServerC:\Users\Administrator\Downloads"
    if (Test-Path $downloadPath) {
        Write-Host "    Downloads目录内容:" -ForegroundColor Green
        Get-ChildItem $downloadPath | Select-Object Name, @{Name='Size(MB)';Expression={[math]::Round($_.Length/1MB, 2)}}, LastWriteTime | Format-Table -AutoSize
    } else {
        Write-Host "    Downloads目录不存在" -ForegroundColor Yellow
        Write-Host "    检查当前目录下的子目录:" -ForegroundColor Yellow
        Get-ChildItem "ServerC:\Users\Administrator" -Directory -ErrorAction SilentlyContinue | Select-Object Name | Format-Table -AutoSize
    }

    Write-Host ""
    Write-Host "[5] 断开连接..."
    Remove-PSDrive -Name "ServerC" -Force -ErrorAction SilentlyContinue

} catch {
    Write-Host ""
    Write-Host "✗ 连接失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "错误详情:" -ForegroundColor Yellow
    Write-Host $_.ScriptStackTrace
}

Write-Host ""
Write-Host "========================================"