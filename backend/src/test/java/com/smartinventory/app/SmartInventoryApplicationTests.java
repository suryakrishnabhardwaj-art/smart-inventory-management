package com.smartinventory.app;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class SmartInventoryApplicationTests {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;
 @Test void contextLoads() {}
 @Test void healthEndpointIsPublic() throws Exception {
  mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
 }
 @Test void loginAcceptsSeededAdminAndRejectsBadPassword() throws Exception {
  mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
    .content("{\"username\":\"admin\",\"password\":\"Admin@12345\"}"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN"));
  mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
    .content("{\"username\":\"admin\",\"password\":\"wrong-password\"}"))
    .andExpect(status().isUnauthorized());
 }
 @Test void protectedInventoryFlowRecordsMovementAndRejectsNegativeStock() throws Exception {
  MvcResult created=mvc.perform(post("/api/products").with(user("admin").roles("ADMIN")).contentType(MediaType.APPLICATION_JSON)
    .content("{\"sku\":\"TEST-API-1\",\"name\":\"API Test Item\",\"category\":\"Test\",\"description\":\"Created in API test\",\"quantity\":5,\"reorderLevel\":2,\"maxStockLevel\":20,\"unitPrice\":12.50}"))
    .andExpect(status().isCreated()).andReturn();
  long id=json.readTree(created.getResponse().getContentAsString()).get("id").asLong();
  mvc.perform(post("/api/inventory/stock-in").with(user("staff").roles("STAFF")).contentType(MediaType.APPLICATION_JSON)
    .content("{\"productId\":"+id+",\"quantity\":3,\"reason\":\"test receipt\"}"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.previousStock").value(5)).andExpect(jsonPath("$.resultingStock").value(8));
  mvc.perform(post("/api/inventory/stock-out").with(user("staff").roles("STAFF")).contentType(MediaType.APPLICATION_JSON)
    .content("{\"productId\":"+id+",\"quantity\":2,\"reason\":\"test issue\"}"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.previousStock").value(8)).andExpect(jsonPath("$.resultingStock").value(6));
  mvc.perform(post("/api/inventory/stock-out").with(user("staff").roles("STAFF")).contentType(MediaType.APPLICATION_JSON)
    .content("{\"productId\":"+id+",\"quantity\":7,\"reason\":\"invalid issue\"}"))
    .andExpect(status().isBadRequest());
  mvc.perform(get("/api/inventory/transactions").with(user("staff").roles("STAFF")).param("productId",Long.toString(id)))
    .andExpect(status().isOk()).andExpect(jsonPath("$[0].type").value("STOCK_OUT"));
  mvc.perform(get("/api/dashboard/low-stock").with(user("staff").roles("STAFF"))).andExpect(status().isOk()).andExpect(jsonPath("$[0].sku").value("OFF-PAP-A4"));
  mvc.perform(get("/api/dashboard/out-of-stock").with(user("staff").roles("STAFF"))).andExpect(status().isOk()).andExpect(jsonPath("$[0].sku").value("ELE-MOU-WL"));
  mvc.perform(get("/api/dashboard/overstock").with(user("staff").roles("STAFF"))).andExpect(status().isOk()).andExpect(jsonPath("$[0].sku").value("OFF-NBK-A5"));
  mvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
 }
}
