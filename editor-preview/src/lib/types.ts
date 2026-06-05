export interface Bone {
  id: number;
  name: string;
  parentId: number | null;
  x: number;
  y: number;
  rotation: number;
  length: number;
  children: Bone[];
}

export interface KeyframeData {
  frameIndex: number;
  x: number;
  y: number;
  rotation: number;
  scaleX: number;
  scaleY: number;
}

export type TimelineItemType = 'minute' | 'block' | 'second' | 'frame';

export interface TimelineNavItem {
  id: string;
  type: TimelineItemType;
  label: string;
  startFrame: number;
  endFrame: number;
  parentId: string | null;
  expanded: boolean;
  children: TimelineNavItem[];
  hasKeyData: boolean;
}

export type ManipulatorMode = 'translate' | 'rotate' | 'scale';

export interface EditorState {
  selectedBoneId: number | null;
  showGrid: boolean;
  onionSkinEnabled: boolean;
  manipulatorMode: ManipulatorMode;
  skeleton: Bone[];
  timelineTree: TimelineNavItem[];
  boneKeyframes: Record<number, KeyframeData[]>; // boneId → 该骨骼的所有关键帧
  selectedFrame: number | null;
  activeTab: 'bones' | 'resources' | 'clips';
  zoomLevel: number;
  showLeftPanel: boolean;
  showRightPanel: boolean;
  showChrome: boolean;
  uiScale: number;
  showSettings: boolean;
}

export const FPS = 24;
export const FRAMES_PER_SEC = 24;
export const FRAMES_PER_30SEC = 30 * 24;
export const FRAMES_PER_MIN = 60 * 24;