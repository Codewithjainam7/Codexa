"use client";
import React, { useEffect, useRef, useCallback } from "react";
import { cn } from "../../lib/utils";

export const CanvasText = ({
  text = "AI-Generated Code",
  className,
  colors = [
    "rgba(255, 255, 255, 1)",
    "rgba(240, 240, 240, 0.9)",
    "rgba(210, 210, 210, 0.8)",
    "rgba(180, 180, 180, 0.7)",
    "rgba(150, 150, 150, 0.6)",
    "rgba(120, 120, 120, 0.5)",
    "rgba(90, 90, 90, 0.4)",
    "rgba(60, 60, 60, 0.3)",
  ],
  animationSpeed = 0.5,
}) => {
  const canvasRef = useRef(null);
  const containerRef = useRef(null);
  const animationRef = useRef(null);
  const offsetRef = useRef(0);

  const getFont = useCallback((size) => {
    return `900 ${size}px 'Sora', 'Plus Jakarta Sans', sans-serif`;
  }, []);

  const drawFrame = useCallback(() => {
    const canvas = canvasRef.current;
    const container = containerRef.current;
    if (!canvas || !container) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const dpr = window.devicePixelRatio || 1;
    const rect = container.getBoundingClientRect();
    
    // Set canvas size accounting for device pixel ratio
    canvas.width = rect.width * dpr;
    canvas.height = rect.height * dpr;
    ctx.scale(dpr, dpr);

    const w = rect.width;
    const h = rect.height;

    // Clear
    ctx.clearRect(0, 0, w, h);

    // Step 1: Draw the text as a solid fill (this becomes the mask)
    const fontSize = h * 0.75;
    ctx.font = getFont(fontSize);
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillStyle = "white";
    ctx.fillText(text, w / 2, h / 2);

    // Step 2: Use composite operation so subsequent draws only appear INSIDE the text
    ctx.globalCompositeOperation = "source-in";

    // Step 3: Draw animated diagonal lines that flow through the text
    const lineGap = 6;
    const lineWidth = 4;
    const totalSpan = lineGap * colors.length;
    offsetRef.current = (offsetRef.current + animationSpeed) % totalSpan;

    const maxDim = Math.max(w, h) * 2;

    for (let i = -maxDim; i < maxDim; i += lineGap) {
      const y = i + offsetRef.current;
      const colorIdx = Math.abs(Math.floor(i / lineGap)) % colors.length;
      ctx.strokeStyle = colors[colorIdx];
      ctx.lineWidth = lineWidth;
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(w, y - w * 0.5);
      ctx.stroke();
    }

    // Reset composite
    ctx.globalCompositeOperation = "source-over";

    animationRef.current = requestAnimationFrame(drawFrame);
  }, [text, colors, animationSpeed, getFont]);

  useEffect(() => {
    animationRef.current = requestAnimationFrame(drawFrame);
    return () => {
      if (animationRef.current) {
        cancelAnimationFrame(animationRef.current);
      }
    };
  }, [drawFrame]);

  // Measure text to set container width
  const measureRef = useRef(null);
  const [dims, setDims] = React.useState({ width: 300, height: 80 });

  useEffect(() => {
    const measure = () => {
      const el = measureRef.current;
      if (el) {
        const rect = el.getBoundingClientRect();
        setDims({ width: rect.width + 16, height: rect.height + 8 });
      }
    };
    measure();
    window.addEventListener("resize", measure);
    return () => window.removeEventListener("resize", measure);
  }, [text]);

  return (
    <span
      className={cn("relative inline-block align-middle", className)}
    >
      {/* Hidden text for measurement */}
      <span
        ref={measureRef}
        className="invisible font-display font-black whitespace-nowrap"
        style={{ fontSize: "inherit", lineHeight: "inherit" }}
        aria-hidden="true"
      >
        {text}
      </span>

      {/* Canvas overlay */}
      <span
        ref={containerRef}
        className="absolute inset-0 overflow-hidden"
        style={{ width: dims.width, height: dims.height }}
      >
        <canvas
          ref={canvasRef}
          className="w-full h-full"
          style={{ width: "100%", height: "100%" }}
        />
      </span>
    </span>
  );
};

export default CanvasText;
