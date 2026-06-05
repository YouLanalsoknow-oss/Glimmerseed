import { useEditorStore } from '@/hooks/useEditorStore';
import { KeyframeData } from '@/lib/types';
import { Dot, Trash2 } from 'lucide-react';

interface PropRowProps {
  label: string;
  value: number;
  onChange: (v: number) => void;
  step?: number;
  readOnly?: boolean;
  uiScale?: number;
}

function PropRow({ label, value, onChange, step, readOnly, uiScale = 1 }: PropRowProps) {
  return (
    <div className="flex items-center justify-between py-1 group">
      <span className="text-[10px] text-[#6D4C41] w-12 flex-shrink-0" style={{ fontSize: `${10 * uiScale}px`, width: `${48 * uiScale}px` }}>{label}</span>
      <input
        type="number"
        value={value}
        step={step ?? 1}
        onChange={(e) => onChange(parseFloat(e.target.value) || 0)}
        readOnly={readOnly}
        className={`w-20 border rounded px-2 py-0.5 text-[11px] text-right focus:outline-none transition-colors
          ${readOnly
            ? 'bg-transparent border-[#C2B2A1] text-[#795548] cursor-default'
            : 'bg-[#F5F0E8] border-[#C2B2A1] text-[#3E2723] focus:border-[#FFB300] group-hover:border-[#A1887F]'
          }`}
        style={{ fontSize: `${11 * uiScale}px`, width: `${80 * uiScale}px` }}
      />
    </div>
  );
}

function frameLabel(fi: number): string {
  const totalSec = Math.floor(fi / 24);
  const m = Math.floor(totalSec / 60);
  const s = totalSec % 60;
  const f = fi % 24;
  return `${m}:${String(s).padStart(2, '0')}:${String(f).padStart(2, '0')}`;
}

interface RightPanelProps {
  uiScale?: number;
}

export default function RightPanel({ uiScale = 1 }: RightPanelProps) {
  const selectedBoneId = useEditorStore((s) => s.selectedBoneId);
  const selectedFrame = useEditorStore((s) => s.selectedFrame);
  const findBoneById = useEditorStore((s) => s.findBoneById);
  const updateBoneProperty = useEditorStore((s) => s.updateBoneProperty);
  const getBoneKeyframe = useEditorStore((s) => s.getBoneKeyframe);
  const upsertBoneKeyframe = useEditorStore((s) => s.upsertBoneKeyframe);
  const deleteBoneKeyframe = useEditorStore((s) => s.deleteBoneKeyframe);

  const bone = selectedBoneId ? findBoneById(selectedBoneId) : null;
  const parentBone = bone?.parentId ? findBoneById(bone.parentId) : null;
  const kf: KeyframeData | undefined =
    bone && selectedFrame !== null
      ? getBoneKeyframe(bone.id, selectedFrame) as KeyframeData | undefined
      : undefined;

  const handleKfProp = (prop: keyof KeyframeData, value: number) => {
    if (!bone || selectedFrame === null) return;
    const existing = getBoneKeyframe(bone.id, selectedFrame) as KeyframeData | undefined;
    const newKf: KeyframeData = {
      frameIndex: selectedFrame,
      x: existing?.x ?? 0,
      y: existing?.y ?? 0,
      rotation: existing?.rotation ?? 0,
      scaleX: existing?.scaleX ?? 1,
      scaleY: existing?.scaleY ?? 1,
      [prop]: value,
    };
    upsertBoneKeyframe(bone.id, newKf);
  };

  const handleDeleteKf = () => {
    if (!bone || selectedFrame === null) return;
    deleteBoneKeyframe(bone.id, selectedFrame);
  };

  if (!bone) {
    return (
      <div className="w-[240px] bg-[#EDE6DD] border-l border-[#C2B2A1] flex items-center justify-center flex-shrink-0" style={{ width: `${240 * uiScale}px` }}>
        <span className="text-[12px] text-[#795548]" style={{ fontSize: `${12 * uiScale}px` }}>未选中骨骼</span>
      </div>
    );
  }

  return (
    <div className="w-[240px] bg-[#EDE6DD] border-l border-[#C2B2A1] flex flex-col select-none flex-shrink-0 overflow-y-auto" style={{ width: `${240 * uiScale}px` }}>
      {/* 骨骼静态信息 */}
      <div className="p-3 border-b border-[#C2B2A1]">
        <div className="flex items-center gap-2 mb-1">
          <span className="w-2.5 h-2.5 rounded-full bg-[#2E7D32]" style={{ width: `${10 * uiScale}px`, height: `${10 * uiScale}px` }} />
          <span className="text-[13px] font-medium text-[#3E2723]" style={{ fontSize: `${13 * uiScale}px` }}>{bone.name}</span>
        </div>
        <div className="text-[10px] text-[#6D4C41]" style={{ fontSize: `${10 * uiScale}px` }}>
          父级: <span className="text-[#5D4037]">{parentBone?.name ?? '无 (根骨骼)'}</span>
          <span className="ml-3">ID: {bone.id}</span>
        </div>
      </div>

      {/* 骨骼默认变换 */}
      <div className="p-3 border-b border-[#C2B2A1]">
        <div className="text-[10px] text-[#6D4C41] uppercase tracking-wider mb-2" style={{ fontSize: `${10 * uiScale}px` }}>默认变换</div>
        <PropRow uiScale={uiScale} label="X" value={Math.round(bone.x)} onChange={(v) => updateBoneProperty(bone.id, 'x', v)} />
        <PropRow uiScale={uiScale} label="Y" value={Math.round(bone.y)} onChange={(v) => updateBoneProperty(bone.id, 'y', v)} />
        <PropRow uiScale={uiScale} label="旋转" value={Math.round(bone.rotation)} onChange={(v) => updateBoneProperty(bone.id, 'rotation', v)} step={1} />
        <PropRow uiScale={uiScale} label="长度" value={Math.round(bone.length)} onChange={(v) => updateBoneProperty(bone.id, 'length', v)} />
      </div>

      {/* 动画关键帧编辑 */}
      <div className="p-3 border-b border-[#C2B2A1]">
        <div className="flex items-center justify-between mb-2">
          <span className="text-[10px] text-[#E65100] uppercase tracking-wider" style={{ fontSize: `${10 * uiScale}px` }}>动画关键帧</span>
          {selectedFrame !== null && (
            <span className="text-[10px] text-[#6D4C41]" style={{ fontSize: `${10 * uiScale}px` }}>{frameLabel(selectedFrame)}</span>
          )}
        </div>

        {kf ? (
          <div className="space-y-0.5">
            <PropRow uiScale={uiScale} label="X" value={kf.x} onChange={(v) => handleKfProp('x', v)} />
            <PropRow uiScale={uiScale} label="Y" value={kf.y} onChange={(v) => handleKfProp('y', v)} />
            <PropRow uiScale={uiScale} label="旋转" value={kf.rotation} onChange={(v) => handleKfProp('rotation', v)} step={1} />
            <PropRow uiScale={uiScale} label="SX" value={kf.scaleX} onChange={(v) => handleKfProp('scaleX', v)} step={0.1} />
            <PropRow uiScale={uiScale} label="SY" value={kf.scaleY} onChange={(v) => handleKfProp('scaleY', v)} step={0.1} />
            <button
              onClick={handleDeleteKf}
              className="mt-2 flex items-center gap-1 text-[10px] text-[#C62828] hover:text-[#B71C1C] transition-colors"
              style={{ fontSize: `${10 * uiScale}px` }}
            >
              <Trash2 size={10 * uiScale} />
              删除此关键帧
            </button>
          </div>
        ) : (
          <div className="space-y-1.5">
            <div className="flex items-center gap-1 text-[10px] text-[#6D4C41]" style={{ fontSize: `${10 * uiScale}px` }}>
              <Dot className="w-3 h-3 text-[#A1887F]" style={{ width: `${12 * uiScale}px`, height: `${12 * uiScale}px` }} />
              此帧无动画数据
            </div>
            {selectedFrame !== null && (
              <button
                onClick={() => handleKfProp('x', 0)}
                className="text-[10px] text-[#E65100] hover:text-[#FF6F00] transition-colors"
                style={{ fontSize: `${10 * uiScale}px` }}
              >
                创建关键帧
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
