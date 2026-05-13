-- ===================================
-- DATABASE SCHEMA FOR DNCS3 PROJECT
-- ===================================

-- 1. USERS TABLE
-- Lưu ý: Phải có trường này trước khi chạy API
CREATE TABLE IF NOT EXISTS `users` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `role` VARCHAR(50) DEFAULT 'USER' COMMENT 'USER or ADMIN',
  `status` VARCHAR(50) DEFAULT 'ACTIVE' COMMENT 'ACTIVE, BLOCKED, DELETED',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_email (email),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS `categories` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL UNIQUE,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. BEAUTY SERVICES TABLE
CREATE TABLE IF NOT EXISTS `services` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `price` DECIMAL(10, 2) NOT NULL,
  `duration` INT DEFAULT 45 COMMENT 'Thời gian tính bằng phút',
  `image_url` VARCHAR(500),
  `category_id` INT,
  `status` VARCHAR(50) DEFAULT 'Hoạt động' COMMENT 'Hoạt động or Tạm ngưng',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (category_id) REFERENCES categories(id),
  INDEX idx_category (category_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. APPOINTMENTS TABLE
CREATE TABLE IF NOT EXISTS `appointments` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `user_id` INT NOT NULL,
  `service_id` INT NOT NULL,
  `appointment_date` DATE NOT NULL,
  `appointment_time` TIME NOT NULL,
  `status` VARCHAR(50) DEFAULT 'PENDING' COMMENT 'PENDING, CONFIRMED, COMPLETED, CANCELLED',
  `note` TEXT,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (service_id) REFERENCES services(id),
  INDEX idx_user (user_id),
  INDEX idx_service (service_id),
  INDEX idx_date (appointment_date),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===================================
-- SAMPLE DATA (Optional)
-- ===================================

-- Thêm admin user
INSERT INTO `users` (name, email, password, phone, role, status) 
VALUES ('Admin', 'admin@example.com', MD5('123456'), '0901234567', 'ADMIN', 'ACTIVE')
ON DUPLICATE KEY UPDATE id=id;

-- Thêm sample categories
INSERT INTO `categories` (name) 
VALUES ('Cắt tóc'), ('Nhuộm tóc'), ('Uốn tóc'), ('Chăm sóc da mặt'), ('Nail')
ON DUPLICATE KEY UPDATE id=id;

-- Thêm sample services
INSERT INTO `services` (name, description, price, duration, category_id, status) 
VALUES 
('Cắt tóc nam', 'Dịch vụ cắt tóc nam chuyên nghiệp', 150000, 30, 1, 'Hoạt động'),
('Nhuộm tóc', 'Nhuộm tóc cao cấp các màu', 350000, 120, 2, 'Hoạt động'),
('Uốn tóc Hàn', 'Uốn tóc kiểu Hàn Quốc', 400000, 90, 3, 'Hoạt động'),
('Chăm sóc da mặt', 'Chăm sóc da mặt bằng mỹ phẩm cao cấp', 250000, 60, 4, 'Hoạt động'),
('Nail tay - chân', 'Sơn móng tay và chân', 200000, 45, 5, 'Hoạt động')
ON DUPLICATE KEY UPDATE id=id;

-- ===================================
-- QUERY EXAMPLES
-- ===================================

-- Lấy tất cả users (trừ những đã xóa)
SELECT id, name, email, phone, role, status, created_at,
  (SELECT COUNT(*) FROM appointments WHERE user_id = users.id AND status != 'CANCELLED') as total_appointments
FROM users 
WHERE status != 'DELETED'
ORDER BY created_at DESC;

-- Lấy appointments của một user
SELECT a.id, a.appointment_date, a.appointment_time, a.status, a.note,
  s.name as service_name, s.price,
  u.name as user_name, u.phone as user_phone
FROM appointments a
JOIN services s ON a.service_id = s.id
JOIN users u ON a.user_id = u.id
WHERE a.user_id = ? AND a.status != 'CANCELLED'
ORDER BY a.appointment_date DESC;

-- Đếm appointments theo status
SELECT status, COUNT(*) as count
FROM appointments
WHERE appointment_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY status;
