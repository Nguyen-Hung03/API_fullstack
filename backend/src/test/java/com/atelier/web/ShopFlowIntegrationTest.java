package com.atelier.web;

import com.atelier.domain.Customer;
import com.atelier.domain.Role;
import com.atelier.repo.CustomerRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ShopFlowIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private CustomerRepository customers;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private Session admin;

    @BeforeEach
    void createAdmin() throws Exception {
        if (customers.findByEmailIgnoreCase("admin@atelier.vn").isEmpty()) {
            Customer customer = new Customer();
            customer.setFullName("Quản trị");
            customer.setEmail("admin@atelier.vn");
            customer.setPhone("0900000001");
            customer.setPasswordHash(passwordEncoder.encode("admin123"));
            customer.setRole(Role.ADMIN);
            customer.setCreatedAt(Instant.now());
            customers.saveAndFlush(customer);
        }
        admin = login("admin@atelier.vn", "admin123");
    }

    @Test
    void registerRejectsInvalidEmailAndPhone() throws Exception {
        mvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Lan","email":"khong-phai-email","phone":"12345","password":"secret1"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ERR_VALIDATION"))
                .andExpect(jsonPath("$.message").value("Dữ liệu không hợp lệ"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void checkoutSuccessThenCancelRestoresStock() throws Exception {
        long categoryId = createCategory("Gốm thử");
        long productId = createProduct(categoryId, "Bình thử", 100000, 5);
        Session customer = registerAndLogin("lan.flow@atelier.vn", "0901111111");

        mvc.perform(post("/api/v1/customers/" + customer.id() + "/cart/items")
                        .header("Authorization", customer.header())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.totalAmount").value(200000));

        MvcResult created = mvc.perform(post("/api/v1/orders")
                        .header("Authorization", customer.header())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":" + customer.id() + ",\"note\":\"Giao giờ hành chính\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.code", containsString("DH")))
                .andExpect(jsonPath("$.totalAmount").value(200000))
                .andReturn();
        long orderId = ((Number) JsonPath.read(created.getResponse().getContentAsString(), "$.id")).longValue();

        mvc.perform(get("/api/v1/products/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(3));
        mvc.perform(get("/api/v1/customers/" + customer.id() + "/cart")
                        .header("Authorization", customer.header()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));

        mvc.perform(patch("/api/v1/orders/" + orderId)
                        .header("Authorization", admin.header())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHIPPED\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR_INVALID_STATUS"));

        mvc.perform(patch("/api/v1/orders/" + orderId)
                        .header("Authorization", admin.header())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(get("/api/v1/products/" + productId))
                .andExpect(jsonPath("$.stockQuantity").value(5));
    }

    @Test
    void checkoutFailsWhenStockIsEmpty() throws Exception {
        long categoryId = createCategory("Đèn thử");
        long productId = createProduct(categoryId, "Đèn thử", 50000, 1);
        Session customer = registerAndLogin("kho.flow@atelier.vn", "0987654321");

        mvc.perform(post("/api/v1/customers/" + customer.id() + "/cart/items")
                        .header("Authorization", customer.header())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"quantity\":2}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/v1/orders")
                        .header("Authorization", customer.header())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"customerId\":" + customer.id() + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR_STOCK_EMPTY"))
                .andExpect(jsonPath("$.message").value("Sản phẩm đã hết hàng"))
                .andExpect(jsonPath("$.details").isArray());

        mvc.perform(get("/api/v1/products/" + productId))
                .andExpect(jsonPath("$.stockQuantity").value(1));
        mvc.perform(get("/api/v1/customers/" + customer.id() + "/orders")
                        .header("Authorization", customer.header()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    private long createCategory(String name) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/categories")
                        .header("Authorization", admin.header())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"description\":\"Mô tả\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private long createProduct(long categoryId, String name, int price, int stock) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/products")
                        .header("Authorization", admin.header())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","price":%d,"stockQuantity":%d,"categoryId":%d,"description":"Hàng thử","imageUrl":"https://example.com/p.jpg"}
                                """.formatted(name, price, stock, categoryId)))
                .andExpect(status().isCreated())
                .andReturn();
        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private Session registerAndLogin(String email, String phone) throws Exception {
        mvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Nguyễn Lan","email":"%s","phone":"%s","password":"secret1","address":"Huế"}
                                """.formatted(email, phone)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        return login(email, "secret1");
    }

    private Session login(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        String token = JsonPath.read(body, "$.token");
        long id = ((Number) JsonPath.read(body, "$.customer.id")).longValue();
        return new Session(token, id);
    }

    private record Session(String token, long id) {
        String header() {
            return "Bearer " + token;
        }
    }
}
