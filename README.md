# 🎓 Campus Connect — Intelligent Campus Placement & Career Guidance Platform

A fully functional **Java OOP project** demonstrating all core Object-Oriented Programming concepts with a modern, responsive frontend.

---

## 📁 Project Structure

```
CampusConnect/
├── src/
│   ├── Main.java                          ← Entry point; seeds data; starts server
│   ├── model/
│   │   ├── User.java                      ← Abstract base class (ABSTRACTION)
│   │   ├── Student.java                   ← Extends User (INHERITANCE)
│   │   ├── Recruiter.java                 ← Extends User (INHERITANCE)
│   │   ├── Admin.java                     ← Extends User (INHERITANCE)
│   │   ├── Job.java                       ← Job listing model
│   │   ├── Application.java               ← Job application model
│   │   ├── Notification.java              ← In-app notification model
│   │   └── Interview.java                 ← Interview schedule model
│   ├── interfaces/
│   │   ├── Schedulable.java               ← Interview scheduling contract
│   │   ├── Notifiable.java                ← Notification delivery contract
│   │   └── Searchable.java                ← Search/filter contract
│   ├── exceptions/
│   │   ├── InvalidLoginException.java     ← Custom: bad credentials
│   │   ├── ProfileIncompleteException.java← Custom: missing profile fields
│   │   └── DuplicateApplicationException.java ← Custom: already applied
│   ├── service/
│   │   ├── AuthService.java               ← Login, register, sessions
│   │   ├── JobService.java                ← Job CRUD; implements Searchable
│   │   ├── ApplicationService.java        ← Apply, status updates
│   │   ├── NotificationService.java       ← Alerts; implements Notifiable
│   │   ├── InterviewService.java          ← Scheduling; implements Schedulable
│   │   └── CareerGuidanceEngine.java      ← Skill-gap & recommendations
│   ├── storage/
│   │   └── FileStorageManager.java        ← File I/O persistence (.txt)
│   └── server/
│       └── CampusServer.java              ← Embedded HTTP server (com.sun.net.httpserver)
├── frontend/
│   ├── index.html          ← Login / Register
│   ├── dashboard.html      ← Role-specific dashboard (Student/Recruiter/Admin)
│   ├── jobs.html           ← Browse & filter jobs (Student)
│   ├── applications.html   ← Track applications (Student)
│   ├── guidance.html       ← Career guidance & skill-gap (Student)
│   ├── interviews.html     ← Interview schedule (Student + Recruiter)
│   ├── profile.html        ← Edit profile (Student + Recruiter)
│   ├── post-job.html       ← Post new job (Recruiter)
│   ├── my-jobs.html        ← Manage posted jobs (Recruiter)
│   ├── applicants.html     ← View/manage applicants per job (Recruiter)
│   ├── admin-students.html ← View all students (Admin)
│   ├── admin-recruiters.html← Approve/reject recruiters (Admin)
│   ├── admin-jobs.html     ← Manage all jobs (Admin)
│   ├── admin-stats.html    ← Placement statistics & charts (Admin)
│   ├── admin-broadcast.html← Send notifications to users (Admin)
│   ├── style.css           ← Full design system (CSS variables, grid, components)
│   └── app.js              ← Shared utilities, API wrapper, render helpers
├── data/
│   ├── students.txt        ← Persisted student records
│   ├── recruiters.txt      ← Persisted recruiter records
│   ├── admins.txt          ← Persisted admin records
│   ├── jobs.txt            ← Persisted job listings
│   ├── applications.txt    ← Persisted applications
│   ├── notifications.txt   ← Persisted notifications
│   └── interviews.txt      ← Persisted interview schedules
├── run.sh                  ← Build & run (Linux/macOS)
└── run.bat                 ← Build & run (Windows)
```

---

## 🚀 How to Run

### Prerequisites
- **JDK 11 or later** — [Download OpenJDK](https://adoptium.net/)
- A modern web browser (Chrome, Firefox, Edge)

### Linux / macOS
```bash
chmod +x run.sh
./run.sh
```

### Windows
```cmd
run.bat
```

### Manual (any OS)
```bash
# 1. Compile
mkdir out
find src -name "*.java" > sources.txt
javac -d out -sourcepath src @sources.txt

# 2. Run (from project root)
cd out
java Main
```

Then open **http://localhost:8080** in your browser.

---

## 🔑 Demo Login Credentials

| Role      | Email                   | Password  |
|-----------|-------------------------|-----------|
| Student   | arjun@student.edu       | pass123   |
| Student   | priya@student.edu       | pass123   |
| Student   | rahul@student.edu       | pass123   |
| Recruiter | deepa@tcs.com           | pass123   |
| Recruiter | suresh@infosys.com      | pass123   |
| Admin     | admin@campus.edu        | admin123  |

> **Note:** `REC005` (kavya@pending.com) is intentionally pending approval — log in as Admin to approve.

---

## 🏗️ OOP Concepts Mapping Table

| Concept | Where Used | Details |
|---------|-----------|---------|
| **Abstraction** | `User.java` | Abstract class with `abstract getDashboard()` |
| **Inheritance** | `Student`, `Recruiter`, `Admin` | All extend `User`; inherit `toFileString()`, `checkPassword()` etc. |
| **Polymorphism** | `getDashboard()` | Returns role-specific string per subclass; `applyForJob()`, `postJob()` behave differently via role checks |
| **Encapsulation** | All model classes | Private fields; public getters/setters only |
| **Interface — Schedulable** | `InterviewService.java` | `scheduleInterview()`, `cancelInterview()`, `rescheduleInterview()` |
| **Interface — Notifiable** | `NotificationService.java` | `sendNotification()`, `sendBroadcast()`, `markAsRead()`, `getUnreadCount()` |
| **Interface — Searchable** | `JobService.java` | `searchJobs(Map<String,String> filters)`, `searchStudents()` |
| **Custom Exception** | `InvalidLoginException` | Thrown on bad credentials in `AuthService.login()` |
| **Custom Exception** | `ProfileIncompleteException` | Thrown in `ApplicationService.applyForJob()` when profile missing fields |
| **Custom Exception** | `DuplicateApplicationException` | Thrown when student re-applies to same job |
| **ArrayList** | `JobService.jobs`, `ApplicationService.applications` | Ordered collections for jobs and applications |
| **HashMap** | `AuthService.students`, `.recruiters`, `.admins` | O(1) user lookup by ID |
| **LinkedList** | `NotificationService.notifications` | `addFirst()` for recent-first ordering |
| **File I/O** | `FileStorageManager.java` | All entities persisted to `.txt` files via `BufferedReader`/`PrintWriter` |

---

## 🌐 API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/login` | ❌ | Authenticate user |
| POST | `/api/logout` | ✅ | Invalidate session token |
| POST | `/api/register/student` | ❌ | Register student account |
| POST | `/api/register/recruiter` | ❌ | Register recruiter account (pending approval) |
| GET  | `/api/jobs` | ✅ | List open jobs (supports filters: skill, company, location, jobType) |
| POST | `/api/jobs/post` | ✅ | Post new job (Recruiter only) |
| POST | `/api/jobs/close` | ✅ | Close a job listing |
| POST | `/api/apply` | ✅ | Apply for a job |
| GET  | `/api/applications` | ✅ | Get applications (by studentId or jobId) |
| POST | `/api/applications/update` | ✅ | Update application status (Recruiter) |
| GET  | `/api/profile/student` | ✅ | Get student profile |
| POST | `/api/profile/student` | ✅ | Update student profile |
| GET  | `/api/profile/recruiter` | ✅ | Get recruiter profile |
| POST | `/api/profile/recruiter` | ✅ | Update recruiter profile |
| GET  | `/api/notifications` | ✅ | Get notifications for user |
| POST | `/api/notifications/read` | ✅ | Mark notification(s) read |
| POST | `/api/interview/schedule` | ✅ | Schedule an interview |
| GET  | `/api/interviews` | ✅ | Get interviews (by studentId or recruiterId) |
| GET  | `/api/guidance` | ✅ | Get career guidance + recommendations |
| GET  | `/api/admin/stats` | ✅ | Platform placement statistics |
| GET  | `/api/admin/users` | ✅ | List all students/recruiters |
| POST | `/api/admin/approve` | ✅ | Approve or reject a recruiter |
| POST | `/api/admin/broadcast` | ✅ | Broadcast notification to users |

---

## 📐 UML Class Diagram (Text)

```
┌─────────────────────────────────┐
│         <<abstract>>            │
│             User                │
│─────────────────────────────────│
│ - userId: String                │
│ - name: String                  │
│ - email: String                 │
│ - password: String              │
│ - role: String                  │
│ - isApproved: boolean           │
│─────────────────────────────────│
│ + checkPassword(): boolean      │
│ + toFileString(): String        │
│ # getDashboard(): String {abs}  │
└─────────────┬───────────────────┘
              │ extends
    ┌─────────┼──────────┐
    ▼         ▼          ▼
┌──────┐  ┌────────┐  ┌───────┐
│Stude-│  │Recruit-│  │ Admin │
│ nt   │  │  er    │  │       │
└──────┘  └────────┘  └───────┘
  ↓ has-a     ↓ has-a
┌──────────┐ ┌──────────┐
│Applicat- │ │   Job    │
│   ion    │ │          │
└──────────┘ └──────────┘

Interfaces:
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ Schedulable │  │ Notifiable  │  │  Searchable  │
└──────┬──────┘  └──────┬──────┘  └──────┬───────┘
       │                │                │
       ▼                ▼                ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│InterviewSvc  │ │Notification  │ │  JobService  │
│              │ │  Service     │ │              │
└──────────────┘ └──────────────┘ └──────────────┘

Services use: FileStorageManager (File I/O)
CampusServer wires all services together
```

---

## 🎨 Features by Role

### 👨‍🎓 Student
- Register & login with profile (CGPA, skills, department, year)
- Browse and filter jobs by skill, location, type, CTC
- One-click apply with cover letter
- Track applications: Applied → Shortlisted → Interview → Selected/Rejected
- Career Guidance: skill-gap analysis, job recommendations ranked by match %
- View interview schedule with meeting links

### 🏢 Recruiter
- Register (pending admin approval)
- Post jobs with required skills, CTC range, eligibility criteria
- View applicants per job with student profiles
- Shortlist / Reject / Select candidates
- Schedule interviews with type, venue, meeting link

### ⚙️ Admin
- Approve or reject recruiter registrations
- View all students and their profiles
- View all jobs across all companies
- Broadcast announcements to students, recruiters, or everyone
- Placement statistics dashboard (rates, avg CTC, funnel chart)

---

## 📦 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Core Java 11+ (no frameworks) |
| HTTP Server | `com.sun.net.httpserver.HttpServer` (built-in JDK) |
| Data Storage | File I/O — pipe-delimited `.txt` files |
| Collections | `ArrayList`, `HashMap`, `LinkedList` |
| Frontend | HTML5 + CSS3 (Grid/Flexbox) + Vanilla JS |
| Font | Inter (Google Fonts) |
| Deployment | Self-contained — single `java Main` command |
# campus-connect
