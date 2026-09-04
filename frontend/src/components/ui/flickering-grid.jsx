"use client";

import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { cn } from "../../lib/utils";

export const FlickeringGrid = ({
  squareSize = 4,
  gridGap = 6,
  flickerChance = 0.1,
  color = "#60A5FA",
  width,
  height,
  className,
  maxOpacity = 0.5,
}) => {
  const canvasRef = useRef(null);
  const containerRef = useRef(null);
  const [canvasSize, setCanvasSize] = useState(() => ({
    width: typeof window !== "undefined" ? window.innerWidth : 1200,
    height: typeof window !== "undefined" ? window.innerHeight : 800,
  }));

  const memoizedColor = useMemo(() => {
    const toRGBA = (colorStr) => {
      if (typeof window === "undefined") {
        return "rgba(96, 165, 250,";
      }
      try {
        const canvas = document.createElement("canvas");
        canvas.width = canvas.height = 1;
        const ctx = canvas.getContext("2d");
        if (!ctx) return "rgba(96, 165, 250,";
        ctx.fillStyle = colorStr;
        ctx.fillRect(0, 0, 1, 1);
        const [r, g, b] = ctx.getImageData(0, 0, 1, 1).data;
        return "rgba(" + r + ", " + g + ", " + b + ",";
      } catch (e) {
        return "rgba(96, 165, 250,";
      }
    };
    return toRGBA(color);
  }, [color]);

  const updateCanvasSize = useCallback(() => {
    if (width && height) {
      setCanvasSize({ width, height });
      return;
    }
    if (containerRef.current) {
      const rect = containerRef.current.getBoundingClientRect();
      const w = width || rect.width || (typeof window !== "undefined" ? window.innerWidth : 1200);
      const h = height || rect.height || (typeof window !== "undefined" ? window.innerHeight : 800);
      if (w > 0 && h > 0) {
        setCanvasSize({ width: Math.floor(w), height: Math.floor(h) });
      }
    }
  }, [width, height]);

  useEffect(() => {
    updateCanvasSize();
    window.addEventListener("resize", updateCanvasSize);

    let resizeObserver;
    if (containerRef.current && typeof ResizeObserver !== "undefined") {
      resizeObserver = new ResizeObserver(() => {
        updateCanvasSize();
      });
      resizeObserver.observe(containerRef.current);
    }

    return () => {
      window.removeEventListener("resize", updateCanvasSize);
      if (resizeObserver) resizeObserver.disconnect();
    };
  }, [updateCanvasSize]);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    let animationFrameId;

    const dpr = typeof window !== "undefined" ? window.devicePixelRatio || 1 : 1;
    const w = canvasSize.width;
    const h = canvasSize.height;

    if (w === 0 || h === 0) return;

    canvas.width = Math.floor(w * dpr);
    canvas.height = Math.floor(h * dpr);
    ctx.scale(dpr, dpr);

    const cols = Math.floor((w + gridGap) / (squareSize + gridGap));
    const rows = Math.floor((h + gridGap) / (squareSize + gridGap));

    const squares = new Float32Array(cols * rows);
    for (let i = 0; i < squares.length; i++) {
      squares[i] = Math.random() * maxOpacity;
    }

    let lastTime = 0;
    const animate = (time) => {
      if (time - lastTime >= 1000 / 30) {
        lastTime = time;

        ctx.clearRect(0, 0, w, h);

        for (let i = 0; i < cols; i++) {
          for (let j = 0; j < rows; j++) {
            const index = i * rows + j;
            if (Math.random() < flickerChance) {
              squares[index] = Math.random() * maxOpacity;
            }

            const opacity = squares[index];
            if (opacity > 0.02) {
              ctx.fillStyle = memoizedColor + " " + opacity + ")";
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
      className={cn("w-full h-full pointer-events-none", className)}
      style={{
        width: width ? (width + "px") : "100%",
        height: height ? (height + "px") : "100%",
      }}
    >
      <canvas
        ref={canvasRef}
        className="w-full h-full block"
        style={{
          width: width ? (width + "px") : "100%",
          height: height ? (height + "px") : "100%",
        }}
      />
    </div>
  );
};

export default FlickeringGrid;
