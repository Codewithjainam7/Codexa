"use client";

import React, { useEffect, useRef } from "react";
import { cn } from "@/lib/utils";

export function DottedGlowBackground({
  className,
  opacity = 1,
  gap = 18,
  radius = 1.5,
  colorDarkVar = "#334155",
  glowColorDarkVar = "#10b981",
  backgroundOpacity = 0,
  speedMin = 0.4,
  speedMax = 1.2,
  speedScale = 1,
}) {
  const canvasRef = useRef(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    let animationFrameId;
    let width = (canvas.width = canvas.offsetWidth);
    let height = (canvas.height = canvas.offsetHeight);

    const handleResize = () => {
      if (!canvas) return;
      width = canvas.width = canvas.offsetWidth;
      height = canvas.height = canvas.offsetHeight;
    };

    window.addEventListener("resize", handleResize);

    // Grid points with organic glowing wave pulse
    let time = 0;

    const render = () => {
      ctx.clearRect(0, 0, width, height);

      time += 0.015 * speedScale;

      const cols = Math.ceil(width / gap);
      const rows = Math.ceil(height / gap);

      for (let i = 0; i <= cols; i++) {
        for (let j = 0; j <= rows; j++) {
          const x = i * gap;
          const y = j * gap;

          // Distance from canvas center for radial falloff
          const dx = x - width / 2;
          const dy = y - height / 2;
          const dist = Math.sqrt(dx * dx + dy * dy);
          const maxDist = Math.sqrt((width / 2) ** 2 + (height / 2) ** 2) || 1;
          const radialFade = Math.max(0, 1 - dist / maxDist);

          // Moving wave formula
          const wave1 = Math.sin(i * 0.2 + time * 1.5) * Math.cos(j * 0.2 + time * 1.2);
          const wave2 = Math.sin((i + j) * 0.15 - time);
          const glowIntensity = Math.max(0, (wave1 + wave2) / 2);

          // Dot size and color
          const dotRadius = radius * (1 + glowIntensity * 0.8);
          
          if (glowIntensity > 0.4) {
            // Glowing emerald / cyan active particle
            ctx.fillStyle = glowColorDarkVar;
            ctx.shadowColor = glowColorDarkVar;
            ctx.shadowBlur = 8 * glowIntensity;
            ctx.globalAlpha = (0.3 + glowIntensity * 0.7) * opacity * radialFade;
          } else {
            // Subtle base grid dot
            ctx.fillStyle = colorDarkVar;
            ctx.shadowBlur = 0;
            ctx.globalAlpha = 0.25 * opacity * radialFade;
          }

          ctx.beginPath();
          ctx.arc(x, y, dotRadius, 0, Math.PI * 2);
          ctx.fill();
        }
      }

      animationFrameId = requestAnimationFrame(render);
    };

    render();

    return () => {
      window.removeEventListener("resize", handleResize);
      cancelAnimationFrame(animationFrameId);
    };
  }, [gap, radius, colorDarkVar, glowColorDarkVar, opacity, speedScale]);

  return (
    <canvas
      ref={canvasRef}
      className={cn(
        "absolute inset-0 w-full h-full pointer-events-none -z-10",
        className
      )}
    />
  );
}

export default DottedGlowBackground;
