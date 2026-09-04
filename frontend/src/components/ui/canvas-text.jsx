"use client";
import React, { useEffect, useRef, useState } from "react";
import { cn } from "../../lib/utils";

export const CanvasText = ({
  text = "AI-Generated Code",
  className,
  backgroundClassName = "bg-neutral-900 border border-neutral-700",
  colors = [
    "rgba(255, 255, 255, 1)",
    "rgba(240, 240, 240, 0.9)",
    "rgba(210, 210, 210, 0.8)",
    "rgba(180, 180, 180, 0.7)",
    "rgba(150, 150, 150, 0.6)",
    "rgba(120, 120, 120, 0.5)",
    "rgba(90, 90, 90, 0.4)",
    "rgba(60, 60, 60, 0.3)",
    "rgba(40, 40, 40, 0.2)",
    "rgba(20, 20, 20, 0.1)",
  ],
  lineGap = 4,
  animationDuration = 20,
}) => {
  const canvasRef = useRef(null);
  const containerRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    let animationFrameId;
    let offset = 0;
    const speed = (lineGap * colors.length) / (animationDuration * 30);

    const render = () => {
      const { width, height } = canvas;
      if (width === 0 || height === 0) {
        animationFrameId = requestAnimationFrame(render);
        return;
      }
      ctx.clearRect(0, 0, width, height);

      offset = (offset + speed) % (lineGap * colors.length);

      const totalLines = Math.ceil((width + height * 2) / lineGap);
      for (let i = -totalLines; i < totalLines * 2; i++) {
        const y = i * lineGap + offset;
        const colorIndex = Math.abs(
          Math.floor((i + offset) % colors.length)
        );
        ctx.strokeStyle = colors[colorIndex] || colors[0];
        ctx.lineWidth = lineGap * 0.85;
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(width, y + height);
        ctx.stroke();
      }

      animationFrameId = requestAnimationFrame(render);
    };

    const updateSize = () => {
      if (containerRef.current && canvas) {
        const rect = containerRef.current.getBoundingClientRect();
        canvas.width = Math.max(rect.width, 100);
        canvas.height = Math.max(rect.height, 30);
      }
    };

    updateSize();
    window.addEventListener("resize", updateSize);
    render();

    return () => {
      cancelAnimationFrame(animationFrameId);
      window.removeEventListener("resize", updateSize);
    };
  }, [colors, lineGap, animationDuration]);

  return (
    <span
      ref={containerRef}
      className={cn(
        "relative inline-flex items-center justify-center overflow-hidden rounded-2xl px-4 py-1 font-extrabold align-middle transition-all shadow-inner",
        backgroundClassName,
        className
      )}
    >
      <canvas
        ref={canvasRef}
        className="absolute inset-0 h-full w-full pointer-events-none opacity-40 mix-blend-screen"
      />
      <span className="relative z-10 text-white font-display tracking-tight drop-shadow-[0_0_20px_rgba(255,255,255,0.6)]">
        {text}
      </span>
    </span>
  );
};

export default CanvasText;
