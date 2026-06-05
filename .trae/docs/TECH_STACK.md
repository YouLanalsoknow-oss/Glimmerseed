# Glimmerseed技术栈详情

## 项目概述
Glimmerseed是自研2D骨骼动画编辑器+安卓悬浮桌宠+本地离线AI对话工具，全项目Skeleton骨骼、IK、PoseSnapshot姿势系统、RenderEngine渲染引擎全部自研，仅做第三方格式兼容：支持导入/导出Live2D、Spine文件。

## 项目架构概览

```
Glimmerseed/
├── app/                    # Android应用主模块
├── editor-core/           # 编辑器核心库
├── render-engine/         # 渲染引擎库
├── floating-preview-base/ # 悬浮窗桌宠基础库
├── backend/               # Java Spring Boot后端
├── backend_fastapi/       # FastAPI后端（本地部署）
├── editor-preview/        # Web编辑器预览
└── server-deploy/         # 服务器部署脚本
```

## 模块详解

### 1. app 模块
- **职责**: Android应用主入口，编辑器UI，用户交互
- **主要技术**:
  - Kotlin 1.9.24
  - Jetpack Compose
  - Material Design 3
  - ViewModel + StateFlow
  - Room (本地数据存储)
  - DataStore (偏好设置)

### 2. editor-core 模块
- **职责**: 编辑器核心逻辑，与UI无关的纯Kotlin库
- **关键组件**:
  - **Skeleton**: 骨骼系统
  - **PoseSnapshot**: 姿势快照系统
  - **Animation**: 动画系统
  - **IKManager**: IK反向运动学
  - **ExportEngine**: 导出引擎
  - **EditorViewModel**: 编辑器状态管理

### 3. render-engine 模块
- **职责**: 自研RenderEngine，无依赖Live2D SDK，仅实现L2D/Spine格式IO解析，2D骨骼模型渲染，OpenGL ES
- **技术**:
  - OpenGL ES 3.0+
  - GLSL着色器
  - 纹理管理

### 4. floating-preview-base 模块
- **职责**: 悬浮窗桌宠功能
- **技术**:
  - WindowManager
  - FloatingView
  - 触摸事件处理

## 依赖管理

### Gradle配置
- **Gradle版本**: 8.13
- **Android Gradle插件**: 8.13.2
- **使用版本目录**: libs.versions.toml

### 核心依赖
```kotlin
// Kotlin与Android
implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
implementation("androidx.core:core-ktx:1.13.1")

// Compose
implementation(platform("androidx.compose:compose-bom:2024.03.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// 架构组件
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

// 协程
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// 序列化
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

// 测试
testImplementation("junit:junit:4.13.2")
androidTestImplementation("androidx.test.ext:junit:1.1.5")
```

## 后端技术栈

### Java Spring Boot (backend/)
- **Java版本**: 21
- **框架**: Spring Boot 3.x
- **数据库**: PostgreSQL + PGVector
- **ORM**: Spring Data JPA
- **构建工具**: Maven
- **关键依赖**:
  - Spring Web
  - Spring Security
  - Spring Data JPA
  - PostgreSQL Driver

### FastAPI (backend_fastapi/)
- **Python版本**: 3.14
- **框架**: FastAPI
- **数据库**: PostgreSQL + PGVector
- **ORM**: SQLAlchemy
- **关键依赖**:
  - fastapi
  - uvicorn
  - sqlalchemy
  - psycopg2-binary

## 前端/预览技术 (editor-preview/)
- **框架**: React 18
- **语言**: TypeScript
- **构建工具**: Vite
- **样式**: Tailwind CSS
- **状态管理**: 自定义hooks (useEditorStore)
- **关键库**:
  - react-router-dom
  - canvas 2D API

## AI相关技术
- **大模型**: Qwen-VL (主), MiniCPM (备)
- **部署方式**: 本地部署
- **向量数据库**: PGVector (PostgreSQL扩展)
- **推理框架**: Hugging Face Transformers

## 开发环境配置

### 推荐开发环境
- **操作系统**: Windows 10/11, Linux
- **IDE**: Android Studio Hedgehog (2023.1.1) 或更高
- **JDK**: JDK 17 (Android) / JDK 21 (后端)
- **Node.js**: 18.x 或更高 (editor-preview)
- **Python**: 3.10+ (FastAPI后端)

### 硬件配置
- **最低配置**: 
  - CPU: 4核
  - 内存: 8GB
  - 存储: 128GB
- **推荐配置**:
  - CPU: 8核+
  - 内存: 16GB+
  - 存储: 256GB SSD

## 代码质量工具

### 格式化工具
- **Spotless**: 6.25.0
- **ktlint**: 1.0.1
- **运行方式**:
  ```bash
  ./gradlew spotlessApply   # 格式化
  ./gradlew spotlessCheck   # 检查
  ```

### 静态分析
- **Lint**: Android Lint
- **配置**: app/lint.xml

### 测试框架
- **单元测试**: JUnit 4
- **运行方式**:
  ```bash
  ./gradlew test                  # 运行所有单元测试
  ./gradlew :editor-core:test    # 仅运行editor-core测试
  ```

## 构建与部署

### Android APK构建
```bash
# Debug构建
./gradlew assembleDebug

# Release构建
./gradlew assembleRelease
```
- **输出位置**: app/build/outputs/apk/

### 后端部署
- **Java版**: Maven构建，JAR包部署
- **FastAPI版**: 直接运行或Docker部署

## Git与版本控制
- **推荐Git工作流**: Git Flow / GitHub Flow
- **主分支**: main
- **开发分支**: develop
- **功能分支**: feature/xxx
- **修复分支**: hotfix/xxx
