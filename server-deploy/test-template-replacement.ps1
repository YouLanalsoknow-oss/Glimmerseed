Write-Host "Testing template replacement logic..." -ForegroundColor Cyan
Write-Host ""

$testPassword = "testPassword123"
$testApiKey = "testApiKey456"
$templatePath = "application.yml.template"
$outputPath = "backend\src\main\resources\application.yml"

if (-not (Test-Path $templatePath)) {
    Write-Host "[FAIL] Template file not found: $templatePath" -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Template file found" -ForegroundColor Green

$templateContent = Get-Content $templatePath -Raw
$configContent = $templateContent -replace '\{\{DB_PASSWORD\}\}', $testPassword
$configContent = $configContent -replace '\{\{API_KEY\}\}', $testApiKey

if ($configContent -match '\{\{.*?\}\}') {
    Write-Host "[FAIL] Placeholders not fully replaced" -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Placeholders replaced successfully" -ForegroundColor Green

Set-Content -Path $outputPath -Value $configContent

if (-not (Test-Path $outputPath)) {
    Write-Host "[FAIL] Output file not created" -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Output file created" -ForegroundColor Green

$outputContent = Get-Content $outputPath -Raw
if ($outputContent -match $testPassword -and $outputContent -match $testApiKey) {
    Write-Host "[OK] Test values found in output" -ForegroundColor Green
} else {
    Write-Host "[FAIL] Test values not found in output" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All tests passed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
