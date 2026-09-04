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
      {/* Full-width glowing Cyan/Violet/Indigo Cyber Aurora atmosphere */}
      <div className="fixed inset-0 overflow-hidden pointer-events-none -z-10 bg-slate-950">
        {/* Ambient Top Light Beam 1 (Vibrant Cyan Glow) */}
        <div className="absolute -top-[25%] left-1/2 -translate-x-1/2 w-[140vw] max-w-[1400px] h-[580px] bg-gradient-to-b from-cyan-500/15 via-blue-600/10 to-transparent rounded-full blur-[120px] opacity-80 pointer-events-none" />

        {/* Ambient Top Light Beam 2 (Electric Violet Glow) */}
        <div className="absolute -top-[15%] left-[10%] w-[80vw] max-w-[800px] h-[480px] bg-gradient-to-br from-violet-600/15 via-indigo-600/10 to-transparent rounded-full blur-[130px] opacity-75 pointer-events-none" />

        {/* Ambient Top Light Beam 3 (Emerald & Sky Cyan Accent) */}
        <div className="absolute -top-[18%] right-[10%] w-[80vw] max-w-[850px] h-[480px] bg-gradient-to-bl from-teal-500/12 via-cyan-600/10 to-transparent rounded-full blur-[130px] opacity-70 pointer-events-none" />

        {/* Dynamic Aceternity Aurora Stripes */}
        <div
          className={cn(
            `
            [--aurora:repeating-linear-gradient(100deg,rgba(6,182,212,0.12)_0%,rgba(99,102,241,0.1)_15%,rgba(168,85,247,0.08)_30%,rgba(20,184,166,0.08)_45%,rgba(6,182,212,0.12)_60%)]
            [--dark-gradient:repeating-linear-gradient(100deg,rgba(2,6,23,0.92)_0%,rgba(2,6,23,0.92)_10%,transparent_15%,transparent_20%,rgba(2,6,23,0.92)_25%)]
            [background-image:var(--dark-gradient),var(--aurora)]
            [background-size:250%,_150%]
            [background-position:50%_50%,50%_50%]
            filter blur-[35px]
            after:content-[""] after:absolute after:inset-0 after:[background-image:var(--dark-gradient),var(--aurora)] 
            after:[background-size:200%,_100%] 
            after:animate-aurora after:[background-attachment:fixed] after:mix-blend-screen
            pointer-events-none
            absolute -inset-[30px] opacity-65 will-change-transform
            [mask-image:linear-gradient(to_bottom,black_0%,black_60%,transparent_100%)]`
          )}
        />

        {/* Fine Matrix Tech Grid Overlay */}
        <div className="absolute inset-0 bg-[linear-gradient(to_right,#38bdf80a_1px,transparent_1px),linear-gradient(to_bottom,#38bdf80a_1px,transparent_1px)] bg-[size:3.5rem_3.5rem] [mask-image:radial-gradient(ellipse_70%_60%_at_50%_0%,#000_70%,transparent_100%)] opacity-40 pointer-events-none" />
      </div>

      <div className="relative z-10 w-full">
        {children}
      </div>
    </div>
  );
};

export default AuroraBackground;
