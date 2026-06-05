import { useEffect, useRef, useCallback, useState } from 'react';
import { useEditorStore } from '@/hooks/useEditorStore';
import { Bone } from '@/lib/types';

export default function ViewportCanvas() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const bgImageRef = useRef<HTMLImageElement | null>(null);
  const [bgLoaded, setBgLoaded] = useState(false);
  const showGrid = useEditorStore((s) => s.showGrid);
  const onionSkinEnabled = useEditorStore((s) => s.onionSkinEnabled);
  const skeleton = useEditorStore((s) => s.skeleton);
  const selectedBoneId = useEditorStore((s) => s.selectedBoneId);
  const selectBone = useEditorStore((s) => s.selectBone);

  useEffect(() => {
    const img = new Image();
    img.src = './wallpaper.png';
    img.onload = () => {
      bgImageRef.current = img;
      setBgLoaded(true);
    };
  }, []);

  const drawGrid = useCallback((ctx: CanvasRenderingContext2D, w: number, h: number) => {
    const gridSize = 50;
    ctx.strokeStyle = 'rgba(0,0,0,0.06)';
    ctx.lineWidth = 0.5;

    for (let x = gridSize; x < w; x += gridSize) {
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, h);
      ctx.stroke();
    }
    for (let y = gridSize; y < h; y += gridSize) {
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(w, y);
      ctx.stroke();
    }

    ctx.strokeStyle = 'rgba(0,0,0,0.12)';
    ctx.lineWidth = 1;
    const cx = Math.floor(w / 2 / gridSize) * gridSize;
    const cy = Math.floor(h / 2 / gridSize) * gridSize;
    ctx.beginPath();
    ctx.moveTo(cx, 0);
    ctx.lineTo(cx, h);
    ctx.stroke();
    ctx.beginPath();
    ctx.moveTo(0, cy);
    ctx.lineTo(w, cy);
    ctx.stroke();
  }, []);

  const drawAxis = useCallback((ctx: CanvasRenderingContext2D) => {
    const ox = 30;
    const oy = 30;
    const len = 25;

    ctx.strokeStyle = '#E53935';
    ctx.lineWidth = 1.5;
    ctx.beginPath();
    ctx.moveTo(ox, oy);
    ctx.lineTo(ox + len, oy);
    ctx.stroke();
    ctx.fillStyle = '#E53935';
    ctx.font = '10px monospace';
    ctx.fillText('X', ox + len + 4, oy + 4);

    ctx.strokeStyle = '#1B5E20';
    ctx.beginPath();
    ctx.moveTo(ox, oy);
    ctx.lineTo(ox, oy + len);
    ctx.stroke();
    ctx.fillStyle = '#1B5E20';
    ctx.fillText('Y', ox + 4, oy + len + 14);

    ctx.fillStyle = '#E53935';
    ctx.beginPath();
    ctx.arc(ox, oy, 3, 0, Math.PI * 2);
    ctx.fill();
  }, []);

  const drawBone = useCallback((ctx: CanvasRenderingContext2D, bone: Bone, isSelected: boolean) => {
    if (bone.parentId === null) {
      ctx.fillStyle = '#FFB300';
      ctx.beginPath();
      ctx.arc(bone.x, bone.y, 5, 0, Math.PI * 2);
      ctx.fill();
    }

    const rad = (bone.rotation * Math.PI) / 180;
    const endX = bone.x + Math.cos(rad) * bone.length;
    const endY = bone.y + Math.sin(rad) * bone.length;

    ctx.strokeStyle = isSelected ? '#FFB300' : '#2E7D32';
    ctx.lineWidth = isSelected ? 2.5 : 1.8;
    ctx.lineCap = 'round';
    ctx.beginPath();
    ctx.moveTo(bone.x, bone.y);
    ctx.lineTo(endX, endY);
    ctx.stroke();

    if (isSelected) {
      ctx.fillStyle = '#FFB300';
      ctx.beginPath();
      ctx.arc(bone.x, bone.y, 4, 0, Math.PI * 2);
      ctx.fill();
      ctx.strokeStyle = 'rgba(255,179,0,0.5)';
      ctx.lineWidth = 1;
      ctx.beginPath();
      ctx.arc(bone.x, bone.y, 8, 0, Math.PI * 2);
      ctx.stroke();
    } else {
      ctx.fillStyle = '#FFFFFF';
      ctx.beginPath();
      ctx.arc(bone.x, bone.y, 3, 0, Math.PI * 2);
      ctx.fill();
    }

    ctx.fillStyle = isSelected ? '#FFB300' : '#FFFFFF';
    ctx.beginPath();
    ctx.arc(endX, endY, isSelected ? 4 : 3, 0, Math.PI * 2);
    ctx.fill();
  }, []);

  const drawSkeleton = useCallback((ctx: CanvasRenderingContext2D, bones: Bone[]) => {
    const drawRecursive = (bone: Bone) => {
      drawBone(ctx, bone, bone.id === selectedBoneId);
      for (const child of bone.children) {
        drawRecursive(child);
      }
    };
    for (const root of bones) {
      drawRecursive(root);
    }
  }, [drawBone, selectedBoneId]);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const parent = canvas.parentElement;
    if (!parent) return;

    const resize = () => {
      canvas.width = parent.clientWidth;
      canvas.height = parent.clientHeight;
    };
    resize();
    const observer = new ResizeObserver(resize);
    observer.observe(parent);
    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    if (bgImageRef.current && bgLoaded) {
      const img = bgImageRef.current;
      const imgRatio = img.width / img.height;
      const canvasRatio = canvas.width / canvas.height;
      let drawWidth, drawHeight, offsetX, offsetY;
      
      if (canvasRatio > imgRatio) {
        drawHeight = canvas.height;
        drawWidth = drawHeight * imgRatio;
        offsetX = (canvas.width - drawWidth) / 2;
        offsetY = 0;
      } else {
        drawWidth = canvas.width;
        drawHeight = drawWidth / imgRatio;
        offsetX = 0;
        offsetY = (canvas.height - drawHeight) / 2;
      }
      
      ctx.fillStyle = '#F5F0E8';
      ctx.fillRect(0, 0, canvas.width, canvas.height);
      ctx.drawImage(img, offsetX, offsetY, drawWidth, drawHeight);
    } else {
      ctx.fillStyle = '#F5F0E8';
      ctx.fillRect(0, 0, canvas.width, canvas.height);
    }

    if (showGrid) {
      drawGrid(ctx, canvas.width, canvas.height);
    }

    if (onionSkinEnabled) {
      const prevBones = skeleton;
      if (prevBones.length > 0) {
        ctx.globalAlpha = 0.15;
        const drawRecursive = (bone: Bone) => {
          const rad = (bone.rotation * Math.PI) / 180;
          const endX = bone.x + Math.cos(rad) * bone.length;
          const endY = bone.y + Math.sin(rad) * bone.length;
          ctx.strokeStyle = '#1976D2';
          ctx.lineWidth = 1.5;
          ctx.lineCap = 'round';
          ctx.beginPath();
          ctx.moveTo(bone.x + 5, bone.y + 5);
          ctx.lineTo(endX + 5, endY + 5);
          ctx.stroke();
          ctx.fillStyle = 'rgba(25,118,210,0.4)';
          ctx.beginPath();
          ctx.arc(bone.x + 5, bone.y + 5, 2.5, 0, Math.PI * 2);
          ctx.fill();
          ctx.beginPath();
          ctx.arc(endX + 5, endY + 5, 2.5, 0, Math.PI * 2);
          ctx.fill();
          for (const c of bone.children) drawRecursive(c);
        };
        for (const root of prevBones) drawRecursive(root);
        ctx.globalAlpha = 1;
      }
    }

    drawAxis(ctx);
    drawSkeleton(ctx, skeleton);
  }, [skeleton, showGrid, onionSkinEnabled, selectedBoneId, drawGrid, drawAxis, drawSkeleton, bgLoaded]);

  const handleCanvasClick = useCallback((e: React.MouseEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    const scaleX = canvas.width / rect.width;
    const scaleY = canvas.height / rect.height;
    const mx = (e.clientX - rect.left) * scaleX;
    const my = (e.clientY - rect.top) * scaleY;

    const threshold = 10;
    let closestId: number | null = null;
    let closestDist = threshold;

    const checkBone = (bone: Bone) => {
      const rad = (bone.rotation * Math.PI) / 180;
      const endX = bone.x + Math.cos(rad) * bone.length;
      const endY = bone.y + Math.sin(rad) * bone.length;

      const dStart = Math.hypot(mx - bone.x, my - bone.y);
      const dEnd = Math.hypot(mx - endX, my - endY);

      if (dStart < closestDist) { closestDist = dStart; closestId = bone.id; }
      if (dEnd < closestDist) { closestDist = dEnd; closestId = bone.id; }

      for (const child of bone.children) checkBone(child);
    };

    for (const root of skeleton) checkBone(root);
    selectBone(closestId);
  }, [skeleton, selectBone]);

  return (
    <canvas
      ref={canvasRef}
      className="w-full h-full cursor-crosshair"
      onClick={handleCanvasClick}
    />
  );
}
