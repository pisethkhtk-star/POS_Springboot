-- ==========================================================
-- POS SAMPLE SEED DATA SCRIPT (PostgreSQL)
-- Database: pos_db
-- ==========================================================

-- 1. ROLES
INSERT INTO roles (name) VALUES 
('ROLE_ADMIN'),
('ROLE_USER'),
('ROLE_CASHIER')
('ROLE_MANAGER'),
('ROLE_CASHIER'),
('ROLE_USER')
ON CONFLICT (name) DO NOTHING;

-- 2. USERS (Password hashes for bcrypt)
-- admin / Admin@1234: $2a$10$wN9aNfC... or standard bcrypt
-- admin@pos.com / Admin@123
-- cashier@pos.com / Cashier@123
INSERT INTO users (username, email, password, phone_number, first_name, last_name, status, change_password, created_at, updated_at)
VALUES 
('admin@pos.com', 'admin@pos.com', '$2a$10$7Zhy1b.XUe2R1LzV7H8p..jOa4p72H6u5F1K3tXjQJj0sQ9Z4n4eK', '012345678', 'Admin', 'User', 'ACTIVE', 'No', NOW(), NOW()),
('cashier@pos.com', 'cashier@pos.com', '$2a$10$7Zhy1b.XUe2R1LzV7H8p..jOa4p72H6u5F1K3tXjQJj0sQ9Z4n4eK', '098765432', 'Sophea', 'Cashier', 'ACTIVE', 'No', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Map Admin to ROLE_ADMIN
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'admin@pos.com' AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- Map Cashier to ROLE_CASHIER
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.email = 'cashier@pos.com' AND r.name = 'ROLE_CASHIER'
ON CONFLICT DO NOTHING;

-- 3. CATEGORIES
INSERT INTO categories (name, icon, created_at, updated_at) VALUES
('Beverages', 'Coffee', NOW(), NOW()),
('Snacks', 'Cookie', NOW(), NOW()),
('Main Course', 'Utensils', NOW(), NOW()),
('Desserts', 'IceCream', NOW(), NOW()),
('Merchandise', 'Shirt', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- 4. SUPPLIERS
INSERT INTO suppliers (name, contact, phone, email, address, created_at, updated_at) VALUES
('Angkor Coffee Supply', 'Mr. Visal', '012888999', 'visal@angkorcoffee.com', 'St. 271, Phnom Penh', NOW(), NOW()),
('Khmer Dairy Farm', 'Mrs. Chantrea', '015222333', 'chantrea@khmerdairy.com', 'Takhmao, Kandal', NOW(), NOW()),
('Phnom Penh Bakery Depot', 'Mr. Rithy', '098444555', 'rithy@ppbakery.com', 'Russian Blvd, Phnom Penh', NOW(), NOW()),
('Eco Cup & Packaging', 'Ms. Sreypov', '077666777', 'sreypov@ecopack.com', 'St. 2004, Phnom Penh', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- 5. INGREDIENTS
INSERT INTO ingredients (name, code, unit, stock, min_stock, cost, created_at, updated_at) VALUES
('Espresso Beans', 'ING-001', 'kg', 25.00, 5.00, 12.00, NOW(), NOW()),
('Fresh Milk', 'ING-002', 'liter', 40.00, 10.00, 2.20, NOW(), NOW()),
('Condensed Milk', 'ING-003', 'can', 50.00, 10.00, 1.10, NOW(), NOW()),
('Matcha Powder', 'ING-004', 'kg', 8.00, 2.00, 28.00, NOW(), NOW()),
('Cane Sugar Syrup', 'ING-005', 'liter', 30.00, 5.00, 1.50, NOW(), NOW()),
('Chocolate Syrup', 'ING-006', 'liter', 15.00, 4.00, 4.50, NOW(), NOW()),
('Paper Cups 16oz', 'ING-007', 'pcs', 500.00, 100.00, 0.08, NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

-- 6. PRODUCTS
INSERT INTO products (name, code, price, cost, stock, min_stock, has_recipe, image, category_id, created_at, updated_at)
SELECT 'Iced Latte', '10000000000001', 2.75, 0.65, 0, 5, true, 'https://images.unsplash.com/photo-1517701604599-bb29b565090c?w=400', c.id, NOW(), NOW()
FROM categories c WHERE c.name = 'Beverages'
ON CONFLICT (code) DO NOTHING;

INSERT INTO products (name, code, price, cost, stock, min_stock, has_recipe, image, category_id, created_at, updated_at)
SELECT 'Iced Americano', '10000000000002', 2.00, 0.32, 0, 5, true, 'https://images.unsplash.com/photo-1551030173-122aabc4489c?w=400', c.id, NOW(), NOW()
FROM categories c WHERE c.name = 'Beverages'
ON CONFLICT (code) DO NOTHING;

INSERT INTO products (name, code, price, cost, stock, min_stock, has_recipe, image, category_id, created_at, updated_at)
SELECT 'Iced Matcha Latte', '10000000000003', 2.80, 0.78, 0, 5, true, 'https://images.unsplash.com/photo-1536256263959-770b48d82b0a?w=400', c.id, NOW(), NOW()
FROM categories c WHERE c.name = 'Beverages'
ON CONFLICT (code) DO NOTHING;

INSERT INTO products (name, code, price, cost, stock, min_stock, has_recipe, image, category_id, created_at, updated_at)
SELECT 'Iced Mocha', '10000000000004', 3.25, 0.85, 0, 5, true, 'https://images.unsplash.com/photo-1578314675249-a6910f80cc4e?w=400', c.id, NOW(), NOW()
FROM categories c WHERE c.name = 'Beverages'
ON CONFLICT (code) DO NOTHING;

INSERT INTO products (name, code, price, cost, stock, min_stock, has_recipe, image, category_id, created_at, updated_at)
SELECT 'Chocolate Chip Cookie', '10000000000005', 1.50, 0.60, 35, 5, false, 'https://images.unsplash.com/photo-1499636136210-6f4ee915583e?w=400', c.id, NOW(), NOW()
FROM categories c WHERE c.name = 'Snacks'
ON CONFLICT (code) DO NOTHING;

INSERT INTO products (name, code, price, cost, stock, min_stock, has_recipe, image, category_id, created_at, updated_at)
SELECT 'Butter Croissant', '10000000000006', 2.00, 0.90, 24, 5, false, 'https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=400', c.id, NOW(), NOW()
FROM categories c WHERE c.name = 'Snacks'
ON CONFLICT (code) DO NOTHING;

INSERT INTO products (name, code, price, cost, stock, min_stock, has_recipe, image, category_id, created_at, updated_at)
SELECT 'Club Sandwich', '10000000000007', 4.50, 2.10, 15, 3, false, 'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400', c.id, NOW(), NOW()
FROM categories c WHERE c.name = 'Main Course'
ON CONFLICT (code) DO NOTHING;

INSERT INTO products (name, code, price, cost, stock, min_stock, has_recipe, image, category_id, created_at, updated_at)
SELECT 'Vanilla Soft Ice Cream', '10000000000008', 1.75, 0.70, 40, 10, false, 'https://images.unsplash.com/photo-1497034825429-c343d7c6a68f?w=400', c.id, NOW(), NOW()
FROM categories c WHERE c.name = 'Desserts'
ON CONFLICT (code) DO NOTHING;

-- 7. PRODUCT INGREDIENTS (RECIPES)
-- Iced Latte Recipe
INSERT INTO product_ingredients (product_id, ingredient_id, quantity, created_at, updated_at)
SELECT p.id, i.id, 0.02, NOW(), NOW() FROM products p, ingredients i WHERE p.code = '10000000000001' AND i.code = 'ING-001'
ON CONFLICT DO NOTHING;
INSERT INTO product_ingredients (product_id, ingredient_id, quantity, created_at, updated_at)
SELECT p.id, i.id, 0.15, NOW(), NOW() FROM products p, ingredients i WHERE p.code = '10000000000001' AND i.code = 'ING-002'
ON CONFLICT DO NOTHING;
INSERT INTO product_ingredients (product_id, ingredient_id, quantity, created_at, updated_at)
SELECT p.id, i.id, 1.00, NOW(), NOW() FROM products p, ingredients i WHERE p.code = '10000000000001' AND i.code = 'ING-007'
ON CONFLICT DO NOTHING;

-- Iced Americano Recipe
INSERT INTO product_ingredients (product_id, ingredient_id, quantity, created_at, updated_at)
SELECT p.id, i.id, 0.02, NOW(), NOW() FROM products p, ingredients i WHERE p.code = '10000000000002' AND i.code = 'ING-001'
ON CONFLICT DO NOTHING;
INSERT INTO product_ingredients (product_id, ingredient_id, quantity, created_at, updated_at)
SELECT p.id, i.id, 1.00, NOW(), NOW() FROM products p, ingredients i WHERE p.code = '10000000000002' AND i.code = 'ING-007'
ON CONFLICT DO NOTHING;

-- Iced Matcha Latte Recipe
INSERT INTO product_ingredients (product_id, ingredient_id, quantity, created_at, updated_at)
SELECT p.id, i.id, 0.015, NOW(), NOW() FROM products p, ingredients i WHERE p.code = '10000000000003' AND i.code = 'ING-004'
ON CONFLICT DO NOTHING;
INSERT INTO product_ingredients (product_id, ingredient_id, quantity, created_at, updated_at)
SELECT p.id, i.id, 0.15, NOW(), NOW() FROM products p, ingredients i WHERE p.code = '10000000000003' AND i.code = 'ING-002'
ON CONFLICT DO NOTHING;
INSERT INTO product_ingredients (product_id, ingredient_id, quantity, created_at, updated_at)
SELECT p.id, i.id, 0.02, NOW(), NOW() FROM products p, ingredients i WHERE p.code = '10000000000003' AND i.code = 'ING-005'
ON CONFLICT DO NOTHING;
INSERT INTO product_ingredients (product_id, ingredient_id, quantity, created_at, updated_at)
SELECT p.id, i.id, 1.00, NOW(), NOW() FROM products p, ingredients i WHERE p.code = '10000000000003' AND i.code = 'ING-007'
ON CONFLICT DO NOTHING;

-- 8. CUSTOMERS
INSERT INTO customers (name, phone, email, address, points, created_at, updated_at) VALUES
('Chan Sokha', '012345678', 'sokha@gmail.com', 'Toul Kork, Phnom Penh', 120, NOW(), NOW()),
('Keo Bopha', '098765432', 'bopha@gmail.com', 'BKK1, Phnom Penh', 85, NOW(), NOW()),
('Vong Dara', '077112233', 'dara@gmail.com', 'Daun Penh, Phnom Penh', 210, NOW(), NOW()),
('Meas Sreyneath', '088998877', 'sreyneath@gmail.com', 'Sen Sok, Phnom Penh', 45, NOW(), NOW())
ON CONFLICT (phone) DO NOTHING;

-- 9. DISCOUNTS
INSERT INTO discounts (name, type, value, min_purchase, start_date, end_date, is_active, created_at, updated_at) VALUES
('Member Welcome 10%', 'PERCENTAGE', 10.00, 0.00, CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '6 months', true, NOW(), NOW()),
('Big Order $2 Off', 'FIXED', 2.00, 15.00, CURRENT_DATE - INTERVAL '10 days', CURRENT_DATE + INTERVAL '6 months', true, NOW(), NOW()),
('Happy Hour 15%', 'PERCENTAGE', 15.00, 5.00, CURRENT_DATE - INTERVAL '5 days', CURRENT_DATE + INTERVAL '3 months', true, NOW(), NOW())
ON CONFLICT DO NOTHING;
