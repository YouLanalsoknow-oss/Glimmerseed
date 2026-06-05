# Debug Session: startup-crash

## Session Info
- **Session ID**: startup-crash
- **Created**: 2026-06-01
- **Status**: [OPEN]
- **Bug**: 软件启动时闪退

## Hypotheses

1. **H1: Database Initialization Failure**
   - AppDatabase.getInstance() 在主线程调用时可能阻塞或抛出异常
   - 证据点: onCreate 中的初始化顺序

2. **H2: HistoryManager/Repository Initialization Failure**
   - HistoryManagerImpl.getInstance() 或 HistoryRepository 创建时可能 NPE
   - 证据点: EditorViewModelFactory 中获取 HistoryManager

3. **H3: DataStore Initialization Failure**
   - TokenManager/SettingsManager/DeviceIdManager 的 init 方法调用问题
   - 证据点: Application 或 Activity 初始化顺序

4. **H4: EditorViewModelFactory Dependency Issue**
   - EditorViewModelFactory 创建时 Context 可能不完整
   - 证据点: Factory 创建逻辑

5. **H5: Room Migration Error**
   - 数据库从 v1 升级到 v2 时迁移失败
   - 证据点: AppDatabase version = 2

## Investigation Log

### Step 1: 收集证据
- 检查 Application 类初始化
- 检查 Activity onCreate 顺序
- 检查 HistoryManagerImpl 单例模式

## Status: IN_PROGRESS
