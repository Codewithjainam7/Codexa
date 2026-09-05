"use client";
import React, { useEffect, useState, useRef } from "react";
import { useTheme } from "../../context/ThemeContext";

export default function BackgroundRippleEffect({
  className = "",
  numCircles = 8,
  mainCircleSize = 220,
  mainCircleOpacity = 0.30,
  interactive = true,
  focalPoints = [
    { x: "50%", y: "24%", size: 280, scale: 1.15 },
    { x: "85%", y: "15%", size: 180, scale: 0.85 },
    { x: "12%", y: "65%", size: 220, scale: 0.95 },
    { x: "65%", y: "82%", size: 200, scale: 0.9 }
  ]
}) {
  const { theme } = useTheme();
  const isDark = theme === "dark";
  const [clickRipples, setClickRipples] = useState([]);
  const containerRef = useRef(null);

  useEffect(() => {
    if (!interactive) return;

    const handleClick = (e) => {
      // Don't trigger if clicked inside footer or interactive controls that prefer quiet
      const target = e.target;
      if (target.closest("footer")) return;

      const rect = containerRef.current?.getBoundingClientRect();
      const x = rect ? e.clientX - rect.left : e.clientX;
      const y = rect ? e.clientY - rect.top : e.clientY;

      const newRipple = {
        id: Date.now() + Math.random(),
        x,
        y,
      };

      setClickRipples((prev) => [...prev.slice(-4), newRipple]);

      setTimeout(() => {
        setClickRipples((prev) => prev.filter((r) => r.id !== newRipple.id));
      }, 1500);
    };

    window.addEventListener("pointerdown", handleClick, { passive: true });
    return () => window.removeEventListener("pointerdown", handleClick);
  }, [interactive]);

  const blueBorder = isDark ? "rgba(59, 130, 246, 0.25)" : "rgba(37, 99, 235, 0.20)";
  const blueGlow = isDark ? "rgba(59, 130, 246, 0.09)" : "rgba(37, 99, 235, 0.06)";
  const outerBorder = isDark ? "rgba(255, 255, 255, 0.06)" : "rgba(15, 23, 42, 0.06)";

  return (
    <div
      ref={containerRef}
      className={`absolute inset-0 pointer-events-none overflow-hidden select-none -z-10 ${className}`}
      aria-hidden="true"
    >
      {/* Background Focal Ripple Systems */}
      {focalPoints.map((focal, fIdx) => (
        <div
          key={fIdx}
          className="absolute transform -translate-x-1/2 -translate-y-1/2"
          style={{ left: focal.x, top: focal.y }}
        >
          {Array.from({ length: numCircles }).map((_, i) => {
            const size = (focal.size || mainCircleSize) + i * 85 * (focal.scale || 1);
            const opacity = Math.max(0.04, mainCircleOpacity - i * 0.032);
            const delay = `${i * 0.35}s`;
            const duration = `${4.5 + i * 0.4}s`;
            const isHighlightRing = i % 2 === 0;

            return (
              <div
                key={i}
                className="absolute rounded-full border transform -translate-x-1/2 -translate-y-1/2 animate-ripple"
                style={{
                  width: `${size}px`,
                  height: `${size}px`,
                  left: "0px",
                  top: "0px",
                  borderColor: isHighlightRing ? blueBorder : outerBorder,
                  background: isHighlightRing ? `radial-gradient(circle, ${blueGlow} 0%, transparent 70%)` : "transparent",
                  opacity: opacity,
                  animationDelay: delay,
                  animationDuration: duration,
                  boxShadow: isHighlightRing && i < 3 
                    ? (isDark ? "0 0 35px -5px rgba(59, 130, 246, 0.18)" : "0 0 25px -5px rgba(37, 99, 235, 0.12)")
                    : "none",
                }}
              />
            );
          })}

          {/* Central Pulsing Electric Blue Core */}
          <div
            className="absolute rounded-full transform -translate-x-1/2 -translate-y-1/2 animate-pulse"
            style={{
              width: `${(focal.size || mainCircleSize) * 0.45}px`,
              height: `${(focal.size || mainCircleSize) * 0.45}px`,
              left: "0px",
              top: "0px",
              background: isDark
                ? "radial-gradient(circle, rgba(59, 130, 246, 0.35) 0%, rgba(37, 99, 235, 0.12) 50%, transparent 80%)"
                : "radial-gradient(circle, rgba(37, 99, 235, 0.28) 0%, rgba(59, 130, 246, 0.08) 50%, transparent 80%)",
              filter: "blur(16px)",
            }}
          />
        </div>
      ))}

      {/* Dynamic User Interaction Click Waves */}
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
