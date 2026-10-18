# EMDR Companion Design System

Calm, clinical. The interface should recede so bilateral stimulation can stay in the foreground. Dark is the default; Light is a linen day mode.

## Color

### Dark

| Token | Hex | Role |
| --- | --- | --- |
| `background` | `#121417` | App canvas |
| `surface` | `#1A1D22` | Setup cards |
| `surfaceElevated` | `#22262C` | Nested wells, slider tracks chrome |
| `primary` | `#7CA982` | Sage accent, selected chips, start, orb default |
| `primaryDim` | `#5E8A66` | Pressed / dim sage |
| `primaryContainer` | `#243028` | Selected chip fill |
| `onPrimary` | `#121417` | Text/icons on sage fills |
| `text` | `#E6E8EC` | Primary copy |
| `textSecondary` | `#A8ADB6` | Supporting copy, unselected labels |
| `textMuted` | `#6B717C` | Captions, units |
| `outline` | `#2A2F36` | Card and chip hairlines |
| `track` | `#2E333B` | Inactive slider track |
| `stop` | `#D9897B` | End session (warm, not alarm) |
| `supporter` | `#1C2026` | Muted supporter card |

### Light

| Token | Hex | Role |
| --- | --- | --- |
| `background` | `#F4F3EF` | Linen canvas |
| `surface` | `#FFFEFA` | Setup cards |
| `surfaceElevated` | `#EBE8E1` | Nested wells |
| `primary` | `#7CA982` | Same sage |
| `primaryDim` | `#5E8A66` | Pressed / dim sage |
| `primaryContainer` | `#DCE8DE` | Selected chip fill |
| `onPrimary` | `#121417` | Text/icons on sage fills |
| `text` | `#2C3036` | Primary copy |
| `textSecondary` | `#5A5F66` | Supporting copy |
| `textMuted` | `#8B9097` | Captions, units |
| `outline` | `#D4D0C8` | Card and chip hairlines |
| `track` | `#D8D4CC` | Inactive slider track |
| `stop` | `#C46B5E` | End session |
| `supporter` | `#E6E4DF` | Muted supporter card |

Appearance: `System` (follow OS), `Light`, or `Dark`. The choice is stored on-device.

## Launch

First launch opens setup. After the first START SESSION, later launches open the session in **Ready / Soft Pulse**: orb at rest with a slow breath-scale, no travel, no audio, until Play. A persistent toolbar gear opens the preferences sheet.

### Orb palette

| Name | Hex |
| --- | --- |
| Sage | `#7CA982` |
| Soft White | `#E6E8EC` |
| Amber | `#D4A574` |
| Teal | `#6BB3B0` |
| Lavender | `#A89CC8` |
| Coral | `#D9897B` |

## Typography

System UI font. Weights: Regular 400, Medium 500.

| Style | Size | Weight | Letter spacing | Use |
| --- | --- | --- | --- | --- |
| Display | 32 | Medium | -0.5 | Screen title |
| Title | 22 | Medium | -0.2 | Session chrome title |
| Section | 15 | Medium | 0.2 | Card headers |
| Body | 15 | Regular | 0 | Descriptive copy |
| Label | 13 | Medium | 0.3 | Chips, BPM value |
| Caption | 12 | Regular | 0.2 | Helper text |

## Shape

| Token | Radius |
| --- | --- |
| Card | 20dp |
| Button | 16dp |
| Chip | 12dp |
| Swatch / orb | 50% |

## Spacing

4 / 8 / 12 / 16 / 24 / 32. Card inner padding is 20dp. Screen horizontal inset is 20dp.

## Touch

Minimum interactive size **48 × 48 dp** on every control (chips, swatches, icon buttons, slider thumbs, start). The start action is additionally padded (`minHeight` 56dp + 16dp vertical outer padding).

## Motion

| Token | Value |
| --- | --- |
| Bilateral interpolation | `sin(phase)` — ease in/out at each edge |
| BPM | 15–90 |
| Frequency | `bpm / 120` Hz for a full left–right–left cycle |
| Chrome idle hide | 3000ms |
| Chrome fade (tap) | 1500ms |
| Chrome fade (pause/play) | 1500ms |
| Audio pan | `-1.0` (left) … `+1.0` (right), equal-power |

## Patterns

- Horizontal — classic bilateral
- Infinity — Lissajous figure-eight (`x = sin t`, `y = sin 2t`)
- Diagonal — linked x/y
- Vertical — y-only (audio pan stays centered)

## Elevation / borders

No drop shadows. Cards use `outline` 1dp hairlines on `surface`. Selected chips use `primaryContainer` fill and `primary` outline.
