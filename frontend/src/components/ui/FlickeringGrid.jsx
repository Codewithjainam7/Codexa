"use client";

import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { cn } from "../../lib/utils";

export const FlickeringGrid = ({
  squareSize = 4,
  gridGap = 6,
  flickerChance = 0.12,
  color = "#F59E0B",
  width,
  height,
  className,
  maxOpacity = 0.45,
}) => {
  const canvasRef = useRef(null);
  const containerRef = useRef(null);
  const [isMobile, setIsMobile] = useState(false);
  const [canvasSize, setCanvasSize] = useState(() => ({
    width: typeof window !== "undefined" ? window.innerWidth : 1440,
    height: typeof window !== "undefined" ? window.innerHeight : 900,
  }));

  // Parse color
  const memoizedColor = useMemo(() => {
    if (typeof window === "undefined") {
      return "rgba(245, 158, 11,";
    }
    try {
      const canvas = document.createElement("canvas");
      canvas.width = canvas.height = 1;
      const ctx = canvas.getContext("2d", { willReadFrequently: true });
      if (!ctx) return "rgba(245, 158, 11,";
      ctx.fillStyle = color;
      ctx.fillRect(0, 0, 1, 1);
      const [r, g, b] = ctx.getImageData(0, 0, 1, 1).data;
      return `rgba(${r}, ${g}, ${b},`;
    } catch (e) {
      return "rgba(245, 158, 11,";
    }
  }, [color]);

  const updateCanvasSize = useCallback(() => {
    const mobileCheck = typeof window !== "undefined" && window.innerWidth < 768;
    setIsMobile(mobileCheck);

    if (width && height) {
      setCanvasSize({ width, height });
      return;
    }
    if (containerRef.current) {
      const rect = containerRef.current.getBoundingClientRect();
      const w = width || rect.width || (typeof window !== "undefined" ? window.innerWidth : 1440);
      const h = height || rect.height || (typeof window !== "undefined" ? window.innerHeight : 900);
      if (w > 0 && h > 0) {
        setCanvasSize({ width: Math.floor(w), height: Math.floor(h) });
      }
    }
  }, [width, height]);

  useEffect(() => {
    updateCanvasSize();
    window.addEventListener("resize", updateCanvasSize);
    return () => window.removeEventListener("resize", updateCanvasSize);
  }, [updateCanvasSize]);

  useEffect(() => {
    // Disable canvas animation loop on mobile for 60fps performance
    if (isMobile) return;

    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    let animationFrameId;
    const dpr = typeof window !== "undefined" ? Math.min(window.devicePixelRatio || 1, 2) : 1;
    const w = canvasSize.width;
    const h = canvasSize.height;

    if (w === 0 || h === 0) return;

    canvas.width = Math.floor(w * dpr);
    canvas.height = Math.floor(h * dpr);
    ctx.scale(dpr, dpr);

    const cols = Math.floor((w + gridGap) / (squareSize + gridGap));
    const rows = Math.floor((h + gridGap) / (squareSize + gridGap));

    if (cols <= 0 || rows <= 0) return;

    const squares = new Float32Array(cols * rows);
    for (let i = 0; i < squares.length; i++) {
      squares[i] = Math.random() * maxOpacity;
    }

    let lastTime = 0;
    const animate = (time) => {
      if (time - lastTime >= 1000 / 24) {
        lastTime = time;
        ctx.clearRect(0, 0, w, h);

        for (let i = 0; i < cols; i++) {
          for (let j = 0; j < rows; j++) {
            const index = i * rows + j;
            if (Math.random() < flickerChance) {
              squares[index] = Math.random() * maxOpacity;
            }

            const opacity = squares[index];
            if (opacity > 0.04) {
              ctx.fillStyle = `${memoizedColor} ${opacity.toFixed(2)})`;
              ctx.fillRect(
                i * (squareSize + gridGap),
                j * (squareSize + gridGap),
                squareSize,
                squareSize
              );
            }
          }
        }
      }

      animationFrameId = requestAnimationFrame(animate);
    };

    animationFrameId = requestAnimationFrame(animate);

    return () => {
      if (animationFrameId) {
        cancelAnimationFrame(animationFrameId);
      }
    };
  }, [
    isMobile,
    canvasSize,
    squareSize,
    gridGap,
    flickerChance,
    maxOpacity,
    memoizedColor,
  ]);

  if (isMobile) {
    // Ultra-lightweight 0-overhead static CSS grid pattern on mobile
    return (
      <div
        className={cn(
          "w-full h-full pointer-events-none select-none opacity-20",
          className
        )}
        style={{
          backgroundImage: `radial-gradient(${color} 1px, transparent 1px)`,
          backgroundSize: `${squareSize + gridGap * 2}px ${squareSize + gridGap * 2}px`,
        }}
      />
    );
  }

  return (
    <div
      ref={containerRef}
      className={cn("w-full h-full pointer-events-none select-none", className)}
      style={{
        width: width ? `${width}px` : "100%",
        height: height ? `${height}px` : "100%",
      }}
    >
      <canvas
        ref={canvasRef}
        className="w-full h-full block"
        style={{
          width: width ? `${width}px` : "100%",
          height: height ? `${height}px` : "100%",
        }}
      />
    </div>
  );
};

export default FlickeringGrid;
