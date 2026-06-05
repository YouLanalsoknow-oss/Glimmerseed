import { Bone, KeyframeData, TimelineNavItem, FRAMES_PER_MIN, FRAMES_PER_30SEC, FRAMES_PER_SEC } from '@/lib/types';

export function createMockSkeleton(): Bone {
  return {
    id: 0,
    name: 'Root',
    parentId: null,
    x: 0,
    y: 0,
    rotation: 0,
    length: 0,
    children: [
      {
        id: 1,
        name: 'Hip',
        parentId: 0,
        x: 0,
        y: 0,
        rotation: 0,
        length: 80,
        children: [
          {
            id: 2,
            name: 'Upper Leg',
            parentId: 1,
            x: 0,
            y: 80,
            rotation: -30,
            length: 120,
            children: [
              {
                id: 3,
                name: 'Lower Leg',
                parentId: 2,
                x: 0,
                y: 200,
                rotation: 20,
                length: 120,
                children: [],
              },
            ],
          },
          {
            id: 4,
            name: 'Upper Arm',
            parentId: 1,
            x: -60,
            y: 40,
            rotation: -45,
            length: 100,
            children: [
              {
                id: 5,
                name: 'Lower Arm',
                parentId: 4,
                x: -60,
                y: 140,
                rotation: 15,
                length: 100,
                children: [],
              },
            ],
          },
        ],
      },
    ],
  };
}

export function createMockBoneKeyframes(): Record<number, KeyframeData[]> {
  const boneIds = [1, 2, 3, 4, 5];
  const baseValues: Record<number, { x: number; y: number; rotation: number }> = {
    1: { x: 0, y: 0, rotation: 0 },
    2: { x: 0, y: 80, rotation: -30 },
    3: { x: 0, y: 200, rotation: 20 },
    4: { x: -60, y: 40, rotation: -45 },
    5: { x: -60, y: 140, rotation: 15 },
  };

  const result: Record<number, KeyframeData[]> = {};
  for (const id of boneIds) {
    const base = baseValues[id]!;
    const keyframes: KeyframeData[] = [];
    for (let fi = 0; fi < FRAMES_PER_MIN * 3; fi += 12) {
      const t = fi / 24;
      const wave = Math.sin(t * 0.8) * 10;
      keyframes.push({
        frameIndex: fi,
        x: Math.round(base.x + wave * (id % 2 === 0 ? 1 : -1)),
        y: Math.round(base.y + wave * 0.5),
        rotation: Math.round(base.rotation + wave * 0.3),
        scaleX: 1.0,
        scaleY: 1.0,
      });
    }
    result[id] = keyframes;
  }
  return result;
}

function buildTimelineTree(totalFrames: number, keyframeFrames: Set<number>): TimelineNavItem[] {
  const totalMinutes = Math.ceil(totalFrames / FRAMES_PER_MIN);
  const minutes: TimelineNavItem[] = [];

  for (let m = 0; m < totalMinutes; m++) {
    const minStart = m * FRAMES_PER_MIN;
    const minEnd = Math.min(minStart + FRAMES_PER_MIN, totalFrames);
    const minuteId = `min_${m}`;

    const blocks: TimelineNavItem[] = [];
    for (let b = 0; b < 2; b++) {
      const blockStart = minStart + b * FRAMES_PER_30SEC;
      if (blockStart >= totalFrames) break;
      const blockEnd = Math.min(blockStart + FRAMES_PER_30SEC, minEnd);
      const blockId = `min_${m}_blk_${b}`;

      const seconds: TimelineNavItem[] = [];
      const secCount = Math.min(30, Math.ceil((blockEnd - blockStart) / FRAMES_PER_SEC));
      for (let s = 0; s < secCount; s++) {
        const secStart = blockStart + s * FRAMES_PER_SEC;
        const secEnd = Math.min(secStart + FRAMES_PER_SEC, blockEnd);
        const secId = `min_${m}_blk_${b}_sec_${s}`;

        const frames: TimelineNavItem[] = [];
        const frameCount = Math.min(24, secEnd - secStart);
        for (let f = 0; f < frameCount; f++) {
          const fi = secStart + f;
          const frameId = `min_${m}_blk_${b}_sec_${s}_frm_${f}`;
          const hasKeyData = keyframeFrames.has(fi);
          frames.push({
            id: frameId,
            type: 'frame',
            label: `${f}`,
            startFrame: fi,
            endFrame: fi + 1,
            parentId: secId,
            expanded: false,
            children: [],
            hasKeyData,
          });
        }

        seconds.push({
          id: secId,
          type: 'second',
          label: `${s}″`,
          startFrame: secStart,
          endFrame: secEnd,
          parentId: blockId,
          expanded: false,
          children: frames,
          hasKeyData: frames.some(f => f.hasKeyData),
        });
      }

      blocks.push({
        id: blockId,
        type: 'block',
        label: `${m}:${String(b * 30).padStart(2, '0')}`,
        startFrame: blockStart,
        endFrame: blockEnd,
        parentId: minuteId,
        expanded: false,
        children: seconds,
        hasKeyData: seconds.some(s => s.hasKeyData),
      });
    }

    minutes.push({
      id: minuteId,
      type: 'minute',
      label: `${m}:00`,
      startFrame: minStart,
      endFrame: minEnd,
      parentId: null,
      expanded: false,
      children: blocks,
      hasKeyData: blocks.some(blk => blk.hasKeyData),
    });
  }

  return minutes;
}

export function getMockTimelineTree(): TimelineNavItem[] {
  const kfs = createMockBoneKeyframes();
  const frames = new Set<number>();
  for (const arr of Object.values(kfs)) {
    for (const kf of arr) frames.add(kf.frameIndex);
  }
  return buildTimelineTree(FRAMES_PER_MIN * 3, frames);
}