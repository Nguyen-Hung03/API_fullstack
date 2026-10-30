package com.atelier.seed;

import com.atelier.api.model.CartItemWriteDto;
import com.atelier.api.model.CheckoutRequestDto;
import com.atelier.domain.Cart;
import com.atelier.domain.Category;
import com.atelier.domain.Customer;
import com.atelier.domain.Product;
import com.atelier.domain.Role;
import com.atelier.repo.CartRepository;
import com.atelier.repo.CategoryRepository;
import com.atelier.repo.CustomerRepository;
import com.atelier.repo.ProductRepository;
import com.atelier.service.CartService;
import com.atelier.service.OrderService;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

@Component
@ConditionalOnProperty(prefix = "atelier.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final String CUSTOMER_PASSWORD = "customer123";

    private final CategoryRepository categories;
    private final ProductRepository products;
    private final CustomerRepository customers;
    private final CartRepository carts;
    private final CartService cartService;
    private final OrderService orders;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CategoryRepository categories, ProductRepository products, CustomerRepository customers,
                      CartRepository carts, CartService cartService, OrderService orders,
                      PasswordEncoder passwordEncoder) {
        this.categories = categories;
        this.products = products;
        this.customers = customers;
        this.carts = carts;
        this.cartService = cartService;
        this.orders = orders;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categories.count() > 0) {
            return;
        }
        Faker faker = new Faker(new Locale("vi"), new Random(20261022));
        Map<String, Category> catalog = seedCategories();
        Product first = seedProducts(catalog);
        Customer lan = seedCustomers(faker);
        seedSampleOrder(lan, first);
        log.info("Đã nạp dữ liệu mẫu. Quản trị: admin@atelier.vn / admin123. Khách: lan.nguyen@atelier.vn / {}",
                CUSTOMER_PASSWORD);
    }

    private Map<String, Category> seedCategories() {
        List<SeedCategory> seeds = List.of(
                new SeedCategory("Gốm sứ", "Men tự nhiên, nung thủ công."),
                new SeedCategory("Vải và chăn", "Lanh, cotton và đồ trải cho nhà."),
                new SeedCategory("Đèn", "Đèn giấy, đèn mây, nến."),
                new SeedCategory("Trà đạo", "Ấm, chén và trà ướp."),
                new SeedCategory("Gỗ nhà", "Khay, kệ và thớt gỗ.")
        );
        Map<String, Category> saved = new LinkedHashMap<>();
        for (SeedCategory seed : seeds) {
            Category category = new Category();
            category.setName(seed.name());
            category.setDescription(seed.description());
            saved.put(seed.name(), categories.save(category));
        }
        return saved;
    }

    private Product seedProducts(Map<String, Category> catalog) {
        List<SeedProduct> seeds = List.of(
                new SeedProduct("Gốm sứ", "Bình gốm men rạn", 450000, 12, "binh", "Men rạn, miệng loe."),
                new SeedProduct("Gốm sứ", "Chén cơm men xanh", 89000, 40, "chen", "Men xanh đồng, lòng nông."),
                new SeedProduct("Gốm sứ", "Đĩa bày men nâu", 220000, 15, "dia", "Dùng bày trái cây hoặc bánh."),
                new SeedProduct("Gốm sứ", "Lọ hoa cổ cao", 680000, 8, "lo", "Cổ thon, hợp cành dài."),
                new SeedProduct("Vải và chăn", "Khăn trải lanh", 320000, 20, "khan", "Lanh dày, màu cát."),
                new SeedProduct("Vải và chăn", "Gối ôm kẻ sọc", 180000, 25, "goi", "Vỏ tháo được."),
                new SeedProduct("Vải và chăn", "Chăn cotton mỏng", 890000, 10, "chan", "Mùa nóng, giặt máy được."),
                new SeedProduct("Vải và chăn", "Tấm trải bàn", 250000, 18, "ban", "Khổ vuông cho bàn bốn người."),
                new SeedProduct("Đèn", "Đèn bàn giấy", 540000, 9, "denban", "Khung gỗ, chụp giấy."),
                new SeedProduct("Đèn", "Đèn thả mây", 1200000, 6, "denmay", "Đan mây, bóng led ấm."),
                new SeedProduct("Đèn", "Nến thơm vỏ gỗ", 150000, 30, "nen", "Mùi gỗ đàn và cam."),
                new SeedProduct("Trà đạo", "Ấm tử sa", 980000, 7, "am", "Dung tích nhỏ, giữ nhiệt."),
                new SeedProduct("Trà đạo", "Bộ chén trà", 420000, 14, "bochen", "Sáu chén, men ngà."),
                new SeedProduct("Trà đạo", "Hộp trà sen", 260000, 22, "tra", "Ướp sen hồ Tây."),
                new SeedProduct("Gỗ nhà", "Khay gỗ óc chó", 510000, 11, "khay", "Góc bo tròn."),
                new SeedProduct("Gỗ nhà", "Kệ sách thấp", 2450000, 4, "ke", "Hai tầng, để sát tường."),
                new SeedProduct("Gỗ nhà", "Thớt gỗ nghiến", 190000, 16, "thot", "Dày, không cong vênh.")
        );
        Product first = null;
        for (SeedProduct seed : seeds) {
            Product product = new Product();
            product.setName(seed.name());
            product.setDescription(seed.blurb());
            product.setPrice(BigDecimal.valueOf(seed.price()));
            product.setStockQuantity(seed.stock());
            product.setImageUrl("https://picsum.photos/seed/atelier-" + seed.imageSeed() + "/800/800");
            product.setCategory(catalog.get(seed.category()));
            product.setCreatedAt(Instant.now());
            product = products.save(product);
            if (first == null) {
                first = product;
            }
        }
        return first;
    }

    private Customer seedCustomers(Faker faker) {
        String[] cities = {"Huế", "Hội An", "Đà Lạt", "Hà Nội", "Sa Đéc"};
        saveCustomer("Quản trị Atelier", "admin@atelier.vn", "0900000001", "admin123",
                "92 Nguyễn Huệ, Huế", Role.ADMIN);
        Customer lan = saveCustomer("Nguyễn Thị Lan", "lan.nguyen@atelier.vn", "0901234567", CUSTOMER_PASSWORD,
                "15 Lê Lợi, Huế", Role.CUSTOMER);
        saveCustomer("Trần Minh Khoa", "minh.tran@atelier.vn", "0912345678", CUSTOMER_PASSWORD,
                "8 Trần Phú, Hội An", Role.CUSTOMER);
        for (int i = 0; i < 4; i++) {
            String local = "khach" + faker.number().digits(6) + i;
            String city = cities[faker.random().nextInt(cities.length)];
            saveCustomer(
                    faker.name().fullName(),
                    local + "@atelier.vn",
                    phone(faker),
                    CUSTOMER_PASSWORD,
                    faker.address().streetAddress() + ", " + city,
                    Role.CUSTOMER);
        }
        return lan;
    }

    private void seedSampleOrder(Customer lan, Product product) {
        CartItemWriteDto item = new CartItemWriteDto();
        item.setProductId(product.getId());
        item.setQuantity(1);
        cartService.add(lan.getId(), item);

        CheckoutRequestDto checkout = new CheckoutRequestDto();
        checkout.setCustomerId(lan.getId());
        checkout.setNote("Đơn mẫu khi khởi tạo dữ liệu");
        orders.checkout(checkout);
    }

    private Customer saveCustomer(String name, String email, String phone, String password, String address, Role role) {
        Customer customer = new Customer();
        customer.setFullName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setPasswordHash(passwordEncoder.encode(password));
        customer.setAddress(address);
        customer.setRole(role);
        customer.setCreatedAt(Instant.now());
        Customer saved = customers.save(customer);
        Cart cart = new Cart();
        cart.setCustomer(saved);
        carts.save(cart);
        return saved;
    }

    private String phone(Faker faker) {
        int[] prefixes = {3, 5, 7, 8, 9};
        int prefix = prefixes[faker.random().nextInt(prefixes.length)];
        return "0" + prefix + String.format("%08d", faker.number().numberBetween(0, 100_000_000));
    }

    private record SeedCategory(String name, String description) {
    }

    private record SeedProduct(String category, String name, long price, int stock, String imageSeed, String blurb) {
    }
}
