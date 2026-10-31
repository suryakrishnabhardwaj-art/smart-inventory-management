package com.smartinventory.app;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import java.util.*;
interface ProductRepository extends JpaRepository<Product,Long> {
 Optional<Product> findBySkuIgnoreCase(String sku);
 List<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(String name,String sku);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select p from Product p where p.id=:id") Optional<Product> lockedById(@Param("id") Long id);
}
interface SupplierRepository extends JpaRepository<Supplier,Long> { List<Supplier> findByNameContainingIgnoreCase(String name); }
interface TransactionRepository extends JpaRepository<InventoryTransaction,Long> { List<InventoryTransaction> findTop100ByOrderByTransactionDateDesc(); List<InventoryTransaction> findByProductIdOrderByTransactionDateDesc(Long productId); }
interface UserRepository extends JpaRepository<AppUser,Long> { Optional<AppUser> findByUsername(String username); }
