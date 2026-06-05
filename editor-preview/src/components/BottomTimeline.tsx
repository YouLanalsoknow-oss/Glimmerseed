import { useMemo, useCallback } from 'react';
import { useEditorStore } from '@/hooks/useEditorStore';
import { TimelineNavItem, TimelineItemType } from '@/lib/types';
import { ChevronRight, ChevronDown, Dot, Folder } from 'lucide-react';

const HEIGHT_CLASS: Record<TimelineItemType, string> = {
  minute: 'h-11',
  block: 'h-9',
  second: 'h-7',
  frame: 'h-5',
};

const COLOR_CLASS: Record<TimelineItemType, string> = {
  minute: 'border-[#64B5F6] bg-[#E3F2FD] text-[#1565C0] hover:bg-[#BBDEFB]',
  block: 'border-[#80DEEA] bg-[#E0F7FA] text-[#00838F] hover:bg-[#B2EBF2]',
  second: 'border-[#BCAAA4] bg-[#EFEBE9] text-[#5D4037] hover:bg-[#D7CCC8]',
  frame: 'border-[#D7CCC8] bg-[#F5F0E8] text-[#6D4C41] hover:bg-[#E8DDD0]',
};

type FlatItem = TimelineNavItem & { depth: number; colorClass: string };

function flattenTree(items: TimelineNavItem[], depth: number): FlatItem[] {
  const result: FlatItem[] = [];
  for (const item of items) {
    result.push({ ...item, depth, colorClass: COLOR_CLASS[item.type] });
    if (item.expanded && item.children.length > 0) {
      result.push(...flattenTree(item.children, depth + 1));
    }
  }
  return result;
}

interface BottomTimelineProps {
  uiScale?: number;
}

export default function BottomTimeline({ uiScale = 1 }: BottomTimelineProps) {
  const timelineTree = useEditorStore((s) => s.timelineTree);
  const selectedFrame = useEditorStore((s) => s.selectedFrame);
  const selectedBoneId = useEditorStore((s) => s.selectedBoneId);
  const toggleExpandItem = useEditorStore((s) => s.toggleExpandItem);
  const selectFrame = useEditorStore((s) => s.selectFrame);

  const flatItems = useMemo(() => flattenTree(timelineTree, 0), [timelineTree]);

  const handleClick = useCallback((item: TimelineNavItem) => {
    if (item.children.length > 0) {
      toggleExpandItem(item.id);
    } else {
      selectFrame(item.startFrame);
    }
  }, [toggleExpandItem, selectFrame]);

  const frameTimeLabel = (frameIndex: number): string => {
    const totalSec = Math.floor(frameIndex / 24);
    const m = Math.floor(totalSec / 60);
    const s = totalSec % 60;
    const f = frameIndex % 24;
    return `${m}:${String(s).padStart(2, '0')}:${String(f).padStart(2, '0')}`;
  };

  return (
    <div className="border-t border-[#C2B2A1] flex-shrink-0 flex flex-col bg-[#EDE6DD] select-none">
      <div className="flex items-center gap-2 px-3 py-1 bg-[#F5F0E8] border-b border-[#C2B2A1] text-[10px] text-[#6D4C41] flex-shrink-0" style={{ fontSize: `${10 * uiScale}px` }}>
        <Folder className="w-3 h-3 text-[#1565C0]" style={{ width: `${12 * uiScale}px`, height: `${12 * uiScale}px` }} />
        <span>时间轴导航</span>
        <span className="text-[#795548]" style={{ fontSize: `${10 * uiScale}px` }}>— 点击文件夹展开子级，点击帧选中</span>
        {selectedFrame !== null && selectedBoneId !== null && (
          <span className="text-[#E65100] ml-auto" style={{ fontSize: `${10 * uiScale}px` }}>
            编辑骨骼 #{selectedBoneId} @ {frameTimeLabel(selectedFrame)}
          </span>
        )}
      </div>

      <div className="flex-1 overflow-x-auto overflow-y-hidden">
        <div className="flex items-center gap-1 px-2 py-2 min-w-max" style={{ gap: `${4 * uiScale}px` }}>
          {flatItems.length === 0 && (
            <div className="text-[11px] text-[#795548] px-2" style={{ fontSize: `${11 * uiScale}px` }}>无数据</div>
          )}
          {flatItems.map((item) => {
            const hasChildren = item.children.length > 0;
            const isExpanded = item.expanded;
            const isSelected = !hasChildren && item.startFrame === selectedFrame;
            const heightCls = HEIGHT_CLASS[item.type];

            return (
              <button
                key={item.id}
                onClick={() => handleClick(item)}
                className={`flex-shrink-0 w-11 ${heightCls} flex flex-col items-center justify-center
                  rounded border text-[9px] font-mono transition-all duration-150 leading-none cursor-pointer
                  ${isSelected
                    ? 'border-[#FFB300] bg-[#FFECB3] text-[#E65100] shadow-[0_0_8px_rgba(255,179,0,0.2)]'
                    : item.colorClass}
                  ${item.hasKeyData && !isSelected ? 'ring-1 ring-[#FFB300]' : ''}
                  ${isExpanded ? 'shadow-[0_0_6px_rgba(100,181,246,0.15)]' : ''}
                `}
                style={{ fontSize: `${9 * uiScale}px`, width: `${44 * uiScale}px` }}
                title={`${item.label} [${item.startFrame}–${item.endFrame}]${item.hasKeyData ? ' · 有关键数据' : ''}`}
              >
                {hasChildren && (
                  isExpanded
                    ? <ChevronDown className="w-2.5 h-2.5 mb-0.5 opacity-60" style={{ width: `${10 * uiScale}px`, height: `${10 * uiScale}px` }} />
                    : <ChevronRight className="w-2.5 h-2.5 mb-0.5 opacity-60" style={{ width: `${10 * uiScale}px`, height: `${10 * uiScale}px` }} />
                )}
                {!hasChildren && item.hasKeyData && (
                  <Dot className="w-2 h-2 text-[#FFB300] mb-0.5" style={{ width: `${8 * uiScale}px`, height: `${8 * uiScale}px` }} />
                )}
                {!hasChildren && !item.hasKeyData && (
                  <span className="w-1 h-1 rounded-full bg-[#A1887F] mb-0.5 flex-shrink-0" style={{ width: `${4 * uiScale}px`, height: `${4 * uiScale}px` }} />
                )}
                <span className={`${item.type === 'frame' ? 'text-[8px]' : 'text-[9px]'} leading-tight`} style={{ fontSize: `${(item.type === 'frame' ? 8 : 9) * uiScale}px` }}>
                  {item.type === 'frame' ? item.startFrame % 24 : item.label}
                </span>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}
