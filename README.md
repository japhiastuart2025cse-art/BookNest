# BookNest: Member 3 Circulation and Fines

BookNest is a library management project. The circulation and fines screens use the Spring Boot API and MySQL database for real issue, return, and fine records.

## What this module does

- Issues a book only when its ID exists, a member ID exists, and a copy is available.
- Uses a 14-day loan by default and saves the issue date and due date.
- Decreases the available-copy count as part of the same database transaction as the issue.
- Returns a book once, saves the return date, and increases the available-copy count in the same transaction.
- Calculates ₹5 per overdue day. A currently overdue loan shows its changing outstanding amount; a late return saves a pending fine.
- Records a fine as paid only when the payment action writes the paid status, date, and note to MySQL.
- Shows borrowing history, active loans, overdue loans, and fines.

## Project layout

- `backend/src/main/java/com/booknest/backend/model`: Java objects for books, members, loans, and fines.
- `backend/src/main/java/com/booknest/backend/repository`: SQL queries using Spring `JdbcTemplate`.
- `backend/src/main/java/com/booknest/backend/service`: validation, fine calculations, and transaction logic.
- `backend/src/main/java/com/booknest/backend/controller`: REST API endpoints.
- `backend/src/main/resources/schema.sql`: complete project schema for books, members, circulation, and fines.
- `frontend/src/services`: JavaScript functions that call the backend.
- `frontend/src/views/CirculationView.jsx` and `FinesView.jsx`: circulation and fines screens.

The existing Books and Members modules are reused. The circulation table uses `books.book_id` and `members.member_id`; it does not create separate book or member records.

## Requirements

- Java 17 or later
- MySQL 8 or a compatible MySQL server
- Node.js and npm

## Configure MySQL

1. Start MySQL and create the database:

	```sql
	CREATE DATABASE booknest;
	```

2. Run `backend/src/main/resources/schema.sql` against the `booknest` database using MySQL Workbench or the MySQL command-line client. This creates all four tables. Run this once for a new database; it does not add sample books or members.

3. The backend reads its database connection from environment variables. The username defaults to `root`; set it if your MySQL user is different. The password is required and is not stored in this project. In PowerShell, set it in the same terminal that will run the backend:

	```powershell
	$env:DB_USERNAME = "root"
	$env:DB_PASSWORD = "your-local-MySQL-password"
	# Optional if your local URL or database name differs:
	$env:DB_URL = "jdbc:mysql://localhost:3306/booknest"
	```

	Replace the example password in your terminal only. Do not put your real password in source files or commit it.

## Run the backend

From the `backend` directory, in the same PowerShell terminal where the database variables are set:

```powershell
.\mvnw.cmd spring-boot:run
```

The API runs at `http://localhost:8080`. The frontend Vite proxy forwards `/api` requests to it.

## Run the frontend

Open a second terminal in the `frontend` directory:

```powershell
npm install
npm run dev
```

Open the URL printed by Vite, normally `http://localhost:5173`.

## API endpoints

- `GET /api/books` and `GET /api/members`: load existing books and members.
- `POST /api/circulation/issue`: issue a book. JSON body: `{"bookId": 101, "memberId": 1}`. `loanDays` is optional; it defaults to 14.
- `POST /api/circulation/{issueId}/return`: return a loan. The return date is optional and defaults to today.
- `GET /api/circulation`: borrowing history.
- `GET /api/circulation/active`: current loans.
- `GET /api/circulation/overdue`: overdue current loans.
- `GET /api/fines` and `GET /api/fines/summary`: fine records and outstanding totals.
- `POST /api/fines/{fineId}/pay`: record a fine payment. A payment is not claimed until this endpoint saves it.

## Verify

From `backend`, run tests with `./mvnw test` on macOS/Linux or `.\mvnw.cmd test` in PowerShell. The integration tests use the configured MySQL database and insert then remove test rows. They require the schema to be installed and a working database connection.

From `frontend`, run `npm run build` to check the production build.

## Files created or changed for this package

- Changed `backend/src/main/java/com/booknest/backend/service/CirculationService.java` to reject a return if the matching book copy cannot be restocked.
- Changed `backend/src/test/java/com/booknest/backend/service/CirculationServiceTest.java` to cover that inventory boundary.
- Changed `backend/src/main/resources/application.properties` so a real database password must be supplied outside the repository.
- Changed `backend/src/main/resources/schema-circulation.sql` so it references, rather than recreates, the existing Books and Members tables.
- Changed `frontend/src/App.jsx` so empty or unavailable Members/Circulation APIs do not display sample members or fake loan activity.
- Changed this `README.md` with database setup, run commands, endpoints, and beginner explanations.

Circulation and fine model, repository, service, controller, frontend views, and API client files were already in the project and are reused rather than recreated.