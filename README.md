# Study Sprint 🎯

A Pomodoro-style focus timer for **university students**, with weather-aware break suggestions. Built for CP3406 (Mobile App Development) Assignment 3.

Study Sprint helps students build a sustainable focus habit: work in focused intervals, take breaks that fit the conditions around them, and review how their study time adds up — all without manipulative gamification or invasive data collection.

---

## Core features

| Feature | What it does |
|---|---|
| ⏱️ **Focus timer** | Configurable Pomodoro cycle (focus → short break → long break). Engine is driven by the system clock, so it survives screen rotation and backgrounding. |
| 🌤️ **Weather-aware breaks** | Fetches current weather for a chosen city (OpenWeatherMap API) and suggests indoor or outdoor break activities. Falls back to an on-device activity library when offline. |
| ✅ **Tasks** | Add study tasks, pick one to focus on, and the app logs time against it. |
| 📊 **Statistics** | Total focus time, session count, current streak, and a per-task breakdown. |
| 🔔 **Daily reminder** | Optional, fully opt-in notification at a time you choose. Respects Do-Not-Disturb. |
| 🎨 **"Deep Focus" theme** | Dark-first Material 3 UI (indigo + amber) designed to be calm and immersive. |

## Screens

| Screen | Purpose |
|---|---|
| **Home / Dashboard** | Today's focus summary, quick-start, active task. |
| **Focus** | The timer itself — the app's main "activity" screen. |
| **Tasks** | Manage study tasks and pick the active one. |
| **Statistics** | Progress, streak, per-task time. |
| **Settings** | Timer lengths, sound, dark mode, reminder, weather city, data controls + privacy note. |

---

## Setup

### 1. Get an API key
Study Sprint uses the free **OpenWeatherMap** API for break suggestions.

1. Sign up at <https://home.openweathermap.org/users/sign_up>
2. Sign in → your name (top right) → **My API keys**
3. Copy the key. *(New keys take ~10 minutes to activate.)*

### 2. Configure your key
Copy the example file and paste your key in:

```bash
cp local.properties.example local.properties
```

Then edit `local.properties`:

```properties
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
OPEN_WEATHER_API_KEY=your_actual_key_here
```

`local.properties` is git-ignored, so your key never enters version control.

### 3. Open & run
1. Open Android Studio → **File → Open** → select the `StudySprint` folder.
2. Let Gradle sync (first run downloads dependencies).
3. Plug in a device or start an emulator (API 26+).
4. Click **Run** ▶.

---

## Architecture

Study Sprint follows **MVVM + Repository** with **Hilt** for dependency injection.

```
ui/                 # Compose screens, theming, navigation, reusable components
  ├── theme/        # Colour, type, theme
  ├── nav/          # NavHost + bottom navigation
  ├── components/   # Shared composables (timer ring, cards, etc.)
  ├── home/
  ├── focus/
  ├── tasks/
  ├── stats/
  └── settings/
data/
  ├── local/        # Room database, DAOs, entities
  ├── remote/       # Retrofit weather API + DTOs
  ├── repository/   # Repository implementations (single source of truth)
  └── model/        # Domain models (UI-facing)
di/                 # Hilt modules (Database, Network, App)
work/               # WorkManager ReminderWorker
util/               # Extensions, formatters
```

**Key libraries:** Jetpack Compose · Material 3 · Navigation-Compose · Hilt · Room · Retrofit/OkHttp/Moshi · WorkManager.

### Why this design
- **ViewModels** hold UI state and survive configuration changes.
- **Repository pattern** gives a single source of truth between Room (offline cache) and the network.
- **Hilt** removes manual wiring and makes classes testable (constructors take interfaces, not concrete singletons).
- **System-clock-based timer** (not a coroutine sleep loop) so the countdown stays accurate across backgrounding.

---

## Testing

```bash
# Non-GUI unit tests (run on JVM — fast)
./gradlew test

# GUI / instrumented tests (run on device or emulator)
./gradlew connectedAndroidTest
```

- **Unit tests** cover the timer state machine, settings persistence, statistics calculation, break-suggestion logic, and weather fallback.
- **GUI tests** use Compose's `createComposeRule()` to verify the Focus and Tasks screens.

---

## Ethical design

This project explicitly addresses the ethical issues explored in Assessment 2:

- **Privacy by default.** All user data (tasks, sessions, settings) is stored **on-device only** in Room. The app sends no analytics and no personal data to any server. Cloud backup of the database is disabled.
- **Minimal permissions.** No location permission is requested — weather uses a city name the user types. Only INTERNET and (opt-in) notification permissions are used.
- **No dark / manipulative patterns.** The streak is informational only (no penalty for breaking it, no loss-aversion pressure). Reminders are opt-in and respect Do-Not-Disturb. There are no infinite loops, no paywalls, no monetisation.
- **Transparency.** A plain-language privacy note in Settings explains exactly what is stored and what leaves the device, plus a one-tap "clear all data" action.
- **Accessibility & inclusiveness.** Material 3 theming, large touch targets (≥48dp), content descriptions, dynamic font scaling, and high-contrast dark mode.

---

## Version control

This project uses Git on GitHub. Commit messages follow conventional prefixes (`feat:`, `fix:`, `test:`, `docs:`).

```bash
git log --oneline
```

---

## Author

**emris05** — CP3406, Assessment 3.
