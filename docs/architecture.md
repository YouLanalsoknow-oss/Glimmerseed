# 模块全景——修订版 v2（多面板交互系统）

---

## 文档说明

- **定位**：项目架构设计蓝图 + 现状说明 + 升级路径
- **维护周期**：每季度回顾一次"现状说明"和"升级路径"
- **相关文档**：[决策日志](./decision-log.md)

---

## 产品定位声明

```
本项目定位：用户自定义手机多面板交互系统
面向目标：让用户在任意应用上层
自由编排可交互的自定义面板
不是：装饰工具、桌面主题、宠物应用
```

---

### 基础层

```
数据模型
├── Kotlin data class（Panel / Stage / Layer）
│   └── 选择理由：原生支持copy()、解构，与kotlinx-serialization天然配合
├── 序列化：kotlinx-serialization（统一全项目）
│   ├── 放弃理由（Moshi）：项目已有kotlinx-serialization，避免两套方案并存
│   └── 迁移说明：Gson遗留用法逐步迁移，不强求一次性改完
└── 版本迁移：手写迁移链
    └── 选择理由：迁移逻辑线性简单，Room Migration机制过重，JSON文件迁移手写足够

资产管理
├── 图片复制：kotlin.io 文件操作（不引入第三方）
├── EXIF修正：ExifInterface（AndroidX官方）
└── 降采样：BitmapFactory.Options.inSampleSize（系统原生）
    └── 选择理由：无需第三方，与Canvas渲染直接衔接，控制精确

数据持久化
├── project.json / stage.json：App私有目录文件读写
├── stage.json变更
│   ├── 所有坐标/尺寸字段改为Float
│   ├── 横竖屏布局各自独立归一化
│   └── 新增画布基准信息字段（仅用于调试）
└── 运行时状态快照：DataStore
    ├── 放弃理由（SharedPreferences）：非类型安全，协程支持差
    └── 选择理由（DataStore）：协程友好，类型安全，适合状态持久化
```

---

### 编辑器层

```
图片输入管线
├── 系统相册：PhotoPicker（API 33+）/ ACTION_PICK（兼容低版本）
│   └── 选择理由：PhotoPicker无需READ_MEDIA权限，优先使用
├── 相机：FileProvider + ACTION_IMAGE_CAPTURE
└── 文件管理器：ACTION_OPEN_DOCUMENT

多图层编辑器
├── 容器：单一自定义Compose Canvas
│   ├── 放弃理由（ViewGroup方案）：项目使用Compose，View体系方案不适用
│   └── 选择理由：Compose Canvas统一管理所有图层绘制，命中测试手写实现
├── 手势识别：Compose detectTransformGestures
│   └── 选择理由：Compose原生手势API，支持多指缩放旋转平移
├── 图层树操作：ViewModel持有 SnapshotStateList<Layer>
│   └── 选择理由：Compose状态感知，图层变化自动触发重组
└── 命中测试：Matrix逆变换点击坐标到图层本地坐标系

编辑器变更
├── 操作归一化坐标
│   └── 编辑器中操作面板位置/尺寸时，存储为Float百分比
├── 预览时映射到当前设备屏幕尺寸
└── 更换设备或横竖屏切换时自动重算

横竖屏双布局
└── Compose的WindowSizeClass + 监听LocalConfiguration
    ├── 放弃理由（configChanges手动处理）：View体系方案，Compose项目不适用
    └── 选择理由：Compose原生响应Configuration变化，轻量且声明式

字体读取
└── SystemFonts.getAvailableFonts()（API 29+）
    └── API 29以下：读取/system/fonts/目录
        └── 选择理由：读取系统全部可用字体，无需维护字体列表

Canvas渲染器（核心引擎）
├── 实现：Compose Canvas + DrawScope
├── 帧驱动：withFrameNanos（Compose协程动画API）
│   └── 选择理由：精确同步VSYNC，比Handler.postDelayed()稳定
│       比Choreographer在Compose项目中更自然
├── Bitmap管理：LruCache（按内存上限自动淘汰）
├── 图层合成：DrawScope.withTransform() 应用各图层Matrix
└── 复用说明：此渲染器同时服务编辑器预览、舞台预览
            悬浮窗使用其View体系移植版本
            （悬浮窗无法直接宿主Compose，需单独处理）
```

---

### 渲染逻辑共享策略（Compose vs View）

渲染逻辑抽象为纯Kotlin层，不依赖任何UI框架，Compose渲染器和View渲染器都是它的消费者：

```
纯Kotlin渲染逻辑层（RenderEngine）
├── 输入：图层列表 + 当前帧索引 + 画布尺寸
├── 输出：每帧的绘制指令序列（DrawCommand列表）
└── 不持有任何Canvas / DrawScope引用

Compose侧（编辑器/舞台预览）
└── DrawScope消费DrawCommand列表执行绘制

View侧（悬浮窗）
└── Canvas消费同一份DrawCommand列表执行绘制
```

DrawCommand是平台无关的绘制描述，类似"在坐标X/Y绘制这个Bitmap，应用这个Matrix"，不包含任何平台API调用。

状态同步策略：
├── 主进程内（编辑器→舞台预览）：实时StateFlow
│   └── 编辑器修改面板后，通过StateFlow通知舞台预览
│   └── 舞台预览实时反映未导出的编辑状态
└── 悬浮窗运行时更新：导出后FileObserver
    └── 与主进程内的更新通道明确区分

通信方式：单进程架构下用StateFlow，悬浮窗Service订阅主进程ViewModel暴露的Flow即可。双进程守护下用Messenger传递简单指令（切换产物路径、暂停、恢复），不需要Binder完整实现，也不引入EventBus。

历史记录
└── Command Pattern（手写）
    ├── 每个操作封装为Command对象（execute + undo）
    ├── 历史栈：ArrayDeque<Command>（内存中）
    ├── 当前阶段：会话结束丢弃（阶段性决策）
    ├── Command设计原则
    │   ├── 无状态设计：只存操作类型+参数，不存完整快照
    │   │   ├── 示例："移动图层"存图层id+位移增量，而不是移动前后的完整图层数据
    │   │   ├── 好处：体积小、序列化简单
    │   │   └── 代价：undo时需要能从当前状态反推，要求操作都是可逆的
    │   ├── 跨版本兼容：序列化时包含版本号，走相同的迁移链
    │   │   └── 简化策略：跨主版本历史栈可作废，降低迁移复杂度
    │   └── 操作过滤：不记录视图类操作
    │       ├── 不记录：画布缩放、平移、预览旋转（观察行为）
    │       ├── 不记录：拖动中的实时位置（只记录最终位置）
    │       └── 记录：添加图层、删除图层、修改属性、调整顺序、关键帧变更
    └── 升级路径：编辑器功能稳定后，将Command栈
                序列化持久化，存储位置视数据量
                决定是文件还是Room

导出器
├── 逐帧合成：复用渲染器逻辑，离屏渲染到Bitmap
│   └── 离屏：Bitmap.createBitmap() + Canvas(bitmap)
│       （Compose渲染器逻辑移植到View Canvas层做离屏）
├── WebP编码（待定）
│   ├── 路线A：NDK集成libwebp（完整动态WebP支持）
│   └── 路线B：PNG序列帧存本地（编码简单，体积较大）
└── 导出线程：Kotlin协程 IO dispatcher + Flow进度回调
    └── 原子替换：先写临时文件，完成后rename，防导出中断损坏旧产物
```

---

### 面板层（新增）

#### 面板的数据结构

每个面板包含三层，不包含任何独立的业务逻辑：

```
PanelData
├── id: String（面板唯一标识）
├── name: String
├── visual: VisualLayer（视觉层）
│   ├── type: VisualType（FRAME_ANIMATION | LAYER_RENDERING）
│   ├── animationId: String?（关联的帧动画资产，VisualType=FRAME_ANIMATION时使用）
│   ├── layers: List<LayerData>（图层列表，VisualType=LAYER_RENDERING时使用）
│   │   └── 每个LayerData包含
│   │       ├── asset: AssetRef（图层引用的资产）
│   │       ├── transform（归一化位置/旋转/缩放）
│   │       └── blending（混合模式/透明度）
│   └── opacity / visible（通用视觉属性）
├── interaction: InteractionLayer（交互层）
│   ├── touchMode: TouchMode（PASSTHROUGH | BLOCKING | REGION_BASED）
│   │   ├── PASSTHROUGH = 全局穿透（默认）
│   │   ├── BLOCKING = 全局拦截
│   │   └── REGION_BASED = 区域配置（部分穿透，部分拦截）
│   └── hitRegions: List<HitRegion>
│       ├── normalizedRect: NormalizedRect（归一化区域坐标）
│       ├── mode: TouchMode（该区域的触摸行为）
│       └── gestureTypes: List<GestureType>
└── behavior: BehaviorLayer（行为层）
    ├── gestureHandlers: List<GestureHandler>
    ├── systemEventHandlers: List<SystemEventHandler>
    └── panelEventHandlers: List<PanelEventHandler>
```

#### 资产引用机制

```
资产存储约定
└── projects/{projectId}/assets/{panelId}/
    └── 面板间不共享资产，避免删除一个面板影响另一个

资产引用结构（AssetRef）
├── type: AssetType（IMAGE / ANIMATION / FONT）
├── localPath: String（私有目录稳定路径）
└── fallback: FallbackBehavior（缺失时行为）
    ├── HIDE — 图片缺失时静默隐藏该图层
    ├── PLACEHOLDER — 动画缺失时显示静态占位
    └── 两种情况均打WARN日志，不崩溃

LayerData.asset 指向 AssetRef
└── 加载时根据 type 选择对应的加载器
```

#### 交互区域与视觉层的对齐

```
交互区域定义基于归一化坐标
与视觉层使用同一套坐标系
渲染器和触摸分发器共用同一套坐标转换逻辑（CoordConverter）

对齐保证
├── HitRegion.normalizedRect 与视觉图层的归一化坐标
│   使用相同的 NormalizedRect 类型
├── 渲染时 VisualLayer 内的图层位置同样归一化
│   渲染基于屏幕密度做额外补偿，统一基于dp作为逻辑单位
└── 触摸分发时 InteractionOrchestrator 调用
    与 PanelRenderer 相同的 CoordConverter
    保证命中测试与视觉位置完全一致
```

#### 行为层的复杂度边界

```
能力边界
├── 支持
│   ├── 条件触发（单条件：触摸位置在区域A内 → 执行动作B）
│   ├── 简单序列（动作A完成后 → 执行动作B）
│   └── 面板间通信（发送面板事件 / 响应面板事件）
├── 不支持
│   ├── 循环逻辑（for/while）
│   ├── 复杂条件嵌套（if-else if-else 的深度嵌套）
│   ├── 外部网络请求（HTTP/WebSocket）
│   └── 动态代码执行（eval / 脚本引擎）
└── 边界的意义
    └── 防止舞台变成脚本引擎
        保持系统的可预期性和调试可行性

行为动作类型（BehaviorAction）
├── ToggleVisibility(panelId) — 切换面板可见性
├── PlayAnimation(animationId) — 播放指定动画
├── SendEvent(eventName, payload) — 发送面板事件
├── SetTouchMode(mode) — 切换触摸模式
└── NoOp — 空操作
```

---

### 事件系统（新增）

#### 面板行为层事件来源

```
├── 手势事件（点击、长按、滑动）
│   └── 由 InteractionLayer 识别后转化为 GestureEvent
├── 系统事件（息屏、亮屏、音量变化、通知）
│   └── 由 StageService 的 BroadcastReceiver 捕获后转化为 SystemEvent
└── 面板间事件（其他面板触发的自定义事件）
    └── 由 InteractionOrchestrator 的事件总线路由
```

#### 面板间事件数据格式

```kotlin
data class PanelEvent(
    val eventType: String,       // 事件类型标识（枚举或字符串）
    val sourcePanelId: String,   // 来源面板id
    val targetPanelId: String?,  // 目标面板id（广播时为null）
    val payload: Map<String, String>  // 载荷（简单键值对）
    // 限制载荷为简单键值对的原因：
    // 防止面板间耦合过深，保持通信轻量
)
```

---

### 舞台层

#### 舞台三个核心职责

```
舞台的三个核心职责

空间编排
├── 面板层级（Z-order，后添加的在上面）
├── 横竖屏布局（各自独立归一化坐标）
│   └── 横竖屏各自独立归一化
└── 空间重叠规则

交互编排
├── 事件路由
│   ├── 触摸事件 → 命中测试 → 按Z-order分发 → 面板触摸配置判断
│   └── 分发顺序：从最上层面板开始，如果拦截则不下发
├── 手势冲突仲裁
│   ├── ACTION_DOWN时执行命中测试
│   │   └── 从最高Z轴面板向下找第一个可消费的面板
│   │       锁定为本次触摸序列的目标面板
│   ├── ACTION_MOVE / ACTION_UP
│   │   └── 持续派发给锁定面板，不切换，不回溯
│   ├── 锁定面板未识别任何手势
│   │   └── 事件不穿透到下层面板
│   │       静默丢弃，防止误触
│   └── 与Android原生事件分发的区别
│       └── 不支持事件冒泡
│           不支持拦截后回传
│           锁定即终态
├── 面板间通信（事件总线模式）
└── 触摸分发顺序

状态编排
├── 面板生命周期（创建/激活/停用/销毁）
├── 激活状态管理
└── 舞台整体暂停恢复（息屏暂停、亮屏恢复）
    └── 息屏：停止帧驱动回调 | 亮屏：从暂停帧位置恢复
```

#### 舞台与面板的关系原则

```
舞台不干涉面板内部
├── StageEngine 只编排外部关系
└── 不关心面板的具体视觉实现

面板不感知舞台
├── PanelView 只暴露事件接口
└── 不直接引用 StageEngine

面板只暴露事件接口，由舞台决定如何路由
├── 面板不知道谁消费了它的事件
└── 舞台是唯一的事件路由决策者
```

#### PanelFactory 面板实例化

```
PanelFactory 接口
└── createPanel(panelData: PanelData): PanelView

VisualType 映射
├── FRAME_ANIMATION → FrameAnimationPanelView
└── LAYER_RENDERING → LayerRenderingPanelView

调用时机
└── StageEngine.addPanel() 内部调用
    StageEngine 持有 PanelFactory 引用
    外部不直接实例化 PanelView
```

#### 舞台形态

```
固定全屏模式
├── 移除小窗模式及一切相关逻辑
├── WindowManager.LayoutParams
│   ├── TYPE_APPLICATION_OVERLAY（API 26+）
│   ├── MATCH_PARENT × MATCH_PARENT
│   ├── FLAG_NOT_FOCUSABLE
│   └── FLAG_LAYOUT_IN_SCREEN
├── 背景：PixelFormat.TRANSLUCENT
└── 每个面板独立配置触摸穿透/拦截
    └── 不再是全局 FLAG_NOT_TOUCHABLE
```

#### 舞台预览重新定位

```
原：看到所有项目叠加后的视觉效果
改：模拟真实运行时的完整交互行为
    ├── 触摸事件路由
    ├── 面板间通信
    └── 状态切换

主进程内编辑器→舞台预览的更新通道
├── 编辑器修改面板后，通过StateFlow通知舞台预览
├── 舞台预览实时反映未导出的编辑状态
└── 与悬浮窗运行时的更新通道明确区分
    ├── 主进程内：实时StateFlow
    └── 悬浮窗：导出后FileObserver
```

#### 舞台性能边界

```
同时激活面板数量上限（运行时动态评估）
├── 根据设备性能等级设定软上限
│   ├── 等级A（高端设备）：建议 ≤ 8 个激活面板
│   ├── 等级B（中端设备）：建议 ≤ 5 个激活面板
│   └── 等级C（低端设备）：建议 ≤ 3 个激活面板
├── 超出上限时舞台给出提示，不强制阻止
└── 渲染器提供帧率监控
    └── 帧率低于阈值时自动降级（降帧率优先于降质量）
```

---

### 跨设备适配

```
坐标归一化
├── 所有位置/尺寸存Float百分比（0.0~1.0）
├── 渲染前统一转换为实际像素，转换逻辑集中在渲染器入口
├── 资产按面板实际像素尺寸动态缩放，不存固定尺寸
└── 横竖屏各自独立归一化

图层尺寸同样归一化
├── 渲染时基于屏幕密度做额外补偿
└── 统一基于dp而非px作为逻辑单位
```

---

### 悬浮窗层

```
前台Service
├── 实现：继承Service，startForeground()
├── 通知：NotificationCompat（最低优先级）
├── 基础重启：START_STICKY + onTaskRemoved + AlarmManager
├── 国产ROM适配（原文档缺失，补充）
│   ├── 问题：小米/华为等ROM忽略START_STICKY，直接杀后台
│   ├── 方向：引导用户开启自启动（提供入口，用户自觉）
│   └── 代码层：双进程守护作为补充手段，非银弹
├── 进程通信：StateFlow（单进程）/ Messenger（双进程）
└── 重启恢复策略
    ├── 面板列表：从stage.json重新加载（完整恢复）
    ├── 运行时状态：从DataStore恢复激活状态
    ├── 播放位置：不追求秒级，恢复到最后已知状态
    └── 权限回收：暂停渲染保留Service，权限恢复后重建View

全屏交互面板层（替代原全屏透明画布）
├── WindowManager.LayoutParams
│   ├── TYPE_APPLICATION_OVERLAY（API 26+）
│   ├── FLAG_NOT_FOCUSABLE
│   ├── FLAG_LAYOUT_IN_SCREEN
│   └── 不再设置 FLAG_NOT_TOUCHABLE（改为面板级触摸配置）
├── 背景：PixelFormat.TRANSLUCENT
├── 尺寸：MATCH_PARENT × MATCH_PARENT
└── 触摸穿透/拦截可按面板独立配置
    ├── 全局穿透（默认）
    ├── 全局拦截
    └── 区域配置

空状态
├── 无任何激活面板时
│   └── 全透明，触摸全面穿透
│       不显示任何提示（用户主动配置的结果）
└── 最后一个面板被移除时
    └── 自动切换为全穿透状态
        不销毁Service，保持待机

渲染器（悬浮窗专用）
├── 实现：自定义View + Canvas（不用Compose）
│   └── 技术背景（悬浮窗不能用Compose的详细说明）
│       ├── WindowManager.addView()接受的是View对象，ComposeView本身是View的子类
│       ├── 真正的问题在于ComposeView需要绑定生命周期才能正常工作
│       │   ├── ViewTreeLifecycleOwner（提供Lifecycle）
│       │   ├── ViewTreeViewModelStoreOwner（提供ViewModel作用域）
│       │   ├── ViewTreeSavedStateRegistryOwner（提供状态保存）
│       │   └── 以上三者通常由Activity/Fragment自动提供
│       ├── 悬浮窗挂载在WindowManager上，没有Activity宿主，需要手动实现并注入
│       ├── 风险：Service被系统回收后的边界情况处理繁琐，极端情况下可能出现重组泄漏
│       └── 结论：技术上可行，工程上风险较高
│
│   └── 未来方向
│       ├── Compose本身不太可能在这个方向做专项优化（小众场景）
│       └── 演进：社区有成熟封装方案（手动注入LifecycleOwner）验证可用后，项目稳定期评估引入
│
│   └── 当前选择理由
│       └── View体系在悬浮窗场景经过更充分验证，维持现有决策
│
├── 帧驱动：Choreographer.postFrameCallback()
│   └── 选择理由：脱离Compose环境，Choreographer是
│               最接近VSYNC的原生方案
├── 逻辑复用：与编辑器渲染器共享图层合成算法
│           通过抽象出纯Kotlin渲染逻辑层实现
└── 渲染入口统一做归一化→实际像素的转换
    └── 转换逻辑集中在PanelRenderer入口

多面板渲染
├── 按stage.json中的Z-order叠加各面板
├── PanelRenderer入口统一做归一化→实际像素的转换
├── 横竖屏：监听Configuration，切换对应布局数据重绘
└── 资产按面板实际像素尺寸动态缩放

息屏暂停
├── BroadcastReceiver监听ACTION_SCREEN_OFF / ACTION_SCREEN_ON
├── 息屏：停止Choreographer回调
└── 亮屏：从暂停帧位置恢复

热更新导出产物
└── FileObserver监听export/目录
    └── 检测新文件写入完成后切换渲染源
```

---

### 权限层

```
悬浮窗权限
├── 检测：Settings.canDrawOverlays()
├── 引导：跳转Settings.ACTION_MANAGE_OVERLAY_PERMISSION
│   └── 时机：首次使用时引导一次，后续用户自觉
└── 防崩：每次addView前检查权限，无权限静默跳过
```

---

### 技术依赖汇总

```
必选依赖
├── AndroidX Core / AppCompat
├── Compose UI（编辑器和舞台界面）
├── Lifecycle（ProcessLifecycleOwner、ViewModel）
├── Kotlin Coroutines + Flow
├── DataStore
└── kotlinx-serialization（统一序列化，替换Gson）

按需依赖
├── ExifInterface（AndroidX）
└── libwebp NDK（动态WebP编码，待定）

刻意不引入
├── Glide / Coil（渲染层自主管理）
├── Lottie（资产格式不匹配）
├── Moshi / Gson（统一用kotlinx-serialization）
├── Room（当前阶段JSON文件足够，历史记录持久化时再评估）
├── 第三方手势库（Compose原生手势API足够）
└── 脚本引擎/热加载（行为层有明确复杂度边界，不引入脚本引擎）

现状说明
├── 已实现：Room依赖已存在（遗留），Gson部分遗留
├── 规划中：以上全部模块
└── 待定：WebP编码方案、Room在历史记录持久化阶段的引入时机
```

---

## 架构升级路线图

### Phase 0: 数据层先行（核心类型定义）
```
目标：完成所有新数据类型定义，确保后续工作有统一的数据契约
交付：
├── NormalizedCoord（归一化坐标类型 + CoordConverter）
├── PanelData（三层数据结构）
├── StageData（stage.json 序列化模型）
├── PanelEvent（面板间事件数据格式）
├── BehaviorAction（行为动作枚举）
└── ProjectFile 扩展适配
影响模块：editor-core（新增数据包，不影响现有代码）
```

### Phase 1: 悬浮窗全屏化重构
```
目标：将小窗预览改造为全屏交互面板层
交付：
├── StageService（替换FloatingPreviewService）
├── PanelManager（面板生命周期管理）
├── PanelView + VisualLayer（面板容器与视觉层）
├── CoordConverter（归一化↔像素转换）
└── 移除小窗模式代码
关键变更：FLAG_NOT_TOUCHABLE → 面板级触摸配置
```

### Phase 2: 舞台引擎 + 完整面板系统
```
目标：实现舞台三个核心职责 + 面板三层完整实现
交付：
├── StageEngine（空间/交互/状态编排器）
├── SpatialOrchestrator / InteractionOrchestrator / StateOrchestrator
├── InteractionLayer（触摸行为配置 + 手势识别）
├── BehaviorLayer（事件响应逻辑）
├── PanelRenderer（多面板渲染入口）
├── 面板间事件总线
├── 行为层复杂度边界守卫
└── 舞台性能监控（帧率降级）
```

### Phase 3: 编辑器适配 + 跨设备适配
```
目标：将新架构暴露到编辑器，实现全链路编排
交付：
├── 编辑器操作归一化坐标
├── stage.json 新格式读写
├── 舞台设置界面（面板列表/拖拽排序/触摸配置）
├── 舞台预览（模拟完整交互行为）
├── 编辑器→舞台预览实时StateFlow通道
└── 跨设备适配与横竖屏测试
```