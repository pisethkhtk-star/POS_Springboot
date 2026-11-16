package com.dinsaren.springbootjwtapi.startup;

import com.dinsaren.springbootjwtapi.constants.Constants;
import com.dinsaren.springbootjwtapi.models.Role;
import com.dinsaren.springbootjwtapi.models.User;
import com.dinsaren.springbootjwtapi.models.UserRole;
import com.dinsaren.springbootjwtapi.models.pos.*;
import com.dinsaren.springbootjwtapi.repository.RoleRepository;
import com.dinsaren.springbootjwtapi.repository.UserRepository;
import com.dinsaren.springbootjwtapi.repository.pos.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final IngredientRepository ingredientRepository;
    private final PosProductRepository productRepository;
    private final ProductIngredientRepository productIngredientRepository;
    private final PosCustomerRepository customerRepository;
    private final SupplierRepository supplierRepository;
    private final DiscountRepository discountRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Checking and seeding default POS data...");
        seedAll();
        log.info("POS Data Seeding completed successfully.");
    }

    @Transactional
    public Map<String, Object> seedAll() {
        Map<String, Object> summary = new LinkedHashMap<>();

        summary.put("users", seedAdminUser());
        summary.put("permissions", seedPermissions());
        summary.put("categories", seedCategories());
        summary.put("suppliers", seedSuppliers());
        summary.put("ingredients", seedIngredients());
        summary.put("products", seedProductsWithRecipes());
        summary.put("customers", seedCustomers());
        summary.put("discounts", seedDiscounts());

        return summary;
    }

    private String seedAdminUser() {
        Role adminRole = roleRepository.findByName(UserRole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(UserRole.ROLE_ADMIN)));

        roleRepository.findByName(UserRole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(UserRole.ROLE_USER)));

        Role cashierRole = roleRepository.findByName(UserRole.ROLE_CASHIER)
                .orElseGet(() -> roleRepository.save(new Role(UserRole.ROLE_CASHIER)));

        // 1. Admin System user (admin / Admin@1234)
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User("admin", "admin@admin.com", passwordEncoder.encode("Admin@1234"), "00000000000");
            admin.setFirstName("Admin");
            admin.setLastName("System");
            admin.setStatus(Constants.STATUS_ACTIVE);
            admin.setChangePassword(Constants.No);
            admin.setRoles(new HashSet<>(Set.of(adminRole)));
            userRepository.save(admin);
            log.info("Admin system user seeded.");
        }

        // 2. POS Admin user (admin@pos.com / Admin@123)
        String posAdminEmail = "admin@pos.com";
        if (!userRepository.existsByEmail(posAdminEmail)) {
            User posAdmin = new User(posAdminEmail, posAdminEmail, passwordEncoder.encode("Admin@123"), "012345678");
            posAdmin.setFirstName("Admin");
            posAdmin.setLastName("User");
            posAdmin.setStatus(Constants.STATUS_ACTIVE);
            posAdmin.setChangePassword(Constants.No);
            posAdmin.setRoles(new HashSet<>(Set.of(adminRole)));
            userRepository.save(posAdmin);
            log.info("POS Admin user seeded (admin@pos.com / Admin@123).");
        }

        // 3. Cashier user (cashier@pos.com / Cashier@123)
        String cashierEmail = "cashier@pos.com";
        if (!userRepository.existsByEmail(cashierEmail)) {
            User cashier = new User(cashierEmail, cashierEmail, passwordEncoder.encode("Cashier@123"), "098765432");
            cashier.setFirstName("Sophea");
            cashier.setLastName("Cashier");
            cashier.setStatus(Constants.STATUS_ACTIVE);
            cashier.setChangePassword(Constants.No);
            cashier.setRoles(new HashSet<>(Set.of(cashierRole)));
            userRepository.save(cashier);
            log.info("POS Cashier user seeded (cashier@pos.com / Cashier@123).");
        }

        return "OK";
    }

    private String seedPermissions() {
        List<String> adminPages = List.of(
                "/", "/pos", "/products", "/categories", "/ingredients",
                "/discounts", "/inventory", "/purchases", "/suppliers",
                "/customers", "/orders", "/reports", "/users", "/settings", "/roles"
        );
        List<String> managerPages = List.of(
                "/", "/pos", "/products", "/categories", "/ingredients",
                "/discounts", "/inventory", "/purchases", "/suppliers",
                "/customers", "/orders"
        );
        List<String> cashierPages = List.of("/pos");

        if (rolePermissionRepository.findByRole("ADMIN").isEmpty()) {
            rolePermissionRepository.save(new RolePermission("ADMIN", adminPages));
        }
        if (rolePermissionRepository.findByRole("MANAGER").isEmpty()) {
            rolePermissionRepository.save(new RolePermission("MANAGER", managerPages));
        }
        if (rolePermissionRepository.findByRole("CASHIER").isEmpty()) {
            rolePermissionRepository.save(new RolePermission("CASHIER", cashierPages));
        }
        return "OK";
    }

    private String seedCategories() {
        Map<String, String> defaultCats = Map.of(
                "Beverages", "Coffee",
                "Snacks", "Cookie",
                "Main Course", "Utensils",
                "Desserts", "IceCream",
                "Merchandise", "Shirt"
        );

        defaultCats.forEach((name, icon) -> {
            if (categoryRepository.findByNameIgnoreCase(name).isEmpty()) {
                categoryRepository.save(new Category(name, icon));
            }
        });
        return "Total categories: " + categoryRepository.count();
    }

    private String seedSuppliers() {
        if (supplierRepository.count() == 0) {
            supplierRepository.saveAll(List.of(
                    new Supplier("Angkor Coffee Supply", "Mr. Visal", "012888999", "visal@angkorcoffee.com", "St. 271, Phnom Penh"),
                    new Supplier("Khmer Dairy Farm", "Mrs. Chantrea", "015222333", "chantrea@khmerdairy.com", "Takhmao, Kandal"),
                    new Supplier("Phnom Penh Bakery Depot", "Mr. Rithy", "098444555", "rithy@ppbakery.com", "Russian Blvd, Phnom Penh"),
                    new Supplier("Eco Cup & Packaging", "Ms. Sreypov", "077666777", "sreypov@ecopack.com", "St. 2004, Phnom Penh")
            ));
        }
        return "Total suppliers: " + supplierRepository.count();
    }

    private String seedIngredients() {
        saveIngredientIfMissing("Espresso Beans", "ING-001", "kg", new BigDecimal("25.00"), new BigDecimal("5.00"), new BigDecimal("12.00"));
        saveIngredientIfMissing("Fresh Milk", "ING-002", "liter", new BigDecimal("40.00"), new BigDecimal("10.00"), new BigDecimal("2.20"));
        saveIngredientIfMissing("Condensed Milk", "ING-003", "can", new BigDecimal("50.00"), new BigDecimal("10.00"), new BigDecimal("1.10"));
        saveIngredientIfMissing("Matcha Powder", "ING-004", "kg", new BigDecimal("8.00"), new BigDecimal("2.00"), new BigDecimal("28.00"));
        saveIngredientIfMissing("Cane Sugar Syrup", "ING-005", "liter", new BigDecimal("30.00"), new BigDecimal("5.00"), new BigDecimal("1.50"));
        saveIngredientIfMissing("Chocolate Syrup", "ING-006", "liter", new BigDecimal("15.00"), new BigDecimal("4.00"), new BigDecimal("4.50"));
        saveIngredientIfMissing("Paper Cups 16oz", "ING-007", "pcs", new BigDecimal("500.00"), new BigDecimal("100.00"), new BigDecimal("0.08"));
        return "Total ingredients: " + ingredientRepository.count();
    }

    private Ingredient saveIngredientIfMissing(String name, String code, String unit, BigDecimal stock, BigDecimal minStock, BigDecimal cost) {
        return ingredientRepository.findByCode(code).orElseGet(() ->
                ingredientRepository.save(new Ingredient(name, code, unit, stock, minStock, cost))
        );
    }

    private String seedProductsWithRecipes() {
        Category beverages = categoryRepository.findByNameIgnoreCase("Beverages").orElse(null);
        Category snacks = categoryRepository.findByNameIgnoreCase("Snacks").orElse(null);
        Category mainCourse = categoryRepository.findByNameIgnoreCase("Main Course").orElse(null);
        Category desserts = categoryRepository.findByNameIgnoreCase("Desserts").orElse(null);

        Integer bevId = beverages != null ? beverages.getId() : 1;
        Integer snackId = snacks != null ? snacks.getId() : 2;
        Integer mainId = mainCourse != null ? mainCourse.getId() : 3;
        Integer desId = desserts != null ? desserts.getId() : 4;

        Ingredient beans = ingredientRepository.findByCode("ING-001").orElse(null);
        Ingredient milk = ingredientRepository.findByCode("ING-002").orElse(null);
        Ingredient matcha = ingredientRepository.findByCode("ING-004").orElse(null);
        Ingredient sugar = ingredientRepository.findByCode("ING-005").orElse(null);
        Ingredient chocolate = ingredientRepository.findByCode("ING-006").orElse(null);
        Ingredient cups = ingredientRepository.findByCode("ING-007").orElse(null);

        // 1. Iced Latte (Recipe)
        if (productRepository.findByCode("10000000000001").isEmpty()) {
            PosProduct latte = productRepository.save(new PosProduct(
                    "Iced Latte", "10000000000001", new BigDecimal("2.75"), new BigDecimal("0.65"),
                    0, 5, true, "https://images.unsplash.com/photo-1517701604599-bb29b565090c?w=400", bevId
            ));
            if (beans != null) productIngredientRepository.save(new ProductIngredient(latte, beans, new BigDecimal("0.02")));
            if (milk != null) productIngredientRepository.save(new ProductIngredient(latte, milk, new BigDecimal("0.15")));
            if (cups != null) productIngredientRepository.save(new ProductIngredient(latte, cups, new BigDecimal("1.00")));
        }

        // 2. Iced Americano (Recipe)
        if (productRepository.findByCode("10000000000002").isEmpty()) {
            PosProduct americano = productRepository.save(new PosProduct(
                    "Iced Americano", "10000000000002", new BigDecimal("2.00"), new BigDecimal("0.32"),
                    0, 5, true, "https://images.unsplash.com/photo-1551030173-122aabc4489c?w=400", bevId
            ));
            if (beans != null) productIngredientRepository.save(new ProductIngredient(americano, beans, new BigDecimal("0.02")));
            if (cups != null) productIngredientRepository.save(new ProductIngredient(americano, cups, new BigDecimal("1.00")));
        }

        // 3. Iced Matcha Latte (Recipe)
        if (productRepository.findByCode("10000000000003").isEmpty()) {
            PosProduct matchaLatte = productRepository.save(new PosProduct(
                    "Iced Matcha Latte", "10000000000003", new BigDecimal("2.80"), new BigDecimal("0.78"),
                    0, 5, true, "https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=400", bevId
            ));
            if (matcha != null) productIngredientRepository.save(new ProductIngredient(matchaLatte, matcha, new BigDecimal("0.015")));
            if (milk != null) productIngredientRepository.save(new ProductIngredient(matchaLatte, milk, new BigDecimal("0.15")));
            if (sugar != null) productIngredientRepository.save(new ProductIngredient(matchaLatte, sugar, new BigDecimal("0.02")));
            if (cups != null) productIngredientRepository.save(new ProductIngredient(matchaLatte, cups, new BigDecimal("1.00")));
        }

        // 4. Iced Mocha (Recipe)
        if (productRepository.findByCode("10000000000004").isEmpty()) {
            PosProduct mocha = productRepository.save(new PosProduct(
                    "Iced Mocha", "10000000000004", new BigDecimal("3.25"), new BigDecimal("0.85"),
                    0, 5, true, "https://images.unsplash.com/photo-1578314675249-a6910f80cc4e?w=400", bevId
            ));
            if (beans != null) productIngredientRepository.save(new ProductIngredient(mocha, beans, new BigDecimal("0.02")));
            if (milk != null) productIngredientRepository.save(new ProductIngredient(mocha, milk, new BigDecimal("0.12")));
            if (chocolate != null) productIngredientRepository.save(new ProductIngredient(mocha, chocolate, new BigDecimal("0.03")));
            if (cups != null) productIngredientRepository.save(new ProductIngredient(mocha, cups, new BigDecimal("1.00")));
        }

        // 5. Standalone snacks & desserts
        if (productRepository.findByCode("10000000000005").isEmpty()) {
            productRepository.save(new PosProduct("Chocolate Chip Cookie", "10000000000005", new BigDecimal("1.50"), new BigDecimal("0.60"), 35, 5, false, "https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=400", snackId));
        }
        if (productRepository.findByCode("10000000000006").isEmpty()) {
            productRepository.save(new PosProduct("Butter Croissant", "10000000000006", new BigDecimal("2.00"), new BigDecimal("0.90"), 24, 5, false, "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=400", snackId));
        }
        if (productRepository.findByCode("10000000000007").isEmpty()) {
            productRepository.save(new PosProduct("Club Sandwich", "10000000000007", new BigDecimal("4.50"), new BigDecimal("2.10"), 15, 3, false, "https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400", mainId));
        }
        if (productRepository.findByCode("10000000000008").isEmpty()) {
            productRepository.save(new PosProduct("Vanilla Soft Ice Cream", "10000000000008", new BigDecimal("1.75"), new BigDecimal("0.70"), 40, 10, false, "https://images.unsplash.com/photo-1497034825429-c343d7c6a68f?w=400", desId));
        }

        return "Total products: " + productRepository.count();
    }

    private String seedCustomers() {
        if (customerRepository.count() == 0) {
            customerRepository.saveAll(List.of(
                    new PosCustomer("Chan Sokha", "012345678", "sokha@gmail.com", "Toul Kork, Phnom Penh", 120),
                    new PosCustomer("Keo Bopha", "098765432", "bopha@gmail.com", "BKK1, Phnom Penh", 85),
                    new PosCustomer("Vong Dara", "077112233", "dara@gmail.com", "Daun Penh, Phnom Penh", 210),
                    new PosCustomer("Meas Sreyneath", "088998877", "sreyneath@gmail.com", "Sen Sok, Phnom Penh", 45)
            ));
        }
        return "Total customers: " + customerRepository.count();
    }

    private String seedDiscounts() {
        if (discountRepository.count() == 0) {
            discountRepository.saveAll(List.of(
                    new Discount(null, "Member Welcome 10%", "PERCENTAGE", new BigDecimal("10.00"), BigDecimal.ZERO, LocalDate.now().minusDays(10), LocalDate.now().plusMonths(6), true),
                    new Discount(null, "Big Order $2 Off", "FIXED", new BigDecimal("2.00"), new BigDecimal("15.00"), LocalDate.now().minusDays(10), LocalDate.now().plusMonths(6), true),
                    new Discount(null, "Happy Hour 15%", "PERCENTAGE", new BigDecimal("15.00"), new BigDecimal("5.00"), LocalDate.now().minusDays(5), LocalDate.now().plusMonths(3), true)
            ));
        }
        return "Total discounts: " + discountRepository.count();
    }
}
