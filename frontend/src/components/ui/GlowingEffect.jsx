"use client";

import React, { memo, useCallback, useEffect, useRef } from "react";
import { cn } from "../../lib/utils";

export const GlowingEffect = memo(
  ({
    blur = 14,
    inactiveZone = 0.01,
    proximity = 64,
    spread = 45,
    variant = "purple",
    glow = true,
    className,
    disabled = false,
    borderWidth = 1.5,
  }) => {
    const containerRef = useRef(null);

    const handleMove = useCallback(
      (e) => {
        if (!containerRef.current || disabled) return;

        const container = containerRef.current;
        const rect = container.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        if (proximity > 0) {
          const isNear =
            x >= -proximity &&
            x <= rect.width + proximity &&
            y >= -proximity &&
            y <= rect.height + proximity;

          if (!isNear) {
            container.style.setProperty("--glow-opacity", "0");
            return;
          }
        }

        container.style.setProperty("--glow-opacity", "1");
        container.style.setProperty("--glow-x", `${x}px`);
        container.style.setProperty("--glow-y", `${y}px`);
      },
      [disabled, proximity]
    );

    useEffect(() => {
      if (disabled) return;
      window.addEventListener("pointermove", handleMove);
      return () => {
        window.removeEventListener("pointermove", handleMove);
      };
    }, [handleMove, disabled]);

    return (
      <div
        ref={containerRef}
        style={{
          "--blur": `${blur}px`,
          "--spread": `${spread * 5}px`,
          "--border-width": `${borderWidth}px`,
          "--glow-opacity": "0.15",
          "--glow-x": "50%",
          "--glow-y": "50%",
        }}
        className={cn(
          "pointer-events-none absolute inset-0 rounded-[inherit] overflow-hidden -z-0",
          className
        )}
      >
        {/* Crisp Royal Purple & Sunset Coral Border Glow Tracker */}
        <div
          className="absolute inset-0 rounded-[inherit] transition-opacity duration-300"
          style={{
            opacity: "var(--glow-opacity)",
            background: `radial-gradient(var(--spread) circle at var(--glow-x) var(--glow-y), rgba(192, 132, 252, 0.95), rgba(244, 63, 94, 0.75), rgba(245, 158, 11, 0.4), transparent 70%)`,
            mask: "linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0)",
            WebkitMask: "linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0)",
            maskComposite: "exclude",
            WebkitMaskComposite: "xor",
            padding: "var(--border-width)",
          }}
        />

        {/* Ambient Blur Spillover */}
        {glow && (
          <div
            className="absolute inset-0 rounded-[inherit] blur-[var(--blur)] transition-opacity duration-300"
            style={{
              opacity: "calc(var(--glow-opacity) * 0.45)",
              background: `radial-gradient(calc(var(--spread) * 0.9) circle at var(--glow-x) var(--glow-y), rgba(192, 132, 252, 0.6), rgba(244, 63, 94, 0.4), transparent 70%)`,
            }}
          />
        )}
      </div>
    );
  }
);

GlowingEffect.displayName = "GlowingEffect";

export default GlowingEffect;
