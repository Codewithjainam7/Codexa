"use client";
import React, { useEffect, useState, useRef } from "react";
import { useTheme } from "../../context/ThemeContext";

export default function BackgroundRippleEffect({
  className = "",
  numCircles = 8,
  mainCircleSize = 260,
  interactive = true,
  focalPoints = [
    { x: "50%", y: "220px", size: 320, scale: 1.2 },
    { x: "85%", y: "480px", size: 240, scale: 0.95 },
    { x: "15%", y: "820px", size: 260, scale: 1.05 },
    { x: "70%", y: "1350px", size: 280, scale: 1.0 }
  ]
}) {
  const { theme } = useTheme();
  const isDark = theme === "dark";
  const [clickRipples, setClickRipples] = useState([]);
  const containerRef = useRef(null);

  useEffect(() => {
    if (!interactive) return;

    const handleClick = (e) => {
      const target = e.target;
      if (target && target.closest && target.closest("footer")) return;

      const rect = containerRef.current?.getBoundingClientRect();
      const x = rect ? e.clientX - rect.left : e.clientX;
      const y = rect ? e.clientY - rect.top : e.clientY;

      const newRipple = {
        id: Date.now() + Math.random(),
        x,
        y,
      };

      setClickRipples((prev) => [...prev.slice(-6), newRipple]);

      setTimeout(() => {
        setClickRipples((prev) => prev.filter((r) => r.id !== newRipple.id));
      }, 1600);
    };

    window.addEventListener("pointerdown", handleClick, { passive: true });
    return () => window.removeEventListener("pointerdown", handleClick);
  }, [interactive]);

  return (
    <div
      ref={containerRef}
      className={`absolute inset-0 pointer-events-none overflow-hidden select-none ${className}`}
      aria-hidden="true"
    >
      {/* Background Focal Sonar Ripple Systems */}
      {focalPoints.map((focal, fIdx) => (
        <div
          key={fIdx}
          className="absolute transform -translate-x-1/2 -translate-y-1/2"
          style={{ left: focal.x, top: focal.y }}
        >
          {Array.from({ length: numCircles }).map((_, i) => {
            const size = (focal.size || mainCircleSize) + i * 95 * (focal.scale || 1);
            const delay = `${i * 0.4}s`;
            const duration = `${6.0 + i * 0.5}s`;
            const isHighlight = i % 2 === 0;

            const borderColor = isDark
              ? (isHighlight 
                  ? (i === 0 ? "rgba(96, 165, 250, 0.55)" : "rgba(59, 130, 246, 0.38)")
                  : "rgba(59, 130, 246, 0.18)")
              : (isHighlight
                  ? (i === 0 ? "rgba(37, 99, 235, 0.45)" : "rgba(37, 99, 235, 0.30)")
                  : "rgba(37, 99, 235, 0.14)");

            const glowBg = isHighlight
              ? (isDark
                  ? `radial-gradient(circle, rgba(59, 130, 246, ${Math.max(0.02, 0.08 - i * 0.01)}) 0%, transparent 70%)`
                  : `radial-gradient(circle, rgba(37, 99, 235, ${Math.max(0.015, 0.05 - i * 0.008)}) 0%, transparent 70%)`)
              : "transparent";

            const shadowGlow = isHighlight && i < 3
              ? (isDark
                  ? "0 0 28px -4px rgba(59, 130, 246, 0.22)"
                  : "0 0 20px -4px rgba(37, 99, 235, 0.12)")
              : "none";

            return (
              <div
                key={i}
                className="absolute rounded-full border transform -translate-x-1/2 -translate-y-1/2 animate-ripple"
                style={{
                  width: `${size}px`,
                  height: `${size}px`,
                  left: "0px",
                  top: "0px",
                  borderColor: borderColor,
                  background: glowBg,
                  animationDelay: delay,
                  animationDuration: duration,
                  boxShadow: shadowGlow,
                }}
              />
            );
          })}

          {/* Central Pulsing Electric Blue Core */}
          <div
            className="absolute rounded-full transform -translate-x-1/2 -translate-y-1/2 animate-pulse"
            style={{
              width: `${(focal.size || mainCircleSize) * 0.5}px`,
              height: `${(focal.size || mainCircleSize) * 0.5}px`,
              left: "0px",
              top: "0px",
              background: isDark
                ? "radial-gradient(circle, rgba(59, 130, 246, 0.25) 0%, rgba(37, 99, 235, 0.08) 50%, transparent 80%)"
                : "radial-gradient(circle, rgba(37, 99, 235, 0.18) 0%, rgba(59, 130, 246, 0.05) 50%, transparent 80%)",
              filter: "blur(20px)",
            }}
          />
        </div>
      ))}

      {/* Dynamic User Click Waves */}
      {clickRipples.map((ripple) => (
        <div
          key={ripple.id}
          className="absolute pointer-events-none"
          style={{ left: `${ripple.x}px`, top: `${ripple.y}px` }}
        >
          <span className="cdx-interactive-ripple cdx-ripple-ring-1" />
          <span className="cdx-interactive-ripple cdx-ripple-ring-2" />
          <span className="cdx-interactive-ripple cdx-ripple-ring-3" />
        </div>
      ))}
    </div>
  );
}
export { BackgroundRippleEffect };
