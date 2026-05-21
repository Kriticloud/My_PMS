-- =====================================================
-- Property Management System (PMS) + POS Database Schema
-- PostgreSQL
-- =====================================================

-- Drop tables if they exist (in reverse dependency order)
DROP TABLE IF EXISTS payment CASCADE;
DROP TABLE IF EXISTS invoice_item CASCADE;
DROP TABLE IF EXISTS invoice CASCADE;
DROP TABLE IF EXISTS pos_order_item CASCADE;
DROP TABLE IF EXISTS pos_order CASCADE;
DROP TABLE IF EXISTS inventory CASCADE;
DROP TABLE IF EXISTS menu_item CASCADE;
DROP TABLE IF EXISTS menu_category CASCADE;
DROP TABLE IF EXISTS booking CASCADE;
DROP TABLE IF EXISTS guest CASCADE;
DROP TABLE IF EXISTS room CASCADE;
DROP TABLE IF EXISTS room_type CASCADE;
DROP TABLE IF EXISTS app_user CASCADE;
DROP TABLE IF EXISTS role CASCADE;
DROP TABLE IF EXISTS property CASCADE;

-- =====================================================
-- 0. PROPERTY (multi-property support)
-- =====================================================
CREATE TABLE property (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    property_type VARCHAR(20) NOT NULL
        CHECK (property_type IN ('HOTEL', 'HOSTEL', 'HOSPITAL', 'RENTAL', 'RESORT')),
    address TEXT,
    contact_phone VARCHAR(20),
    contact_email VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 1. ROLES & USERS (RBAC)
-- =====================================================
CREATE TABLE role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    role_id BIGINT NOT NULL REFERENCES role(id),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 2. ROOM MANAGEMENT
-- =====================================================
CREATE TABLE room_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,       -- Standard, Deluxe, Suite
    description VARCHAR(255),
    base_price NUMERIC(10, 2) NOT NULL,
    max_occupancy INT NOT NULL DEFAULT 2,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE room (
    id BIGSERIAL PRIMARY KEY,
    room_number VARCHAR(10) NOT NULL UNIQUE,
    room_type_id BIGINT NOT NULL REFERENCES room_type(id),
    property_id BIGINT REFERENCES property(id),
    floor INT NOT NULL DEFAULT 1,
    capacity INT NOT NULL DEFAULT 1,
    occupied_count INT NOT NULL DEFAULT 0,
    ward_name VARCHAR(50),
    unit_label VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
        CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'CLEANING', 'SANITIZING', 'MAINTENANCE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 3. GUEST MANAGEMENT
-- =====================================================
CREATE TABLE guest (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    id_type VARCHAR(30),                    -- Passport, Driver's License, etc.
    id_number VARCHAR(50),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 4. BOOKING SYSTEM
-- =====================================================
CREATE TABLE booking (
    id BIGSERIAL PRIMARY KEY,
    booking_number VARCHAR(20) NOT NULL UNIQUE,
    guest_id BIGINT NOT NULL REFERENCES guest(id),
    room_id BIGINT NOT NULL REFERENCES room(id),
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    actual_check_in TIMESTAMP,
    actual_check_out TIMESTAMP,
    num_guests INT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED'
        CHECK (status IN ('RESERVED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED')),
    special_requests TEXT,
    total_amount NUMERIC(12, 2) DEFAULT 0,
    created_by BIGINT REFERENCES app_user(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 5. POS - MENU & INVENTORY
-- =====================================================
CREATE TABLE menu_category (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,       -- Food, Beverages, Desserts, etc.
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE menu_item (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category_id BIGINT NOT NULL REFERENCES menu_category(id),
    price NUMERIC(10, 2) NOT NULL,
    description VARCHAR(255),
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    menu_item_id BIGINT NOT NULL REFERENCES menu_item(id) UNIQUE,
    quantity INT NOT NULL DEFAULT 0,
    min_stock_level INT NOT NULL DEFAULT 5,
    unit VARCHAR(20) DEFAULT 'pcs',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 6. POS ORDERS
-- =====================================================
CREATE TABLE pos_order (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(20) NOT NULL UNIQUE,
    booking_id BIGINT REFERENCES booking(id),   -- NULL for walk-in
    guest_name VARCHAR(100),                     -- For walk-in customers
    room_number VARCHAR(10),                     -- Quick reference
    order_type VARCHAR(20) NOT NULL DEFAULT 'WALKIN'
        CHECK (order_type IN ('ROOM_SERVICE', 'RESTAURANT', 'WALKIN')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PREPARING', 'SERVED', 'CANCELLED')),
    total_amount NUMERIC(12, 2) DEFAULT 0,
    created_by BIGINT REFERENCES app_user(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pos_order_item (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES pos_order(id),
    menu_item_id BIGINT NOT NULL REFERENCES menu_item(id),
    quantity INT NOT NULL DEFAULT 1,
    unit_price NUMERIC(10, 2) NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL,
    notes VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 7. INVOICING & BILLING
-- =====================================================
CREATE TABLE invoice (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(20) NOT NULL UNIQUE,
    booking_id BIGINT NOT NULL REFERENCES booking(id),
    guest_id BIGINT NOT NULL REFERENCES guest(id),
    subtotal NUMERIC(12, 2) NOT NULL DEFAULT 0,
    tax_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'PAID', 'PARTIALLY_PAID', 'CANCELLED')),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

CREATE TABLE invoice_item (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoice(id),
    description VARCHAR(255) NOT NULL,
    item_type VARCHAR(20) NOT NULL
        CHECK (item_type IN ('ROOM_CHARGE', 'POS_CHARGE', 'SERVICE_CHARGE', 'TAX', 'DISCOUNT')),
    amount NUMERIC(10, 2) NOT NULL,
    reference_id BIGINT,                    -- booking_id or pos_order_id
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 8. PAYMENTS
-- =====================================================
CREATE TABLE payment (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoice(id),
    amount NUMERIC(12, 2) NOT NULL,
    payment_mode VARCHAR(20) NOT NULL
        CHECK (payment_mode IN ('CASH', 'CARD', 'UPI')),
    transaction_ref VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED'
        CHECK (status IN ('COMPLETED', 'PENDING', 'FAILED', 'REFUNDED')),
    paid_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_by BIGINT REFERENCES app_user(id)
);

-- =====================================================
-- INDEXES for performance
-- =====================================================
CREATE INDEX idx_booking_guest ON booking(guest_id);
CREATE INDEX idx_booking_room ON booking(room_id);
CREATE INDEX idx_booking_dates ON booking(check_in_date, check_out_date);
CREATE INDEX idx_booking_status ON booking(status);
CREATE INDEX idx_pos_order_booking ON pos_order(booking_id);
CREATE INDEX idx_pos_order_item_order ON pos_order_item(order_id);
CREATE INDEX idx_invoice_booking ON invoice(booking_id);
CREATE INDEX idx_payment_invoice ON payment(invoice_id);
CREATE INDEX idx_room_status ON room(status);
CREATE INDEX idx_menu_item_category ON menu_item(category_id);
