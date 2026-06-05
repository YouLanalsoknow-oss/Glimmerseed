## 1. 架构设计
```mermaid
flowchart TD
    subgraph 前端层
        A["React + TypeScript + TailwindCSS"]
        A1["Toolbar 组件"]
        A2["LeftPanel 组件"]
        A3["ViewportCanvas 组件"]
        A4["PropertyPanel 组件"]
        A5["Timeline 组件"]
    end
    subgraph 数据层
        B["zustand Store"]
        B1["editorStore - 编辑器状态"]
    end
    subgraph 渲染层
        C["Canvas API"]
        C1["骨骼绘制"]
        C2["网格绘制"]
        C3["时间轴绘制"]
    end
    A --> B
    A3 --> C
    A5 --> C
    B --> A1
    B --> A2
    B --> A3
    B --> A4
    B --> A5
```

## 2. 技术描述
- 前端：React@18 + TypeScript + TailwindCSS@3 + Vite
- 初始化工具：vite-init（react-ts 模板）
- 状态管理：zustand
- UI 库：lucide-react（图标）
- 后端：无（纯前端静态页面）
- 数据：全部使用硬编码模拟数据

## 3. 路由定义
| 路由 | 用途 |
|-----|------|
| / | 编辑器主页（唯一页面） |

## 4. 组件树
```
App
├── EditorLayout
│   ├── TopToolbar
│   │   ├── ToolbarButton (×N)
│   │   ├── Divider (×N)
│   │   └── StatusDisplay
│   ├── EditorBody
│   │   ├── LeftPanel
│   │   │   ├── TabRow
│   │   │   ├── BoneTreePanel
│   │   │   ├── ResourcePanel
│   │   │   ├── AnimationClipsPanel
│   │   │   └── PanelActions
│   │   ├── ViewportCanvas
│   │   │   ├── GridLayer
│   │   │   ├── AxisIndicator
│   │   │   ├── SkeletonRenderer
│   │   │   └── OnionSkinPreview
│   │   └── RightPanel
│   │       ├── BoneInfoCard
│   │       ├── PositionEditor
│   │       ├── RotationEditor
│   │       └── ScaleEditor
│   └── BottomTimeline
│       ├── TimeRuler
│       ├── Playhead
│       └── KeyframeTrack (×3)
```

## 5. 数据模型
```typescript
interface Bone {
  id: number;
  name: string;
  parentId: number | null;
  x: number;
  y: number;
  rotation: number;
  length: number;
  children: Bone[];
}

interface Keyframe {
  time: number;
  type: 'normal' | 'linear' | 'bezier' | 'step';
  selected: boolean;
}

interface EditorState {
  selectedBoneId: number | null;
  showGrid: boolean;
  onionSkinEnabled: boolean;
  isPlaying: boolean;
  manipulatorMode: 'translate' | 'rotate' | 'scale';
  skeleton: Bone[];
  keyframeTracks: Keyframe[][];
}

// 存储结构
interface EditorStore extends EditorState {
  toggleGrid: () => void;
  toggleOnionSkin: () => void;
  togglePlay: () => void;
  selectBone: (id: number) => void;
  setManipulatorMode: (mode: string) => void;
  updateBoneProperty: (id: number, prop: string, value: number) => void;
}
```