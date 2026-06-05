import React from 'react';
import { useEditorStore } from '@/hooks/useEditorStore';
import { X } from 'lucide-react';

export default function SettingsPanel() {
  const uiScale = useEditorStore((s) => s.uiScale);
  const showSettings = useEditorStore((s) => s.showSettings);
  const setUiScale = useEditorStore((s) => s.setUiScale);
  const toggleSettings = useEditorStore((s) => s.toggleSettings);

  if (!showSettings) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
      <div className="w-[400px] max-h-[300px] rounded-lg border border-[#C2B2A1] bg-[#F5F0E8] shadow-xl">
        <div className="flex items-center justify-between border-b border-[#C2B2A1] px-4 py-3">
          <h3 className="text-sm font-semibold text-[#5D4037]">设置</h3>
          <button
            onClick={toggleSettings}
            className="rounded hover:bg-[#EDE6DD] p-1 text-[#795548] hover:text-[#5D4037] transition-colors"
          >
            <X size={16} />
          </button>
        </div>
        <div className="p-4 space-y-6">
          <div>
            <div className="flex justify-between items-center mb-2">
              <label className="text-xs text-[#5D4037]">UI 缩放</label>
              <span className="text-xs text-[#795548] font-mono">{(uiScale * 100).toFixed(0)}%</span>
            </div>
            <input
              type="range"
              min="50"
              max="200"
              value={uiScale * 100}
              onChange={(e) => setUiScale(Number(e.target.value) / 100)}
              className="w-full h-2 bg-[#D7CCC8] rounded-lg appearance-none cursor-pointer accent-[#8D6E63]"
            />
            <div className="flex justify-between text-[10px] text-[#A1887F] mt-1">
              <span>50%</span>
              <span>100%</span>
              <span>150%</span>
              <span>200%</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
