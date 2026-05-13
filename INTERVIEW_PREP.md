# Mini Jira — Complete Interview Preparation Guide

> Written in simple language. Read this like a story — top to bottom. By the end, you should be able to explain every part of this project confidently in an interview.

---

## TABLE OF CONTENTS

1. [What is this project?](#1-what-is-this-project)
2. [The Big Picture — How Everything Connects](#2-the-big-picture)
3. [Backend — Explained Simply](#3-backend)
   - 3.1 What is Spring Boot?
   - 3.2 What are Microservices?
   - 3.3 API Gateway
   - 3.4 Auth Service
   - 3.5 Workspace Service
   - 3.6 Task Service
   - 3.7 JWT — How Login Works
   - 3.8 The Database
   - 3.9 WebSockets — Real-time Updates
4. [Frontend — Explained Simply](#4-frontend)
   - 4.1 What is React?
   - 4.2 Project Structure
   - 4.3 Routing
   - 4.4 State Management (Zustand)
   - 4.5 How Frontend Talks to Backend (Axios)
   - 4.6 Token Refresh — Staying Logged In
5. [Common Interview Questions & How to Answer Them](#5-interview-questions)
6. [Key Terms Cheat Sheet](#6-key-terms-cheat-sheet)

---

## 1. What Is This Project?

**Mini Jira** is a project management web application — similar to Jira or Trello. It allows teams to:

- Create **workspaces** (like an organization or company)
- Invite **members** to workspaces with different roles (Admin, Manager, Member)
- Create **projects** inside a workspace
- Manage **tasks** on a **Kanban board** (columns like "To Do → In Progress → Done")
- Drag and drop tasks between columns
- Comment on tasks
- See updates in **real-time** (without refreshing the page)

**Why did you build this?**
> "I built this project to demonstrate senior-level full-stack development skills — specifically microservices architecture on the backend with Spring Boot, JWT-based security, real-time WebSocket communication, and a modern React frontend with Tailwind CSS."

---

## 2. The Big Picture

Think of the project as two separate worlds that talk to each other:

```
┌─────────────────────────────────────────────────────────────────┐
│                        USER'S BROWSER                           │
│                                                                 │
│   React Frontend (http://localhost:3000)                        │
│   - Shows the UI (pages, buttons, forms)                        │
│   - Sends HTTP requests to the backend                          │
└────────────────────────┬────────────────────────────────────────┘
                         │  HTTP Requests (REST API)
                         │  WebSocket (real-time)
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                   API GATEWAY (port 8084)                       │
│   - Single entry point for ALL backend requests                 │
│   - Checks if the user is logged in (validates JWT token)       │
│   - Routes requests to the correct microservice                 │
└──────────┬─────────────────┬──────────────────┬────────────────┘
           │                 │                  │
           ▼                 ▼                  ▼
   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
   │ Auth Service │  │  Workspace   │  │ Task Service │
   │  port 8081   │  │   Service    │  │  port 8083   │
   │              │  │  port 8082   │  │              │
   │ - Register   │  │              │  │ - Tasks      │
   │ - Login      │  │ - Workspaces │  │ - Comments   │
   │ - Logout     │  │ - Members    │  │ - WebSocket  │
   │ - Refresh    │  │ - Projects   │  │              │
   └──────┬───────┘  └──────┬───────┘  └──────┬───────┘
          │                 │                  │
          └─────────────────┴──────────────────┘
                            │
                            ▼
               ┌────────────────────────┐
               │   MySQL Database       │
               │   (minijira_db)        │
               └────────────────────────┘
```

**In plain English:**
1. You open the website in your browser (React app)
2. You log in — the frontend sends your email/password to the backend
3. The backend (API Gateway) receives it and forwards it to the Auth Service
4. Auth Service checks your password, creates a token (JWT), and sends it back
5. Now every time you do something (create a task, move a card), the frontend sends that token to prove who you are
6. The backend processes the request and updates the database
7. If anything changes on the board, all users see it instantly via WebSocket

---

## 3. Backend

### 3.1 What Is Spring Boot?

Spring Boot is a Java framework that makes it easy to build web servers. Instead of writing hundreds of lines of configuration, Spring Boot automatically sets up your server, database connection, security, etc.

**Analogy:** Spring Boot is like a pre-configured office building. You just bring your desk (your code) and start working — you don't build the plumbing or electricity yourself.

**Version used:** Spring Boot 3.3.6 with Java 21

---

### 3.2 What Are Microservices?

A traditional application is a **monolith** — one big program that does everything. If one part crashes, everything crashes.

**Microservices** split the application into small, independent services. Each service:
- Runs its own Java process (separate application)
- Has its own port
- Can be deployed independently
- Only does one specific job

**Our microservices:**

| Service | Port | Job |
|---------|------|-----|
| API Gateway | 8084 | Routes requests, checks tokens |
| Auth Service | 8081 | Login, register, logout |
| Workspace Service | 8082 | Workspaces, members, projects |
| Task Service | 8083 | Tasks, comments, real-time updates |

**Interview answer when asked "Why microservices?":**
> "Microservices allow each part of the system to scale independently. For example, if task management has heavy traffic, we can deploy more instances of the task service without scaling the auth service. They also allow teams to work independently on different services without stepping on each other's code."

---

### 3.3 API Gateway

**File:** `BE/api-gateway/src/main/java/com/minijira/gateway/filter/JwtAuthFilter.java`

The API Gateway is the **front door** to the entire backend. The React frontend only knows one address — the API Gateway at port 8084. The frontend never talks directly to individual services.

**What it does:**
1. Receives every HTTP request from the frontend
2. Checks if the request needs authentication (protected routes)
3. If protected, validates the JWT token in the `Authorization` header
4. If token is valid → forwards the request to the correct service
5. If token is missing or invalid → returns `401 Unauthorized`

**Route mapping (configured in `application.yml`):**

| Frontend calls... | Gateway forwards to... |
|-------------------|------------------------|
| `/api/auth/**` | Auth Service (port 8081) — NO token check |
| `/api/workspaces/**` | Workspace Service (port 8082) — token required |
| `/api/tasks/**` | Task Service (port 8083) — token required |
| `/ws/**` | Task Service WebSocket — token required |

**Why this is smart:**
- Frontend doesn't need to know 4 different addresses — just one
- Security check happens in one place (not duplicated in every service)
- Easy to add new services later by adding a new route

---

### 3.4 Auth Service

**Files:**
- Controller: `BE/auth-service/src/main/java/com/minijira/auth/controller/AuthController.java`
- Service: `BE/auth-service/src/main/java/com/minijira/auth/service/AuthService.java`
- JWT Utility: `BE/auth-service/src/main/java/com/minijira/auth/security/JwtUtil.java`

This service handles everything about user identity.

**Endpoints:**

| Method | URL | What it does |
|--------|-----|--------------|
| POST | `/api/auth/register` | Create a new account |
| POST | `/api/auth/login` | Log in, get tokens |
| POST | `/api/auth/refresh` | Get a new access token |
| POST | `/api/auth/logout` | Invalidate session |

**Registration flow:**
1. User sends `{ email, password, fullName }`
2. Service checks: is this email already taken?
3. Hashes the password with BCrypt (never store plain text!)
4. Saves the new `User` to the database
5. Returns `{ accessToken, refreshToken, userId, email, fullName }`

**Login flow:**
1. User sends `{ email, password }`
2. Service loads the user from database by email
3. BCrypt compares the submitted password with the stored hash
4. If match: delete any old refresh tokens, generate new JWT access token (15 min) + refresh token (7 days)
5. Save refresh token in `refresh_tokens` table
6. Return both tokens to the frontend

**Database tables:**
- `users` — stores `id, email, password_hash, full_name, avatar_url, created_at`
- `refresh_tokens` — stores `id, user_id, token, expires_at, created_at`

**Why do we use BCrypt for passwords?**
> "BCrypt is a one-way hashing algorithm. Once a password is hashed, you cannot reverse it back to the original. When a user logs in, we hash their input and compare it to the stored hash. This way, even if our database is stolen, attackers cannot read the passwords."

---

### 3.5 Workspace Service

**Files:**
- Controller: `BE/workspace-service/src/main/java/com/minijira/workspace/controller/WorkspaceController.java`
- Service: `BE/workspace-service/src/main/java/com/minijira/workspace/service/WorkspaceService.java`

This service manages the organizational structure.

**Entities (database tables):**

**Workspace** — like a company or team
```
id, name, slug, owner_id, created_at, updated_at
```

**WorkspaceMember** — who is in the workspace and what role they have
```
id, workspace_id, user_id, role (ADMIN/MANAGER/MEMBER), joined_at
```

**Project** — a project inside a workspace (e.g., "Mobile App")
```
id, workspace_id, name, description, created_by, created_at
```

**BoardColumn** — the columns on the Kanban board (e.g., "To Do", "In Progress")
```
id, project_id, name, position, created_at
```

**Role-Based Access Control (RBAC):**

| Action | Who can do it |
|--------|---------------|
| Create workspace | Any logged-in user |
| Add/remove members | ADMIN only |
| Create project | Any workspace MEMBER |
| View workspace | Any workspace MEMBER |

**When you create a project, 4 default columns are automatically created:**
1. To Do
2. In Progress
3. In Review
4. Done

**Slug generation:**
When you create a workspace named "My Team", the service auto-generates a URL-friendly slug like `my-team`. If that already exists, it appends a counter: `my-team-2`.

---

### 3.6 Task Service

**Files:**
- Controller: `BE/task-service/src/main/java/com/minijira/task/controller/TaskController.java`
- Service: `BE/task-service/src/main/java/com/minijira/task/service/TaskService.java`
- WebSocket Config: `BE/task-service/src/main/java/com/minijira/task/config/WebSocketConfig.java`

This is the heart of the application — it manages the actual work items.

**Task Entity:**
```
id, project_id, column_id, title, description,
assignee_id, created_by, priority (LOW/MEDIUM/HIGH/CRITICAL),
due_date, position (for drag-drop ordering),
created_at, updated_at
```

**TaskComment Entity:**
```
id, task_id, user_id, content, created_at, updated_at
```

**Endpoints:**

| Method | URL | What it does |
|--------|-----|--------------|
| POST | `/api/tasks/` | Create a new task |
| GET | `/api/tasks/project/{projectId}` | List all tasks in a project |
| GET | `/api/tasks/{taskId}` | Get one task |
| PUT | `/api/tasks/{taskId}` | Update task details |
| PATCH | `/api/tasks/{taskId}/move` | Move task to a different column |
| DELETE | `/api/tasks/{taskId}` | Delete task |
| POST | `/api/tasks/{taskId}/comments` | Add comment |
| GET | `/api/tasks/{taskId}/comments` | Get all comments |

**Position system for drag-and-drop:**
Each task has a `position` number within its column. When you drag a task to a new position, the service updates `column_id` and recalculates `position`. Tasks are fetched ordered by column then position, so the board displays correctly.

**Real-time events via WebSocket:**
After every task change, the service broadcasts an event to all connected clients:
- `TASK_CREATED` — when a new task is created
- `TASK_UPDATED` — when a task is edited
- `TASK_MOVED` — when a task is dragged to another column
- `TASK_DELETED` — when a task is removed

Everyone looking at the same project board sees changes instantly.

---

### 3.7 JWT — How Login Works

**JWT = JSON Web Token**

Think of a JWT like a **hotel key card**:
- The hotel (backend) gives you a key card when you check in (login)
- Every time you enter a room (make a request), you swipe the card
- The door (backend) verifies it's a valid card — without calling the front desk (database) every time
- After 15 minutes, the key card expires (access token expires)
- You go to the front desk with your long-term pass (refresh token) to get a new key card

**Structure of a JWT:**

A JWT looks like: `eyJhbGci...header.eyJ1c2VySWQi...payload.signature`

It has 3 parts separated by dots:
1. **Header** — algorithm used (e.g., HS256)
2. **Payload** — data inside the token (userId, email, expiration time)
3. **Signature** — proves the token wasn't tampered with (signed with a secret key)

**Two tokens we use:**

| Token | Lifespan | Purpose |
|-------|----------|---------|
| Access Token | 15 minutes | Sent with every API request |
| Refresh Token | 7 days | Used only to get a new access token |

**Why two tokens?**
> "Short-lived access tokens limit damage if stolen — an attacker can only use it for 15 minutes. The refresh token lives longer but is stored securely and only sent to one specific endpoint. This is the industry-standard approach."

**JWT Secret:** All 4 services share the same secret key. This means any service can verify a token without calling the auth service.

---

### 3.8 The Database

**Database:** MySQL (single database `minijira_db` shared across services)

**ORM:** Spring Data JPA with Hibernate — instead of writing raw SQL, we write Java classes (Entities) and JPA generates the SQL automatically.

**Key tables summary:**

| Table | Service | Purpose |
|-------|---------|---------|
| `users` | Auth | User accounts |
| `refresh_tokens` | Auth | Session management |
| `workspaces` | Workspace | Organizations |
| `workspace_members` | Workspace | Who belongs where + role |
| `projects` | Workspace | Projects within workspaces |
| `columns` | Workspace | Board columns (To Do, etc.) |
| `tasks` | Task | The actual work items |
| `task_comments` | Task | Comments on tasks |

**Hibernate DDL Auto = update:**
When the application starts, Hibernate compares the Java entity classes with the actual database tables and auto-creates or updates tables. Great for development, but in production you'd use database migration tools like Flyway.

---

### 3.9 WebSockets — Real-time Updates

**File:** `BE/task-service/src/main/java/com/minijira/task/config/WebSocketConfig.java`

Regular HTTP is like sending a letter — you ask, the server answers, the connection closes. To get updates, you'd have to keep asking (polling), which is wasteful.

**WebSocket** keeps a persistent connection open. The server can push data to the client anytime without the client asking.

**Protocol:** We use **STOMP over SockJS**
- **STOMP** = Simple Text Oriented Messaging Protocol — a messaging format on top of WebSocket
- **SockJS** = a fallback library — if WebSocket isn't supported, it falls back to long-polling

**How it works in our project:**

```
1. Frontend connects to ws://localhost:8084/ws
2. Frontend subscribes to: /topic/project/{projectId}
3. When ANY user moves a task in that project:
   - Backend sends event to /topic/project/{projectId}
   - ALL subscribed users instantly receive the update
   - Frontend updates the board without page refresh
```

**Events sent:** `{ type: "TASK_MOVED", task: { ...taskData } }`

---

## 4. Frontend

### 4.1 What Is React?

React is a JavaScript library for building user interfaces. Instead of manipulating HTML directly, you build **components** — reusable pieces of UI that automatically update when data changes.

**Key React concept — State:** Variables that, when changed, cause the UI to re-render automatically. No manual DOM manipulation needed.

**Build tool:** Vite (much faster than the old Create React App)

**Node version runs at:** Port 3000

---

### 4.2 Project Structure

```
FE/src/
├── main.jsx           ← Entry point, mounts the React app into index.html
├── App.jsx            ← Defines all routes (URL → Component mapping)
├── index.css          ← Global styles (Tailwind CSS directives)
│
├── api/
│   ├── axiosInstance.js  ← Configured HTTP client (auto-adds auth token)
│   └── authApi.js        ← Functions for login/register/logout/refresh
│
├── pages/
│   ├── LoginPage.jsx      ← Login form
│   ├── RegisterPage.jsx   ← Registration form
│   ├── DashboardPage.jsx  ← Main page after login (placeholder)
│   └── BoardPage.jsx      ← Kanban board for a project (placeholder)
│
├── store/
│   └── authStore.js       ← Stores logged-in user info (Zustand)
│
├── components/ui/     ← Reusable UI components (to be built)
├── features/          ← Feature-specific components (to be built)
│   ├── auth/
│   ├── board/
│   ├── tasks/
│   └── workspace/
├── hooks/             ← Custom React hooks (to be built)
└── lib/               ← Utility functions (to be built)
```

---

### 4.3 Routing

**File:** `FE/src/App.jsx`

**Library:** React Router DOM v6

Routing means: "when the user goes to this URL, show this component."

**Our routes:**

| URL | Component | Access |
|-----|-----------|--------|
| `/login` | LoginPage | Public (anyone) |
| `/register` | RegisterPage | Public (anyone) |
| `/` | DashboardPage | Protected (must be logged in) |
| `/board/:projectId` | BoardPage | Protected (must be logged in) |

**ProtectedRoute — How we guard pages:**

```jsx
// Pseudocode of what ProtectedRoute does:
if (user has access token) {
  show the requested page
} else {
  redirect to /login
}
```

The `:projectId` in `/board/:projectId` is a URL parameter — the actual project ID is dynamic. For example, `/board/42` shows the board for project 42.

---

### 4.4 State Management — Zustand

**File:** `FE/src/store/authStore.js`

**What is state management?**
When a user logs in, we need to remember who they are across the entire app — not just on the login page. State management stores shared data that any component can read or update.

**Why Zustand over Redux?**
> "Zustand is a lightweight alternative to Redux. It requires much less boilerplate code. For a project of this size, Zustand gives all the power we need with a simpler API."

**What we store:**

```javascript
{
  accessToken: "eyJhbGci...",
  refreshToken: "eyJhbGci...",
  user: {
    id: 1,
    email: "user@example.com",
    fullName: "John Doe"
  }
}
```

**Persistence:** We use Zustand's `persist` middleware, which automatically saves this state to `localStorage`. So if you refresh the page, you stay logged in.

**Actions available:**
- `setAuth(data)` — called after login/register, stores tokens + user info
- `clearAuth()` — called on logout, clears everything

---

### 4.5 How Frontend Talks to Backend — Axios

**File:** `FE/src/api/axiosInstance.js`

**Axios** is an HTTP client library for making API calls from JavaScript.

**Our Axios instance is pre-configured:**

```
Base URL: /api  (Vite proxies this to http://localhost:8084)
```

**Why proxy?**
The frontend (port 3000) and backend (port 8084) are on different ports. Browsers block cross-origin requests by default (CORS policy). Vite's proxy makes it look like both are on port 3000, bypassing the issue during development.

**Request Interceptor — Auto-adds the token:**
Every HTTP request automatically gets an `Authorization` header attached:
```
Authorization: Bearer eyJhbGci...
```
You write the API call once — the interceptor adds the token automatically. No manual work.

**Auth API functions** (`FE/src/api/authApi.js`):

| Function | HTTP | Endpoint | Purpose |
|----------|------|----------|---------|
| `registerUser(data)` | POST | `/api/auth/register` | Create account |
| `loginUser(data)` | POST | `/api/auth/login` | Log in |
| `refreshToken(token)` | POST | `/api/auth/refresh` | Renew access token |
| `logoutUser(token)` | POST | `/api/auth/logout` | Log out |

---

### 4.6 Token Refresh — Staying Logged In

**File:** `FE/src/api/axiosInstance.js` (Response Interceptor)

Access tokens expire every 15 minutes. We don't want the user to be kicked out mid-session. The Response Interceptor handles this automatically.

**How it works:**
1. You make an API call, but your access token expired
2. Backend returns `401 Unauthorized`
3. The response interceptor catches the 401
4. It automatically calls `POST /api/auth/refresh` with the refresh token
5. Backend returns a new access token
6. The interceptor retries your original request with the new token
7. You never knew anything happened — the request just succeeds

**If refresh also fails** (refresh token expired):
- `clearAuth()` is called
- User is redirected to `/login`

**Additional Libraries Installed (ready to use):**

| Library | Purpose |
|---------|---------|
| TanStack React Query | Smart data fetching with caching |
| @dnd-kit | Drag-and-drop for the Kanban board |
| SockJS + STOMP | WebSocket real-time connection |
| date-fns | Date formatting utilities |
| clsx | Conditional CSS class names |

---

## 5. Interview Questions & How to Answer Them

---

### Q: "Tell me about your project."

**Answer:**
> "I built Mini Jira — a full-stack project management tool similar to Jira or Trello. The backend is a Spring Boot microservices application with 4 services: an API Gateway, an Auth Service, a Workspace Service, and a Task Service. The frontend is a React app built with Vite and Tailwind CSS. Key features include JWT authentication with token refresh, role-based access control with ADMIN/MANAGER/MEMBER roles, a Kanban board with drag-and-drop, real-time task updates via WebSocket, and a multi-tenant workspace model. I deployed the backend on Railway and the frontend on Vercel, with Docker for containerization."

---

### Q: "Why did you choose microservices instead of a monolith?"

**Answer:**
> "Microservices give you independent scalability — if the task management feature gets heavy traffic, I can scale only that service. They also allow independent deployment — I can update the auth service without touching the task service. Each service has a clear, bounded responsibility, which keeps the code clean and maintainable. The trade-off is added complexity — network calls between services, distributed debugging, and shared JWT secrets across services. For a learning project, this complexity was intentional to demonstrate senior-level architecture knowledge."

---

### Q: "How does authentication work?"

**Answer:**
> "I use JWT-based authentication. When a user logs in, the Auth Service verifies their password using BCrypt and issues two tokens: a short-lived access token (15 minutes) and a long-lived refresh token (7 days). The access token is sent with every API request in the Authorization header. The API Gateway validates the token's signature before forwarding the request. When the access token expires, the React frontend's Axios interceptor automatically calls the refresh endpoint to get a new one without disrupting the user's session. I chose stateless JWT over server-side sessions because it scales better — any service can validate the token without hitting the database or a session store."

---

### Q: "What is the API Gateway and why do you need it?"

**Answer:**
> "The API Gateway is the single entry point for all frontend requests. It serves three purposes: First, routing — it maps URL patterns to the correct microservice. Second, security — it validates JWT tokens in one centralized place, so individual services don't each need to handle that logic. Third, abstraction — the frontend only knows one address (port 8084), not the addresses of all four services. This makes it easy to add, move, or scale services without changing the frontend. I used Spring Cloud Gateway because it integrates well with the Spring ecosystem and supports reactive, non-blocking request handling."

---

### Q: "How does the real-time feature work?"

**Answer:**
> "I used WebSockets with the STOMP protocol and SockJS fallback. When a user opens a project board, the frontend establishes a persistent WebSocket connection to the Task Service through the API Gateway. It subscribes to a channel specific to that project: `/topic/project/{projectId}`. When any user creates, updates, moves, or deletes a task, the Task Service broadcasts an event to that channel. All connected clients receive the event and update their board immediately — no polling, no page refresh. STOMP is a messaging protocol that sits on top of WebSocket and gives us pub/sub semantics. SockJS is a fallback in case the user's browser or network doesn't support raw WebSocket."

---

### Q: "How does drag-and-drop work for tasks?"

**Answer:**
> "I use the `@dnd-kit` library on the frontend for drag-and-drop interactions. Each task has a `position` field in the database. When a task is dragged to a new position, the frontend sends a PATCH request to `/api/tasks/{taskId}/move` with the target column and new position. The Task Service updates the task's `column_id` and `position`, then broadcasts a `TASK_MOVED` WebSocket event so all users see the change. Tasks are fetched ordered by column and position, ensuring consistent rendering across all clients."

---

### Q: "What is Zustand and why not Redux?"

**Answer:**
> "Zustand is a lightweight state management library for React. I used it to store authentication state — the access token, refresh token, and user info — across the entire app. I chose Zustand over Redux because Redux requires a lot of boilerplate: action creators, reducers, store configuration, middleware. Zustand achieves the same result with a fraction of the code. For the size and scope of this project, that simplicity was the right tradeoff. I also used Zustand's `persist` middleware to automatically sync the auth state to localStorage, so users stay logged in across page refreshes."

---

### Q: "How do you handle CORS?"

**Answer:**
> "CORS — Cross-Origin Resource Sharing — is a browser security policy that blocks requests from one origin (domain/port) to a different origin. In development, the frontend is on port 3000 and the backend on port 8084 — different ports, different origins. I solved this two ways: First, Vite's proxy configuration rewrites frontend requests from `/api` to `http://localhost:8084/api`, making them look same-origin to the browser. Second, the API Gateway has CORS configuration that explicitly allows requests from `http://localhost:3000`, including all HTTP methods and headers. In production, the backend would allow the Vercel frontend domain."

---

### Q: "How does RBAC (Role-Based Access Control) work?"

**Answer:**
> "In the Workspace Service, each workspace member has a role: ADMIN, MANAGER, or MEMBER. When a user makes a request — say, to remove a member — the service extracts the user's ID from the JWT token (which was validated by the gateway), looks up their role in the `workspace_members` table, and checks if they have permission. For example, only ADMINs can add or remove members. Regular MEMBERs can view the workspace and create projects, but can't manage membership. This is checked at the service layer, not the database layer, giving us flexibility to define fine-grained rules in code."

---

### Q: "What is TanStack React Query and why use it?"

**Answer:**
> "React Query is a data-fetching library that handles the complexity of async data in React — loading states, error states, caching, background refetching, and stale data management. Without it, every API call requires manual state variables: `isLoading`, `error`, `data`. React Query manages all of that automatically. It also caches responses, so if you navigate away and come back, you see the data immediately while it refetches in the background. It's already installed in this project and will be used for workspace and task data fetching."

---

### Q: "Walk me through what happens when I create a task."

**Answer:**
> "First, on the frontend, the user fills out a form and clicks 'Create Task'. The React component calls `POST /api/tasks/` via Axios, with the access token in the Authorization header. Axios's request interceptor automatically attaches the token.
>
> The request hits the API Gateway on port 8084. The JWT filter validates the token — if valid, the request is forwarded to the Task Service on port 8083.
>
> In the Task Service, the `JwtAuthFilter` extracts the user's ID from the token and sets it as the 'principal' (the logged-in user). The `TaskController` receives the request and calls `TaskService.create()`.
>
> The service creates a `Task` entity, calculates its initial position (it goes to the bottom of the column), and saves it to MySQL via JPA. Then it uses `SimpMessagingTemplate` to broadcast a `TASK_CREATED` event to the WebSocket topic `/topic/project/{projectId}`.
>
> All connected browser clients receive the event and update their Kanban board in real-time. The API also returns the created task as JSON, and the frontend adds it to the local state."

---

## 6. Key Terms Cheat Sheet

| Term | Simple Explanation |
|------|--------------------|
| **Spring Boot** | Java framework that makes building web servers easy |
| **Microservices** | Breaking one app into small, independent services |
| **API Gateway** | The front door — routes requests, checks security |
| **JWT** | A secure token that proves who you are |
| **BCrypt** | A way to encrypt passwords so they can't be reversed |
| **Access Token** | Short-lived proof of identity (15 min) |
| **Refresh Token** | Long-lived token used to get new access tokens (7 days) |
| **Axios** | JavaScript library for making HTTP requests |
| **Axios Interceptor** | Code that runs automatically before/after every request |
| **React** | JavaScript library for building UI components |
| **Vite** | Fast build tool and dev server for React |
| **Zustand** | Lightweight state management for React |
| **React Router** | Library that maps URLs to React components |
| **ProtectedRoute** | A wrapper that redirects to login if not authenticated |
| **CORS** | Browser security policy — controls cross-origin requests |
| **WebSocket** | Persistent connection for real-time bidirectional communication |
| **STOMP** | Messaging protocol on top of WebSocket |
| **SockJS** | WebSocket fallback for older browsers/networks |
| **Tailwind CSS** | Utility-first CSS framework — style with class names |
| **JPA/Hibernate** | Java ORM — write Java classes instead of SQL |
| **Entity** | A Java class that maps to a database table |
| **DTO** | Data Transfer Object — what the API sends/receives |
| **RBAC** | Role-Based Access Control — different permissions per role |
| **Kanban** | Project management board with columns and moveable cards |
| **Docker** | Packages apps into containers for consistent deployment |
| **Railway** | Cloud platform for deploying backend services |
| **Vercel** | Cloud platform for deploying frontend apps |
| **TanStack Query** | Smart data-fetching library with caching and loading states |
| **@dnd-kit** | Drag-and-drop library for React |
| **Maven** | Java build tool — manages dependencies, compiles, packages |
| **Lombok** | Java library that generates getters/setters/constructors automatically |

---

## Final Tips for the Interview

1. **Start high-level, then zoom in.** Begin with the architecture diagram in your head (Section 2), then go deeper when asked.

2. **Always say WHY, not just WHAT.** Don't just say "I used JWT" — say "I used JWT because it's stateless and any service can validate tokens without hitting the database."

3. **Own the tradeoffs.** Microservices add complexity. JWT has limitations (can't invalidate a token before expiry without a blocklist). Acknowledge these — it shows maturity.

4. **Use the analogy of the hotel key card** when explaining JWT. Interviewers love simple analogies.

5. **If you don't know something, say:** "I haven't implemented that yet, but the way I'd approach it is..." — shows thinking, not memorization.

6. **This project demonstrates:**
   - Microservices design
   - API Gateway pattern
   - JWT authentication + refresh flow
   - WebSocket real-time communication
   - RBAC (role-based permissions)
   - React modern state management
   - RESTful API design
   - Docker + cloud deployment
