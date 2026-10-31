CREATE DATABASE IF NOT EXISTS smart_inventory_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_inventory_db;
-- Hibernate creates/updates these tables from validated JPA entities on startup.
-- Unique constraints: products.sku, app_users.username. Foreign keys: products.supplier_id,
-- inventory_transactions.product_id. See backend/src/main/java/com/smartinventory/app/Domain.java.
