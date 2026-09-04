"use client";

import React from "react";
import { cn } from "@/lib/utils";

export function AnimatedCircularProgressBar({
  max = 100,
  min = 0,
  value = 0,
  gaugePrimaryColor = "#10b981",
  gaugeSecondaryColor = "rgba(255, 255, 255, 0.08)",
  className,
}) {
  const circumference = 2 * Math.PI * 40;
  const currentPercent = Math.min(100, Math.max(0, Math.round(((value - min) / (max - min)) * 100)));
  const strokeDashoffset = circumference - (circumference * currentPercent) / 100;

  return (
    <div
      className={cn("relative flex items-center justify-center size-20 sm:size-24", className)}
    >
      <svg
        className="size-full -rotate-90 transform"
        viewBox="0 0 100 100"
      >
        {/* Background Track */}
        <circle
          cx="50"
          cy="50"
          r="40"
          strokeWidth="7"
          stroke={gaugeSecondaryColor}
          fill="none"
          className="transition-colors"
        />
        {/* Animated Progress Ring */}
        <circle
          cx="50"
          cy="50"
          r="40"
          strokeWidth="7"
          stroke={gaugePrimaryColor}
          fill="none"
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={strokeDashoffset}
          className="transition-all duration-700 ease-out"
          style={{
            filter: "drop-shadow(0 0 8px rgba(16, 185, 129, 0.4))",
          }}
        />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
        <span className="font-mono font-black text-sm sm:text-base text-emerald-400">
          {currentPercent}%
        </span>
      </div>
    </div>
  );
}

export default AnimatedCircularProgressBar;
