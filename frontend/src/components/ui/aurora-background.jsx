"use client";
import React from "react";
import { cn } from "../../lib/utils";

export const AuroraBackground = ({
  className,
  children,
  showRadialGradient = true,
  ...props
}) => {
  return (
    <div
      className={cn(
        "relative flex flex-col items-center justify-start bg-slate-950 text-slate-100 transition-colors overflow-hidden min-h-screen",
        className
      )}
      {...props}
    >
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div
          className={cn(
            `
            [--aurora:repeating-linear-gradient(100deg,rgba(16,185,129,0.22)_10%,rgba(20,184,166,0.18)_15%,rgba(6,182,212,0.15)_20%,rgba(99,102,241,0.12)_25%,rgba(16,185,129,0.2)_30%)]
            [--dark-gradient:repeating-linear-gradient(100deg,rgba(2,6,23,0.92)_0%,rgba(2,6,23,0.92)_7%,transparent_10%,transparent_12%,rgba(2,6,23,0.92)_16%)]
            [background-image:var(--dark-gradient),var(--aurora)]
            [background-size:300%,_200%]
            [background-position:50%_50%,50%_50%]
            filter blur-[25px]
            after:content-[""] after:absolute after:inset-0 after:[background-image:var(--dark-gradient),var(--aurora)] 
            after:[background-size:200%,_100%] 
            after:animate-aurora after:[background-attachment:fixed] after:mix-blend-screen
            pointer-events-none
            absolute -inset-[20px] opacity-75 will-change-transform`,
            showRadialGradient &&
              `[mask-image:radial-gradient(ellipse_at_50%_20%,black_45%,transparent_80%)]`
          )}
        />
      </div>
      <div className="relative z-10 w-full">
        {children}
      </div>
    </div>
  );
};

export default AuroraBackground;
