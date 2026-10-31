package com.smartinventory.app;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
@Configuration class DemoData {
 @Bean CommandLineRunner seedDemo(SupplierRepository suppliers,ProductRepository products){return args->{if(!products.findAll().isEmpty())return;
  Supplier office=suppliers.save(new Supplier("Northstar Office Supplies","Meera Shah","orders@northstar.example","+91 80 4000 1200","Bengaluru, Karnataka"));
  Supplier electronics=suppliers.save(new Supplier("Vertex Electronics","Arjun Rao","sales@vertex.example","+91 40 4200 8830","Hyderabad, Telangana"));
  products.save(make("OFF-PAP-A4","Premium A4 Paper","Office supplies",office,42,50,300,"285.00"));
  products.save(make("OFF-PEN-BLU","Blue Ballpoint Pens","Office supplies",office,180,40,250,"125.00"));
  products.save(make("ELE-MOU-WL","Wireless Mouse","Electronics",electronics,0,12,80,"799.00"));
  products.save(make("ELE-USB-64","USB Drive 64 GB","Electronics",electronics,36,15,60,"599.00"));
  products.save(make("OFF-NBK-A5","Hardcover Notebook A5","Office supplies",office,325,30,250,"185.00"));
 };}
 private Product make(String sku,String name,String category,Supplier supplier,int qty,int reorder,int max,String price){Product p=new Product();p.sku=sku;p.name=name;p.category=category;p.description="Demonstration inventory item";p.supplier=supplier;p.quantity=qty;p.reorderLevel=reorder;p.maxStockLevel=max;p.unitPrice=new BigDecimal(price);return p;}
}
