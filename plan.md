# Assignment Plan — SE3005 Software Construction & Development + DAA Assignment 1

> **Source basis:** This plan consolidates the uploaded **SE3005 Assignment 1: Life at FAST — Team Module Division Plan** and the uploaded **Design & Analysis of Algorithms Assignment 1**.  
> The two documents describe different courses/assignments, so they are kept as separate workstreams rather than mixing their requirements.

---

# Part A — SE3005: Software Construction & Development
R.898710832550uz
## 1. Assignment Overview

### Assignment
**SE3005 — Software Construction & Development**  
**Assignment 1: Life at FAST**

### System
**Campus Management System — "Life at FAST"**

The system contains:

- Five actor types across the domain
- Four major class-diagram/module clusters
- A custom exception hierarchy
- Persistent file-based storage
- Application logging
- Role-specific use cases
- Shared enums, comparators, utilities, and domain classes

### Team Architecture

| Member | Primary Responsibility |
|---|---|
| **Hasan** | Core Domain + Academic Office Admin |
| **Kabeer** | Student + Teaching Assistant |
| **Saim** | Instructor + FYP Module |

The division follows architectural boundaries so the foundation can be built first and the other modules can then be developed in parallel.

---

# 2. Global Development Strategy

## Phase 1 — Foundation

Hasan develops the shared/core domain first.

Build:

- `Person`
- `Administrator`
- `AcademicOfficeAdmin`
- `Course`
- `Section`
- `Schedule`
- `Enrollment`
- All shared enums
- All comparators
- Complete exception hierarchy
- Logger utility
- Shared package/file conventions

Kabeer and Saim create only minimal stubs/interfaces for classes that Hasan's foundation needs.

### Goal

The shared contracts must become stable enough for all three developers to work independently.

---

## Phase 2 — Module Development

### Hasan

Develop:

- Academic Office Admin use cases
- Course management
- Section management
- Instructor assignment
- Room assignment
- Request approval/rejection
- Course/section/enrollment file persistence
- Required logging

### Kabeer

Develop:

- `Student`
- `NormalStudent`
- `TeachingAssistant`
- `Attendance`
- Request hierarchy
- Assessment hierarchy
- `Assignment`
- `Submission`
- `Feedback`
- Student use cases
- TA use cases
- Student/assignment/request/attendance persistence
- Required logging

### Saim

Develop:

- `Evaluator`
- `Instructor`
- `VisitingInstructor`
- `PermanentInstructor`
- `FYPGroup`
- `FYPMeeting`
- `FYPEvaluation`
- Instructor use cases
- FYP use cases
- Instructor/FYP persistence
- Required logging

---

## Phase 3 — Integration

Replace all Phase 1 stubs with the actual implementations.

Perform:

- Cross-module compilation
- Integration testing
- End-to-end use-case testing
- File persistence testing
- Logging verification
- Dependency verification
- Regression testing

### Important

File I/O and logging must be integrated from the beginning rather than added only at the end.

---

# 3. Shared Project Conventions

## 3.1 Repository

Use a private GitHub repository.

Branches:

```text
main
dev/Hasan
dev/Kabeer
dev/Saim
```

Development flow:

```text
Developer branch
      ↓
Commit
      ↓
Push
      ↓
Pull Request
      ↓
Review / resolve conflicts
      ↓
Merge into main
```

---

# 4. Package Structure

Use the package structure specified in the team plan:

```text
com.fast.campus.model
com.fast.campus.enums
com.fast.campus.exception
com.fast.campus.util
com.fast.campus.service
```

Suggested organization:

```text
src/
└── com/
    └── fast/
        └── campus/
            ├── model/
            ├── enums/
            ├── exception/
            ├── util/
            └── service/

data/
logs/
```

---

# 5. Shared Enumerations

These are shared by multiple modules and must be agreed upon and committed early.

## EnrollmentStatus

```text
ACTIVE
DROPPED
COMPLETED
```

## AttendanceStatus

```text
PRESENT
ABSENT
LATE
```

## RequestStatus

```text
PENDING
APPROVED
REJECTED
```

## RequestCategory

```text
PROFESSOR
CLASSMATE
OTHER
```

## SubmissionStatus

```text
PENDING
SUBMITTED
LATE
EVALUATED
```

## Day

```text
MONDAY
TUESDAY
WEDNESDAY
THURSDAY
FRIDAY
SATURDAY
```

---

# 6. Shared Comparators

Implement:

```text
StudentNameComparator
RequestPriorityComparator
RequestDateComparator
AssignmentDeadlineComparator
FYPMeetingDateComparator
```

Required comparison signatures:

```text
compare(Student o1, Student o2)
compare(Request o1, Request o2)
compare(Assignment o1, Assignment o2)
compare(FYPMeeting o1, FYPMeeting o2)
```

All shared comparators should be committed early because both Kabeer and Saim may depend on them.

---

# 7. Exception Hierarchy

Base:

```text
CampusException
```

Hierarchy:

```text
CampusException
├── CourseException
│   ├── CourseFullException
│   └── CourseClashException
│
├── RequestException
│   └── InvalidRequestException
│
├── AssessmentException
│   └── SubmissionDeadlineException
│
├── FYPException
│   ├── InvalidFYPGroupException
│   └── InvalidFYPEvaluationException
│
└── UserException
    └── UnauthorizedActionException
```

Each exception should represent the relevant invalid operation rather than using generic exceptions everywhere.

---

# 8. Hasan — Core Domain + Academic Office Admin

## 8.1 Ownership

Hasan owns the foundational layer because the other modules depend on it.

The foundation must be stable before full parallel development starts.

---

## 8.2 Person

Create:

```text
Person
```

Properties and behavior should support the common identity/user functionality required by the actor hierarchy.

`Person` is abstract.

---

## 8.3 Administrator Hierarchy

Create:

```text
Administrator
    ↓
AcademicOfficeAdmin
```

`Administrator` is abstract.

---

## 8.4 Course

Required information:

```text
courseCode
title
creditHours
prerequisites
sections
```

Collections specified by the plan:

```text
Set<Course> prerequisites
List<Section> sections
```

Required operations:

```text
addPrerequisite()
addSection()
getSections()
```

---

## 8.5 Section

Required information:

```text
sectionId
capacity
course
instructor
teachingAssistant
schedule
enrollments
```

Required operations:

```text
enroll()
drop()
isFull()
getAvailableSeats()
assignInstructor()
assignTA()
getEnrolledStudents()
hasClash()
```

---

## 8.6 Schedule

Required information:

```text
day
startTime
endTime
room
```

Required operations:

```text
hasClash()
getScheduleInfo()
```

Use the shared `Day` enum.

---

## 8.7 Enrollment

Required information:

```text
enrollmentId
student
section
enrollmentDate
status
```

Required operations:

```text
cancel()
getStatus()
```

Use:

```text
EnrollmentStatus
```

---

# 9. Hasan — Academic Office Admin Use Cases

Implement:

### Course

- Create Course
- Update Course
- Search Course

### Section

- Create Section
- Update Section
- Set Section Capacity
- Assign Room
- Assign Instructor

### Requests

- View Requests
- Approve Request
- Reject Request

---

# 10. Hasan — Persistence

Own these data files:

```text
data/courses.txt
data/sections.txt
data/enrollments.txt
```

Every relevant operation should update persistent data appropriately.

---

# 11. Hasan — Logging

Log:

- Course creation
- Course update
- Section creation
- Instructor assignment
- Request approval
- Request rejection

Build the shared Logger utility so all developers can use the same logging format.

---

# 12. Kabeer — Student + Teaching Assistant

## 12.1 Student

Required information:

```text
studentId
totalCreditHours
enrolledSections
registeredCourses
```

Required operations:

```text
viewCourses()
viewSection()
dropSection()
viewTimetable()
submitCourseClashRequest()
```

The team plan lists `viewCourses` twice; retain the intended functionality as one operation rather than implementing duplicate methods solely because of the repeated listing.

---

## 12.2 NormalStudent

Inheritance:

```text
NormalStudent extends Student
```

Required information:

```text
assignedSection
```

Required operation:

```text
getRole()
```

---

## 12.3 TeachingAssistant

Inheritance:

```text
TeachingAssistant extends Student
```

Required information:

```text
assignedSection
```

Required operations:

```text
createAssignment()
readSubmissions()
giveEvaluationAndSubmission()
getRole()
```

---

# 13. Kabeer — Attendance

Create:

```text
Attendance
```

Required information:

```text
student
section
date
status
```

Required operations:

```text
getStatus()
setStatus()
```

Use:

```text
AttendanceStatus
```

---

# 14. Kabeer — Request Hierarchy

Base:

```text
Request
```

`Request` is abstract.

Required information:

```text
requestId
requestDate
description
status
priority
```

Required operations:

```text
submit()
getStatus()
setStatus()
getPriority()
getDetails()
```

---

## 14.1 CourseClashRequest

Extends:

```text
Request
```

Required information:

```text
conflictingSection
requestedSection
```

Required operation:

```text
getConflictDetails()
```

---

## 14.2 GenericRequest

Extends:

```text
Request
```

Required information:

```text
category
```

Use:

```text
RequestCategory
```

Required operation:

```text
getCategory()
```

---

# 15. Kabeer — Assessment Hierarchy

## Assessment

Abstract base class.

Required information:

```text
id
title
description
deadline
totalMarks
```

Provide getters.

---

## Assignment

Extends:

```text
Assessment
```

Required information:

```text
section
createdBy
submissions
```

`createdBy` is a:

```text
TeachingAssistant
```

Required operations:

```text
addSubmission()
getSubmissions()
isDeadlinePassed()
```

---

## Submission

Required information:

```text
submissionId
assignment
student
submissionDate
content
marks
feedback
status
```

Required operations:

```text
submit()
isLate()
assignMarks()
addFeedback()
getMarks()
getStatus()
```

---

## Feedback

Required information:

```text
feedbackId
evaluator
comments
date
```

Required operation:

```text
getComments()
```

---

# 16. Kabeer — Normal Student Use Cases

Implement:

- View Available Courses
- View Course Credit Hours
- Register Course
  - Calculate Total Credit Hours
  - Check Section Clash
- Drop Course
- View Registered Courses
- View Timetable
- View Assignments
- Submit Assignment
- View Attendance
- View Attendance Percentage
- Submit Course Clash Request
- View Requests

---

# 17. Kabeer — Teaching Assistant Use Cases

Implement:

- View Enrolled Students
- View Assigned Section
- Create Assignment
  - Set Total Marks
  - Set Assignment Deadline
- View Submissions
- Check Late Submissions
- Evaluate Submission
  - Assign Marks
  - Give Feedback

---

# 18. Kabeer — Persistence

Own:

```text
data/students.txt
data/assignments.txt
data/submissions.txt
data/requests.txt
data/attendance.txt
```

---

# 19. Kabeer — Logging

Log:

- Registrations
- Course drops
- Assignment creation
- Assignment submissions
- Late submissions
- Evaluations
- Request submissions

---

# 20. Saim — Instructor + FYP Module

## 20.1 Evaluator

Create abstract:

```text
Evaluator
```

Required operation:

```text
evaluate(): void
```

---

# 21. Instructor Hierarchy

Base:

```text
Instructor extends Person
```

Required information:

```text
teacherId
assignedSections
```

Use:

```text
List<Section>
```

Required operations:

```text
viewCourses()
viewSection()
viewStudents()
markAttendance()
updateAttendance()
calculateAttendancePercentage()
```

---

## 21.1 VisitingInstructor

Extends:

```text
Instructor
```

Required:

```text
getRole()
```

---

## 21.2 PermanentInstructor

Extends:

```text
Instructor
```

Required operations:

```text
assignTAAsStudent(NormalStudent, section)
viewFYPGroup()
scheduleFYPMeeting()
evaluateFYPIdea()
provideFYPFeedback()
getRole()
```

---

# 22. Saim — FYPGroup

Required information:

```text
groupId
title
description
members
supervisor
meetings
evaluations
```

Use:

```text
List<Student> members
List<FYPMeeting> meetings
List<FYPEvaluation> evaluations
PermanentInstructor supervisor
```

Required operations:

```text
addMember()
removeMember()
getMembers()
assignSupervisor()
addMeeting()
addEvaluation()
getDetails()
```

---

# 23. Saim — FYPMeeting

Required information:

```text
meetingId
meetingDate
agenda
notes
```

Required operations:

```text
getMeetingDetails()
updateNotes()
```

---

# 24. Saim — FYPEvaluation

Required information:

```text
evaluationId
evaluationDate
score
feedback
```

Required operations:

```text
evaluate(score)
addFeedback()
getScore()
getFeedback()
```

---

# 25. Saim — Instructor Use Cases

## VisitingInstructor

- View Assigned Courses
- View Assigned Sections
- View Enrolled Students
- Mark Attendance
  - Mark Present
  - Mark Absent
  - Mark Late
- Update Attendance
- Calculate Attendance Percentage

## PermanentInstructor

All VisitingInstructor functionality plus:

- Assign Teaching Assistant
- View FYP Groups
  - View FYP Group Details
  - View FYP Members
- Schedule FYP Meeting
- Evaluate FYP Idea
  - Provide FYP Feedback

---

# 26. Saim — Persistence

Own:

```text
data/instructors.txt
data/fypgroups.txt
data/fypmeetings.txt
data/fyp evaluations.txt
```

---

# 27. Saim — Logging

Log:

- Attendance marking
- Attendance updates
- TA assignments
- FYP meeting scheduling
- FYP evaluations

---

# 28. Cross-Module Dependencies

| Dependency | Defined By | Consumed By | Strategy |
|---|---|---|---|
| Course | Hasan | Kabeer, Saim | Foundation first |
| Section | Hasan | Kabeer, Saim | Foundation first |
| Student | Kabeer | Hasan, Saim | Stub early |
| TeachingAssistant | Kabeer | Hasan/Saim | Share interface once stable |
| Instructor | Saim | Hasan | Stub interface |
| Enums | Hasan | Kabeer, Saim | Commit Day 1 |
| Exceptions | Hasan | Kabeer, Saim | Commit Day 1 |
| Logger | Hasan | All | Shared utility |

---

# 29. Stub Strategy

When a module needs a class before its owner has completed it:

1. Create the minimum class/interface.
2. Match the expected package.
3. Match constructors.
4. Match required return types.
5. Do not implement business logic.
6. Replace the stub during Phase 3.
7. Run integration tests after replacement.

Example:

```text
Student stub
    ↓
Used temporarily by Enrollment/FYPGroup
    ↓
Kabeer implements real Student
    ↓
Phase 3 replacement
    ↓
Integration testing
```

---

# 30. Shared Files to Finalize Before Coding

Agree on:

### Enum package

Example:

```text
com.fast.campus.enums.*
```

### File naming

All data files:

```text
data/
```

### Logs

All logs:

```text
logs/
```

### Logger format

```text
[YYYY-MM-DD HH:mm:ss] [LEVEL] [Actor] Event
```

### Data format

One record per line.

Use pipe delimiters:

```text
|
```

Example:

```text
COURSE|CS101|Intro to SE|3
```

---

# 31. Integration Checklist

Before final submission, verify:

## Compilation

- [ ] All branches compile independently
- [ ] Main branch compiles
- [ ] No temporary stubs remain
- [ ] No broken imports
- [ ] Package structure is consistent

## Domain

- [ ] Person hierarchy works
- [ ] Course works
- [ ] Section works
- [ ] Schedule clash checking works
- [ ] Enrollment works
- [ ] Student hierarchy works
- [ ] Instructor hierarchy works
- [ ] FYP hierarchy works

## Admin

- [ ] Create course
- [ ] Update course
- [ ] Search course
- [ ] Create/update section
- [ ] Capacity management
- [ ] Room assignment
- [ ] Instructor assignment
- [ ] Request approval/rejection

## Student

- [ ] Course viewing
- [ ] Registration
- [ ] Credit-hour calculation
- [ ] Clash checking
- [ ] Drop course
- [ ] Timetable
- [ ] Assignment viewing
- [ ] Assignment submission
- [ ] Attendance viewing
- [ ] Attendance percentage
- [ ] Clash request

## TA

- [ ] View assigned section
- [ ] View students
- [ ] Create assignment
- [ ] View submissions
- [ ] Detect late submissions
- [ ] Evaluate submissions
- [ ] Assign marks
- [ ] Give feedback

## Instructor

- [ ] View assigned courses
- [ ] View sections
- [ ] View students
- [ ] Mark attendance
- [ ] Update attendance
- [ ] Calculate attendance percentage

## FYP

- [ ] Manage members
- [ ] Assign supervisor
- [ ] View group
- [ ] Schedule meeting
- [ ] Update meeting notes
- [ ] Evaluate FYP idea
- [ ] Provide feedback

## Persistence

- [ ] All required `.txt` files exist
- [ ] Records are pipe-delimited
- [ ] Save/load behavior is verified
- [ ] File names match the agreed convention

## Logging

- [ ] Shared Logger is used
- [ ] Timestamp is present
- [ ] Log level is present
- [ ] Actor is present
- [ ] Event is present
- [ ] Required events are logged

---

# 32. Git/Team Workflow Checklist

For every member:

- [ ] Work only on own branch
- [ ] Make focused commits
- [ ] Pull/rebase before major integration where appropriate
- [ ] Resolve conflicts carefully
- [ ] Open PR to `main`
- [ ] Verify build after merging
- [ ] Do not commit generated/unnecessary files
- [ ] Do not overwrite another member's module without agreement

---

# 33. Academic Integrity / AI Policy

The SE3005 team plan explicitly states:

- No code sharing between teams.
- Each member must understand and be able to explain their own code.
- AI assistance is prohibited for generating complete solutions.
- Every submitted line should be understood by the person submitting it.

Therefore, use this plan as a **development/organization checklist**, while ensuring that all submitted implementation is written, understood, and explained by the responsible team member.

---

# Part B — DAA Assignment 1

> **Important:** This is a separate assignment from the SE3005 "Life at FAST" project. The uploaded DAA document requires handwritten/scanned submission, so this section is a study and completion plan rather than part of the Java campus-management implementation.

---

# 34. DAA Assignment Overview

**Design & Analysis of Algorithms Assignment 1**

Topics covered:

1. Efficiency & RAM Model
2. Loop Invariants & Correctness
3. Insertion Sort & Inversions
4. Merge Sort
5. Quick Sort
6. Asymptotic Notation
7. Comparing Rates of Growth
8. Time Complexity of Recursive Algorithms

The assignment states that only handwritten submissions in scanned PDF form are considered for grading.

---

# 35. Q1 — Efficiency & RAM Model

## Required Topics

### RAM Model

Prepare:

- Precise definition of the RAM model
- Which operations cost one step
- Which operations do not constitute a single step
- Why a loop is not one step
- Why a library sort is not one step

### Running-Time Derivations

For snippets A, B, and C:

- Identify every line
- Assign costs `c₁, c₂, ...`
- Count executions
- Form exact `T(n)`
- Simplify to Θ notation

For Snippet C specifically:

First express line 4 as an explicit summation:

```text
Σ ...
```

Then evaluate it.

### Sorting Comparison

Compare:

```text
Insertion sort = 8n²
Merge sort = 64n log₂n
```

Find exactly when insertion sort finishes first and determine the largest such `n`.

### Exponential vs Quadratic

Compare:

```text
100n²
2ⁿ
```

Find the smallest `n` where the exponential algorithm becomes slower.

### Cost Models

Understand the difference between:

```text
RAM / uniform-cost model
```

and

```text
Logarithmic-cost model
```

Use a concrete large-integer example such as RSA arithmetic or repeated squaring.

---

# 36. Q2 — Loop Invariants & Correctness

## Linear Search

Prepare:

- Pseudocode
- Loop invariant
- Initialization
- Maintenance
- Termination
- Correctness conclusion

Use the NADRA/CNIC search scenario given in the assignment.

## Selection Sort

Prepare:

- Loop invariant
- Why outer loop stops at `n - 1`
- Best-case complexity
- Worst-case complexity
- Explanation of why both are the same

## Binary Addition

Implement a procedure for:

```text
A[1..n]
B[1..n]
```

where the least-significant bit is first.

Produce:

```text
C[1..n+1]
```

Include:

- Carry handling
- Final carry
- Running time

---

# 37. Q3 — Insertion Sort & Inversions

## Trace

Use:

```text
A = <x1, x2, ..., x8>
```

The assignment instructs the student to obtain the eight two-digit values by repeatedly selecting page numbers from a book.

Show:

- Array after every outer-loop pass
- Full comparisons for the pass inserting `7`
- Every shift
- Position where `7` is temporarily held

## Line-Cost Analysis

Reproduce the insertion-sort line-cost table.

Use:

```text
cᵢ
tᵢ
```

and derive:

```text
T(n)
```

Then derive:

- Best case
- Worst case
- Input responsible for each

## Inversions

For:

```text
<2, 3, 8, 6, 1>
```

list every inversion.

Then determine:

- Which permutation has maximum inversions
- Number of maximum inversions
- Why insertion sort takes Θ(n + I)

## Inversion Counting

Design an algorithm based on merge sort that counts inversions during merging.

Target:

```text
Θ(n log n)
```

---

# 38. Q4 — Merge Sort

## Trace

Use the same eight values from Q3.

Show:

- Recursion tree
- Splits
- Recursive subarrays
- Every merge result

## MERGE Without Sentinels

Rewrite merge so that:

1. Compare elements while both halves contain elements.
2. Stop when either half is exhausted.
3. Copy the remainder of the other half.
4. Preserve Θ(n) running time.

## Recurrence Proof

Prove by induction:

```text
T(n) = 2T(n/2) + n
T(1) = 1
```

for powers of two.

Target:

```text
T(n) = n log₂n + n
```

Include:

- Induction hypothesis
- Base case
- Inductive step
- Final conclusion

## Binary Insertion Sort

Explain why binary search does not reduce worst-case insertion sort to Θ(n log n).

Focus on the operation that still requires:

```text
Θ(n)
```

per pass:

**shifting elements to make room for the inserted value.**

## Successive Merging

Given:

```text
p sorted arrays
m elements each
```

Calculate the cost of:

```text
merge first 2
merge result with 3rd
merge result with 4th
...
merge result with pth
```

Then identify a faster merging strategy.

---

# 39. Q5 — Quick Sort

## Lomuto Partition

Trace:

```text
<9, 4, 15, 2, 11, 7, 3, 8>
```

Using:

- Last element as pivot
- Lomuto scheme

Show after every iteration:

```text
array
i
j
```

Then give returned pivot index.

## Identical Values

For an array containing `n` identical values:

Determine:

- Partition return position
- Left/right subarray sizes
- Recurrence
- Running time

## Decreasing Input

For strictly decreasing input with deterministic last-element pivot:

- Derive recurrence
- Solve recurrence
- Show Θ(n²)

## Nearly Sorted Cheques

Use the result from Q3(c) concerning:

```text
Θ(n + I)
```

where `I` is the number of inversions.

Explain the implications for an array with only a handful of out-of-order elements.

## Randomized Pivot

Understand exactly what randomization changes:

- It randomizes pivot selection.
- It changes the probability distribution of bad splits.
- It does not make the Θ(n²) worst-case mathematically impossible.
- It makes consistently bad behavior unlikely under the standard randomized analysis.

---

# 40. Q6 — Asymptotic Notation

Prepare the formal set-theoretic definitions of:

```text
O(g(n))
Ω(g(n))
Θ(g(n))
```

For each, understand:

- Bounding function
- Constants
- `n₀`
- What is required after `n₀`
- What is unrestricted before `n₀`

## Proofs to Prepare

### O Proof

```text
100n + 5 = O(n²)
```

Provide:

- One valid `(c, n₀)`
- A second valid `(c, n₀)`
- Explanation why both work

### Ω Proof

```text
5n² = Ω(n)
```

### Θ Proof

```text
½n² − ½n = Θ(n²)
```

Prove both:

```text
O(n²)
Ω(n²)
```

with explicit constants.

### Non-Equivalence

Prove:

```text
n ≠ Θ(n²)
```

using contradiction.

### Θ Equivalence

Prove both directions:

```text
f(n) = Θ(g(n))
        ⇔
f(n) = O(g(n)) and f(n) = Ω(g(n))
```

### Maximum Identity

For asymptotically non-negative functions:

```text
max(f(n), g(n)) = Θ(f(n) + g(n))
```

### Polynomial Shift

Prove:

```text
(n + a)^b = Θ(n^b)
```

for real constants `a` and `b > 0`.

### Terminology

Understand why:

```text
"running time is at least O(n²)"
```

is not meaningful as written.

The likely intended statement should use an appropriate lower-bound notation such as:

```text
Ω(n²)
```

### Exponential Comparisons

Determine:

```text
2^(n+1) = O(2^n)
```

and

```text
2^(2n) = O(2^n)
```

with justification.

### Factorial Logarithm

Prove:

```text
log(n!) = Θ(n log n)
```

using:

Upper bound:

```text
n! ≤ n^n
```

Lower bound:

Keep the largest `ceil(n/2)` factors.

---

# 41. Q7 — Comparing Rates of Growth

## Ordering

Arrange:

```text
n log n
2^n
log₂ n
n!
√n
n²
500
n^(1/3)
4^n
n log(n²)
```

in non-decreasing asymptotic growth.

Where two expressions have the same order, explicitly state:

```text
Θ(...)
```

and justify.

## Pairwise Relations

For each row, determine whether A is:

```text
O(B)
o(B)
Ω(B)
ω(B)
Θ(B)
```

The four rows are:

```text
1. n^(lg c)       vs c^(lg n)
2. lg² n          vs n^(1/2)
3. 2^n            vs 2^(n/2)
4. n^(1/2)        vs n^(sin n)
```

Every cell should be marked yes/no as requested.

---

# 42. Q8 — Recursive Algorithms

## Stooge Sort

Given:

```text
T(n) = 3T(2n/3) + Θ(1)
```

for the recursive structure in the assignment.

Prepare:

- Recurrence
- Recursion-tree analysis
- Time-complexity function
- Big-Θ result

Carefully account for the fact that the first `2n/3` elements are processed, then the last `2n/3`, then the first `2n/3` again.

---

## Recursion Tree Problems

Analyze:

### Recurrence 1

```text
T(n) = 3T(n/2) + Θ(n³)
```

Use recursion-tree levels to obtain an asymptotic upper bound.

### Recurrence 2

```text
T(n) = 4T(n - 1) + n
```

Use a recursion tree to estimate the growth.

### Recurrence 3

```text
T(n) = T(n/5) + T(4n/5) + n lg n
```

Use the recursion-tree method to obtain an asymptotic upper bound.

---

# 43. Substitution / Induction Proofs for Q8(b)

For each recurrence:

1. State the guessed asymptotic upper bound.
2. Assume the bound holds for smaller inputs.
3. Substitute the inductive hypothesis into the recurrence.
4. Simplify.
5. Choose constants where necessary.
6. Complete the inductive step.
7. State the final asymptotic bound.

---

# 44. DAA Submission Checklist

## Q1

- [ ] RAM model
- [ ] Exact line-cost analysis
- [ ] Snippet A
- [ ] Snippet B
- [ ] Snippet C summation
- [ ] Insertion vs merge comparison
- [ ] Quadratic vs exponential threshold
- [ ] RAM vs logarithmic cost

## Q2

- [ ] Linear-search pseudocode
- [ ] Loop invariant
- [ ] Initialization
- [ ] Maintenance
- [ ] Termination
- [ ] Selection-sort invariant
- [ ] `n-1` explanation
- [ ] Best/worst case
- [ ] Binary addition algorithm

## Q3

- [ ] Eight-value insertion sort trace
- [ ] Detailed insertion of 7
- [ ] Line-cost table
- [ ] Best case
- [ ] Worst case
- [ ] Inversions
- [ ] Maximum inversion permutation
- [ ] Θ(n + I)
- [ ] Θ(n log n) inversion counting

## Q4

- [ ] Merge-sort recursion tree
- [ ] All merge results
- [ ] Non-sentinel MERGE
- [ ] Induction proof
- [ ] Binary insertion-sort explanation
- [ ] Successive merging complexity
- [ ] Faster merging method

## Q5

- [ ] Lomuto trace
- [ ] Identical-value case
- [ ] Decreasing-order case
- [ ] Nearly sorted case
- [ ] Randomized pivot analysis

## Q6

- [ ] O definition
- [ ] Ω definition
- [ ] Θ definition
- [ ] All requested proofs
- [ ] Exponential comparisons
- [ ] log(n!) proof

## Q7

- [ ] Growth ordering
- [ ] Equal-growth pairs
- [ ] All O/o/Ω/ω/Θ cells

## Q8

- [ ] Stooge-sort recurrence
- [ ] Stooge-sort complexity
- [ ] Three recursion-tree bounds
- [ ] Three substitution proofs

---

# 45. Final Completion Order

## SE3005

```text
1. Agree packages and conventions
2. Create repository and branches
3. Commit enums
4. Commit exception hierarchy
5. Build Person / core domain
6. Create temporary stubs
7. Parallel module implementation
8. Implement file persistence
9. Implement logging
10. Replace stubs
11. Integration testing
12. End-to-end testing
13. Review every class against requirements
14. Verify Git history / branches
15. Final build
```

## DAA

```text
1. Select the required x1...x8 values
2. Solve Q1
3. Solve Q2
4. Trace insertion sort for Q3
5. Complete inversion analysis
6. Complete merge-sort work
7. Complete quick-sort work
8. Complete asymptotic proofs
9. Complete growth-rate table
10. Complete recursive recurrence analysis
11. Check every mark-bearing subpart
12. Handwrite final answers
13. Scan pages clearly
14. Verify all questions are present
15. Submit scanned PDF according to the assignment requirement
```

---

# 46. Source Boundaries

This plan preserves the requirements and terminology from the uploaded documents.

The **SE3005 section** is based on the Team Module Division Plan, including its module ownership, classes, enums, comparators, exceptions, use cases, file responsibilities, cross-module dependencies, development phases, Git/package conventions, logging format, file format, stubs, and academic-integrity/AI rules.

The **DAA section** is based on the uploaded DAA Assignment 1 and covers all listed questions and subparts from Q1 through Q8.

Where the two documents concern different courses, they are intentionally kept separate rather than treating one assignment's requirements as requirements of the other.
