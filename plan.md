# Assignment Plan — SE3005 Software Construction & Development

> **Source basis:** This plan consolidates the uploaded **SE3005 Assignment 1: Life at FAST — Team Module Division Plan**.  
> The two documents describe different courses/assignments, so they are kept as separate workstreams rather than mixing their requirements.

---

# Part A — SE3005: Software Construction & Development

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

| Member     | Primary Responsibility              |
| ---------- | ----------------------------------- |
| **Hasan**  | Core Domain + Academic Office Admin |
| **Kabeer** | Student + Teaching Assistant        |
| **Saim**   | Instructor + FYP Module             |

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


This plan preserves the requirements and terminology from the uploaded documents.
