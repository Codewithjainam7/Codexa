"use client";
import React, { useEffect, useRef, useCallback, useState } from "react";
import { cn } from "../../lib/utils";

export const CanvasText = ({
  text = "AI-Generated Code",
  className,
  colors = [
    "rgba(0, 168, 232, 1)",      // Fresh Sky #00A8E8
    "rgba(0, 126, 167, 0.95)",   // Cerulean #007EA7
    "rgba(255, 255, 255, 0.95)",  // Pure White #FFFFFF
    "rgba(56, 189, 248, 0.9)",    // Sky Blue
    "rgba(0, 168, 232, 0.85)",   // Fresh Sky
    "rgba(0, 126, 167, 0.8)",    // Cerulean
    "rgba(255, 255, 255, 0.9)",   // White flash
  ],
  animationSpeed = 0.5,
}) => {
  const canvasRef = useRef(null);
  const containerRef = useRef(null);
  const animationRef = useRef(null);
  const offsetRef = useRef(0);
  const [size, setSize] = useState({ width: 0, height: 0 });

  const updateDimensions = useCallback(() => {
    if (!containerRef.current || !canvasRef.current) return;
    const rect = containerRef.current.getBoundingClientRect();
    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    
    const w = Math.ceil(rect.width);
    const h = Math.ceil(rect.height);

    if (w > 0 && h > 0) {
      canvasRef.current.width = w * dpr;
      canvasRef.current.height = h * dpr;
      setSize({ width: w, height: h });
    }
  }, []);

  useEffect(() => {
    updateDimensions();
    window.addEventListener("resize", updateDimensions);
    const timer = setTimeout(updateDimensions, 100);
    return () => {
      window.removeEventListener("resize", updateDimensions);
      clearTimeout(timer);
    };
  }, [updateDimensions, text]);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || size.width === 0 || size.height === 0) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const dpr = Math.min(window.devicePixelRatio || 1, 2);
    const w = size.width;
    const h = size.height;

    const render = () => {
      ctx.save();
      ctx.scale(dpr, dpr);
      ctx.clearRect(0, 0, w, h);

      // Step 1: Draw text as solid mask
      const fontSize = h * 0.78;
      ctx.font = `800 ${fontSize}px 'Space Grotesk', -apple-system, sans-serif`;
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillStyle = "#ffffff";
      ctx.fillText(text, w / 2, h / 2 + h * 0.02);

      // Step 2: Source-in clip operation
      ctx.globalCompositeOperation = "source-in";

      // Step 3: Flowing animated diagonal lines
      const lineGap = 6.5;
      const lineWidth = 4.5;
      const totalSpan = lineGap * colors.length;
      offsetRef.current = (offsetRef.current + animationSpeed) % totalSpan;

      const maxDim = Math.max(w, h) * 2.5;

      for (let i = -maxDim; i < maxDim; i += lineGap) {
        const y = i + offsetRef.current;
        const colorIdx = Math.abs(Math.floor(i / lineGap)) % colors.length;
        ctx.strokeStyle = colors[colorIdx];
        ctx.lineWidth = lineWidth;
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(w, y - w * 0.45);
        ctx.stroke();
      }

      ctx.restore();
      animationRef.current = requestAnimationFrame(render);
    };

    animationRef.current = requestAnimationFrame(render);

    return () => {
      if (animationRef.current) {
        cancelAnimationFrame(animationRef.current);
      }
    };
  }, [size, text, colors, animationSpeed]);

  return (
    <span
      ref={containerRef}
      className={cn("relative inline-block align-middle select-none", className)}
    >
      {/* High-Contrast Crisp Fallback Gradient Text */}
      <span
        className="font-display font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-white via-[#00A8E8] to-[#007EA7]"
        style={{ fontSize: "inherit", lineHeight: "inherit" }}
      >
        {text}
      </span>

      {/* Superimposed Animated Canvas */}
      <canvas
        ref={canvasRef}
        className="absolute inset-0 pointer-events-none w-full h-full"
        style={{ width: "100%", height: "100%" }}
      />
    </span>
  );
};

export default CanvasText;
