"use client";

import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { cn } from "../../lib/utils";

export const FlickeringGrid = ({
  squareSize = 4,
  gridGap = 6,
  flickerChance = 0.12,
  color = "#3B82F6",
  width,
  height,
  className,
  maxOpacity = 0.45,
}) => {
  const canvasRef = useRef(null);
  const containerRef = useRef(null);
  const [isMobile, setIsMobile] = useState(() => {
    return typeof window !== "undefined" ? window.innerWidth < 768 : false;
  });
  const [canvasSize, setCanvasSize] = useState(() => ({
    width: typeof window !== "undefined" ? window.innerWidth : 1440,
    height: typeof window !== "undefined" ? window.innerHeight : 900,
  }));

  // Parse color to RGBA prefix
  const memoizedColor = useMemo(() => {
    if (typeof window === "undefined") {
      return "rgba(59, 130, 246,";
    }
    try {
      const canvas = document.createElement("canvas");
      canvas.width = canvas.height = 1;
      const ctx = canvas.getContext("2d", { willReadFrequently: true });
      if (!ctx) return "rgba(59, 130, 246,";
      ctx.fillStyle = color;
      ctx.fillRect(0, 0, 1, 1);
      const [r, g, b] = ctx.getImageData(0, 0, 1, 1).data;
      return `rgba(${r}, ${g}, ${b},`;
    } catch (e) {
      return "rgba(59, 130, 246,";
    }
  }, [color]);

  const updateCanvasSize = useCallback(() => {
    if (typeof window === "undefined") return;
    const mobileCheck = window.innerWidth < 768;
    setIsMobile(mobileCheck);

    if (width && height) {
      setCanvasSize({ width, height });
      return;
    }

    const container = containerRef.current;
    let w = 0;
    let h = 0;

    if (container) {
      const rect = container.getBoundingClientRect();
      w = rect.width;
      h = rect.height;
    }

    if (!w || !h) {
      w = typeof window !== "undefined" ? window.innerWidth : 1440;
      h = typeof window !== "undefined" ? window.innerHeight : 900;
    }

    if (w > 0 && h > 0) {
      setCanvasSize((prev) => {
        const nw = Math.floor(w);
        const nh = Math.floor(h);
        if (prev.width === nw && prev.height === nh) return prev;
        return { width: nw, height: nh };
      });
    }
  }, [width, height]);

  useEffect(() => {
    updateCanvasSize();

    let resizeObserver;
    const container = containerRef.current;
    if (container && typeof ResizeObserver !== "undefined") {
      resizeObserver = new ResizeObserver(() => {
        updateCanvasSize();
      });
      resizeObserver.observe(container);
    }

    window.addEventListener("resize", updateCanvasSize);
    window.addEventListener("orientationchange", updateCanvasSize);

    return () => {
      if (resizeObserver) resizeObserver.disconnect();
      window.removeEventListener("resize", updateCanvasSize);
      window.removeEventListener("orientationchange", updateCanvasSize);
    };
  }, [updateCanvasSize]);

  useEffect(() => {
    // Disable heavy canvas animation loop on mobile for 60fps performance
    if (isMobile) return;

    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    let animationFrameId;
    const dpr = typeof window !== "undefined" ? Math.min(window.devicePixelRatio || 1, 2) : 1;
    const w = canvasSize.width;
    const h = canvasSize.height;

    if (w <= 0 || h <= 0) return;

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

  return (
    <div
      ref={containerRef}
      className={cn("w-full h-full pointer-events-none select-none relative", className)}
      style={{
        width: width ? `${width}px` : "100%",
        height: height ? `${height}px` : "100%",
      }}
    >
      {isMobile ? (
        <div
          className="w-full h-full pointer-events-none select-none opacity-20"
          style={{
            backgroundImage: `radial-gradient(${color} 1px, transparent 1px)`,
            backgroundSize: `${squareSize + gridGap * 2}px ${squareSize + gridGap * 2}px`,
          }}
        />
      ) : (
        <canvas
          ref={canvasRef}
          className="w-full h-full block"
          style={{
            width: "100%",
            height: "100%",
          }}
        />
      )}
    </div>
  );
};

export default FlickeringGrid;
