"use client";
import React, { useEffect, useState, useRef } from "react";
import { useTheme } from "../../context/ThemeContext";

export default function BackgroundRippleEffect({
  className = "",
  interactive = true,
}) {
  const { theme } = useTheme();
  const isDark = theme === "dark";
  const [ripples, setRipples] = useState([]);
  const containerRef = useRef(null);
  const lastMoveRef = useRef(0);

  useEffect(() => {
    if (!interactive) return;

    const addRippleAt = (clientX, clientY, isClick = false) => {
      const rect = containerRef.current?.getBoundingClientRect();
      const x = rect ? clientX - rect.left : clientX;
      const y = rect ? clientY - rect.top : clientY;

      const newRipple = {
        id: Date.now() + Math.random(),
        x,
        y,
        size: isClick ? 420 : 280,
        opacity: isClick ? (isDark ? 0.45 : 0.35) : (isDark ? 0.25 : 0.18),
        duration: isClick ? 1800 : 1400,
      };

      setRipples((prev) => [...prev.slice(-8), newRipple]);

      setTimeout(() => {
        setRipples((prev) => prev.filter((r) => r.id !== newRipple.id));
      }, newRipple.duration);
    };

    const handlePointerDown = (e) => {
      const target = e.target;
      if (target && target.closest && target.closest("footer")) return;
      addRippleAt(e.clientX, e.clientY, true);
    };

    const handlePointerMove = (e) => {
      const now = Date.now();
      if (now - lastMoveRef.current > 180) {
        lastMoveRef.current = now;
        const target = e.target;
        if (target && target.closest && target.closest("footer")) return;
        addRippleAt(e.clientX, e.clientY, false);
      }
    };

    window.addEventListener("pointerdown", handlePointerDown, { passive: true });
    window.addEventListener("pointermove", handlePointerMove, { passive: true });
    return () => {
      window.removeEventListener("pointerdown", handlePointerDown);
      window.removeEventListener("pointermove", handlePointerMove);
    };
  }, [interactive, isDark]);

  // Ambient fluid focal wave ripples
  const ambientWaves = [
    { x: "30%", y: "25%", delay: "0s", duration: "7s", scale: 1.2 },
    { x: "70%", y: "45%", delay: "2.5s", duration: "8s", scale: 1.0 },
    { x: "20%", y: "75%", delay: "4s", duration: "7.5s", scale: 1.1 },
    { x: "80%", y: "85%", delay: "1.2s", duration: "8.5s", scale: 0.9 }
  ];

  return (
    <div
      ref={containerRef}
      className={`absolute inset-0 pointer-events-none overflow-hidden select-none z-0 ${className}`}
      aria-hidden="true"
    >
      {/* Soft Fluid Ambient Water Ripples */}
      {ambientWaves.map((wave, idx) => (
        <div
          key={idx}
          className="absolute transform -translate-x-1/2 -translate-y-1/2"
          style={{ left: wave.x, top: wave.y }}
        >
          <div
            className="rounded-full animate-fluid-ripple"
            style={{
              width: `${380 * wave.scale}px`,
              height: `${380 * wave.scale}px`,
              background: isDark
                ? "radial-gradient(circle, rgba(59, 130, 246, 0.16) 0%, rgba(37, 99, 235, 0.05) 50%, transparent 75%)"
                : "radial-gradient(circle, rgba(37, 99, 235, 0.12) 0%, rgba(59, 130, 246, 0.03) 50%, transparent 75%)",
              animationDelay: wave.delay,
              animationDuration: wave.duration,
              filter: "blur(8px)",
            }}
          />
        </div>
      ))}

      {/* Dynamic Cursor & Click Water Wave Ripples */}
      {ripples.map((ripple) => (
        <div
          key={ripple.id}
          className="absolute pointer-events-none transform -translate-x-1/2 -translate-y-1/2"
          style={{ left: `${ripple.x}px`, top: `${ripple.y}px` }}
        >
          <span
            className="block rounded-full animate-water-expand"
            style={{
              width: `${ripple.size}px`,
              height: `${ripple.size}px`,
              background: isDark
                ? `radial-gradient(circle, rgba(96, 165, 250, ${ripple.opacity}) 0%, rgba(59, 130, 246, ${ripple.opacity * 0.4}) 45%, transparent 75%)`
                : `radial-gradient(circle, rgba(37, 99, 235, ${ripple.opacity}) 0%, rgba(59, 130, 246, ${ripple.opacity * 0.4}) 45%, transparent 75%)`,
              animationDuration: `${ripple.duration}ms`,
            }}
          />
        </div>
      ))}
    </div>
  );
}

export { BackgroundRippleEffect };
