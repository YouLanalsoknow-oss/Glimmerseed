import { useState } from 'react';
import { ChevronDown, ChevronRight, Plus, Trash2, Image, Film } from 'lucide-react';
import { useEditorStore } from '@/hooks/useEditorStore';
import { Bone } from '@/lib/types';

interface BoneTreeNodeProps {
  bone: Bone;
  depth: number;
  onSelect: (id: number) => void;
  uiScale?: number;
}

function BoneTreeNode({ bone, depth, onSelect, uiScale = 1 }: BoneTreeNodeProps) {
  const selectedBoneId = useEditorStore((s) => s.selectedBoneId);
  const [expanded, setExpanded] = useState(true);
  const isSelected = selectedBoneId === bone.id;
  const hasChildren = bone.children && bone.children.length > 0;

  return (
    <div>
      <div
        onClick={() => onSelect(bone.id)}
        className={`flex items-center gap-1 py-1 cursor-pointer text-[12px] transition-colors
          ${isSelected ? 'bg-[#D6CBBE] text-[#3E2723]' : 'hover:bg-[#E8DDD0] text-[#5D4037]'}`}
        style={{ paddingLeft: `${(8 + depth * 16) * uiScale}px`, fontSize: `${12 * uiScale}px` }}
      >
        <span
          className="w-4 h-4 flex items-center justify-center flex-shrink-0"
          onClick={(e) => {
            e.stopPropagation();
            if (hasChildren) setExpanded(!expanded);
          }}
        >
          {hasChildren ? (
            expanded ? <ChevronDown size={12 * uiScale} /> : <ChevronRight size={12 * uiScale} />
          ) : (
            <span className="w-3" />
          )}
        </span>
        <span className="w-2 h-2 rounded-full flex-shrink-0"
          style={{ backgroundColor: isSelected ? '#FFB300' : '#2E7D32', width: `${8 * uiScale}px`, height: `${8 * uiScale}px` }}
        />
        <span className="truncate">{bone.name}</span>
      </div>
      {expanded && hasChildren && (
        <div>
          {bone.children.map((child) => (
            <BoneTreeNode key={child.id} bone={child} depth={depth + 1} onSelect={onSelect} uiScale={uiScale} />
          ))}
        </div>
      )}
    </div>
  );
}

interface PanelActionProps {
  icon: React.ReactNode;
  label: string;
  onClick: () => void;
  uiScale?: number;
}

function PanelAction({ icon, label, onClick, uiScale = 1 }: PanelActionProps) {
  return (
    <button
      onClick={onClick}
      className="flex items-center gap-1.5 px-2 py-1.5 rounded-md hover:bg-[#E8DDD0] text-[#6D4C41] hover:text-[#3E2723] text-[11px] transition-colors"
      style={{ fontSize: `${11 * uiScale}px` }}
    >
      <span className="w-3.5 h-3.5 flex items-center justify-center" style={{ width: `${14 * uiScale}px`, height: `${14 * uiScale}px` }}>{icon}</span>
      <span>{label}</span>
    </button>
  );
}

interface LeftPanelProps {
  uiScale?: number;
}

export default function LeftPanel({ uiScale = 1 }: LeftPanelProps) {
  const skeleton = useEditorStore((s) => s.skeleton);
  const selectBone = useEditorStore((s) => s.selectBone);
  const activeTab = useEditorStore((s) => s.activeTab);
  const setActiveTab = useEditorStore((s) => s.setActiveTab);

  return (
    <div className="w-[220px] bg-[#EDE6DD] border-r border-[#C2B2A1] flex flex-col select-none flex-shrink-0" style={{ width: `${220 * uiScale}px` }}>
      <div className="flex border-b border-[#C2B2A1]">
        {(['bones', 'resources', 'clips'] as const).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={`flex-1 py-1.5 text-[11px] transition-colors
              ${activeTab === tab ? 'bg-[#F5F0E8] text-[#3E2723] border-b border-[#8D6E63]' : 'text-[#6D4C41] hover:text-[#3E2723]'}`}
            style={{ fontSize: `${11 * uiScale}px` }}
          >
            {tab === 'bones' ? '骨骼树' : tab === 'resources' ? '资源' : '动画剪辑'}
          </button>
        ))}
      </div>

      <div className="flex-1 overflow-y-auto">
        {activeTab === 'bones' && (
          <div className="py-1">
            {skeleton.map((bone) => (
              <BoneTreeNode key={bone.id} bone={bone} depth={0} onSelect={selectBone} uiScale={uiScale} />
            ))}
          </div>
        )}
        {activeTab === 'resources' && (
          <div className="flex items-center justify-center h-full text-[12px] text-[#795548]" style={{ fontSize: `${12 * uiScale}px` }}>
            暂无资源
          </div>
        )}
        {activeTab === 'clips' && (
          <div className="flex items-center justify-center h-full text-[12px] text-[#795548]" style={{ fontSize: `${12 * uiScale}px` }}>
            暂无动画剪辑
          </div>
        )}
      </div>

      <div className="border-t border-[#C2B2A1] p-1 flex flex-wrap gap-0.5">
        <PanelAction uiScale={uiScale} icon={<Plus size={12 * uiScale} />} label="添加骨骼" onClick={() => {}} />
        <PanelAction uiScale={uiScale} icon={<Trash2 size={12 * uiScale} />} label="删除骨骼" onClick={() => {}} />
        <PanelAction uiScale={uiScale} icon={<Image size={12 * uiScale} />} label="导入纹理" onClick={() => {}} />
        <PanelAction uiScale={uiScale} icon={<Film size={12 * uiScale} />} label="新建动画" onClick={() => {}} />
      </div>
    </div>
  );
}
