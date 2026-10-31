package com.smartinventory.app;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.*;

@RestController @RequestMapping("/api")
class ApiController {
 final ProductRepository products; final SupplierRepository suppliers; final TransactionRepository txs; final UserRepository users; final PasswordEncoder encoder;
 ApiController(ProductRepository p,SupplierRepository s,TransactionRepository t,UserRepository u,PasswordEncoder e){products=p;suppliers=s;txs=t;users=u;encoder=e;}
 @GetMapping("/health") Map<String,Object> health(){return Map.of("status","UP","service","smart-inventory-api");}
 record LoginRequest(@NotBlank String username,@NotBlank String password){}
 @PostMapping("/auth/login") Map<String,Object> login(@Valid @RequestBody LoginRequest r){AppUser u=users.findByUsername(r.username()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid username or password"));if(!encoder.matches(r.password(),u.password))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid username or password");return Map.of("username",u.username,"role",u.role.replace("ROLE_",""));}
 @PostMapping("/auth/logout") ResponseEntity<Void> logout(){return ResponseEntity.noContent().build();}
 @GetMapping("/products") List<Product> products(@RequestParam(required=false) String search){return search==null?products.findAll():products.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(search,search);}
 @GetMapping("/products/{id}") Product product(@PathVariable Long id){return products.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"));}
 record ProductRequest(@NotBlank String sku,@NotBlank String name,String category,String description,Long supplierId,@Min(0) int quantity,@Min(0) int reorderLevel,@Min(0) int maxStockLevel,@NotNull @DecimalMin("0.0") BigDecimal unitPrice){}
 @PostMapping("/products") ResponseEntity<Product> addProduct(@Valid @RequestBody ProductRequest r){if(products.findBySkuIgnoreCase(r.sku()).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"SKU already exists");return ResponseEntity.status(201).body(products.save(makeProduct(r,new Product())));}
 @PutMapping("/products/{id}") Product editProduct(@PathVariable Long id,@Valid @RequestBody ProductRequest r){Product p=product(id);products.findBySkuIgnoreCase(r.sku()).filter(x->!x.id.equals(id)).ifPresent(x->{throw new ResponseStatusException(HttpStatus.CONFLICT,"SKU already exists");});int currentStock=p.quantity;makeProduct(r,p);p.quantity=currentStock;return products.save(p);}
 Product makeProduct(ProductRequest r,Product p){p.sku=r.sku();p.name=r.name();p.category=r.category();p.description=r.description();p.quantity=r.quantity();p.reorderLevel=r.reorderLevel();p.maxStockLevel=r.maxStockLevel();p.unitPrice=r.unitPrice();p.supplier=r.supplierId()==null?null:suppliers.findById(r.supplierId()).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Supplier not found"));return p;}
 @DeleteMapping("/products/{id}") ResponseEntity<?> deleteProduct(@PathVariable Long id){Product p=product(id);if(txs.findByProductIdOrderByTransactionDateDesc(id).size()>0)throw new ResponseStatusException(HttpStatus.CONFLICT,"Product has transaction history and cannot be deleted");products.delete(p);return ResponseEntity.noContent().build();}
 @GetMapping("/suppliers") List<Supplier> suppliers(@RequestParam(required=false) String search){return search==null?suppliers.findAll():suppliers.findByNameContainingIgnoreCase(search);}
 @GetMapping("/suppliers/{id}") Supplier supplier(@PathVariable Long id){return suppliers.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Supplier not found"));}
 record SupplierRequest(@NotBlank String name,String contactPerson,@Email String email,String phone,String address){}
 @PostMapping("/suppliers") ResponseEntity<Supplier> addSupplier(@Valid @RequestBody SupplierRequest r){return ResponseEntity.status(201).body(suppliers.save(new Supplier(r.name(),r.contactPerson(),r.email(),r.phone(),r.address())));}
 @PutMapping("/suppliers/{id}") Supplier editSupplier(@PathVariable Long id,@Valid @RequestBody SupplierRequest r){Supplier s=supplier(id);s.name=r.name();s.contactPerson=r.contactPerson();s.email=r.email();s.phone=r.phone();s.address=r.address();return suppliers.save(s);}
 @DeleteMapping("/suppliers/{id}") ResponseEntity<?> deleteSupplier(@PathVariable Long id){Supplier s=supplier(id);products.findAll().stream().filter(p->p.supplier!=null&&p.supplier.id.equals(id)).forEach(p->p.supplier=null);products.flush();suppliers.delete(s);return ResponseEntity.noContent().build();}
 record StockRequest(@NotNull Long productId,@Positive int quantity,String reason){}
 @PostMapping("/inventory/stock-in") @Transactional InventoryTransaction stockIn(@Valid @RequestBody StockRequest r,Authentication auth){return transact(r,TxType.STOCK_IN,auth);}
 @PostMapping("/inventory/stock-out") @Transactional InventoryTransaction stockOut(@Valid @RequestBody StockRequest r,Authentication auth){return transact(r,TxType.STOCK_OUT,auth);}
 InventoryTransaction transact(StockRequest r,TxType type,Authentication auth){Product p=products.lockedById(r.productId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"));int old=p.quantity;if(type==TxType.STOCK_OUT&&r.quantity()>old)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Stock-out quantity exceeds available stock");p.quantity=type==TxType.STOCK_IN?old+r.quantity():old-r.quantity();products.save(p);return txs.save(new InventoryTransaction(p,type,r.quantity(),old,p.quantity,r.reason(),auth.getName()));}
 @GetMapping("/inventory/transactions") List<InventoryTransaction> transactions(@RequestParam(required=false) Long productId){return productId==null?txs.findTop100ByOrderByTransactionDateDesc():txs.findByProductIdOrderByTransactionDateDesc(productId);}
 @GetMapping("/inventory/transactions/{id}") InventoryTransaction transaction(@PathVariable Long id){return txs.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Transaction not found"));}
 @GetMapping("/dashboard/summary") Map<String,Object> summary(){List<Product> all=products.findAll();long low=all.stream().filter(p->p.quantity<=p.reorderLevel&&p.quantity>0).count();long out=all.stream().filter(p->p.quantity==0).count();long over=all.stream().filter(p->p.quantity>p.maxStockLevel).count();BigDecimal value=all.stream().map(p->p.unitPrice.multiply(BigDecimal.valueOf(p.quantity))).reduce(BigDecimal.ZERO,BigDecimal::add);return Map.of("productCount",all.size(),"lowStockCount",low,"outOfStockCount",out,"overstockCount",over,"inventoryValue",value,"recentTransactions",txs.findTop100ByOrderByTransactionDateDesc().stream().limit(8).toList());}
 @GetMapping("/dashboard/low-stock") List<Product> low(){return products.findAll().stream().filter(p->p.quantity<=p.reorderLevel&&p.quantity>0).toList();}
 @GetMapping("/dashboard/out-of-stock") List<Product> out(){return products.findAll().stream().filter(p->p.quantity==0).toList();}
 @GetMapping("/dashboard/overstock") List<Product> over(){return products.findAll().stream().filter(p->p.quantity>p.maxStockLevel).toList();}
 @GetMapping("/dashboard/recommendations") List<Map<String,Object>> recommendations(){return products.findAll().stream().filter(p->p.quantity<=p.reorderLevel).map(p->{Map<String,Object> row=new LinkedHashMap<>();row.put("product",p);row.put("recommendedQuantity",Math.max(0,p.maxStockLevel-p.quantity));row.put("basis","Configured reorder and maximum stock levels; history is not yet sufficient for consumption forecasting.");return row;}).toList();}
}
