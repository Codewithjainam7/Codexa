"use client";
import React from "react";
import { cn } from "../../lib/utils";

export const AuroraBackground = ({
  className,
  children,
  ...props
}) => {
  return (
    <div
      className={cn(
        "relative w-full text-slate-100",
        className
      )}
      {...props}
    >
      {/* Full-width glowing Aurora atmosphere */}
      <div className="fixed inset-0 overflow-hidden pointer-events-none -z-10 bg-slate-950">
        {/* Ambient Top Light Beam 1 (Emerald/Teal Glow) */}
        <div className="absolute -top-[20%] left-1/2 -translate-x-1/2 w-[140vw] max-w-[1400px] h-[650px] bg-gradient-to-b from-emerald-500/20 via-teal-500/15 to-transparent rounded-full blur-[90px] opacity-80 animate-pulse pointer-events-none" />

        {/* Ambient Top Light Beam 2 (Cyan/Indigo Glow) */}
        <div className="absolute -top-[10%] left-[20%] w-[90vw] max-w-[900px] h-[500px] bg-gradient-to-br from-cyan-500/15 via-indigo-500/10 to-transparent rounded-full blur-[100px] opacity-70 pointer-events-none" />

        {/* Ambient Top Light Beam 3 (Emerald/Green Accent) */}
        <div className="absolute -top-[15%] right-[15%] w-[80vw] max-w-[800px] h-[450px] bg-gradient-to-bl from-emerald-400/15 via-teal-600/10 to-transparent rounded-full blur-[100px] opacity-65 pointer-events-none" />

        {/* Dynamic Aceternity Aurora Canvas Stripes */}
        <div
          className={cn(
            `
            [--aurora:repeating-linear-gradient(100deg,rgba(16,185,129,0.18)_0%,rgba(20,184,166,0.15)_15%,rgba(6,182,212,0.12)_30%,rgba(99,102,241,0.08)_45%,rgba(16,185,129,0.18)_60%)]
            [--dark-gradient:repeating-linear-gradient(100deg,rgba(2,6,23,0.85)_0%,rgba(2,6,23,0.85)_10%,transparent_15%,transparent_20%,rgba(2,6,23,0.85)_25%)]
            [background-image:var(--dark-gradient),var(--aurora)]
            [background-size:250%,_150%]
            [background-position:50%_50%,50%_50%]
            filter blur-[35px]
            after:content-[""] after:absolute after:inset-0 after:[background-image:var(--dark-gradient),var(--aurora)] 
            after:[background-size:200%,_100%] 
            after:animate-aurora after:[background-attachment:fixed] after:mix-blend-screen
            pointer-events-none
            absolute -inset-[30px] opacity-60 will-change-transform
            [mask-image:linear-gradient(to_bottom,black_0%,black_60%,transparent_100%)]`
          )}
        />

        {/* Subtle grid pattern overlay */}
        <div className="absolute inset-0 bg-[linear-gradient(to_right,#1e293b15_1px,transparent_1px),linear-gradient(to_bottom,#1e293b15_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_100%)] opacity-40 pointer-events-none" />
      </div>

      <div className="relative z-10 w-full">
        {children}
      </div>
    </div>
  );
};

export default AuroraBackground;
