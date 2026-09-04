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
  const [isInView, setIsInView] = useState(true);
  const [canvasSize, setCanvasSize] = useState({ width: 0, height: 0 });

  const memoizedColor = useMemo(() => {
    const toRGBA = (colorStr) => {
      if (typeof window === "undefined") {
        return "rgba(96, 165, 250,";
      }
      const canvas = document.createElement("canvas");
      canvas.width = canvas.height = 1;
      const ctx = canvas.getContext("2d");
      if (!ctx) return "rgba(96, 165, 250,";
      ctx.fillStyle = colorStr;
      ctx.fillRect(0, 0, 1, 1);
      const [r, g, b] = ctx.getImageData(0, 0, 1, 1).data;
      return "rgba(" + r + ", " + g + ", " + b + ",";
    };
    return toRGBA(color);
  }, [color]);

  const updateCanvasSize = useCallback(() => {
    if (width && height) {
      setCanvasSize({ width, height });
      return;
    }
    if (containerRef.current) {
      const { clientWidth, clientHeight } = containerRef.current;
      setCanvasSize({
        width: width || clientWidth || (typeof window !== "undefined" ? window.innerWidth : 800),
        height: height || clientHeight || (typeof window !== "undefined" ? window.innerHeight : 800),
      });
    }
  }, [width, height]);

  useEffect(() => {
    updateCanvasSize();
    window.addEventListener("resize", updateCanvasSize);
    return () => window.removeEventListener("resize", updateCanvasSize);
  }, [updateCanvasSize]);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    let animationFrameId;
    let gridParams;

    const setupGrid = () => {
      const dpr = window.devicePixelRatio || 1;
      const w = canvasSize.width;
      const h = canvasSize.height;

      if (w === 0 || h === 0) return null;

      canvas.width = w * dpr;
      canvas.height = h * dpr;
      ctx.scale(dpr, dpr);

      const cols = Math.floor((w + gridGap) / (squareSize + gridGap));
      const rows = Math.floor((h + gridGap) / (squareSize + gridGap));

      const squares = new Float32Array(cols * rows);
      for (let i = 0; i < squares.length; i++) {
        squares[i] = Math.random() * maxOpacity;
      }

      return { cols, rows, squares, dpr, w, h };
    };

    gridParams = setupGrid();

    let lastTime = 0;
    const animate = (time) => {
      if (!isInView) {
        animationFrameId = requestAnimationFrame(animate);
        return;
      }

      // 30 fps throttle
      if (time - lastTime >= 1000 / 30) {
        lastTime = time;

        if (gridParams) {
          const { cols, rows, squares, w, h } = gridParams;
          ctx.clearRect(0, 0, w, h);

          for (let i = 0; i < cols; i++) {
            for (let j = 0; j < rows; j++) {
              const index = i * rows + j;
              if (Math.random() < flickerChance) {
                squares[index] = Math.random() * maxOpacity;
              }

              const opacity = squares[index];
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
    isInView,
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
        className="w-full h-full"
        style={{
          width: width ? (width + "px") : "100%",
          height: height ? (height + "px") : "100%",
        }}
      />
    </div>
  );
};

export default FlickeringGrid;
