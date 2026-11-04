package com.dinsaren.springbootjwtapi.startup;

import com.dinsaren.springbootjwtapi.constants.Constants;
import com.dinsaren.springbootjwtapi.models.Role;
import com.dinsaren.springbootjwtapi.models.User;
import com.dinsaren.springbootjwtapi.models.UserRole;
import com.dinsaren.springbootjwtapi.models.product.Product;
import com.dinsaren.springbootjwtapi.models.product.ProductCategory;
import com.dinsaren.springbootjwtapi.repository.ProductCategoryRepository;
import com.dinsaren.springbootjwtapi.repository.ProductRepository;
import com.dinsaren.springbootjwtapi.repository.RoleRepository;
import com.dinsaren.springbootjwtapi.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;

    public DataSeeder(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            ProductCategoryRepository productCategoryRepository,
            ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.productCategoryRepository = productCategoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedAdminUser();
        seedProducts();
    }

    private void seedAdminUser() {
        Role adminRole = roleRepository.findByName(UserRole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(UserRole.ROLE_ADMIN)));

        roleRepository.findByName(UserRole.ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(UserRole.ROLE_USER)));

        Optional<User> existing = userRepository.findByUsername("admin");
        if (existing.isPresent()) {
            log.info("Admin user already exists, skipping seed.");
            return;
        }

        User admin = new User("admin", "admin@admin.com", passwordEncoder.encode("Admin@1234"), "00000000000");
        admin.setFirstName("Admin");
        admin.setLastName("Admin");
        admin.setStatus(Constants.STATUS_ACTIVE);
        admin.setChangePassword(Constants.No);

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        admin.setRoles(roles);

        userRepository.save(admin);
        log.info("Default admin user seeded successfully.");
    }

    private void seedProducts() {
        if (productRepository.count() > 0) {
            log.info("Products already seeded, skipping.");
            return;
        }

        ProductCategory catGadgets = productCategoryRepository.save(new ProductCategory(0, "Electronics & Gadgets"));
        ProductCategory catLaptops = productCategoryRepository.save(new ProductCategory(0, "Computers & Laptops"));
        ProductCategory catPhones = productCategoryRepository.save(new ProductCategory(0, "Smartphones & Tablets"));
        ProductCategory catAudio = productCategoryRepository.save(new ProductCategory(0, "Audio & Sound"));
        ProductCategory catAccessories = productCategoryRepository.save(new ProductCategory(0, "Accessories"));

        productRepository.save(new Product(0, "iPhone 16 Pro Max", "PRD-IP16PM", catPhones, 1199.00, 950.00, 25, "256GB Desert Titanium with A18 Pro chip", Constants.STATUS_ACTIVE));
        productRepository.save(new Product(0, "MacBook Pro 14\" M4", "PRD-MBP14M4", catLaptops, 1599.00, 1250.00, 15, "Space Black, 16GB Unified Memory, 512GB SSD", Constants.STATUS_ACTIVE));
        productRepository.save(new Product(0, "Sony WH-1000XM5", "PRD-SONY-XM5", catAudio, 349.99, 240.00, 40, "Wireless Noise-Canceling Headphones with 30-hour battery", Constants.STATUS_ACTIVE));
        productRepository.save(new Product(0, "iPad Air 11\" M2", "PRD-IPADAIR", catPhones, 599.00, 450.00, 30, "Starlight 128GB Liquid Retina display", Constants.STATUS_ACTIVE));
        productRepository.save(new Product(0, "Dell XPS 15 OLED", "PRD-DELL-XPS", catLaptops, 1899.00, 1450.00, 8, "Intel Core Ultra 7, 32GB RAM, 1TB SSD, 3.5K OLED Touch", Constants.STATUS_ACTIVE));
        productRepository.save(new Product(0, "Samsung Galaxy S25 Ultra", "PRD-S25ULTRA", catPhones, 1299.99, 980.00, 20, "Titanium Gray 512GB with S Pen and Galaxy AI", Constants.STATUS_ACTIVE));
        productRepository.save(new Product(0, "AirPods Pro (2nd Gen)", "PRD-APP2-USBC", catAudio, 249.00, 170.00, 65, "Active Noise Cancellation with USB-C MagSafe Case", Constants.STATUS_ACTIVE));
        productRepository.save(new Product(0, "Logitech MX Master 3S", "PRD-MXM3S", catAccessories, 99.99, 65.00, 50, "Performance Wireless Mouse with Quiet Clicks", Constants.STATUS_ACTIVE));
        productRepository.save(new Product(0, "Apple Watch Series 10", "PRD-AWS10", catGadgets, 399.00, 290.00, 3, "Jet Black Aluminum 46mm GPS", Constants.STATUS_ACTIVE));
        productRepository.save(new Product(0, "Anker 737 Power Bank 24000mAh", "PRD-ANKER737", catAccessories, 119.99, 75.00, 0, "140W Fast Charging 3-Port Portable Charger", Constants.STATUS_ACTIVE));
        productRepository.save(new Product(0, "Keychron K2 V2 Wireless", "PRD-KEY-K2", catAccessories, 89.99, 55.00, 28, "75% Compact Bluetooth Mechanical Keyboard with Gateron Brown", Constants.STATUS_ACTIVE));
        productRepository.save(new Product(0, "LG UltraFine 27\" 4K Monitor", "PRD-LG-274K", catGadgets, 449.00, 320.00, 12, "IPS Display with USB-C 60W Power Delivery and HDR400", Constants.STATUS_ACTIVE));

        log.info("Products seeded successfully.");
    }
}
