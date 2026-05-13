# 📱 DNCS3 Backend API - Hướng Dẫn Setup

## 🚀 Để Bắt Đầu

### 1️⃣ Cấu hình Database

#### Bước 1: Tạo Database
```sql
CREATE DATABASE dncs3 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### Bước 2: Chạy SQL Schema
- Mở file `DATABASE_SCHEMA.sql`
- Chạy tất cả các câu lệnh SQL trong PhpMyAdmin hoặc MySQL Workbench

### 2️⃣ Cấu hình File `config.php`

Mở file `config.php` và điều chỉnh thông số:

```php
define('DB_HOST', 'localhost');     // Host MySQL
define('DB_USER', 'root');          // Username MySQL
define('DB_PASS', '');              // Password MySQL (nếu có)
define('DB_NAME', 'dncs3');         // Tên database
```

### 3️⃣ Setup Web Server

**Dùng XAMPP/WAMP:**
```
Đặt folder `backend` vào thư mục htdocs hoặc www
```

**URL API Base:**
```
http://localhost/backend/api/
```

---

## 📋 API Endpoints

### ✅ Danh Sách API cho Quản Lý Người Dùng

#### 1️⃣ **Lấy Tất Cả Users** (Có sẵn từ trước)
```
GET /get_users.php

Response:
[
  {
    "id": 1,
    "name": "Nguyễn Văn A",
    "email": "a@example.com",
    "phone": "0901234567",
    "role": "USER",
    "status": "ACTIVE",
    "created_at": "2024-01-15 10:30:00",
    "total_appointments": 5
  }
]
```

---

#### 2️⃣ **Cập Nhật User** ⭐ MỚI
```
POST /update_user.php

Request Body:
{
  "id": 1,
  "name": "Nguyễn Văn A Updated",
  "email": "a.updated@example.com",
  "phone": "0901234568",
  "status": "ACTIVE"
}

Response:
{
  "success": true,
  "message": "Cập nhật người dùng thành công"
}

Error Response:
{
  "success": false,
  "message": "Email đã tồn tại" | "Email không hợp lệ" | "Vui lòng điền đầy đủ thông tin"
}
```

**Lưu ý:**
- Validate: Email không được trùng lặp
- Email phải có format hợp lệ
- Tất cả trường (name, email, phone) bắt buộc

---

#### 3️⃣ **Soft Delete User** ⭐ MỚI
```
POST /delete_user.php

Request Body:
{
  "id": 1
}

Response:
{
  "success": true,
  "message": "Đã xóa người dùng"
}

Notes:
- Chỉ cập nhật status = 'DELETED' (không xóa thật)
- Dữ liệu vẫn giữ lại trong database
- Dùng cho audit trail và statistics
```

---

#### 4️⃣ **Khóa Tài Khoản** ⭐ MỚI
```
POST /block_user.php

Request Body:
{
  "id": 1,
  "reason": "Vi phạm điều khoản sử dụng"  // Optional
}

Response:
{
  "success": true,
  "message": "Đã khóa tài khoản người dùng"
}

Effect:
- User không thể đăng nhập
- Tất cả appointments bị hủy/pause (tùy logic backend)
- Status = 'BLOCKED'
```

---

#### 5️⃣ **Mở Khóa Tài Khoản** ⭐ MỚI
```
POST /unblock_user.php

Request Body:
{
  "id": 1
}

Response:
{
  "success": true,
  "message": "Đã mở khóa tài khoản người dùng"
}

Effect:
- User có thể đăng nhập lại
- Status = 'ACTIVE'
```

---

#### 6️⃣ **Tìm Kiếm Users** ⭐ MỚI
```
GET /search_users.php?query=nguyễn&searchType=name

Query Parameters:
- query (required): Từ khóa tìm kiếm
- searchType (optional): "name" | "email" | "phone" | null (tất cả)

Response:
[
  {
    "id": 1,
    "name": "Nguyễn Văn A",
    "email": "a@example.com",
    "phone": "0901234567",
    "role": "USER",
    "status": "ACTIVE",
    "created_at": "2024-01-15 10:30:00",
    "total_appointments": 5
  }
]

Search Examples:
GET /search_users.php?query=nguyễn              // Tìm theo tất cả
GET /search_users.php?query=a@example.com&searchType=email
GET /search_users.php?query=090&searchType=phone
```

**Lưu ý:**
- Tìm kiếm không case-sensitive
- Loại bỏ những user có status = 'DELETED'
- Sắp xếp theo created_at DESC

---

## 🔒 HTTP Status Codes

```
200 OK         - Request thành công
400 Bad Request - Thiếu parameter hoặc dữ liệu không hợp lệ
404 Not Found  - Username/User không tồn tại
500 Error      - Lỗi server/database
```

---

## 📱 Cách Sử Dụng từ Android App

### Retrofit Call Example:

```kotlin
// 1. Cập nhật user
val updatedUser = User(
    id = 1,
    name = "Nguyễn Văn A",
    email = "a@example.com",
    phone = "0901234567",
    status = "ACTIVE"
)
viewModel.updateUser(updatedUser) { success ->
    if (success) {
        // Hiển thị toast
    }
}

// 2. Khóa user
viewModel.blockUser(userId = 1) { success ->
    if (success) {
        // Refresh list
    }
}

// 3. Mở khóa user
viewModel.unblockUser(userId = 1) { success ->
    if (success) {
        // Refresh list
    }
}

// 4. Tìm kiếm
viewModel.searchUsers(query = "nguyễn", searchType = "name")

// 5. Xóa user (soft delete)
viewModel.deleteUser(userId = 1) { success ->
    if (success) {
        // Refresh list
    }
}
```

---

## ✨ Database Thay Đổi Cần Thiết

Nếu table `users` đã tồn tại, hãy thêm cột này (nếu chưa có):

```sql
-- Nếu chưa có, chạy:
ALTER TABLE users ADD COLUMN status VARCHAR(50) DEFAULT 'ACTIVE';
ALTER TABLE users ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
ALTER TABLE users ADD INDEX idx_status (status);

-- Xóa constraints cũ (nếu có):
ALTER TABLE users DROP CONSTRAINT email UNIQUE;
ALTER TABLE users ADD CONSTRAINT email UNIQUE (email);
```

---

## 🛠️ Testing API

### Dùng Postman:

**1. Cập nhật user:**
```
Method: POST
URL: http://localhost/backend/api/update_user.php
Body (JSON):
{
  "id": 1,
  "name": "Test",
  "email": "test@example.com",
  "phone": "0901234567",
  "status": "ACTIVE"
}
```

**2. Khóa user:**
```
Method: POST
URL: http://localhost/backend/api/block_user.php
Body (JSON):
{
  "id": 1
}
```

**3. Tìm kiếm:**
```
Method: GET
URL: http://localhost/backend/api/search_users.php?query=nguyễn&searchType=name
```

---

## 📝 Notes

- ✅ Tất cả API dùng `UTF-8` encoding
- ✅ Return format luôn là JSON
- ✅ Soft delete: Không xóa dữ liệu thật, chỉ cập nhật status
- ✅ Email validation: Kiểm tra format hợp lệ + không trùng lặp
- ✅ Appointments được tính không tính những appointment CANCELLED

---

## 🐛 Troubleshooting

**Lỗi: "Lỗi kết nối cơ sở dữ liệu"**
- Kiểm tra DB_HOST, DB_USER, DB_PASS trong config.php
- Đảm bảo MySQL đang chạy

**Lỗi: "Thiếu thông tin bắt buộc"**
- Kiểm tra request body có đầy đủ fields không
- Validate dữ liệu trước khi gửi

**Lỗi: "Email đã tồn tại"**
- Email này đã được đăng ký bởi user khác
- Dùng email khác hoặc kiểm tra database

---

## 📞 Support

Nếu có vấn đề, debug bằng:
```php
// Thêm vào config.php
error_reporting(E_ALL);
ini_set('display_errors', 1);

// Xem chi tiết lỗi MySQL
echo json_encode([
    'success' => false,
    'message' => 'Lỗi: ' . $conn->error,
    'query' => 'SQL query đó'
]);
```

---

**Done! 🎉 Backend API sẵn sàng sử dụng!**
