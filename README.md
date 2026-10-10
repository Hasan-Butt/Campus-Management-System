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

Requires a JDK (Java 8 or newer). From the project root:

```
javac -d build $(find src -name '*.java')
java -cp build com.fast.campus.Main
```

(Windows PowerShell: `javac -d build (Get-ChildItem -Recurse src -Filter *.java).FullName`, then the same `java` line.)
In an IDE, run `src/com/fast/campus/Main.java` with the project root as the working directory, so `data/` and `logs/` resolve.

To also see log lines in the console while debugging: `java -Dcampus.log.console=true -cp build com.fast.campus.Main`

### Demo logins (from `data/`)

| Role (main menu) | ID |
|---|---|
| 1 Academic Office Admin | — |
| 2 Normal Student | `22K-1234` (Saim Arif), `23L-3022` (Ali Khan), `22I-3001` (Sara Khan) |
| 3 Teaching Assistant | `22K-5678` (Ahmad Butt, TA of CS101-A) |
| 4 Permanent Instructor | `1122` (Arslan Asif) |
| 5 Visiting Instructor | `2233` (Faizan) |

---

## Data File Format

One record per line, pipe-delimited:

```
COURSE|CS101|Intro to Programming|4
SECTION|CS101-A|CS101|50|MONDAY|08:00|09:30|Room-301|1122
```

Every file in `data/` starts with a `# Format:` line describing its columns. A `|` typed into any text field is stored as `/`.

---

## Log Format

```
[YYYY-MM-DD HH:mm:ss] [LEVEL] [Actor] Event
```

Written to `logs/logs.log` (the folder is created automatically on the first run).

---

## Development Flow

```
dev/<member>  →  commit  →  push  →  Pull Request  →  review  →  merge to main
```

> **Phase 1 (Foundation):** Hasan stabilises shared enums, exceptions, logger, and core models before Kabeer and Saim begin their modules.
