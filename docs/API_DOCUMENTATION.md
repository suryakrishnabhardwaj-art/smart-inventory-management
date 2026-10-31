# REST API

Base URL: `http://localhost:8080/api`. JSON requests and responses. HTTP Basic credentials are used after login. `POST /auth/login` validates credentials and returns the username and role; the frontend retains Basic credentials in session storage. `POST /auth/logout` returns 204 and the frontend clears that session.

| Method | Route | Access | Purpose |
|---|---|---|---|
| GET | `/health` | Public | Health status |
| POST | `/auth/login` | Public | Validate username/password |
| POST | `/auth/logout` | Authenticated | Logout acknowledgement |
| GET | `/products?search=` | Authenticated | Search/list products |
| GET | `/products/{id}` | Authenticated | Product details |
| POST/PUT/DELETE | `/products[/{id}]` | ADMIN | Product CRUD |
| GET | `/suppliers[/{id}]` | Authenticated | Supplier list/details |
| POST/PUT/DELETE | `/suppliers[/{id}]` | ADMIN | Supplier CRUD |
| POST | `/inventory/stock-in` | Authenticated | Record receipt |
| POST | `/inventory/stock-out` | Authenticated | Record issue |
| GET | `/inventory/transactions?productId=` | Authenticated | Recent 100 or product history |
| GET | `/inventory/transactions/{id}` | Authenticated | Transaction details |
| GET | `/dashboard/summary` | Authenticated | Live KPIs and recent movements |
| GET | `/dashboard/low-stock` | Authenticated | Reorder threshold alerts |
| GET | `/dashboard/out-of-stock` | Authenticated | Zero quantity |
| GET | `/dashboard/overstock` | Authenticated | Above maximum quantity |
| GET | `/dashboard/recommendations` | Authenticated | Threshold-based reorder quantity |

Product body fields: `sku`, `name`, `category`, `description`, `supplierId`, `quantity`, `reorderLevel`, `maxStockLevel`, `unitPrice`. Supplier body: `name`, `contactPerson`, `email`, `phone`, `address`. Stock body: `productId`, positive `quantity`, `reason`. Errors use standard Spring error responses and appropriate 400/401/403/404/409 status codes.
