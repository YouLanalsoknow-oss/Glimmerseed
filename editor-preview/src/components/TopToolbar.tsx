import {
  FileText, FolderOpen, Save, Undo2, Redo2,
  MousePointer2, RotateCw, Maximize2, Grid3X3,
  Layers, Play, MonitorPlay, Settings,
  PanelLeft, PanelRight, EyeOff,
} from 'lucide-react';
import { useEditorStore } from '@/hooks/useEditorStore';

interface ToolBtnProps {
  icon: React.ReactNode;
  label: string;
  active?: boolean;
  onClick: () => void;
  accent?: string;
  uiScale?: number;
}

function ToolBtn({ icon, label, active, onClick, accent, uiScale = 1 }: ToolBtnProps) {
  return (
    <button
      onClick={onClick}
      title={label}
      className={`flex flex-col items-center justify-center gap-0.5 px-2 py-1.5 rounded-md min-w-[42px] transition-colors
        ${active ? (accent || 'bg-[#D6CBBE] text-[#3E2723]') : 'hover:bg-[#E8DDD0] text-[#5D4037] hover:text-[#3E2723]'}`}
      style={{ fontSize: `${10 * uiScale}px`, minWidth: `${42 * uiScale}px` }}
    >
      <span className="w-4 h-4 flex items-center justify-center" style={{ transform: `scale(${uiScale})` }}>{icon}</span>
      <span className="text-[10px] leading-none">{label}</span>
    </button>
  );
}

function ToolbarDivider() {
  return <div className="w-px h-8 bg-[#C2B2A1] mx-0.5 self-center" />;
}

function frameLabel(fi: number): string {
  const totalSec = Math.floor(fi / 24);
  const m = Math.floor(totalSec / 60);
  const s = totalSec % 60;
  const f = fi % 24;
  return `${m}:${String(s).padStart(2, '0')}:${String(f).padStart(2, '0')}`;
}

interface TopToolbarProps {
  uiScale?: number;
}

export default function TopToolbar({ uiScale = 1 }: TopToolbarProps) {
  const showGrid = useEditorStore((s) => s.showGrid);
  const onionSkinEnabled = useEditorStore((s) => s.onionSkinEnabled);
  const manipulatorMode = useEditorStore((s) => s.manipulatorMode);
  const selectedBoneId = useEditorStore((s) => s.selectedBoneId);
  const selectedFrame = useEditorStore((s) => s.selectedFrame);
  const toggleGrid = useEditorStore((s) => s.toggleGrid);
  const toggleOnionSkin = useEditorStore((s) => s.toggleOnionSkin);
  const setManipulatorMode = useEditorStore((s) => s.setManipulatorMode);
  const findBoneById = useEditorStore((s) => s.findBoneById);
  const showLeftPanel = useEditorStore((s) => s.showLeftPanel);
  const showRightPanel = useEditorStore((s) => s.showRightPanel);
  const toggleLeftPanel = useEditorStore((s) => s.toggleLeftPanel);
  const toggleRightPanel = useEditorStore((s) => s.toggleRightPanel);
  const toggleChrome = useEditorStore((s) => s.toggleChrome);
  const toggleSettings = useEditorStore((s) => s.toggleSettings);

  const selectedBone = selectedBoneId ? findBoneById(selectedBoneId) : null;

  return (
    <div className="h-11 bg-[#EDE6DD] border-b border-[#C2B2A1] flex items-center px-1 gap-0 select-none flex-shrink-0" style={{ height: `${44 * uiScale}px` }}>
      <div className="flex items-center gap-0">
        <ToolBtn uiScale={uiScale} icon={<FileText size={14 * uiScale} />} label="新建" onClick={() => {}} />
        <ToolBtn uiScale={uiScale} icon={<FolderOpen size={14 * uiScale} />} label="打开" onClick={() => {}} />
        <ToolBtn uiScale={uiScale} icon={<Save size={14 * uiScale} />} label="保存" onClick={() => {}} />
      </div>

      <ToolbarDivider />

      <div className="flex items-center gap-0">
        <ToolBtn uiScale={uiScale} icon={<Undo2 size={14 * uiScale} />} label="撤销" onClick={() => {}} />
        <ToolBtn uiScale={uiScale} icon={<Redo2 size={14 * uiScale} />} label="重做" onClick={() => {}} />
      </div>

      <ToolbarDivider />

      <div className="flex items-center gap-0">
        <ToolBtn
          uiScale={uiScale}
          icon={<MousePointer2 size={14 * uiScale} />}
          label="平移"
          active={manipulatorMode === 'translate'}
          onClick={() => setManipulatorMode('translate')}
        />
        <ToolBtn
          uiScale={uiScale}
          icon={<RotateCw size={14 * uiScale} />}
          label="旋转"
          active={manipulatorMode === 'rotate'}
          onClick={() => setManipulatorMode('rotate')}
        />
        <ToolBtn
          uiScale={uiScale}
          icon={<Maximize2 size={14 * uiScale} />}
          label="缩放"
          active={manipulatorMode === 'scale'}
          onClick={() => setManipulatorMode('scale')}
        />
      </div>

      <ToolbarDivider />

      <div className="flex items-center gap-0">
        <ToolBtn uiScale={uiScale} icon={<Grid3X3 size={14 * uiScale} />} label="网格" active={showGrid} onClick={toggleGrid} />
        <ToolBtn uiScale={uiScale} icon={<Layers size={14 * uiScale} />} label="洋葱皮" active={onionSkinEnabled} onClick={toggleOnionSkin} />
      </div>

      <ToolbarDivider />

      <div className="flex items-center gap-0">
        <ToolBtn uiScale={uiScale} icon={<Play size={14 * uiScale} />} label="播放" onClick={() => {}} />
      </div>

      <ToolbarDivider />

      <ToolBtn
        uiScale={uiScale}
        icon={<MonitorPlay size={14 * uiScale} />}
        label="预览"
        accent="bg-[#B3D4FF] text-[#0D47A1]"
        onClick={() => {}}
      />

      <ToolbarDivider />

      <button
        onClick={toggleChrome}
        title="进入壁纸模式 (隐藏工具栏和时间轴)"
        className="flex items-center gap-1.5 px-3 py-1.5 rounded-md border border-[#A1887F]
          bg-gradient-to-r from-[#D6CBBE] to-[#C9B8A8] hover:from-[#E0D5C8] hover:to-[#D1C0AF]
          text-[#5D4037] hover:text-[#3E2723] transition-all text-[11px]"
        style={{ fontSize: `${11 * uiScale}px`, padding: `${6 * uiScale}px ${12 * uiScale}px` }}
      >
        <EyeOff size={13 * uiScale} />
        <span>壁纸</span>
      </button>

      <div className="flex-1" />

      <div className="flex items-center gap-2 text-[11px] text-[#6D4C41] mr-2" style={{ fontSize: `${11 * uiScale}px` }}>
        <span className="py-0.5 px-2 rounded bg-[#F5F0E8] border border-[#C2B2A1]">
          骨骼 <span className="text-[#1565C0]">{selectedBone?.name ?? '-'}</span>
        </span>
        {selectedFrame !== null && (
          <span className="py-0.5 px-2 rounded bg-[#FFF3E0] text-[#E65100] border border-[#FFCC80]">
            帧 {frameLabel(selectedFrame)}
          </span>
        )}
      </div>

      <ToolbarDivider />
      <ToolBtn
        uiScale={uiScale}
        icon={<PanelLeft size={14 * uiScale} />}
        label="骨骼树"
        active={showLeftPanel}
        accent="bg-[#C8E6C9] text-[#1B5E20]"
        onClick={toggleLeftPanel}
      />
      <ToolBtn
        uiScale={uiScale}
        icon={<PanelRight size={14 * uiScale} />}
        label="属性"
        active={showRightPanel}
        accent="bg-[#FFE0B2] text-[#E65100]"
        onClick={toggleRightPanel}
      />

      <ToolBtn uiScale={uiScale} icon={<Settings size={14 * uiScale} />} label="设置" onClick={toggleSettings} />
    </div>
  );
}
