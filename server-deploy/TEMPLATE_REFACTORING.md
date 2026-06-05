# 配置文件模板重构说明

## 变更概述

本次重构将配置文件生成逻辑从内联方式提取到独立的模板文件，消除了 `deploy-all.ps1` 和 `4-configure.bat` 中的重复代码。

## 变更文件

1. **新增** `application.yml.template`
   - 配置文件模板，使用 `{{PLACEHOLDER}}` 格式标记动态配置项

2. **修改** `deploy-all.ps1`
   - 移除了第32-54行的内联配置模板代码
   - 改为读取模板文件并进行占位符替换

3. **修改** `4-configure.bat`
   - 移除了第12-39行的内联配置模板代码
   - 改为调用 PowerShell 进行模板读取和占位符替换

## 占位符说明

模板文件中的占位符：
- `{{DB_PASSWORD}}` - 数据库密码
- `{{API_KEY}}` - SiliconFlow API Key

## 使用方法

### 使用 PowerShell 脚本 (deploy-all.ps1)
```powershell
.\deploy-all.ps1
```
脚本会自动读取模板文件并替换占位符。

### 使用 Batch 脚本 (4-configure.bat)
```batch
4-configure.bat
```
脚本会自动调用 PowerShell 进行模板处理。

## 维护说明

如需修改配置结构或添加新配置项，只需编辑 `application.yml.template` 文件即可，无需修改两个脚本。

## 测试验证

运行测试脚本验证功能：
```powershell
.\test-template-replacement.ps1
```

## 注意事项

1. 确保 `application.yml.template` 文件与脚本位于同一目录
2. 确保系统已安装 PowerShell（用于 Batch 脚本的模板处理）
3. 模板文件中的占位符必须使用 `{{PLACEHOLDER}}` 格式
