import { create } from 'zustand';
import { Bone, EditorState, KeyframeData, ManipulatorMode, TimelineNavItem } from '@/lib/types';
import { createMockSkeleton, getMockTimelineTree, createMockBoneKeyframes } from '@/lib/mockData';

interface EditorStore extends EditorState {
  toggleGrid: () => void;
  toggleOnionSkin: () => void;
  selectBone: (id: number | null) => void;
  setManipulatorMode: (mode: ManipulatorMode) => void;
  updateBoneProperty: (id: number, prop: string, value: number) => void;
  setActiveTab: (tab: 'bones' | 'resources' | 'clips') => void;
  findBoneById: (id: number) => Bone | null;
  setZoomLevel: (level: number) => void;
  toggleExpandItem: (itemId: string) => void;
  selectFrame: (frame: number) => void;
  getBoneKeyframe: (boneId: number, frameIndex: number) => KeyframeData | undefined;
  upsertBoneKeyframe: (boneId: number, kf: KeyframeData) => void;
  deleteBoneKeyframe: (boneId: number, frameIndex: number) => void;
  toggleLeftPanel: () => void;
  toggleRightPanel: () => void;
  toggleChrome: () => void;
  setUiScale: (scale: number) => void;
  toggleSettings: () => void;
}

function findBone(bones: Bone[], id: number): Bone | null {
  for (const bone of bones) {
    if (bone.id === id) return bone;
    const found = findBone(bone.children, id);
    if (found) return found;
  }
  return null;
}

function updateBoneInTree(bones: Bone[], id: number, updater: (bone: Bone) => Bone): Bone[] {
  return bones.map((bone) => {
    if (bone.id === id) return updater(bone);
    return { ...bone, children: updateBoneInTree(bone.children, id, updater) };
  });
}

function toggleExpandInTree(items: TimelineNavItem[], itemId: string): TimelineNavItem[] {
  return items.map(item => {
    if (item.id === itemId) return { ...item, expanded: !item.expanded };
    return { ...item, children: toggleExpandInTree(item.children, itemId) };
  });
}

export const useEditorStore = create<EditorStore>((set, get) => ({
  selectedBoneId: 3,
  showGrid: true,
  onionSkinEnabled: false,
  manipulatorMode: 'translate',
  skeleton: [createMockSkeleton()],
  timelineTree: getMockTimelineTree(),
  boneKeyframes: createMockBoneKeyframes(),
  selectedFrame: 0,
  activeTab: 'bones',
  zoomLevel: 2,
  showLeftPanel: true,
  showRightPanel: false,
  showChrome: true,
  uiScale: 1,
  showSettings: false,

  toggleGrid: () => set((s) => ({ showGrid: !s.showGrid })),
  toggleOnionSkin: () => set((s) => ({ onionSkinEnabled: !s.onionSkinEnabled })),
  selectBone: (id) => set({ selectedBoneId: id }),
  setManipulatorMode: (mode) => set({ manipulatorMode: mode }),
  setActiveTab: (tab) => set({ activeTab: tab }),
  updateBoneProperty: (id, prop, value) => {
    set((s) => ({
      skeleton: updateBoneInTree(s.skeleton, id, (bone) => ({ ...bone, [prop]: value })),
    }));
  },
  findBoneById: (id) => {
    for (const root of get().skeleton) {
      const found = findBone([root], id);
      if (found) return found;
    }
    return null;
  },
  setZoomLevel: (level) => set({ zoomLevel: Math.max(0, Math.min(level, 6)) }),

  toggleExpandItem: (itemId) => {
    set((s) => ({
      timelineTree: toggleExpandInTree(s.timelineTree, itemId),
    }));
  },

  selectFrame: (frame) => set({ selectedFrame: Math.max(0, frame) }),

  getBoneKeyframe: (boneId, frameIndex) => {
    const kfs = get().boneKeyframes[boneId];
    return kfs?.find(k => k.frameIndex === frameIndex);
  },

  upsertBoneKeyframe: (boneId, kf) => {
    set((s) => {
      const existing = s.boneKeyframes[boneId] ? [...s.boneKeyframes[boneId]] : [];
      const idx = existing.findIndex(k => k.frameIndex === kf.frameIndex);
      if (idx >= 0) {
        existing[idx] = kf;
      } else {
        existing.push(kf);
        existing.sort((a, b) => a.frameIndex - b.frameIndex);
      }
      return { boneKeyframes: { ...s.boneKeyframes, [boneId]: existing } };
    });
  },

  deleteBoneKeyframe: (boneId, frameIndex) => {
    set((s) => {
      const existing = (s.boneKeyframes[boneId] ?? []).filter(k => k.frameIndex !== frameIndex);
      return { boneKeyframes: { ...s.boneKeyframes, [boneId]: existing } };
    });
  },

  toggleLeftPanel: () => set((s) => ({ showLeftPanel: !s.showLeftPanel })),
  toggleRightPanel: () => set((s) => ({ showRightPanel: !s.showRightPanel })),
  toggleChrome: () => set((s) => ({ showChrome: !s.showChrome })),
  setUiScale: (scale) => set({ uiScale: Math.max(0.5, Math.min(scale, 2)) }),
  toggleSettings: () => set((s) => ({ showSettings: !s.showSettings })),
}));