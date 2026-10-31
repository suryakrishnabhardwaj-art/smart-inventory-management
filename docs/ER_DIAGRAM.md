# Entity relationship diagram

```mermaid
erDiagram
  APP_USERS { bigint id PK string username UK string password_hash string role }
  SUPPLIERS { bigint id PK string name string contact_person string email string phone string address }
  PRODUCTS { bigint id PK string sku UK string name string category string description int quantity int reorder_level int max_stock_level decimal unit_price bigint supplier_id FK }
  INVENTORY_TRANSACTIONS { bigint id PK bigint product_id FK string type int quantity int previous_stock int resulting_stock datetime transaction_date string reason string performed_by }
  SUPPLIERS o|--o{ PRODUCTS : supplies
  PRODUCTS ||--o{ INVENTORY_TRANSACTIONS : has
```

JPA entities are defined in `backend/src/main/java/com/smartinventory/app/Domain.java`. Users are keyed by unique username; product SKU is unique. Supplier deletion clears product links. Product deletion is blocked when historical transactions exist.
