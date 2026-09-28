# ADR-0006: Monochromatic Luxury Cybersecurity Design System

## Status
**Accepted** (2026-09-06)

## Context & Problem Statement
Developer tools and static analysis dashboards frequently suffer from visual fatigue: over-saturated neon gradients, rainbow-colored badge sprawl, frivolous particle animations, and cluttered typography. In high-stakes enterprise code auditing and security operations, visual noise distracts security engineers and senior architects from critical vulnerabilities.

Codexa required a distinct, publication-grade user interface that communicates precision, seriousness, luxury, and enterprise cybersecurity resilience.

## Decision Drivers
- **Visual Ergonomics**: High-contrast, low-fatigue dark mode optimized for long auditing sessions on OLED displays.
- **Hierarchical Information Density**: High data density without visual clutter; critical findings must command immediate attention.
- **Brand Identity**: Premium, minimalist, Apple/Linear-inspired monochromatic elegance.

## Considered Options
1. **Generic Material / Bootstrap UI**: Functional but visually utilitarian, lacking modern design distinction and brand authority.
2. **Neon "Cyberpunk" Aesthetic**: High contrast but tiring to read, visually unprofessional for executive board presentations.
3. **Monochromatic Luxury Design System with Selective Functional Accents**: Pure OLED black (`#000000`), micro-borders in neutral zinc/slate, subtle typography (Sora / Plus Jakarta Sans), and reserved emerald accents strictly for verified security elements.

## Decision Outcome
Chosen option: **Monochromatic Luxury Cybersecurity Design System**.

### Core Visual Principles:
- **Base Canvas**: Pure `#000000` (OLED black) and `#09090b` (zinc-950) backdrops.
- **Micro-Borders & Glass**: 1px subtle borders (`border-neutral-800` / `border-white/10`) with `backdrop-blur-md` translucent card surfaces.
- **Selective Functional Accents**:
  - `emerald-500` / `emerald-400`: Strictly reserved for passing production verdicts, security certifications, and primary action confirmation.
  - `rose-500` / `red-400`: Reserved for critical security vulnerabilities (CWE-89 SQLi, CWE-78 RCE).
  - `amber-400`: Reserved for medium quality and architectural warnings.
- **Motion & Typography**: Smooth 60fps micro-animations via Framer Motion, Aceternity UI components (`CanvasText` text-masking, `AuroraBackground` ambient glow), and clean geometric typography.

## Consequences
- **Positive**: Cohesive, distinguished user experience praised across executive audits and developer demos.
- **Positive**: Information hierarchy guides user focus directly to high-severity findings without decorative distraction.
- **Positive**: Low power consumption on OLED displays and reduced eye fatigue during extended security triage.
- **Neutral**: Requires strict frontend component discipline to prevent accidental color drift across contributors.
