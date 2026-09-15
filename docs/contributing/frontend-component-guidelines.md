# Codexa Frontend UI & Design Guidelines

## Design Principles
1. **Monochromatic Cyber-Security Luxury Aesthetic**: Use deep slate/neutral blacks (`#000000`, `neutral-900`), stark white typography, and subtle border highlights (`neutral-800`).
2. **Functional Accents**: Emerald (`emerald-500`) is reserved strictly for operational success metrics and active navigation tabs.
3. **Zero Sparkles**: Avoid frivolous decorative sparkle icons. Maintain a clean, high-precision developer terminal vibe.

## Component Structure
- Keep components modular under `src/components/`.
- Reusable primitive UI elements live under `src/components/ui/`.
- Utility styles combined using `cn(...)` (`clsx` + `tailwind-merge`).
