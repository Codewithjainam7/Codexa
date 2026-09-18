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
  const measureRef = useRef(null);
  const animationRef = useRef(null);
  const offsetRef = useRef(0);
  const [size, setSize] = useState({ width: 0, height: 0 });

  // Default theme-aware fallback palette
  const activeColors = colors && colors.length > 0
    ? colors
    : [
        "rgba(37, 99, 235, 1)",      // Royal Blue
        "rgba(29, 78, 216, 0.95)",   // Deep Blue
        "rgba(15, 23, 42, 0.95)",    // Slate 900
        "rgba(59, 130, 246, 0.9)",   // Vibrant Blue
        "rgba(96, 165, 250, 0.85)",  // Sky Blue
        "rgba(15, 23, 42, 0.9)",
      ];

  const updateDimensions = useCallback(() => {
    if (!containerRef.current) return;
    const rect = containerRef.current.getBoundingClientRect();
    const dpr = typeof window !== "undefined" ? Math.min(window.devicePixelRatio || 1, 2) : 1;
    
    let w = Math.ceil(rect.width);
    let h = Math.ceil(rect.height);

    // Fallback to measureRef if container has zero bounding rect during layout transition
    if ((w === 0 || h === 0) && measureRef.current) {
      w = Math.ceil(measureRef.current.offsetWidth || measureRef.current.scrollWidth || 0);
      h = Math.ceil(measureRef.current.offsetHeight || measureRef.current.scrollHeight || 0);
    }

    if (w > 0 && h > 0) {
      if (canvasRef.current) {
        canvasRef.current.width = w * dpr;
        canvasRef.current.height = h * dpr;
      }
      setSize((prev) => {
        if (prev.width === w && prev.height === h) return prev;
        return { width: w, height: h };
      });
    }
  }, []);

  useEffect(() => {
    updateDimensions();

    let resizeObserver;
    const container = containerRef.current;
    if (container && typeof ResizeObserver !== "undefined") {
      resizeObserver = new ResizeObserver(() => {
        updateDimensions();
      });
      resizeObserver.observe(container);
    }

    window.addEventListener("resize", updateDimensions);
    window.addEventListener("orientationchange", updateDimensions);

    // Multiple staggered timers to catch CSS media query / hidden-to-visible transitions
    const timer1 = setTimeout(updateDimensions, 60);
    const timer2 = setTimeout(updateDimensions, 200);
    const timer3 = setTimeout(updateDimensions, 500);

    return () => {
      if (resizeObserver) resizeObserver.disconnect();
      window.removeEventListener("resize", updateDimensions);
      window.removeEventListener("orientationchange", updateDimensions);
      clearTimeout(timer1);
      clearTimeout(timer2);
      clearTimeout(timer3);
    };
  }, [updateDimensions, text]);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas || size.width === 0 || size.height === 0) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    const dpr = typeof window !== "undefined" ? Math.min(window.devicePixelRatio || 1, 2) : 1;
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
      {/* Measuring / Fallback text: Never leaves a blank hole if canvas is mounting */}
      <span
        ref={measureRef}
        className={cn(
          "font-extrabold font-display whitespace-nowrap block transition-opacity duration-200",
          size.width > 0 ? "invisible" : "visible bg-gradient-to-r from-blue-500 to-indigo-400 bg-clip-text text-transparent"
        )}
        style={{ fontSize: "inherit", lineHeight: "inherit" }}
        aria-hidden={size.width > 0}
      >
        {text}
      </span>

      {/* Canvas rendering view */}
      {size.width > 0 && (
        <canvas
          ref={canvasRef}
          className="absolute inset-0 w-full h-full pointer-events-none block"
          style={{ width: "100%", height: "100%" }}
          aria-label={text}
        />
      )}
    </span>
  );
};

export default CanvasText;
