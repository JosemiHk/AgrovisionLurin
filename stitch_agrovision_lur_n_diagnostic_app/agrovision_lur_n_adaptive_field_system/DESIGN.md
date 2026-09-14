---
name: AgroVision Lurín Adaptive Field System
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
  secondary: '#a83900'
  on-secondary: '#ffffff'
  secondary-container: '#fc6018'
  on-secondary-container: '#531800'
  tertiary: '#00450d'
  on-tertiary: '#ffffff'
  tertiary-container: '#055f18'
  on-tertiary-container: '#86d881'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#acf4a4'
  primary-fixed-dim: '#91d78a'
  on-primary-fixed: '#002203'
  on-primary-fixed-variant: '#0c5216'
  secondary-fixed: '#ffdbcf'
  secondary-fixed-dim: '#ffb59a'
  on-secondary-fixed: '#380d00'
  on-secondary-fixed-variant: '#802a00'
  tertiary-fixed: '#a3f69c'
  tertiary-fixed-dim: '#88d982'
  on-tertiary-fixed: '#002204'
  on-tertiary-fixed-variant: '#005312'
  background: '#fdf8fd'
  on-background: '#1c1b1f'
  surface-variant: '#e5e1e7'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '800'
    lineHeight: 48px
    letterSpacing: -0.02em
  display-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '800'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 34px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
  title-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 20px
    fontWeight: '700'
    lineHeight: 26px
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '500'
    lineHeight: 26px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '500'
    lineHeight: 24px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '700'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '700'
    lineHeight: 18px
    letterSpacing: 0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  spacing-4: 0.25rem
  spacing-8: 0.5rem
  spacing-12: 0.75rem
  spacing-16: 1rem
  spacing-20: 1.25rem
  spacing-24: 1.5rem
  spacing-32: 2rem
  spacing-40: 2.5rem
  spacing-48: 3rem
  touch-target-min: 3rem
  gutter-mobile: 1rem
  gutter-tablet: 1.5rem
  margin-mobile: 1rem
  margin-tablet: 2rem
---

## Brand & Style

This design system is engineered for agronomists, parcel managers, and field irrigators operating under high-intensity sunlight in the Lurín coastal valley of Peru. The visual aesthetic fuses Material 3 Adaptive fundamentals with tactile, outdoor-rugged clarity. It prioritizes instant legibility, fault-tolerant interactions for gloved or soiled hands, and rapid cognitive triage of crop diagnostics, valve telemetry, and water pressure readings.

Key attributes:
- **Utilitarian & Resilient:** Crisp contours, no delicate micro-elements, and zero reliance on faint low-contrast dividers.
- **Sunlight-Optimized High Contrast:** Elevated text grades, dense structural cards, and crisp bordering that cut through glare and dusty mobile screens.
- **Dignified Craft:** Balances rugged industrial field requirements with modern digital agronomy elegance, using deep botanical hues alongside mineral and ochre working tones.

## Colors

The palette is tuned specifically for outdoor field conditions, high-UV glare environments, and rapid status assessment across irrigation zones.

### Core Roles
- **Primary (`#1B5E20` - Deep Forest Green):** Anchors top app bars, primary action buttons, confirmed valve states, and key navigation highlights.
- **Secondary (`#E65100` - Earth Amber / Ochre):** Used for manual irrigation overrides, active cycle triggers, and primary actionable highlights demanding urgent attention without indicating failure.
- **Surface Off-White (`#F8F9FA`):** An anti-glare, eye-resting background surface that cuts specular screen reflection under open field sunlight compared to pure white.
- **Card Surface (`#FFFFFF`):** High-luminance crisp white containers providing clear structural separation against the off-white canvas.
- **Text & Content Base (`#1C1B1F` - Dark Charcoal):** Minimum 12:1 contrast ratio against card surfaces to maintain absolute readability in direct sun.
- **Structural Outlines (`#DDE2D5`):** Subdued botanical-tinted neutral borders providing physical card containment without visual noise.

### Agricultural Telemetry & Status Roles
- **Healthy / Optimal (`#2E7D32`):** Soil moisture saturation optimal, fertigation balance normal.
- **Warning / Water Stress (`#F57F17`):** Drip pressure drop, low reservoir reserves, mild thermal stress.
- **Critical / Pest Alert (`#C62828`):** Phytosanitary alerts, pump failures, valve line ruptures.
- **Offline / Syncing State (`#4E5D52`):** Indicates un-synced field edits pending network connectivity along the canal corridors.

## Typography

Typography prioritizes open counters, strong x-height, and bold terminal strokes. **Plus Jakarta Sans** provides supreme legibility across varied viewing angles, smudged screens, and vibrations typical of tractors or all-terrain transport.

### Hierarchy Guidelines
- **Strict Minimum Body Size:** Body copy never drops below 16px (`body-md`), ensuring farm operators do not struggle to decipher flow rates or soil metrics.
- **Metric Emphases:** For field sensor readouts (bar pressure, pH, cubic meters/hour), use `display-mobile` with `fontWeight: 800` paired immediately with `label-md` uppercase unit indicators.
- **High-contrast weights:** Headlines utilize weights of 700 to 800 to resist edge-bleed caused by mobile screen backlights fighting bright Peruvian sunshine.

## Layout & Spacing

The layout is built on a responsive 4-column structure for mobile handheld devices, expanding to an 8-column system on ruggedized tablets (e.g., in tractor docks). 

### Layout Behavior
- **Touch Targets:** The absolute minimum interactive bounding box is `48px` (`3rem`), with `56px` preferred for field switches, valve toggle rows, and primary confirmation buttons.
- **Thumb Zone Anchor:** Critical operational controls (Emergency Pump Shutoff, Valve Group Toggle, Camera Inspection Triggers) reside in the lower 35% of the mobile viewport.
- **Card Spacing:** Standard spacing between monitoring cards is `16px`, maintaining distinct tap boundaries to prevent accidental triggers when navigating with rough or wet fingers.

## Elevation & Depth

This design system uses a high-sunlight, low-shadow elevation approach. Ambient shadows diffuse and wash out completely under 100,000-lux ambient direct sunlight; therefore, depth and container separation rely on **crisp structural borders (`#DDE2D5`)** supplemented with subtle, high-spread grounding shadows.

### Elevation Levels
- **Level 0 (Canvas Base):** Surface Off-White (`#F8F9FA`), flat.
- **Level 1 (Sensor & Sector Cards):** Pure White (`#FFFFFF`), 1.5px solid border in `#DDE2D5`, `box-shadow: 0 2px 4px rgba(28, 27, 31, 0.04)`.
- **Level 2 (Active Selector & Alert Cards):** Pure White (`#FFFFFF`), 2px solid border in status color (`#2E7D32`, `#F57F17`, or `#C62828`), `box-shadow: 0 4px 12px rgba(28, 27, 31, 0.08)`.
- **Level 3 (Modal Sheets & Irrigation Controllers):** Pure White, full top-border accent, `box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.16)`.

## Shapes

The shape system employs rounded ergonomics (`16px` / `1rem` on primary cards and panels) for comfort and modern utility, combined with full pill radii (`9999px`) for functional telemetry status chips, switches, and action badges.

- **Containers & Cards:** `16px` border-radius creates defined, easy-to-track visual blocks across the field dashboard.
- **Buttons & Chips:** `24px` to `9999px` full pill shapes for rapid recognizability as interactive physical touch elements.
- **Data Callout Flags:** `8px` compact rounding for sensor readout tags to maximize internal screen real estate.

## Components

### Action Buttons
- **Primary (Field Action):** Deep Forest Green background (`#1B5E20`), pure white label (`#FFFFFF`), minimum height `52px`, pill-shaped, uppercase font-weight 700. Active press state shifts to `#0E3813`.
- **Secondary (Override & Valve Controls):** Muted Earth Ochre background (`#E65100`), white label, minimum height `52px`, pill-shaped.
- **Tonal / Outlined:** Off-white background, 2px stroke (`#1B5E20`), text `#1B5E20`, minimum height `48px`.

### Telemetry & Status Badges (Pill Badges)
- Built as full pill-radius components (`height: 32px`, horizontal padding `12px`).
- **Optimal Status:** `#E8F5E9` background with `#1B5E20` text and a solid `8px` `#2E7D32` circular status dot.
- **Water Stress:** `#FFF3E0` background with `#E65100` text and a pulsing `#F57F17` alert dot.
- **Pest/Pathogen Hazard:** `#FFEBEE` background with `#C62828` text, bold icon prefix, and high-visibility red perimeter outline.

### Offline-First Connectivity Indicators
- Fixed, high-visibility persistent banner at top viewport:
  - **Online / Synced:** Discrete green micro-pill (`#2E7D32`) reading "Sincronizado" (Synced).
  - **Offline / Local Queue:** Earth Ochre solid pill (`#E65100`) reading "Modo Local — 14 Registros Pendientes" (Offline Mode — 14 Pending Queued Actions) with an explicit cloud-off icon, assuring field personnel their data is saved locally without network coverage.

### Sensor & Sector Monitoring Cards
- Container: Surface `#FFFFFF`, border `1.5px solid #DDE2D5`, radius `16px`, padding `16px 20px`.
- Structure: Header row with parcel identifier (`Sector B-14 Lurín Sur`) and status badge; primary metric center-aligned at `32px` font size with units in `14px` bold; bottom row containing quick-action toggle switch (minimum 48dp touch footprint) for manual line irrigation.

### Input Fields & Selectors
- Background: `#FFFFFF`. Minimum field height: `56px`.
- Border: `2px solid #DDE2D5`. When focused, border transitions to `2px solid #1B5E20` with a 4px light-green halo ring (`rgba(27, 94, 32, 0.15)`).
- Typography: Input text is strictly 18px (`body-lg`) to prevent zoom and misreading during data logging under direct sunlight.