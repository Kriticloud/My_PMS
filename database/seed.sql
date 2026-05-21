-- =====================================================
-- Seed Data for PMS + POS System
-- =====================================================

-- 1. Roles
INSERT INTO role (name, description) VALUES
('ADMIN', 'System administrator with full access'),
('FRONT_DESK', 'Front desk staff managing bookings and check-in/out'),
('RESTAURANT_STAFF', 'Restaurant staff managing POS orders');

-- 2. Default Admin User (password: admin123 - BCrypt encoded)
INSERT INTO app_user (username, password, full_name, email, role_id) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'System Admin', 'admin@pms.com', 1),
('frontdesk1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John Doe', 'john@pms.com', 2),
('waiter1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane Smith', 'jane@pms.com', 3);

-- 3. Room Types
INSERT INTO room_type (name, description, base_price, max_occupancy) VALUES
('Standard', 'Standard room with basic amenities', 2500.00, 2),
('Deluxe', 'Deluxe room with premium amenities and city view', 4500.00, 3),
('Suite', 'Luxury suite with living area and premium services', 8000.00, 4);

-- 4. Rooms
INSERT INTO room (room_number, room_type_id, floor, status) VALUES
('101', 1, 1, 'AVAILABLE'),
('102', 1, 1, 'AVAILABLE'),
('103', 1, 1, 'AVAILABLE'),
('104', 2, 1, 'AVAILABLE'),
('201', 1, 2, 'AVAILABLE'),
('202', 2, 2, 'AVAILABLE'),
('203', 2, 2, 'AVAILABLE'),
('204', 3, 2, 'AVAILABLE'),
('301', 2, 3, 'AVAILABLE'),
('302', 3, 3, 'AVAILABLE');

-- 5. Menu Categories
INSERT INTO menu_category (name, description) VALUES
('Starters', 'Appetizers and starters'),
('Main Course', 'Main course dishes'),
('Beverages', 'Hot and cold beverages'),
('Desserts', 'Sweet treats and desserts');

-- 6. Menu Items
INSERT INTO menu_item (name, category_id, price, description) VALUES
('Caesar Salad', 1, 350.00, 'Fresh romaine lettuce with Caesar dressing'),
('Soup of the Day', 1, 250.00, 'Chef special soup'),
('Grilled Chicken', 2, 550.00, 'Grilled chicken breast with herbs'),
('Pasta Alfredo', 2, 450.00, 'Creamy fettuccine alfredo'),
('Butter Chicken', 2, 500.00, 'Classic Indian butter chicken'),
('Steamed Rice', 2, 150.00, 'Basmati steamed rice'),
('Coffee', 3, 150.00, 'Freshly brewed coffee'),
('Fresh Juice', 3, 200.00, 'Seasonal fresh fruit juice'),
('Soft Drink', 3, 100.00, 'Chilled soft drink'),
('Chocolate Brownie', 4, 300.00, 'Warm chocolate brownie with ice cream'),
('Ice Cream', 4, 200.00, 'Choice of vanilla, chocolate, or strawberry');

-- 7. Inventory
INSERT INTO inventory (menu_item_id, quantity, min_stock_level, unit) VALUES
(1, 50, 10, 'servings'),
(2, 40, 10, 'servings'),
(3, 30, 5, 'servings'),
(4, 35, 5, 'servings'),
(5, 25, 5, 'servings'),
(6, 60, 10, 'servings'),
(7, 100, 20, 'cups'),
(8, 40, 10, 'glasses'),
(9, 80, 15, 'bottles'),
(10, 25, 5, 'servings'),
(11, 30, 5, 'scoops');

-- 8. Sample Guests
INSERT INTO guest (first_name, last_name, email, phone, id_type, id_number, address) VALUES
('Rahul', 'Sharma', 'rahul.sharma@email.com', '+91-9876543210', 'Aadhaar', '1234-5678-9012', 'Mumbai, India'),
('Priya', 'Patel', 'priya.patel@email.com', '+91-9876543211', 'Passport', 'J1234567', 'Delhi, India'),
('Amit', 'Kumar', 'amit.kumar@email.com', '+91-9876543212', 'Driver License', 'DL-1234567890', 'Bangalore, India');
