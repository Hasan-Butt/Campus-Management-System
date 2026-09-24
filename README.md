# Life at FAST — Campus Management System

**SE3005 Software Construction & Development | Assignment 1**

---

## Team

| Member | Branch | Primary Responsibility |
|--------|--------|------------------------|
| Hasan  | `dev/Hasan` | Core Domain + Academic Office Admin |
| Kabeer | `dev/Kabeer` | Student + Teaching Assistant |
| Saim   | `dev/Saim` | Instructor + FYP Module |

---

## Project Structure

```
src/com/fast/campus/
├── enums/          — Shared enumerations (all members)
├── exception/      — Exception hierarchy (Hasan owns)
├── util/           — Logger, FileManager (Hasan owns)
├── comparator/     — Shared comparators (all members)
├── model/          — Domain classes (split by owner)
└── service/        — Business logic / use cases (split by owner)

data/               — Flat-file persistence (pipe-delimited)
logs/               — Application log output
docs/               — Diagrams, notes
```

---

## Building & Running

### Compile (from project root)
Open File `Main.java` in src\com\fast\campus and click run next to the class name method and make sure you have JDK installed in your IDE to run this.

---

## Data File Format

One record per line, pipe-delimited:

```
COURSE|CS101|Intro to SE|3
SECTION|A|CS101|40|MONDAY|08:00|09:30|Room-101
```

---

## Log Format

```
[YYYY-MM-DD HH:mm:ss] [LEVEL] [Actor] Event
```

---

## Development Flow

```
dev/<member>  →  commit  →  push  →  Pull Request  →  review  →  merge to main
```

> **Phase 1 (Foundation):** Hasan stabilises shared enums, exceptions, logger, and core models before Kabeer and Saim begin their modules.
