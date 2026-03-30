-- ============================================
-- Street League - Database Migration Script
-- Sponsorship & Shop Module Enhancements
-- ============================================

-- ─── Sponsorship Table Enhancements ──────────────────────────────────────────

-- Add payment_proof column to sponsorship table (if not exists)
ALTER TABLE sponsorship
ADD COLUMN IF NOT EXISTS payment_proof VARCHAR(500) AFTER end_date;

-- Add target_type column for sponsorship target classification
ALTER TABLE sponsorship
ADD COLUMN IF NOT EXISTS target_type VARCHAR(50) AFTER status;

-- Add description column for sponsor's message
ALTER TABLE sponsorship
ADD COLUMN IF NOT EXISTS description TEXT AFTER target_type;

-- Add expected_benefits column
ALTER TABLE sponsorship
ADD COLUMN IF NOT EXISTS expected_benefits VARCHAR(500) AFTER description;

-- Add tournament_id foreign key (for tournament sponsorships)
ALTER TABLE sponsorship
ADD COLUMN IF NOT EXISTS tournament_id BIGINT AFTER event_id;

-- Add foreign key constraint for tournament
ALTER TABLE sponsorship
ADD CONSTRAINT fk_sponsorship_tournament
FOREIGN KEY (tournament_id) REFERENCES tournament(id) ON DELETE CASCADE;

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_date DATETIME NOT NULL,
    total_amount DOUBLE NOT NULL,
    status VARCHAR(50) NOT NULL,
    shipping_address VARCHAR(500),
    phone_number VARCHAR(50),
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_order_user (user_id),
    INDEX idx_order_date (order_date),
    INDEX idx_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create cart table
CREATE TABLE IF NOT EXISTS cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME NOT NULL,
    total_amount DOUBLE NOT NULL DEFAULT 0.0,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_cart_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create cart_item table
CREATE TABLE IF NOT EXISTS cart_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity INT NOT NULL,
    subtotal DOUBLE NOT NULL,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES cart(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_item_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE RESTRICT,
    INDEX idx_cart_item_cart (cart_id),
    INDEX idx_cart_item_product (product_id),
    UNIQUE KEY unique_cart_product (cart_id, product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create order_item table
CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    subtotal DOUBLE NOT NULL,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE RESTRICT,
    INDEX idx_order_item_order (order_id),
    INDEX idx_order_item_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create product_review table
CREATE TABLE IF NOT EXISTS product_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at DATETIME NOT NULL,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_review_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE,
    CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_review_product (product_id),
    INDEX idx_review_user (user_id),
    INDEX idx_review_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Create player_merch table for player merchandise submissions
CREATE TABLE IF NOT EXISTS player_merch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DOUBLE NOT NULL,
    stock INT NOT NULL,
    category VARCHAR(100) NOT NULL,
    image VARCHAR(500),
    sport_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    seller_id BIGINT NOT NULL,
    approved_by BIGINT,
    submitted_at DATETIME NOT NULL,
    approved_at DATETIME,
    rejection_reason TEXT,
    product_id BIGINT,
    CONSTRAINT fk_player_merch_seller FOREIGN KEY (seller_id) REFERENCES player_profile(id) ON DELETE CASCADE,
    CONSTRAINT fk_player_merch_approver FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_player_merch_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE SET NULL,
    INDEX idx_player_merch_seller (seller_id),
    INDEX idx_player_merch_status (status),
    INDEX idx_player_merch_submitted (submitted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Sample Data (Optional - for testing)
-- ============================================

-- Sample Order Statuses (for reference)
-- PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED

-- Sample Sponsorship with payment proof
-- UPDATE sponsorship 
-- SET payment_proof = 'https://example.com/payment-proof-001.pdf'
-- WHERE id = 1;

-- ============================================
-- Verification Queries
-- ============================================

-- Check if payment_proof column was added
-- SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH 
-- FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_NAME = 'sponsorship' AND COLUMN_NAME = 'payment_proof';

-- Check orders table structure
-- DESCRIBE orders;

-- Check cart table structure
-- DESCRIBE cart;

-- Check cart_item table structure
-- DESCRIBE cart_item;

-- Check order_item table structure
-- DESCRIBE order_item;

-- Check product_review table structure
-- DESCRIBE product_review;

-- Count records in new tables
-- SELECT 
--     (SELECT COUNT(*) FROM orders) as total_orders,
--     (SELECT COUNT(*) FROM cart) as total_carts,
--     (SELECT COUNT(*) FROM cart_item) as total_cart_items,
--     (SELECT COUNT(*) FROM order_item) as total_order_items,
--     (SELECT COUNT(*) FROM product_review) as total_reviews;

-- ============================================
-- Rollback Script (Use with caution!)
-- ============================================

-- To rollback these changes, uncomment and run:
-- ALTER TABLE sponsorship DROP COLUMN payment_proof;
-- DROP TABLE IF EXISTS product_review;
-- DROP TABLE IF EXISTS cart_item;
-- DROP TABLE IF EXISTS cart;
-- DROP TABLE IF EXISTS order_item;
-- DROP TABLE IF EXISTS orders;

-- ============================================
-- Notes
-- ============================================

-- 1. This script is idempotent for table creation (uses IF NOT EXISTS)
-- 2. Foreign keys use CASCADE for orders/items, RESTRICT for products
-- 3. Indexes added for common query patterns
-- 4. Rating constraint ensures values between 1-5
-- 5. Backup your database before running this script!

-- ============================================
-- Post-Migration Checklist
-- ============================================

-- [ ] Backup database completed
-- [ ] Migration script executed successfully
-- [ ] Verification queries run
-- [ ] Application restarted
-- [ ] Test endpoints working
-- [ ] Sample data created (if needed)
