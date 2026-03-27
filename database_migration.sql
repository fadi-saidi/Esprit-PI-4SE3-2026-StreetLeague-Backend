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

-- Check order_item table structure
-- DESCRIBE order_item;

-- Check product_review table structure
-- DESCRIBE product_review;

-- Count records in new tables
-- SELECT 
--     (SELECT COUNT(*) FROM orders) as total_orders,
--     (SELECT COUNT(*) FROM order_item) as total_order_items,
--     (SELECT COUNT(*) FROM product_review) as total_reviews;

-- ============================================
-- Rollback Script (Use with caution!)
-- ============================================

-- To rollback these changes, uncomment and run:
-- ALTER TABLE sponsorship DROP COLUMN payment_proof;
-- DROP TABLE IF EXISTS product_review;
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
