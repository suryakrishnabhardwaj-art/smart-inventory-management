# Smart Inventory Management

A React + Spring Boot + MySQL inventory application for product and supplier management, traceable stock movements, live alerts, valuation, and threshold-based reorder guidance.

## Demo access

**Username:** `admin`  
**Password:** `Admin@12345`

The development bootstrap creates the admin only if that username does not exist. Passwords are stored as BCrypt hashes. Set `ADMIN_USERNAME` and `ADMIN_PASSWORD` before first startup to choose different credentials. This demo password is for local development only.

## Requirements and environment check

This workspace was checked with OpenJDK 17.0.20, Node 22.19, npm 10.9, and a running Windows `MySQL80` service. Java 21 is not installed, so the backend targets Java 17 (supported by Spring Boot 3.3.5). Maven is not installed on the system PATH; Maven 3.9.9 was downloaded locally into the workspace for this session and is ignored by Git. The README commands use that local copy. MySQL requires a root password, and no credentials were provided, so database connectivity has not yet been verified.

## Database setup (PowerShell)

1. Ensure MySQL Server 8 is running and you have a MySQL account with permission to create/use `smart_inventory_db`.
2. Create the database with `& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe' -u root -p -e "CREATE DATABASE smart_inventory_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"` (or run [database/schema.sql](database/schema.sql)). MySQL is installed here, but its client is not on PATH; use the full path above or add the MySQL `bin` directory to PATH.
3. Set connection variables in the backend terminal (replace the password locally; do not commit it). Keep this terminal open and run the backend from the same terminal so the variables are inherited:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/smart_inventory_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "YOUR_MYSQL_PASSWORD"
$env:ADMIN_USERNAME = "admin"
$env:ADMIN_PASSWORD = "Admin@12345"
```

Tables are created/updated by Hibernate from the JPA entities. The development seed adds 2 suppliers and 5 products when the catalog is empty. An equivalent optional SQL seed is in [database/seed.sql](database/seed.sql). Updating product details does not rewrite current stock; stock adjustments must use the audited movement endpoints.

## Run in VS Code

Terminal 1, backend:

```powershell
cd "C:\Users\veluv\Smart inventory management\backend"
$env:JAVA_HOME = Split-Path (Split-Path (Get-Command java).Source)
& "..\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
```

Terminal 2, frontend:

```powershell
cd "C:\Users\veluv\Smart inventory management\frontend"
npm.cmd install
npm.cmd run dev
```

Open `http://localhost:5173`. Backend health is `http://localhost:8080/api/health`. Vite proxies `/api` to the backend. The UI uses HTTP Basic authentication over local development HTTP; every API call is checked by Spring Security. Change the seeded password and use HTTPS before any non-local deployment. Swagger UI is not included.

## Features

- Products: search, add, edit, delete when no transaction history, thresholds, category, valuation.
- Suppliers: search, add, edit, safe removal (product supplier links are cleared).
- Stock in/out: positive quantities, row-level pessimistic locking, atomic stock and transaction update, no negative stock.
- Dashboard: live totals, inventory value, recent transactions, low stock, out-of-stock, and overstock counts.
- Reports: downloadable CSV, inventory valuation, transparent threshold-based reorder quantity. Consumption forecasting is explicitly unavailable until enough history has accumulated; no synthetic forecast is shown.
- ADMIN can manage products and suppliers. STAFF can read inventory and perform stock movements. Backend authorization is enforced.

## API

All routes use `/api`; all except health and login require HTTP Basic authentication. Products and suppliers mutations require ADMIN. See [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md).

## Tests and build

`cd backend; $env:JAVA_HOME = Split-Path (Split-Path (Get-Command java).Source); ..\apache-maven-3.9.9\bin\mvn.cmd test` runs the context and API integration tests against an isolated H2 test database; `cd frontend; npm.cmd run build` creates the production build. Testing the running app against persistent MySQL requires valid MySQL credentials. See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), [docs/ER_DIAGRAM.md](docs/ER_DIAGRAM.md), [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md), and [docs/DEMO.md](docs/DEMO.md) for architecture, schema, routes, demo narration, and viva preparation.

## Troubleshooting

- **Maven not recognized:** install Maven 3.9+ and reopen the terminal, then confirm `mvn -version`.
- **npm.ps1 is blocked:** use `npm.cmd` in PowerShell.
- **Communications link failure:** start MySQL, confirm port 3306, and check `DB_URL`, username, and password.
- **`Access denied for user 'root'@'localhost' (using password: NO)`:** `DB_PASSWORD` was unset in the backend terminal. Set it to the password you chose for MySQL, in that same terminal, then start Spring Boot again. This is a MySQL authentication error, not a Java compilation error.
- **Access denied / unknown database:** create `smart_inventory_db` and grant the configured account access.
- **Port is occupied:** set `SERVER_PORT` for Java or change Vite's port in `frontend/vite.config.js`.
- **401 on an API call:** sign in again; credentials are kept in the current browser session.
- **Demo login says invalid credentials:** the bootstrap creates `admin` only when it is absent. It deliberately does not replace a password already stored in MySQL. If this is only your local demo database and you intend to reset that account, back up first, then run `DELETE FROM app_users WHERE username = 'admin';` in `smart_inventory_db` and restart the backend with the desired `ADMIN_USERNAME` and `ADMIN_PASSWORD`. The bootstrap will recreate the account with a BCrypt hash.

## Team

2520090182 — V. Bhardwaj · 2520090089 — P. Uday · 2520090094 — A. Chaitanya
