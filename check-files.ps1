# 在服务器上运行这个脚本查看Downloads目录
Write-Host "========================================"
Write-Host "  检查服务器上的文件"
Write-Host "========================================"
Write-Host ""

# 检查当前目录
Write-Host "[1] 当前目录:"
Get-Location

Write-Host ""
Write-Host "[2] 用户下载目录:"
$downloadDir = "$env:USERPROFILE\Downloads"
if (Test-Path $downloadDir) {
    Write-Host "  ✓ 找到 $downloadDir" -ForegroundColor Green
    Write-Host ""
    Write-Host "  文件列表:" -ForegroundColor Cyan
    Get-ChildItem $downloadDir | Select-Object Name, @{Name='Size(MB)';Expression={[math]::Round($_.Length/1MB, 2)}}, LastWriteTime | Format-Table -AutoSize
} else {
    Write-Host "  ✗ 未找到 $downloadDir" -ForegroundColor Red
    Write-Host ""
    Write-Host "  检查用户配置文件目录:" -ForegroundColor Yellow
    Get-ChildItem $env:USERPROFILE | Select-Object Name, Mode | Format-Table -AutoSize
}

Write-Host ""
Write-Host "[3] C盘根目录:"
Get-ChildItem "C:\" | Select-Object Name, Mode | Format-Table -AutoSize

Write-Host ""
Write-Host "[4] 查找所有 .exe 文件(递归搜索):" -ForegroundColor Cyan
$exeFiles = Get-ChildItem "C:\" -Recurse -Filter "*.exe" -ErrorAction SilentlyContinue -Depth 3
if ($exeFiles.Count -gt 0) {
    Write-Host ""
    Write-Host "  找到 $($exeFiles.Count) 个 .exe 文件:" -ForegroundColor Green
    $exeFiles | Select-Object -First 30 FullName, @{Name='Size(MB)';Expression={[math]::Round($_.Length/1MB, 2)}} | Format-Table -AutoSize
}

Write-Host ""
Write-Host "[5] 查找所有 .msi 文件(递归搜索):" -ForegroundColor Cyan
$msiFiles = Get-ChildItem "C:\" -Recurse -Filter "*.msi" -ErrorAction SilentlyContinue -Depth 3
if ($msiFiles.Count -gt 0) {
    Write-Host ""
    Write-Host "  找到 $($msiFiles.Count) 个 .msi 文件:" -ForegroundColor Green
    $msiFiles | Select-Object -First 30 FullName, @{Name='Size(MB)';Expression={[math]::Round($_.Length/1MB, 2)}} | Format-Table -AutoSize
}

Write-Host ""
Write-Host "[6] 查找所有 .zip 文件:" -ForegroundColor Cyan
$zipFiles = Get-ChildItem "C:\" -Recurse -Filter "*.zip" -ErrorAction SilentlyContinue -Depth 3
if ($zipFiles.Count -gt 0) {
    Write-Host ""
    Write-Host "  找到 $($zipFiles.Count) 个 .zip 文件:" -ForegroundColor Green
    $zipFiles | Select-Object -First 30 FullName, @{Name='Size(MB)';Expression={[math]::Round($_.Length/1MB, 2)}} | Format-Table -AutoSize
}

Write-Host ""
Write-Host "========================================"