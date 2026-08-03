# Study Sprint

A focus and learning app for university students, built for CP3406 (Mobile Computing Applications) Assignment 3. Study Sprint pairs a Pomodoro-style focus timer with weather-aware break suggestions, a task list that tracks time spent per subject, and a flashcard system that uses the SM-2 spaced-repetition algorithm to schedule reviews.

The app is designed around user autonomy rather than engagement metrics. There are no punitive streaks, no manipulative notifications, and no tracking. All data stays on the device.

## Features

**Focus timer.** A configurable Pomodoro cycle (focus, short break, long break). The engine is driven off the system clock rather than a sleep loop, so the countdown stays accurate across rotation and backgrounding without drift.

**Weather-aware breaks.** When a break begins the app fetches the current weather for a city the user specifies and suggests an indoor or outdoor activity accordingly. If the network call fails the app falls back to an on-device activity library, so the break screen is never empty. No location permission is requested.

**Tasks.** The user can add study tasks, pick one to focus on, and the app logs the time spent on each. Completed focus sessions are credited to the active task captured at the start of the phase.

**Flashcards with spaced repetition.** Decks of flashcards are reviewed using the SM-2 algorithm (the scheduling method Anki is based on). Cards the user knows well appear less often over time; cards they struggle with come back sooner. Each review presents a card, the user reveals the answer, then rates their recall on a four-point scale (again, hard, ok, easy). The algorithm updates the card's ease factor and interval accordingly.

**Statistics.** Total focus time, session count, a day streak computed from session dates, and a per-task bar chart showing where time has gone.

**Daily reminder.** An optional, opt-in notification at a chosen time. It respects Do Not Disturb and is off by default.

## Screens

| Screen | Purpose |
|---|---|
| Home | Today's focus summary, quick-start button, active task, shortcuts to tasks and flashcards. |
| Focus | The timer itself. Countdown ring, phase label, start/pause/skip controls, active task chip, break suggestion card. |
| Tasks | Add, set active, complete, and delete study tasks. |
| Flashcards | Deck list, deck detail (add and delete cards), and the spaced-repetition review screen. |
| Statistics | Total focus, sessions, streak, per-task breakdown. |
| Settings | Timer lengths, sound, dark mode, weather city, reminder toggle and time, data controls and privacy note. |

## Setup

### API key

Study Sprint uses the free OpenWeatherMap API for break suggestions. Sign up at https://home.openweathermap.org/users/sign_up and copy your API key from the My API keys page. New keys take around ten minutes to activate.

### Configuration

Copy the example file and add your key:

```
cp local.properties.example local.properties
```

Then edit `local.properties`:

```
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
OPEN_WEATHER_API_KEY=your_key_here
```

The file is git-ignored, so the key never enters version control.

### Running

Open the project in Android Studio (File, Open, select the StudySprint folder). Let Gradle sync, then run on an emulator or device running API 26 or higher.

## Architecture

Study Sprint follows MVVM with a repository layer, using Hilt for dependency injection. ViewModels hold UI state and survive configuration changes. Repositories are the single source of truth between Room (offline cache) and the network. Hilt wires concrete implementations to interfaces so they can be swapped for fakes in tests.

Pure logic (the timer engine, the SM-2 algorithm, the streak calculator, the break suggestion picker) is kept in plain Kotlin classes with no Android dependencies. Where time is involved it is passed in as a parameter rather than read from the system clock, which makes every rule deterministic and unit-testable on the JVM.

```
ui/                 Compose screens, theming, navigation, shared components
  theme/            Colour, type, theme
  nav/              NavHost and bottom navigation
  components/       Countdown ring, break suggestion card
  home/             Home screen and ViewModel
  focus/            Focus screen and ViewModel
  tasks/            Tasks screen and ViewModel
  flashcards/       Deck list, deck detail, review screens and ViewModels
  stats/            Statistics screen and ViewModel
  settings/         Settings screen and ViewModel
data/
  local/            Room database, DAOs, entities
  remote/           Retrofit weather API and DTOs
  repository/       Repository interfaces and implementations, mappers
  model/            Domain models
spacedrepetition/   SM-2 algorithm and card schedule
timer/              Timer engine, break controller
di/                 Hilt modules (database, network, repositories, utilities)
util/               Stats calculator, phase alerter
work/               WorkManager reminder worker
```

Libraries: Jetpack Compose, Material 3, Navigation-Compose, Hilt, Room, Retrofit, OkHttp, Moshi, WorkManager.

## Testing

```
./gradlew test                  # non-GUI unit tests on the JVM
./gradlew connectedAndroidTest  # GUI tests on a device or emulator
```

Unit tests cover the SM-2 algorithm (intervals, ease bounds, fail-reset), the timer state machine (tick, pause and resume, phase advancement, long-break cycle), streak calculation, break suggestion picking, and the clock formatter. GUI tests use Compose's `createComposeRule` to verify the Focus and Tasks screens render and respond to input.

## Ethical design

The project addresses the ethical issues explored in Assessment 2 through concrete design decisions rather than a checklist.

Privacy by default. All user data (tasks, sessions, flashcards, settings) is stored on-device in Room. No analytics or personal data is transmitted to any server. Cloud backup of the database is explicitly disabled. The only network request sends a city name to OpenWeatherMap.

Minimal permissions. No location permission is requested. Weather uses a city name the user types in. Only internet and an opt-in notification permission are used.

No manipulative patterns. The streak is informational only; there is no penalty for breaking it and no loss-aversion pressure. Reminders are opt-in and respect Do Not Disturb. There are no infinite loops, no paywalls, and no monetisation.

Transparency. A plain-language privacy note in Settings explains what is stored and what leaves the device, with a one-tap clear-all-data action.

Accessibility. Material 3 theming, large touch targets, content descriptions for screen readers, dynamic font scaling, and a high-contrast dark theme.

## Version control

The project uses Git on GitHub. Commit messages describe the work in plain language.

## Author

emris05, CP3406 Assessment 3.
