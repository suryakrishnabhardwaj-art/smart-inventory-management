package com.smartinventory.app;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name="suppliers") @JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY) class Supplier {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @NotBlank @Column(nullable=false) String name;
 String contactPerson; @Email String email; String phone; String address;
 Supplier() {} Supplier(String name,String contactPerson,String email,String phone,String address){this.name=name;this.contactPerson=contactPerson;this.email=email;this.phone=phone;this.address=address;}
}
@Entity @Table(name="products", indexes=@Index(columnList="sku",unique=true)) @JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY) class Product {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @NotBlank @Column(nullable=false,unique=true) String sku;
 @NotBlank @Column(nullable=false) String name;
 String category; @Column(length=1000) String description;
 @ManyToOne(fetch=FetchType.EAGER) @JoinColumn(name="supplier_id") Supplier supplier;
 @Min(0) int quantity; @Min(0) int reorderLevel; @Min(0) int maxStockLevel;
 @NotNull @DecimalMin("0.0") BigDecimal unitPrice;
 Instant createdAt=Instant.now(); Instant updatedAt=Instant.now();
 @PreUpdate void changed(){updatedAt=Instant.now();}
 Product() {}
}
@Entity @Table(name="inventory_transactions", indexes=@Index(columnList="transactionDate")) @JsonAutoDetect(fieldVisibility=JsonAutoDetect.Visibility.ANY) class InventoryTransaction {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @ManyToOne(optional=false) Product product;
 @Enumerated(EnumType.STRING) @Column(nullable=false) TxType type;
 int quantity; int previousStock; int resultingStock; Instant transactionDate=Instant.now(); String reason; String performedBy;
 InventoryTransaction() {}
 InventoryTransaction(Product p,TxType t,int q,int old,int next,String reason,String by){product=p;type=t;quantity=q;previousStock=old;resultingStock=next;this.reason=reason;performedBy=by;}
}
enum TxType { STOCK_IN, STOCK_OUT }
@Entity @Table(name="app_users") class AppUser {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @Column(nullable=false,unique=true) String username;
 @Column(nullable=false) String password;
 @Column(nullable=false) String role="ROLE_STAFF";
 AppUser() {} AppUser(String u,String p,String r){username=u;password=p;role=r;}
}
