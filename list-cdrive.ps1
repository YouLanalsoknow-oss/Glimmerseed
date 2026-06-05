$server = "8.134.80.158"
$password = "5-iKje5Q.Dt48fw"
$securePassword = ConvertTo-SecureString $password -AsPlainText -Force
$credential = New-Object System.Management.Automation.PSCredential("$server\Administrator", $securePassword)

Write-Host "========================================"
Write-Host "  查看服务器C盘"
Write-Host "========================================"
Write-Host ""

try {
    Write-Host "[1] 连接到C盘..."
    New-PSDrive -Name "S" -PSProvider FileSystem -Root "\\$server\C$" -Credential $credential -ErrorAction Stop | Out-Null
    Write-Host "    ✓ 连接成功" -ForegroundColor Green

    Write-Host ""
    Write-Host "[2] C盘根目录内容:" -ForegroundColor Cyan
    Get-ChildItem "S:\" -ErrorAction SilentlyContinue | Select-Object Name, Mode | Format-Table -AutoSize

    Write-Host ""
    Write-Host "[3] 查找包含下载/down/Download的目录:" -ForegroundColor Cyan
    Get-ChildItem "S:\Users" -Directory -ErrorAction SilentlyContinue | ForEach-Object {
        $userPath = "S:\Users\$($_.Name)"
        if (Test-Path "$userPath\Downloads") {
            Write-Host ""
            Write-Host "  找到 Downloads 目录: $userPath\Downloads" -ForegroundColor Green
            Write-Host "  文件列表:" -ForegroundColor Yellow
            Get-ChildItem "$userPath\Downloads" -ErrorAction SilentlyContinue | Select-Object Name, @{Name='Size(MB)';Expression={[math]::Round($_.Length/1MB, 2)}} | Format-Table -AutoSize
        } elseif (Test-Path "$userPath\下载") {
            Write-Host ""
            Write-Host "  找到 下载 目录: $userPath\下载" -ForegroundColor Green
            Write-Host "  文件列表:" -ForegroundColor Yellow
            Get-ChildItem "$userPath\下载" -ErrorAction SilentlyContinue | Select-Object Name, @{Name='Size(MB)';Expression={[math]::Round($_.Length/1MB, 2)}} | Format-Table -AutoSize
        }
    }

    Write-Host ""
    Write-Host "[4] 扫描整个C盘查找安装包..." -ForegroundColor Cyan
    Write-Host "    正在查找 .exe 和 .msi 文件..."
    $exeFiles = Get-ChildItem "S:\" -Recurse -Filter "*.exe" -ErrorAction SilentlyContinue -Depth 3 | Select-Object -First 20 FullName, Length
    $msiFiles = Get-ChildItem "S:\" -Recurse -Filter "*.msi" -ErrorAction SilentlyContinue -Depth 3 | Select-Object -First 20 FullName, Length

    if ($exeFiles.Count -gt 0) {
        Write-Host ""
        Write-Host "  找到 $($exeFiles.Count) 个 .exe 文件:" -ForegroundColor Green
        $exeFiles | Format-Table -AutoSize
    }

    if ($msiFiles.Count -gt 0) {
        Write-Host ""
        Write-Host "  找到 $($msiFiles.Count) 个 .msi 文件:" -ForegroundColor Green
        $msiFiles | Format-Table -AutoSize
    }

    Write-Host ""
    Write-Host "[5] 断开连接..."
    Remove-PSDrive -Name "S" -Force -ErrorAction SilentlyContinue

} catch {
    Write-Host ""
    Write-Host "✗ 错误: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""
    Write-Host "错误堆栈:" -ForegroundColor Yellow
    Write-Host $_.ScriptStackTrace
}

Write-Host ""
Write-Host "========================================"