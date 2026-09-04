"use client";
import React, { useEffect, useRef, useCallback, useState } from "react";
import { cn } from "../../lib/utils";

export const CanvasText = ({
  text = "AI-Generated Code",
  className,
  colors,
  animationSpeed = 0.5,
}) => {
  const canvasRef = useRef(null);
  const containerRef = useRef(null);
  const animationRef = useRef(null);
  const offsetRef = useRef(0);
  const [size, setSize] = useState({ width: 0, height: 0 });

  // Default theme-aware fallback palette
  const activeColors = colors && colors.length > 0
    ? colors
    : [
        "rgba(217, 119, 6, 1)",      // Ochre #D97706
        "rgba(180, 83, 9, 0.95)",    // Deep Amber #B45309
        "rgba(15, 23, 42, 0.95)",    // Slate 900 #0F172A
        "rgba(245, 158, 11, 0.9)",   // Amber #F59E0B
        "rgba(217, 119, 6, 0.85)",
        "rgba(15, 23, 42, 0.9)",
      ];

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
    const timer = setTimeout(updateDimensions, 80);
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
      ctx.font = `900 ${fontSize}px 'Space Grotesk', 'Plus Jakarta Sans', -apple-system, sans-serif`;
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillStyle = "#ffffff";
      ctx.fillText(text, w / 2, h / 2 + h * 0.02);

      // Step 2: Source-in clip operation
      ctx.globalCompositeOperation = "source-in";

      // Step 3: Flowing animated diagonal lines
      const lineGap = 6.5;
      const lineWidth = 4.5;
      const totalSpan = lineGap * activeColors.length;
      offsetRef.current = (offsetRef.current + animationSpeed) % totalSpan;

      const maxDim = Math.max(w, h) * 2.5;

      for (let i = -maxDim; i < maxDim; i += lineGap) {
        const y = i + offsetRef.current;
        const colorIdx = Math.abs(Math.floor(i / lineGap)) % activeColors.length;
        ctx.strokeStyle = activeColors[colorIdx];
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
  }, [size, activeColors, animationSpeed, text]);

  return (
    <span
      ref={containerRef}
      className={cn("relative inline-block align-middle select-none", className)}
    >
      {/* Hidden text measuring node */}
      <span
        className="invisible font-extrabold font-display whitespace-nowrap block"
        style={{ fontSize: "inherit", lineHeight: "inherit" }}
        aria-hidden="true"
      >
        {text}
      </span>

      {/* Canvas rendering view */}
      <canvas
        ref={canvasRef}
        className="absolute inset-0 w-full h-full pointer-events-none"
        style={{ width: "100%", height: "100%" }}
        aria-label={text}
      />
    </span>
  );
};

export default CanvasText;
