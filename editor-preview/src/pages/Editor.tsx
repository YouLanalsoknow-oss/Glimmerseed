import TopToolbar from '@/components/TopToolbar';
import LeftPanel from '@/components/LeftPanel';
import ViewportCanvas from '@/components/ViewportCanvas';
import RightPanel from '@/components/RightPanel';
import BottomTimeline from '@/components/BottomTimeline';
import SettingsPanel from '@/components/SettingsPanel';
import { useEditorStore } from '@/hooks/useEditorStore';
import { Eye } from 'lucide-react';

export default function Editor() {
  const showLeftPanel = useEditorStore((s) => s.showLeftPanel);
  const showRightPanel = useEditorStore((s) => s.showRightPanel);
  const showChrome = useEditorStore((s) => s.showChrome);
  const uiScale = useEditorStore((s) => s.uiScale);
  const toggleChrome = useEditorStore((s) => s.toggleChrome);

  return (
    <div 
      className="h-screen w-screen flex flex-col overflow-hidden bg-[#F5F0E8] relative"
      style={{ fontSize: `${16 * uiScale}px` }}
    >
      {showChrome && <TopToolbar uiScale={uiScale} />}

      <div className="flex-1 flex overflow-hidden">
        {showChrome && showLeftPanel && <LeftPanel uiScale={uiScale} />}
        <div className="flex-1 relative">
          <ViewportCanvas />
        </div>
        {showChrome && showRightPanel && <RightPanel uiScale={uiScale} />}
      </div>

      {showChrome && <BottomTimeline uiScale={uiScale} />}

      {!showChrome && (
        <button
          onClick={toggleChrome}
          title="退出壁纸模式"
          className="absolute top-3 right-3 z-50 flex items-center gap-1.5 px-3 py-2
            rounded-lg border border-[#A1887F] bg-[#EDE6DD]/80 backdrop-blur-md
            text-[#5D4037] hover:text-[#3E2723] hover:border-[#8D6E63] hover:bg-[#F5F0E8]
            transition-all text-[11px] shadow-lg"
          style={{ fontSize: `${11 * uiScale}px`, padding: `${6 * uiScale}px ${12 * uiScale}px` }}
        >
          <Eye size={14 * uiScale} />
          <span>退出壁纸</span>
        </button>
      )}
      
      <SettingsPanel />
    </div>
  );
}
