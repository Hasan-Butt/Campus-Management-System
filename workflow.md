# Team Development Workflow & Project Guide

**Project:** Campus Management System — *Life at FAST*  
**Course:** SE3005 — Software Construction & Development (Assignment 1)  
**Target Repository:** Private GitHub Repository  

---

## 1. Team Responsibilities & Ownership Matrix

To prevent merge conflicts and ensure clear accountability, the project is divided into distinct module ownership boundaries. Every team member has designated files and services they own.

| Member | Branch | Domain & Services | Models & Files Owned | Persistence Files |
|---|---|---|---|---|
| **Hasan** | `dev/Hasan` | **Core Domain & Academic Office Admin**<br>• Course/Section lifecycle<br>• Room & Instructor assignments<br>• Request decisions (Approve/Reject)<br>• Foundational architecture | • `AcademicOfficeAdmin.java`<br>• `Administrator.java`<br>• `Person.java`<br>• `Course.java`<br>• `Section.java`<br>• `Schedule.java`<br>• `Enrollment.java`<br>• `AcademicOfficeService.java`<br>• `com.fast.campus.exception.*`<br>• `Logger.java`, `FileManager.java` | • `data/courses.txt`<br>• `data/sections.txt`<br>• `data/enrollments.txt` |
| **Kabeer** | `dev/Kabeer` | **Student & Teaching Assistant Module**<br>• Student registration/drop & timetable<br>• Course clash requests & generic requests<br>• TA assignment creation & grading<br>• Submissions & Student attendance view | • `Student.java`<br>• `NormalStudent.java`<br>• `TeachingAssistant.java`<br>• `Attendance.java`<br>• `Request.java`<br>• `CourseClashRequest.java`<br>• `GenericRequest.java`<br>• `Assessment.java`<br>• `Assignment.java`<br>• `Submission.java`<br>• `Feedback.java`<br>• `StudentService.java` | • `data/students.txt`<br>• `data/assignments.txt`<br>• `data/submissions.txt`<br>• `data/requests.txt`<br>• `data/attendance.txt` |
| **Saim** | `dev/Saim` | **Instructor & FYP Module**<br>• Instructor operations (view sections/courses)<br>• Daily attendance marking & percentage calc<br>• TA appointment (Permanent Instructor)<br>• FYP Group lifecycle, meetings & evaluation | • `Instructor.java`<br>• `VisitingInstructor.java`<br>• `PermanentInstructor.java`<br>• `FYPGroup.java`<br>• `FYPMeeting.java`<br>• `FYPEvaluation.java`<br>• `InstructorService.java` | • `data/instructors.txt`<br>• `data/fypgroups.txt`<br>• `data/fypmeetings.txt`<br>• `data/fyp_evaluations.txt` |

> **Shared Code (Read-Only / Collaborative):**
> - `com.fast.campus.enums.*`: Agreed upon enums (Day, AttendanceStatus, EnrollmentStatus, RequestCategory, RequestStatus, SubmissionStatus).
> - `com.fast.campus.comparator.*`: Comparators for sorting students, dates, priorities, deadlines, and meetings.
> - `com.fast.campus.util.*`: `Logger` and `FileManager`.

---

## 2. Project Directory Structure

```
SCD-Assigment-1/
├── .gitignore                      # Ignores build artifacts, IDE configs, logs
├── README.md                       # Project overview & quickstart
├── workflow.md                     # Team workflow guidelines (this file)
├── plan.md                         # Master assignment specification & roadmap
│
├── src/
│   └── com/
│       └── fast/
│           └── campus/
│               ├── enums/          # Shared domain enums
│               ├── exception/      # System custom exception hierarchy
│               ├── util/           # Shared utilities (Logger, FileManager)
│               ├── comparator/     # Shared domain comparators
│               ├── model/          # All entity models & domain abstractions (Objects & Attributes)
│               └── service/        # Business use cases & workflows (Logic & Orchestration)
│
├── data/                           # Flat-file database storage (pipe-delimited)
│   ├── courses.txt
│   ├── sections.txt
│   ├── enrollments.txt
│   ├── students.txt
│   ├── assignments.txt
│   ├── submissions.txt
│   ├── requests.txt
│   ├── attendance.txt
│   ├── instructors.txt
│   ├── fypgroups.txt
│   ├── fypmeetings.txt
│   └── fyp_evaluations.txt
│
├── logs/                           # Auto-generated application logs (logs/campus.log)
├── docs/                           # Documentation, diagrams, screenshots
└── out/                            # Compiled bytecode (git-ignored)
```

---

## 3. Architecture Guide: "What Code Goes Where?"

A common question in Java development is: **"Does this method belong in the Model or in the Service?"**

Here is the clean separation of concerns followed across the entire project:

```
┌────────────────────────────────────────────────────────────────────────┐
│                              USER / UI / CLI                           │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ calls
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                             SERVICE LAYER                              │
│                      (com.fast.campus.service.*)                       │
│  • Business logic & Use cases (e.g. registerCourse, evaluateFYP)       │
│  • Multi-entity validation (prerequisites, clash checks, credit limits)│
│  • Coordinates between Models, File I/O, and Logger                   │
└──────────────────┬───────────────────┬───────────────────┬─────────────┘
                   │ reads/writes      │ logs              │ manipulates
                   ▼                   ▼                   ▼
            ┌──────────────┐    ┌──────────────┐    ┌────────────────────┐
            │  FILE I/O    │    │    LOGGER    │    │    MODEL LAYER     │
            │(FileManager) │    │ (Logger.java)│    │(com.fast.campus.   │
            │  data/*.txt  │    │logs/campus.log│   │     model.*)       │
            └──────────────┘    └──────────────┘    │ • Fields/Attributes│
                                                    │ • Getters/Setters  │
                                                    │ • Self-state rules │
                                                    │   (isFull, isLate) │
                                                    └────────────────────┘
```

### 3.1 `com.fast.campus.model` — Domain Entities (State & Self-Contained Rules)
**What belongs here:**
- **Attributes / Fields:** Instance variables representing the entity (`id`, `title`, `capacity`, `members`, `schedule`).
- **Constructors:** Initializing new object instances.
- **Getters & Setters:** Standard encapsulation accessors.
- **Entity Invariant Methods:** Pure functions that only examine or update the object's *own internal state*.
  - *Examples:*
    - `Section.isFull()`: checks if `enrollments.size() >= capacity`.
    - `Schedule.hasClash(otherSchedule)`: compares its own day/time with another schedule.
    - `Assignment.isDeadlinePassed()`: checks if `LocalDate.now().isAfter(deadline)`.
    - `Submission.assignMarks(marks)`: updates its own `marks` and sets status to `EVALUATED`.

❌ **What DOES NOT belong in Model files:**
- File writing/reading (Never call `FileManager` from a Model class).
- Multi-step business workflows (e.g. finding a student, checking 3 conditions, creating a record).
- Console printing / user input menus.

---

### 3.2 `com.fast.campus.service` — Business Logic & Use Cases (Orchestration)
**What belongs here:**
- **Use Cases specified in the plan:** e.g., `registerCourse()`, `dropCourse()`, `assignInstructor()`, `scheduleFYPMeeting()`, `evaluateSubmission()`.
- **Validation Rules:**
  - Checking prerequisite completion before allowing enrollment.
  - Ensuring student does not exceed the maximum credit hour limit.
  - Checking schedule clash across all existing enrolled sections before registering.
- **Multi-Object Orchestration:**
  - Taking a `Student` and a `Section`, calling `section.enroll(enrollment)`, updating `student.getEnrolledSections().add(...)`.
- **Persistence Triggers:** Calling `FileManager.appendLine(...)` or `FileManager.writeAllLines(...)` after an operation succeeds.
- **Logging Triggers:** Calling `Logger.info(...)` / `Logger.error(...)` when events occur.

---

### 3.3 Concrete Comparison: Model vs. Service

| Concept | Placed in `model/` (Entity) | Placed in `service/` (Business Logic) |
|---|---|---|
| **Course Capacity** | `section.isFull()` (returns `boolean`) | `academicOfficeService.createSection(...)` or `studentService.registerCourse(...)` throwing `CourseFullException` if `section.isFull()` |
| **Schedule Conflict** | `schedule.hasClash(other)` (returns `boolean`) | `studentService.registerCourse(...)` looping over all registered sections and checking clashes |
| **Assignment Evaluation** | `submission.assignMarks(score)` | `studentService.evaluateSubmission(sub, score, feedback)` + file saving + logging |
| **Request Decisions** | `request.setStatus(RequestStatus.APPROVED)` | `academicOfficeService.approveRequest(req)` + file saving + logging |

---

### 3.4 Other Packages
- **`com.fast.campus.enums`:** Fixed enumeration constants (no business logic).
- **`com.fast.campus.exception`:** Custom checked exceptions thrown when validation fails.
- **`com.fast.campus.comparator`:** Sorting logic implementing `Comparator<T>` (e.g., sorting requests by priority, meetings by date).
- **`com.fast.campus.util`:** Infrastructure utilities (`Logger` for writing logs, `FileManager` for reading/writing text files).
- **`data/`:** Flat files storing data records using pipe delimiter (`|`).

---

## 4. Member Kickoff Guide: "Where Do I Start?"

When you sit down to start coding on your branch, follow this roadmap:

### 4.1 Hasan (`dev/Hasan`) — Academic Office & Core Domain
1. **Switch to your branch:**
   ```bash
   git checkout dev/Hasan
   ```
2. **Files you open:**
   - **Service (Your main logic):**
     - [`src/com/fast/campus/service/AcademicOfficeService.java`](file:///d:/SCD-Assigment-1/src/com/fast/campus/service/AcademicOfficeService.java)
   - **Models (Your entities):**
     - `Course.java`, `Section.java`, `Schedule.java`, `Enrollment.java`, `AcademicOfficeAdmin.java`
   - **Persistence files you write to:**
     - `data/courses.txt`, `data/sections.txt`, `data/enrollments.txt`
3. **What to implement first:**
   - In `AcademicOfficeService.java`:
     - Load initial courses/sections from `data/courses.txt` and `data/sections.txt` using `FileManager.readLines(...)`.
     - Implement `createCourse`: Validate duplicate courseCode -> add to list -> append to `data/courses.txt` -> `Logger.info(...)`.
     - Implement `createSection` and `assignInstructor` / `assignRoom` -> update model -> rewrite `data/sections.txt` -> `Logger.info(...)`.
     - Implement `approveRequest` / `rejectRequest` -> update status -> log.

---

### 4.2 Kabeer (`dev/Kabeer`) — Student & Teaching Assistant
1. **Switch to your branch:**
   ```bash
   git checkout dev/Kabeer
   ```
2. **Files you open:**
   - **Service (Your main logic):**
     - [`src/com/fast/campus/service/StudentService.java`](file:///d:/SCD-Assigment-1/src/com/fast/campus/service/StudentService.java)
   - **Models (Your entities):**
     - `Student.java`, `NormalStudent.java`, `TeachingAssistant.java`, `Attendance.java`
     - `Request.java`, `CourseClashRequest.java`, `GenericRequest.java`
     - `Assessment.java`, `Assignment.java`, `Submission.java`, `Feedback.java`
   - **Persistence files you write to:**
     - `data/students.txt`, `data/assignments.txt`, `data/submissions.txt`, `data/requests.txt`, `data/attendance.txt`
3. **What to implement first:**
   - In `StudentService.java`:
     - Implement `registerCourse(Student student, Section section)`:
       1. Check if section is full (`section.isFull()`) -> throw `CourseFullException`.
       2. Check schedule clash against `student.getEnrolledSections()` (`sec.hasClash(section)`) -> throw `CourseClashException`.
       3. Create new `Enrollment`, link to section and student.
       4. Save to `data/enrollments.txt` & log event via `Logger.info(...)`.
     - Implement `dropCourse`: Change enrollment status to `DROPPED`, update persistence, log.
     - Implement `submitAssignment(Submission sub)`: Validate deadline (`assignment.isDeadlinePassed()`), mark `LATE` if needed, save to `data/submissions.txt`.
     - Implement TA grading: `evaluateSubmission(sub, marks, feedback)` -> update submission, save, log.
     - Implement `submitCourseClashRequest` -> save to `data/requests.txt`.

---

### 4.3 Saim (`dev/Saim`) — Instructor & FYP Module
1. **Switch to your branch:**
   ```bash
   git checkout dev/Saim
   ```
2. **Files you open:**
   - **Service (Your main logic):**
     - [`src/com/fast/campus/service/InstructorService.java`](file:///d:/SCD-Assigment-1/src/com/fast/campus/service/InstructorService.java)
   - **Models (Your entities):**
     - `Instructor.java`, `VisitingInstructor.java`, `PermanentInstructor.java`
     - `FYPGroup.java`, `FYPMeeting.java`, `FYPEvaluation.java`
   - **Persistence files you write to:**
     - `data/instructors.txt`, `data/fypgroups.txt`, `data/fypmeetings.txt`, `data/fyp_evaluations.txt`, `data/attendance.txt`
3. **What to implement first:**
   - In `InstructorService.java`:
     - Implement `markAttendance(instructor, student, section, status)`:
       - Create `Attendance` record, append to `data/attendance.txt`, log with `Logger.info(...)`.
     - Implement `calculateAttendancePercentage(student, section)`:
       - Read lines from `data/attendance.txt` for this student & section -> count `PRESENT` / total sessions * 100.
     - Implement `assignTA(permanentInstructor, normalStudent, section)`:
       - Validate instructor is permanent, assign TA to section, log.
     - Implement `createFYPGroup`, `scheduleFYPMeeting`, `evaluateFYPIdea`:
       - Validate constraints, update group models, persist to `data/fypgroups.txt`, `data/fypmeetings.txt`, `data/fyp_evaluations.txt`.

---

## 5. Git Branching & Synchronization Strategy

We strictly follow a **Feature Branch / Developer Branch** workflow. Direct pushes to `main` are prohibited.

### 5.1 Branch Layout
```text
           (Hasan)     dev/Hasan    ───────┐
                                           │
  main ────────────────────────────────────┼──────► (Stable Release)
   ▲                                       │
   │       (Kabeer)    dev/Kabeer   ───────┤
   │                                       │
   └────── (Saim)      dev/Saim     ───────┘
```

- `main`: Always kept compilable, clean, and passing tests.
- `dev/Hasan`: Hasan's active working branch.
- `dev/Kabeer`: Kabeer's active working branch.
- `dev/Saim`: Saim's active working branch.

---

## 6. Step-by-Step Developer Workflow

### Step 1: Clone & Initial Setup
Clone the repository and verify your remote:
```bash
git clone <repository-url>
cd SCD-Assigment-1
```

### Step 2: Switch to Your Assigned Branch
Before touching any code, checkout your dedicated branch:

- **For Hasan:**
  ```bash
  git checkout -b dev/Hasan
  ```
- **For Kabeer:**
  ```bash
  git checkout -b dev/Kabeer
  ```
- **For Saim:**
  ```bash
  git checkout -b dev/Saim
  ```

### Step 3: Syncing with `main` Frequently (Rebase / Merge)
Before you start coding each session, bring the latest changes from `main`:
```bash
git checkout main
git pull origin main
git checkout dev/<YourName>
git merge main
```
*Resolve any conflicts locally before proceeding.*

### Step 4: Making Changes
- Work **only** within your designated models and services.
- If you need a method from another teammate’s class that is still a stub, **do not change the method signature** without coordinating. Call the existing stub method.
- Follow the logging and persistence standards (detailed in Section 7).

### Step 5: Verify Local Compilation
Never commit broken code. Ensure the entire project compiles with 0 errors:
Open File `Main.java` in src\com\fast\campus and click run next to the class name method and make sure you have JDK installed in your IDE to run this.


### Step 6: Commit and Push
Use clear, semantic commit messages:
```bash
git add src/com/fast/campus/model/YourModel.java src/com/fast/campus/service/YourService.java
git commit -m "feat(student): implement course registration validation and clash check"
git push -u origin dev/<YourName>
```

### Step 7: Pull Request & Merge to `main`
1. Go to GitHub and open a **Pull Request (PR)** from `dev/<YourName>` into `main`.
2. Provide a brief summary of what was implemented or fixed.
3. Request at least one teammate review.
4. Once verified that the build is green and no conflicts exist, merge the PR into `main`.
5. Other teammates then pull `main` into their local developer branches.

---

## 7. Development Standards & Conventions

### 7.1 Application Logging (`Logger.java`)
All business operations and state updates must be logged using the shared logger:
```java
import com.fast.campus.util.Logger;

// Info level: successful actions, state transitions
Logger.info("AcademicOfficeAdmin", "Course created: CS101");
Logger.info("Student", "Student 22F-1234 registered for Section A");

// Warning level: handled constraints (e.g. late submissions, full sections)
Logger.warn("Student", "Late submission attempt for assignment A1");

// Error level: caught exceptions or unexpected states
Logger.error("Instructor", "Failed to update attendance: Record not found");
```
Log output format: `[YYYY-MM-DD HH:mm:ss] [LEVEL] [Actor] Event` (written to console and `logs/campus.log`).

### 7.2 Persistence Convention (`FileManager.java`)
All entity persistence uses flat-file storage in the `data/` directory.

- **Delimiter:** Pipe character (`|`)
- **Structure:** One record per line.
- **Header:** Lines beginning with `#` are treated as comments/headers and ignored by `FileManager.readLines()`.

**Sample file entry (`data/courses.txt`):**
```text
# Format: COURSE|courseCode|title|creditHours
COURSE|CS101|Introduction to Software Engineering|3
COURSE|CS201|Data Structures|4
```

**Using `FileManager`:**
```java
import com.fast.campus.util.FileManager;

// Reading records
List<String> records = FileManager.readLines("data/courses.txt");

// Appending a single record
FileManager.appendLine("data/courses.txt", "COURSE|CS301|Algorithms|3");

// Rewriting all lines upon update/delete
FileManager.writeAllLines("data/courses.txt", updatedRecordsList);
```

### 7.3 Exception Handling
Throw domain-specific exceptions from `com.fast.campus.exception.*` rather than generic `RuntimeException` or `Exception`:
- `CourseFullException` when section capacity is reached.
- `CourseClashException` when timetable periods conflict.
- `SubmissionDeadlineException` when submissions are submitted past the deadline.
- `InvalidFYPGroupException` when group constraints are violated.
- `UnauthorizedActionException` for role permission violations.

---

## 8. Phased Milestone Roadmap

```text
┌────────────────────────────────────────────────────────┐
│ Phase 1: Foundation (COMPLETED ✅)                      │
│ - Shared Enums, Exceptions, Comparators, Logger        │
│ - Base Person & Administrator, Stubs for Student/Inst  │
│ - Verified compiling with 0 errors (53 classes)        │
└──────────────────────────┬─────────────────────────────┘
                           │
┌──────────────────────────▼─────────────────────────────┐
│ Phase 2: Parallel Implementation (CURRENT 🚀)          │
│ - Hasan: AcademicOfficeService & Course/Section FileIO │
│ - Kabeer: StudentService & Attendance/Request Logic    │
│ - Saim: InstructorService & FYP Management Logic       │
└──────────────────────────┬─────────────────────────────┘
                           │
┌──────────────────────────▼─────────────────────────────┐
│ Phase 3: Integration & Testing                         │
│ - Replace remaining stub methods with real integrations│
│ - Cross-module test suite & end-to-end user flows      │
│ - Persistence consistency verification                 │
│ - Final demo / submission package                      │
└────────────────────────────────────────────────────────┘
```

---

## 9. Golden Rules for Collaboration

1. **Never edit another member's primary service without communicating.**
2. **Never change existing method signatures in shared models** without discussing with the dependent teammate.
3. **Keep Model classes focused on state and single-entity rules; place multi-step use cases, file I/O, and logging into Service classes.**
4. **Always pull `main` before starting work** to prevent large merge conflicts.
5. **Always compile before pushing.** If `javac` fails, do not push!
6. **Keep commits atomic and descriptive** (e.g., `feat(fyp): add meeting schedule logic` rather than `updates`).
