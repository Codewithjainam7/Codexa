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
        "relative w-full text-neutral-100",
        className
      )}
      {...props}
    >
      {/* Full-width glowing Monochrome Aurora atmosphere */}
      <div className="fixed inset-0 overflow-hidden pointer-events-none -z-10 bg-black">
        {/* Ambient Top Light Beam 1 (Pure White / Silver Glow) */}
        <div className="absolute -top-[25%] left-1/2 -translate-x-1/2 w-[140vw] max-w-[1400px] h-[550px] bg-gradient-to-b from-white/10 via-neutral-400/5 to-transparent rounded-full blur-[100px] opacity-70 pointer-events-none" />

        {/* Ambient Top Light Beam 2 (Zinc Glow) */}
        <div className="absolute -top-[15%] left-[20%] w-[90vw] max-w-[900px] h-[450px] bg-gradient-to-br from-neutral-300/8 via-neutral-700/5 to-transparent rounded-full blur-[110px] opacity-60 pointer-events-none" />

        {/* Ambient Top Light Beam 3 (Charcoal Accent) */}
        <div className="absolute -top-[18%] right-[15%] w-[80vw] max-w-[800px] h-[400px] bg-gradient-to-bl from-white/8 via-neutral-500/5 to-transparent rounded-full blur-[110px] opacity-55 pointer-events-none" />

        {/* Dynamic Aceternity Aurora Monochrome Stripes */}
        <div
          className={cn(
            `
            [--aurora:repeating-linear-gradient(100deg,rgba(255,255,255,0.08)_0%,rgba(200,200,200,0.06)_15%,rgba(150,150,150,0.05)_30%,rgba(100,100,100,0.04)_45%,rgba(255,255,255,0.08)_60%)]
            [--dark-gradient:repeating-linear-gradient(100deg,rgba(0,0,0,0.92)_0%,rgba(0,0,0,0.92)_10%,transparent_15%,transparent_20%,rgba(0,0,0,0.92)_25%)]
            [background-image:var(--dark-gradient),var(--aurora)]
            [background-size:250%,_150%]
            [background-position:50%_50%,50%_50%]
            filter blur-[35px]
            after:content-[""] after:absolute after:inset-0 after:[background-image:var(--dark-gradient),var(--aurora)] 
            after:[background-size:200%,_100%] 
            after:animate-aurora after:[background-attachment:fixed] after:mix-blend-screen
            pointer-events-none
            absolute -inset-[30px] opacity-50 will-change-transform
            [mask-image:linear-gradient(to_bottom,black_0%,black_50%,transparent_100%)]`
          )}
        />

        {/* Fine Monochrome Matrix Grid Overlay */}
        <div className="absolute inset-0 bg-[linear-gradient(to_right,#ffffff08_1px,transparent_1px),linear-gradient(to_bottom,#ffffff08_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_70%,transparent_100%)] opacity-30 pointer-events-none" />
      </div>

      <div className="relative z-10 w-full">
        {children}
      </div>
    </div>
  );
};

export default AuroraBackground;
