# VOLTAR — Real Battery Intelligence. No Fake Numbers.

> **Brand Positioning**: Advanced Reliability  
> **Product Category**: Android Battery Intelligence & Diagnostics  
> **Platform Architect & Creator**: Aryan Singh ([Portfolio](https://aryan-cine-dev-portfolio.vercel.app/) • [GitHub](https://github.com/aryansingh0777raghav) • [LinkedIn](https://www.linkedin.com/in/iamaryan07))

---

## ⚡ Executive Summary

**VOLTAR** is a production-quality, native Android application engineered to deliver true, verifiable battery intelligence. Unlike conventional battery utility apps that manufacture arbitrary percentages or guess degradation through unverified heuristics, VOLTAR strictly relies on **real hardware-reported counters**, **repeated charging session deltas**, and **transparent algorithmic estimation**.

- **No Root Required**: 100% compliant with standard Android security architecture.
- **Privacy-First & Local Storage**: All database records, sessions, and machine analytics reside exclusively on your device using Room SQLite. No accounts, no cloud sync, no tracking.
- **Hardware-Honest Transparency**: Clearly indicates whether any displayed metric is *Hardware Reported*, *Calculated*, *Estimated*, or *Unavailable*.

---

## 🏗 Architecture & Technology Stack

The application follows Google's recommended Modern Android Architecture (MVVM + Clean Architecture):

```
app
├── core
│   ├── database (Room SQLite persistence: observations, charging sessions, health analyses)
│   ├── datastore (Jetpack DataStore preferences: Theme, Monitoring intervals, Custom capacity)
│   ├── di (AppContainer dependency injection)
│   ├── model (BatteryObservation, ChargingSession, HealthAnalysis, DeviceProfile)
│   └── repository (BatteryRepository data orchestration)
├── battery
│   ├── BatteryDiagnosticsEngine (BatteryManager APIs, sticky intent telemetry)
│   ├── BatteryHealthEngine (ΔQ / ΔSoC algorithm, outlier rejection, milestone progression)
│   └── DeviceCompatibilityLayer (OEM hardware capability matrix & factory database)
├── monitoring
│   ├── BatteryChangeReceiver (BroadcastReceiver for power events & boot restoration)
│   ├── BatteryMonitoringWorker (WorkManager low-power periodic background task)
│   └── BatteryMonitoringScheduler (Configurable periodic scheduler)
└── ui
    ├── theme (Light [Default], Dark, System themes with Emerald Green accents)
    ├── components (Glassmorphic cards, Apple-grade bezier Canvas charts, circular health gauges)
    ├── screens
    │   ├── splash (Branded splash experience)
    │   ├── onboarding (Educational walkthrough on SoC vs Health)
    │   ├── home (Hero Health Gauge, 2x2 live metrics grid, Health Trend)
    │   ├── analytics (Multi-tab trend analysis, statistical aggregations)
    │   ├── charging (Real-time Charging Lab, calculated wattage, session tracking)
    │   ├── history (Chronological logs, session inspection)
    │   └── settings (Theme picker, WorkManager interval, CSV/JSON export, Creator profile)
    └── navigation (Navigation Compose + Scaffold container)
```

---

## 📐 Battery Health Estimation Methodology

### 1. Direct Hardware Verification
When supported by the underlying Linux kernel and fuel gauge hardware (e.g. accessible sysfs `charge_full` and `charge_full_design`):
$$\text{Health Percentage} = \left(\frac{\text{Full Charge Capacity}}{\text{Design Capacity}}\right) \times 100$$

### 2. Progressive Charging-Session Delta Algorithm
When direct hardware full-charge registers are not exposed by the OEM, VOLTAR calculates capacity through repeated charge-counter integrals:
$$\text{Estimated Capacity} \approx \frac{\Delta Q}{\Delta \text{SoC}} = \frac{Q_{\text{end}} - Q_{\text{start}}}{\text{SoC}_{\text{end}} - \text{SoC}_{\text{start}}}$$

*Example*: If the hardware charge-counter gains $1,200\text{ mAh}$ while the battery level transitions from $25\%$ to $49\%$ ($\Delta \text{SoC} = 0.24$):
$$\text{Estimated Capacity} = \frac{1200\text{ mAh}}{0.24} = 5,000\text{ mAh}$$

### 3. Data Integrity & Noise Rejection Controls
To guarantee scientific reliability, observations are automatically rejected if:
- $\Delta \text{SoC} < 10\%$ (transient top-ups provide insufficient signal-to-noise ratio).
- Temperature drops below $10^\circ\text{C}$ or exceeds $45^\circ\text{C}$ (thermal variance distorts internal resistance).
- Charge counter readings jump unexpectedly or report negative deltas.
- Calculated capacity falls outside physical boundaries ($< 1,000\text{ mAh}$ or $> 12,000\text{ mAh}$).

Multiple valid sessions are aggregated using **robust median analysis** to eliminate transient charging outliers.

---

## 📅 Progressive Milestones (24h / 7d / 14d)

1. **First 24 Hours (Preliminary)**: Captures baseline usage and initial charging session. Confidence: *Low / Preliminary (0.35–0.50)*.
2. **7-Day Analysis (Validated)**: Cross-references multiple charging cycles across diverse thermal conditions. Confidence: *Medium / 7-Day Validated (0.65–0.80)*.
3. **14-Day Analysis (High Confidence)**: Evaluates longitudinal capacity trends and verifies estimate stability. Confidence: *High / 14-Day High Confidence (0.85–0.95)*.

---

## 🎨 Design Philosophy & Theme System

Crafted to embody an **Apple-inspired utility aesthetic**:
- **Glassmorphism**: Soft ambient blur, translucent surfaces (`#FFFFFF` in light mode, `#151515` in dark mode), subtle 1dp borders.
- **Restrained Emerald Green Accent** (`#00E676` in dark mode, `#059669` in light mode).
- **Three Persistent Theme Modes**:
  - **Light (Default)**: `#F5F6F8` background, `#111111` typography.
  - **Dark**: `#080808` obsidian background, `#FFFFFF` typography.
  - **System**: Automatically follows device configuration.

---

## 👨‍💻 Creator & Lead Platform Architect

- **Architect**: Aryan Singh
- **Tagline**: Software Architect & Platform Engineer | Independent Filmmaker
- **Location**: Gorakhpur, Uttar Pradesh, India
- **Bio**: *"Software Architect and Independent Filmmaker specializing in zero-latency systems, local-first architectures, and high-craft digital products."*
- **Links**:
  - [Portfolio](https://aryan-cine-dev-portfolio.vercel.app/)
  - [GitHub](https://github.com/aryansingh0777raghav)
  - [LinkedIn](https://www.linkedin.com/in/iamaryan07)
  - [IMDb](https://www.imdb.com/name/nm18214429/)
  - [Instagram](https://www.instagram.com/iam_aryannnn07)
  - Email: `aryansingh979211@gmail.com`
- **Flagship Projects**: ArKTest Beta, ArCV, CarHari, VnishAr, CineOn Studio 7.

---

## 📲 Download & Install Pre-built APK

You can directly download and install the official production release APK:
- **Production APK**: [`release/VOLTAR-v1.0.0-Production.apk`](release/VOLTAR-v1.0.0-Production.apk) (12.9 MB, Pre-signed & optimized)

---

## 🛠 Building the Application

### Prerequisites
- JDK 17 or JDK 21
- Android SDK (API 35)

### Build Commands
```powershell
# Run unit test suite
.\gradlew testDebugUnitTest

# Assemble production release APK
.\gradlew assembleRelease
```
The output production APK is generated at:
`app/build/outputs/apk/release/app-release.apk`
