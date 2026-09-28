# Codexa Frontend Component Architecture & Design System Guidelines

This specification defines the component organization, Tailwind CSS styling conventions, Framer Motion animation standards, and accessibility guidelines for the **Codexa Frontend** (React 18 + Vite + Tailwind CSS).

---

## 1. Directory Structure & Component Taxonomy

The frontend codebase is organized into distinct functional layers:

```
frontend/src/
├── components/             # High-level feature views and composite widgets
│   ├── LandingView.jsx     # Landing hero, feature highlights, bento showcase
│   ├── NewAnalysisView.jsx # Zip upload, GitHub URL submission, parameter form
│   ├── AnalysisView.jsx    # Real-time scan progress, radar metrics, finding list
│   ├── RemediationModal.jsx# AI/deterministic code diff refactoring viewer
│   ├── Header.jsx          # Top navigation bar, status indicator, logo
│   └── Footer.jsx          # Monochromatic footer with watermark branding
├── components/ui/          # Headless, reusable primitive UI components
│   ├── button.jsx          # Polymorphic button variants
│   ├── badge.jsx           # Severity badges (Critical, High, Medium, Low)
│   ├── dialog.jsx          # Accessible modal dialogs
│   ├── tabs.jsx            # Tab navigation primitives
│   ├── canvas-text.jsx     # Aceternity animated text-mask effect
│   └── aurora-background.jsx # Subdued ambient ambient glow
├── lib/
│   ├── utils.js            # Tailwind class merger (cn helper)
│   └── api.js              # REST client and SSE event listener wrapper
└── index.css               # Global typography, CSS variables, and base resets
```

---

## 2. Monochromatic Luxury Design Principles (ADR-0006)

To maintain Codexa's signature **Monochromatic Luxury Cybersecurity** aesthetic:

### Color Palette Hierarchy:
- **Canvas / Surface**: Pure black (`bg-black` / `#000000`) and deep zinc (`bg-neutral-950` / `bg-neutral-900/60`).
- **Borders & Dividers**: Subtle hairline borders (`border-neutral-800` or `border-white/10`).
- **Typography**: High-contrast white (`text-white`) for headings, subdued zinc (`text-neutral-400` / `text-neutral-500`) for body and secondary metadata.
- **Selective Functional Accents**:
  - `emerald-500` / `emerald-400`: Strictly reserved for verified security certifications, passing production scores ($\ge 85$), and active submission buttons.
  - `red-500` / `rose-400`: Reserved for critical security violations (CWE-89 SQLi, CWE-78 RCE).
  - `amber-400`: Reserved for medium quality and architectural warnings.

### Disallowed Visual Anti-Patterns:
- **Zero Frivolous Sparkles**: Do not add sparkle stars, rainbow glitters, or gamified cartoon badges.
- **No Harsh Solid Neon Backgrounds**: Use translucent backdrops (`bg-neutral-900/80 backdrop-blur-md`) rather than opaque neon surfaces.

---

## 3. Tailwind Styling & Class Merging (`cn` Utility)

All dynamic or polymorphic class composition must use the `cn(...)` utility (`clsx` + `tailwind-merge`):

```jsx
import { cn } from "../../lib/utils";

export const Badge = ({ variant = "default", className, children, ...props }) => {
  return (
    <span
      className={cn(
        "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium tracking-wide border",
        {
          "bg-white/10 text-white border-white/20": variant === "default",
          "bg-emerald-500/10 text-emerald-400 border-emerald-500/20": variant === "success",
          "bg-red-500/10 text-red-400 border-red-500/20": variant === "critical",
          "bg-amber-500/10 text-amber-400 border-amber-500/20": variant === "warning",
        },
        className // Allows caller overrides without Tailwind class collision
      )}
      {...props}
    >
      {children}
    </span>
  );
};
```

---

## 4. Framer Motion Animation Guidelines

Motion in Codexa serves functional orientation rather than decoration:

1. **Subtle Spring Physics**: Use damping ratios of $25-30$ and stiffness of $200-300$:
   ```jsx
   <motion.div
     initial={{ opacity: 0, y: 8 }}
     animate={{ opacity: 1, y: 0 }}
     transition={{ type: "spring", stiffness: 260, damping: 20 }}
   >
   ```
2. **Layout Animation Discipline**: Use `layoutId` only when morphing active tabs or moving selection pills. Avoid animating broad CSS grid layouts to prevent browser recalculation jank.
3. **Respect `prefers-reduced-motion`**:
   ```jsx
   const shouldReduceMotion = useReducedMotion();
   const animate = shouldReduceMotion ? { opacity: 1 } : { opacity: 1, y: 0 };
   ```

---

## 5. Aceternity UI Components Integration

### `CanvasText` Component (`src/components/ui/canvas-text.jsx`)
Implements an offscreen HTML5 2D canvas text-mask:
- Text letterforms are rendered first via `ctx.fillText()`.
- Uses `ctx.globalCompositeOperation = "source-in"` so animated diagonal monochromatic lines flow strictly *inside* the letter boundaries.
- Resets with `cancelAnimationFrame` in React `useEffect` cleanups to prevent memory leaks.

---

## 6. Real-Time State & Server-Sent Events (SSE) Streaming

During scan execution, the frontend establishes an SSE stream to `/api/v1/analyses/{jobId}/progress`:

```javascript
useEffect(() => {
  if (!jobId || status === "COMPLETED" || status === "FAILED") return;

  const eventSource = new EventSource(`/api/v1/analyses/${jobId}/progress`);
  
  eventSource.onmessage = (event) => {
    const data = JSON.parse(event.data);
    setProgress(data.progressPercent);
    setStage(data.currentStage);
    if (data.status === "COMPLETED") {
      eventSource.close();
      refetchResults();
    }
  };

  eventSource.onerror = () => {
    eventSource.close();
    // Fall back to HTTP polling every 2 seconds
  };

  return () => eventSource.close();
}, [jobId, status]);
```

---

## 7. Accessibility & Keyboard Navigation (a11y)

- **Focus Rings**: Interactive elements must provide high-visibility focus states (`focus-visible:ring-2 focus-visible:ring-emerald-500 focus-visible:outline-none`).
- **Screen Reader Support**: Modals must trap focus (`DialogPrimitive` from Radix UI), declare `role="dialog"`, and bind `aria-labelledby` to title headers.
- **Color Contrast**: All body text must maintain a minimum contrast ratio of **4.5:1** against backgrounds (WCAG 2.1 AA compliance).
