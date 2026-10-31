# Hackathon demo notes

## One-minute explanation

Small businesses often discover shortages only when a customer order is due, while excess stock ties up cash. Smart Inventory Management connects product and supplier records to atomic stock-in and stock-out operations. Every movement creates an audit record, so users can verify who changed stock, when, and why. The dashboard derives live stock alerts and inventory valuation from MySQL. Reorder suggestions use explicit thresholds and clearly state when demand history is not sufficient for a reliable forecast.

## Suggested live demo

1. Sign in with the documented local admin credentials.
2. Review the dashboard. The seed catalog includes low stock and an out-of-stock example.
3. Search products by name/SKU; add or edit a product and show validation/duplicate SKU handling.
4. Record stock-in for the wireless mouse, then show the updated stock and transaction audit.
5. Attempt a stock-out greater than the available balance and show the rejected request.
6. Open alerts and reports, export CSV, and explain the reorder threshold calculation.
7. Show suppliers and a product's supplier association.

## Outcomes

- **CO1:** Inventory shortage/overstock problem and relational ER design.
- **CO2:** MySQL schema, keys, constraints, stock persistence and query-backed alerts.
- **CO3:** Java REST API, validation, layered repository/service responsibilities, security.
- **CO4:** React/Vite integration with Spring API and MySQL.
- **CO5:** Stock thresholds, valuation and transparent reorder logic.
- **CO6:** Demonstration workflow and automated build/test commands.

## Viva questions and answers

**Why use a database transaction for stock changes?** It ensures the balance update and movement record either both persist or both roll back.

**How do you handle two users updating one item at once?** A pessimistic write lock serializes updates for that product row; the later stock-out rechecks the resulting balance.

**How are passwords protected?** BCrypt hashes passwords; the stored hash is never a plaintext password.

**What is an overstock?** Current quantity greater than the product's configured maximum stock level.

**How is reorder quantity chosen?** The current version recommends the gap to the configured maximum when at/below reorder point. It does not claim a demand forecast until consumption history is modeled.

**How are suppliers safely deleted?** Product supplier references are cleared first; transaction history references products and remains intact.
