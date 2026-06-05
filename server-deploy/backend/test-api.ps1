# ================================
#   Glimmerseed API 测试脚本
# ================================

$BaseUrl = "http://8.134.80.158:8080/api"

Write-Host "================================"
Write-Host "  Glimmerseed API 测试"
Write-Host "================================"
Write-Host ""

# 测试1: 用户登录
Write-Host "[测试1] 用户登录..."
$loginBody = @{
    email = "admin@example.com"
    password = "password"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$BaseUrl/auth/login" -Method POST -ContentType "application/json" -Body $loginBody
    Write-Host "✓ 登录成功" -ForegroundColor Green
    Write-Host "  Token: $($loginResponse.token.Substring(0, [Math]::Min(20, $loginResponse.token.Length)))..." -ForegroundColor Gray
    $userId = $loginResponse.userId
} catch {
    Write-Host "✗ 登录失败: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 测试2: 获取桌宠列表
Write-Host "[测试2] 获取桌宠列表..."
try {
    $petsResponse = Invoke-RestMethod -Uri "$BaseUrl/pets" -Method GET -Headers @{"X-User-Id" = $userId}
    Write-Host "✓ 获取成功" -ForegroundColor Green
    Write-Host "  桌宠数量: $($petsResponse.data.Count)" -ForegroundColor Gray
} catch {
    Write-Host "✗ 获取失败: $_" -ForegroundColor Red
}

Write-Host ""

# 测试3: 创建桌宠
Write-Host "[测试3] 创建桌宠..."
$petBody = @{
    name = "小萌"
    appearance = "可爱猫咪"
    personality = "活泼开朗"
    color = "粉色"
} | ConvertTo-Json

try {
    $createPetResponse = Invoke-RestMethod -Uri "$BaseUrl/pets" -Method POST -ContentType "application/json" -Headers @{"X-User-Id" = $userId} -Body $petBody
    Write-Host "✓ 创建成功" -ForegroundColor Green
    Write-Host "  桌宠ID: $($createPetResponse.data.id)" -ForegroundColor Gray
    Write-Host "  桌宠名称: $($createPetResponse.data.name)" -ForegroundColor Gray
    $petId = $createPetResponse.data.id
} catch {
    Write-Host "✗ 创建失败: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# 测试4: 发送消息
Write-Host "[测试4] 发送消息..."
$chatBody = @{
    petId = $petId
    content = "你好呀，你叫什么名字？"
} | ConvertTo-Json

Write-Host "  发送: 你好呀，你叫什么名字？" -ForegroundColor Cyan

try {
    $chatResponse = Invoke-RestMethod -Uri "$BaseUrl/chat" -Method POST -ContentType "application/json" -Headers @{"X-User-Id" = $userId} -Body $chatBody
    Write-Host "✓ 发送成功" -ForegroundColor Green
    Write-Host "  桌宠回复: $($chatResponse.data.content)" -ForegroundColor Cyan
} catch {
    Write-Host "✗ 发送失败: $_" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================"
Write-Host "  测试完成！"
Write-Host "================================"