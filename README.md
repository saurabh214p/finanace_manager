# Personal Finance Manager API

A RESTful Spring Boot API for managing personal finances. Users can track income and expenses through categorized transactions, set savings goals with automatic progress tracking, and generate monthly or yearly financial reports.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Security | Spring Security (session-based) |
| Persistence | Spring Data JPA + Hibernate |
| Database | H2 (in-memory, default) |
| Validation | Jakarta Bean Validation |
| Build | Maven |
| Utilities | Lombok |

---

## Project Structure

```
src/main/java/finance/management/
├── ManagementApplication.java
├── config/
│   ├── DataInitializer.java       # Seeds default categories on startup
│   └── SecurityConfig.java        # Spring Security configuration
├── controller/
│   ├── AuthController.java
│   ├── CategoryController.java
│   ├── ReportController.java
│   ├── SavingsGoalController.java
│   └── TransactionController.java
├── dto/
│   ├── request/                   # Incoming request bodies
│   └── response/                  # Outgoing response bodies
├── entity/
│   ├── Category.java
│   ├── CategoryType.java          # Enum: INCOME, EXPENSE
│   ├── SavingsGoal.java
│   ├── Transaction.java
│   └── User.java
├── exception/
│   ├── BadRequestException.java   # → 400
│   ├── ConflictException.java     # → 409
│   ├── ForbiddenException.java    # → 403
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java  # → 404
├── repository/
│   ├── CategoryRepository.java
│   ├── SavingsGoalRepository.java
│   ├── TransactionRepository.java
│   └── UserRepository.java
├── security/
│   └── CustomUserDetailsService.java
└── service/
    ├── impl/
    │   ├── AuthServiceImpl.java
    │   ├── CategoryServiceImpl.java
    │   ├── ReportServiceImpl.java
    │   ├── SavingsGoalServiceImpl.java
    │   └── TransactionServiceImpl.java
    ├── AuthService.java
    ├── CategoryService.java
    ├── ReportService.java
    ├── SavingsGoalService.java
    └── TransactionService.java
```

---

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+

### Run Locally

1. **Clone the repository**
   ```bash
   git clone <your-repo-url>
   cd management
   ```

2. **Run the application**
   ```bash
   mvn spring-boot:run
   ```
   The app starts on `http://localhost:8080` using an in-memory H2 database. No external database setup is needed.

3. **H2 Console** (optional, for inspecting data)
   ```
   http://localhost:8080/h2-console
   JDBC URL: jdbc:h2:mem:financedb
   Username: sa
   Password: (leave blank)
   ```

### Deploy to Production

To switch from H2 to PostgreSQL, update `application.yaml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/finance_db
    driver-class-name: org.postgresql.Driver
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
```
And add the PostgreSQL driver dependency to `pom.xml`:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

## API Documentation

All endpoints (except `/api/auth/register` and `/api/auth/login`) require an active session.  
Authentication is session-based — login sets a session cookie that must be included in subsequent requests.

### Base URL
```
http://localhost:8080/api
```

---

### Authentication

#### Register
```
POST /api/auth/register
```
**Request body:**
```json
{
  "username": "john@example.com",
  "password": "securePassword123",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890"
}
```
**Validations:**
- `username` — required, must be a valid email
- `password` — required, minimum 6 characters
- `fullName` — required
- `phoneNumber` — required, 7–15 digits, optional leading `+`

**Response `201 Created`:**
```json
{
  "message": "User registered successfully",
  "userId": 1
}
```

---

#### Login
```
POST /api/auth/login
```
**Request body:**
```json
{
  "username": "john@example.com",
  "password": "securePassword123"
}
```
**Response `200 OK`:**
```json
{
  "message": "Login successful"
}
```
Sets a session cookie (`JSESSIONID`) used for all subsequent authenticated requests.

---

#### Logout
```
POST /api/auth/logout
```
**Response `200 OK`:**
```json
{
  "message": "Logout successful"
}
```
Invalidates the session server-side.

---

### Transactions

#### Create Transaction
```
POST /api/transactions
```
**Request body:**
```json
{
  "amount": 5000.00,
  "date": "2024-01-15",
  "category": "Salary",
  "description": "January Salary"
}
```
**Validations:**
- `amount` — required, must be > 0, max 2 decimal places
- `date` — required, format `YYYY-MM-DD`, cannot be in the future
- `category` — required, must be an existing default or user-created category

**Response `201 Created`:**
```json
{
  "id": 1,
  "amount": 5000.00,
  "date": "2024-01-15",
  "category": "Salary",
  "description": "January Salary",
  "type": "INCOME"
}
```
> The `type` field (`INCOME` or `EXPENSE`) is derived automatically from the category.

---

#### Get Transactions
```
GET /api/transactions
```
**Optional query parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `category` | String | Filter by category name |
| `startDate` | Date (`YYYY-MM-DD`) | Filter from this date |
| `endDate` | Date (`YYYY-MM-DD`) | Filter up to this date |
| `type` | `INCOME` or `EXPENSE` | Filter by transaction type |

**Response `200 OK`:**
```json
{
  "transactions": [
    {
      "id": 1,
      "amount": 5000.00,
      "date": "2024-01-15",
      "category": "Salary",
      "description": "January Salary",
      "type": "INCOME"
    }
  ]
}
```
Results are sorted by date descending.

---

#### Update Transaction
```
PUT /api/transactions/{id}
```
**Request body** (all fields optional):
```json
{
  "amount": 5500.00,
  "category": "Salary",
  "description": "Updated description"
}
```
> **Note:** The `date` field is immutable. If included in the request body, it is silently ignored. This preserves the integrity of historical financial records.

---

#### Delete Transaction
```
DELETE /api/transactions/{id}
```
**Response `200 OK`:**
```json
{
  "message": "Transaction deleted successfully"
}
```

---

### Categories

#### Get All Categories
```
GET /api/categories
```
Returns default system categories plus the user's own custom categories.

**Response `200 OK`:**
```json
{
  "categories": [
    { "name": "Salary",     "type": "INCOME",  "custom": false },
    { "name": "Food",       "type": "EXPENSE", "custom": false },
    { "name": "Freelance",  "type": "INCOME",  "custom": true  }
  ]
}
```

**Default categories seeded on startup:**

| Name | Type |
|------|------|
| Salary | INCOME |
| Food | EXPENSE |
| Rent | EXPENSE |
| Transportation | EXPENSE |
| Entertainment | EXPENSE |
| Healthcare | EXPENSE |
| Utilities | EXPENSE |

---

#### Create Custom Category
```
POST /api/categories
```
**Request body:**
```json
{
  "name": "Freelance",
  "type": "INCOME"
}
```
`type` must be `INCOME` or `EXPENSE`.

**Response `201 Created`:**
```json
{
  "name": "Freelance",
  "type": "INCOME",
  "custom": true
}
```

---

#### Delete Custom Category
```
DELETE /api/categories/{name}
```
**Rules:**
- Default categories cannot be deleted (`403 Forbidden`)
- Categories referenced by existing transactions cannot be deleted (`400 Bad Request`)

**Response `200 OK`:**
```json
{
  "message": "Category deleted successfully"
}
```

---

### Savings Goals

#### Create Goal
```
POST /api/goals
```
**Request body:**
```json
{
  "goalName": "Emergency Fund",
  "targetAmount": 10000.00,
  "targetDate": "2027-01-01",
  "startDate": "2024-01-01"
}
```
**Validations:**
- `targetAmount` — required, must be > 0
- `targetDate` — required, must be a future date
- `startDate` — optional, defaults to today if not provided, must be before `targetDate`

**Response `201 Created`:**
```json
{
  "id": 1,
  "goalName": "Emergency Fund",
  "targetAmount": 10000.00,
  "targetDate": "2027-01-01",
  "startDate": "2024-01-01",
  "currentProgress": 6550.00,
  "progressPercentage": 65.5,
  "remainingAmount": 3450.00
}
```

**Progress calculation:**
```
currentProgress = SUM(income) - SUM(expenses)   [for transactions on or after startDate]
progressPercentage = (currentProgress / targetAmount) * 100
remainingAmount = targetAmount - currentProgress
```
Both `currentProgress` and `remainingAmount` floor at 0 (never go negative).

---

#### Get All Goals
```
GET /api/goals
```
**Response `200 OK`:**
```json
{
  "goals": [ { ...goal with progress... } ]
}
```

---

#### Get Single Goal
```
GET /api/goals/{id}
```

#### Update Goal
```
PUT /api/goals/{id}
```
**Request body** (all fields optional):
```json
{
  "targetAmount": 15000.00,
  "targetDate": "2028-01-01"
}
```

#### Delete Goal
```
DELETE /api/goals/{id}
```

---

### Reports

#### Monthly Report
```
GET /api/reports/monthly/{year}/{month}
```
`month` must be between 1 and 12.

**Response `200 OK`:**
```json
{
  "year": 2024,
  "month": 1,
  "totalIncome": {
    "Salary": 5500.00,
    "Freelance": 1500.00
  },
  "totalExpenses": {
    "Food": 450.00
  },
  "netSavings": 6550.00
}
```

---

#### Yearly Report
```
GET /api/reports/yearly/{year}
```
**Response `200 OK`:**
```json
{
  "year": 2024,
  "totalIncome": {
    "Salary": 8500.00,
    "Freelance": 1500.00
  },
  "totalExpenses": {
    "Food": 450.00
  },
  "netSavings": 9550.00
}
```

---

## Error Responses

All errors follow this format:
```json
{
  "error": "Description of what went wrong"
}
```

| HTTP Status | Exception | Cause |
|-------------|-----------|-------|
| 400 | `BadRequestException` | Invalid input (negative amount, future date, etc.) |
| 400 | `MethodArgumentNotValidException` | Bean validation failure (missing required field, etc.) |
| 401 | `AuthenticationException` | Invalid credentials or no active session |
| 403 | `ForbiddenException` | Accessing another user's resource, or deleting a default category |
| 404 | `ResourceNotFoundException` | Entity not found |
| 409 | `ConflictException` | Duplicate username or category name |

---

## Design Decisions

### Session-Based Authentication
Spring Security session authentication was chosen over JWT for simplicity. Sessions are stored server-side and invalidated immediately on logout, providing instant revocation without token blacklisting. The session ID is stored in a `JSESSIONID` cookie.

### Transaction Date is Immutable
Once a transaction is created its date cannot be updated, even if a `date` field is sent in a PUT request — it is silently ignored. This preserves the integrity of historical records and ensures goal progress calculations remain consistent.

### Category Type is Derived, Not Stored per Transaction
Transactions do not store their own `INCOME`/`EXPENSE` type. Instead, the type is read from the linked `Category` entity. This ensures a transaction's type is always consistent with its category and cannot be set independently.

### Goal Progress is Calculated on the Fly
`currentProgress`, `progressPercentage`, and `remainingAmount` are not stored in the database. They are computed dynamically every time a goal is fetched, by querying the sum of income and expenses since the goal's `startDate`. This means deleting a transaction is immediately reflected in goal progress and reports without any additional update logic.

### Data Isolation
Every query is scoped to the authenticated user via Spring Security's `@AuthenticationPrincipal`. Users cannot read or modify each other's transactions, custom categories, or goals. Accessing another user's resource returns `403 Forbidden` (not 404) to distinguish "not found" from "not yours".

### Default Categories are Shared, Custom Categories are User-Scoped
Default categories (`Salary`, `Food`, etc.) are seeded once at startup and shared across all users. Custom categories belong to a specific user and are invisible to others. A user can create a custom category with the same name as another user's custom category — uniqueness is enforced only within the same user's scope.

### In-Memory H2 Database by Default
The default configuration uses H2 in-memory for zero-configuration local development. The schema is created automatically by Hibernate (`ddl-auto: update`) and default categories are re-seeded on each startup. For production, swap to PostgreSQL via `application.yaml`.

---

## Running E2E Tests

```bash
bash financial_manager_tests.sh http://localhost:8080/api
```

Or against the deployed Render instance:
```bash
bash financial_manager_tests.sh https://finanace-manager.onrender.com/api
```

The test suite covers 8 scenarios: registration, authentication, transactions, categories, savings goals, deletion impact on goals and reports, report generation, and data isolation between users.