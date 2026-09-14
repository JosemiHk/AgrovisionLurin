---
name: AgroVision Lurín
colors:
  surface: '#fdf8fd'
  surface-dim: '#ddd9de'
  surface-bright: '#fdf8fd'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f7f2f8'
  surface-container: '#f1ecf2'
  surface-container-high: '#ebe7ec'
  surface-container-highest: '#e5e1e7'
  on-surface: '#1c1b1f'
  on-surface-variant: '#41493e'
  inverse-surface: '#313034'
  inverse-on-surface: '#f4eff5'
  outline: '#717a6d'
  outline-variant: '#c0c9bb'
  surface-tint: '#2a6b2c'
  primary: '#00450d'
  on-primary: '#ffffff'
  primary-container: '#1b5e20'
  on-primary-container: '#90d689'
  inverse-primary: '#91d78a'
  secondary: '#875200'
  on-secondary: '#ffffff'
  secondary-container: '#f89c00'
  on-secondary-container: '#623a00'
  tertiary: '#00433a'
  on-tertiary: '#ffffff'
  tertiary-container: '#005d51'
  on-tertiary-container: '#83d4c4'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#acf4a4'
  primary-fixed-dim: '#91d78a'
  on-primary-fixed: '#002203'
  on-primary-fixed-variant: '#0c5216'
  secondary-fixed: '#ffddba'
  secondary-fixed-dim: '#ffb865'
  on-secondary-fixed: '#2b1700'
  on-secondary-fixed-variant: '#663d00'
  tertiary-fixed: '#a0f2e1'
  tertiary-fixed-dim: '#84d5c5'
  on-tertiary-fixed: '#00201b'
  on-tertiary-fixed-variant: '#005046'
  background: '#fdf8fd'
  on-background: '#1c1b1f'
  surface-variant: '#e5e1e7'
typography:
  display-lg:
    fontFamily: Space Grotesk
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Space Grotesk
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Space Grotesk
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Space Grotesk
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 30px
  headline-sm:
    fontFamily: Space Grotesk
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Public Sans
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
  body-md:
    fontFamily: Public Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Public Sans
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Space Grotesk
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.02em
  label-md:
    fontFamily: Space Grotesk
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.03em
  label-sm:
    fontFamily: Space Grotesk
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 14px
    letterSpacing: 0.04em
  data-metric:
    fontFamily: Space Grotesk
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.02em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  touch-min: 48px
  touch-expanded: 56px
  gutter-xs: 0.25rem
  gutter-sm: 0.5rem
  gutter-md: 1rem
  gutter-lg: 1.5rem
  gutter-xl: 2rem
  margin-mobile: 1rem
  margin-tablet: 1.5rem
  margin-desktop: 2.5rem
---

## Brand & Style

The design system serves an agronomic monitoring platform engineered for extreme outdoor utility, field resilience, and rapid situational awareness in demanding environments like Lurín's coastal agricultural valley. Built for field managers, agronomists, and irrigation specialists navigating harsh midday sunlight and variable network conditions, the visual tone conveys industrial precision, biological health, and reliable engineering.

The design movement combines **High-Contrast Utilitarianism** with **Modern Industrial Field Ergonomics**. Surfaces prioritize anti-glare legibility, strict structural hierarchies, and physical affordances that function effortlessly even when operated single-handed, under bright glare, or with work gloves.

## Colors

The palette is tuned specifically for sunlit legibility and instant field comprehension:

- **Primary (`#1B5E20` Deep Forest Green)**: Anchors system status, verified states, primary interactions, and canopy health. Delivers strict WCAG AAA contrast against light backgrounds.
- **Secondary (`#FFA000` Amber Accent)**: High-visibility accent reserved for equipment warnings, pending states, valve transitions, and critical telemetry anomalies. Paired strictly with `#1C1B1F` Charcoal text to prevent solar washout.
- **Tertiary (`#00695C` Teal)**: Dedicated telemetry color for hydrologic markers, moisture sensors, valve lines, and irrigation schedules.
- **Error / Urgent (`#C62828` Alert Crimson)**: Dedicated to critical mechanical failures, soil drought thresholds, pressure drops, and shutoffs.
- **Base Surface (`#F8F9FA` Clean Neutral)**: Clean, neutral off-white devoid of pink, yellow, or warm undertones to eliminate color distortion under direct natural sunlight.
- **On-Surface / Typography (`#1C1B1F` Charcoal)**: High-density dark neutral providing punchy, anti-glare clarity across all text and icon glyphs.

## Typography

Typography pairs **Space Grotesk** for numerical metrics, headings, and field operational badges with **Public Sans** for continuous data descriptions and tabular metadata:

- **Space Grotesk**: Brings technical, geometric clarity to sensor readouts (e.g., pH, bar, m³/h, liters) and navigational waypoints.
- **Public Sans**: Provides distortion-free reading on ruggedized field tablets and smartphones under varying screen brightness levels.
- **Minimum Body Size**: In-field body text never drops below 14px on portable handheld views to maintain legibility while walking over uneven terrain.

## Layout & Spacing

The layout is built around a rigorous, touch-first responsive grid that scales seamlessly from field handhelds to control-room monitors:

- **Touch Safety Minimum**: Every interactive target enforces an absolute bounding box of no less than `48px` (`touch-min`), with `56px` (`touch-expanded`) for single-tap actions like zone valves and emergency stops.
- **Breakpoints & Grids**:
  - **Mobile (<768px)**: 4-column fluid layout with `margin-mobile` (16px) margins and 16px gutters. Secondary metrics collapse into swipeable cards or expandable drawer panels.
  - **Tablet (768px - 1199px)**: 8-column layout with 24px margins and gutters. Optimized for horizontal cab mounts in tractors and field pickups.
  - **Desktop (>=1200px)**: 12-column layout with 40px margins and 24px gutters, fixed at a maximum canvas width of 1440px to retain scan efficiency.

## Elevation & Depth

To prevent wash-out under full sunlight, elevation relies on **structural borders and tonal stepping** rather than soft, diffuse drop shadows.

- **Surface Base**: `#F8F9FA` for overall canvas backdrop.
- **Card / Panel Surfaces**: `#FFFFFF` solid fill bounded by a crisp 1.5px border (`#E0E0E0` default, transitioning to `#1B5E20` when focused or active).
- **Elevated Interactive Controls (Floating Action / Valve Bar)**: Single physical offset shadow: `0px 4px 0px 0px rgba(28, 27, 31, 0.12)` accompanied by a 1.5px outline in `#1C1B1F`.
- **Modals and Urgent Field Overlays**: Scrim using `#1C1B1F` at 65% opacity with a solid, high-contrast modal surface bordered in 2px `#1C1B1F`.

## Shapes

The design system adopts a **Soft Industrial (`1`)** shape language:
- Buttons, inputs, chips, and field cards utilize `0.25rem` (4px) base border radius.
- System modules and field overview containers use `0.5rem` (8px).
- Modals, action sheets, and floating HUD panels cap at `0.75rem` (12px).
- Avoids large circular pills or rounded capsules to preserve perimeter screen estate for high-density agricultural sensor telemetry.

## Components

### Buttons
- **Primary**: Background `#1B5E20`, label `#FFFFFF`, min-height 48px, horizontal padding 20px, font `Space Grotesk` 14px bold. Active state darkens to `#0D3310`.
- **Warning / Accent**: Background `#FFA000`, text `#1C1B1F`, min-height 48px. Strict color pair ensures outdoor readability.
- **Emergency / Shutoff**: Background `#C62828`, text `#FFFFFF`, min-height 56px with full-width tap target on mobile views.

### Chips & Telemetry Badges
- **Moisture / Irrigation**: Tinted background `#E0F2F1`, text `#00695C`, 1px solid `#80CBC4`.
- **Warning / Alert**: Background `#FFF8E1`, text `#1C1B1F`, 1px solid `#FFA000`.
- **Normal / Nominal**: Background `#E8F5E9`, text `#1B5E20`, 1px solid `#A5D6A7`.

### Inputs & Selectors
- Height strictly 52px. Off-white `#FFFFFF` fill with 1.5px border `#9E9E9E`.
- Focus state activates 2px solid `#1B5E20` with a 2px offset outline.
- Labels remain positioned above or top-pinned inside inputs to remain visible during thumb operations.

### Cards & Zone Modules
- White background (`#FFFFFF`), 1.5px border `#E0E0E0`, padding 16px.
- Metric values use `Space Grotesk` 32px with unit labels placed to the right in `Public Sans` 14px.

### Toggle Switches & Radio Controls
- Touch frame: 48px x 48px touch container enclosing a 52px x 28px track. Active tracks fill with `#1B5E20`; inactive tracks use `#BDBDBD`. Thumb is solid `#FFFFFF` with a 1px `#1C1B1F` stroke.