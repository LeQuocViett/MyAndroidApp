<!-- API Documentation for DNCS3 User Management -->

# 📚 DNCS3 API Documentation - Quản Lý Người Dùng

## Base URL
```
http://localhost/backend/api
```

---

## 📋 API Endpoints Summary

| # | Method | Endpoint | Purpose | Status |
|---|--------|----------|---------|--------|
| 1 | `GET` | `/get_users.php` | Lấy tất cả users | ✅ |
| 2 | `POST` | `/update_user.php` | Cập nhật user | ✅ NEW |
| 3 | `POST` | `/delete_user.php` | Xóa user (soft) | ✅ NEW |
| 4 | `POST` | `/block_user.php` | Khóa tài khoản | ✅ NEW |
| 5 | `POST` | `/unblock_user.php` | Mở khóa tài khoản | ✅ NEW |
| 6 | `GET` | `/search_users.php` | Tìm kiếm users | ✅ NEW |

---

## 🔧 Detailed API Specifications

### 1️⃣ GET ALL USERS
```http
GET /get_users.php

Response (200 OK):
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
  },
  {
    "id": 2,
    "name": "Admin",
    "email": "admin@example.com",
    "phone": "0901234568",
    "role": "ADMIN",
    "status": "ACTIVE",
    "created_at": "2024-01-01 00:00:00",
    "total_appointments": 0
  }
]
```

---

### 2️⃣ UPDATE USER ⭐ NEW
```http
POST /update_user.php
Content-Type: application/json

Request:
{
  "id": 1,
  "name": "Nguyễn Văn A",
  "email": "a.new@example.com",
  "phone": "0909999999",
  "status": "ACTIVE"
}

Response (200 OK):
{
  "success": true,
  "message": "Cập nhật người dùng thành công"
}

Response (400 Bad Request):
{
  "success": false,
  "message": "Email đã tồn tại" 
  // or: "Email không hợp lệ"
  // or: "Vui lòng điền đầy đủ thông tin"
  // or: "Thiếu thông tin bắt buộc"
}
```

**Validation Rules:**
- `id` (required): User ID, phải là số
- `name` (required): Không được trống
- `email` (required): Phải hợp lệ, không được trùng (ngoại trừ user hiện tại)
- `phone` (required): Không được trống
- `status` (optional): ACTIVE | BLOCKED (default: ACTIVE)

---

### 3️⃣ DELETE USER (Soft Delete) ⭐ NEW
```http
POST /delete_user.php
Content-Type: application/json

Request:
{
  "id": 1
}

Response (200 OK):
{
  "success": true,
  "message": "Đã xóa người dùng"
}

Response (400 Bad Request):
{
  "success": false,
  "message": "Người dùng không tồn tại"
}
```

**Notes:**
- Chỉ cập nhật `status = 'DELETED'`, không xóa từ database
- Dữ liệu vẫn có thể xem/restore sau này
- Dùng cho audit trail

---

### 4️⃣ BLOCK USER ⭐ NEW
```http
POST /block_user.php
Content-Type: application/json

Request:
{
  "id": 1,
  "reason": "Spam content"  // Optional
}

Response (200 OK):
{
  "success": true,
  "message": "Đã khóa tài khoản người dùng"
}

Response (400 Bad Request):
{
  "success": false,
  "message": "Người dùng không tồn tại"
}
```

**Effects:**
- `status = 'BLOCKED'`
- User không thể login
- Appointments bị hủy (optional, tuỳ backend logic)

---

### 5️⃣ UNBLOCK USER ⭐ NEW
```http
POST /unblock_user.php
Content-Type: application/json

Request:
{
  "id": 1
}

Response (200 OK):
{
  "success": true,
  "message": "Đã mở khóa tài khoản người dùng"
}

Response (400 Bad Request):
{
  "success": false,
  "message": "Người dùng không tồn tại"
}
```

**Effects:**
- `status = 'ACTIVE'`
- User có thể login bình thường

---

### 6️⃣ SEARCH USERS ⭐ NEW
```http
GET /search_users.php?query=nguyễn&searchType=name

Query Parameters:
- query (required, string): Từ khóa tìm kiếm
- searchType (optional, string): name | email | phone | null (search all)

Response (200 OK):
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

Response (Empty):
[]

Response (400 Bad Request):
{
  "success": false,
  "message": "Vui lòng nhập từ khóa tìm kiếm"
}
```

**Examples:**
```
GET /search_users.php?query=nguyễn
GET /search_users.php?query=a@example.com&searchType=email
GET /search_users.php?query=0901&searchType=phone
GET /search_users.php?query=admin&searchType=name
```

**Search Features:**
- Case-insensitive
- Loại bỏ users có `status = 'DELETED'`
- Kết quả sắp xếp theo `created_at DESC`
- Tính toán `total_appointments` từ DB

---

## 📝 Common Response Formats

### Success Response
```json
{
  "success": true,
  "message": "Thao tác thành công"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Mô tả lỗi chi tiết"
}
```

### Array Response (GET)
```json
[
  { "id": 1, ... },
  { "id": 2, ... }
]
```

---

## 🔐 Security Notes

✅ **Implemented:**
- Input validation (email format, required fields)
- Prepared statements (prevent SQL injection)
- Email uniqueness check
- UTF-8 encoding

⚠️ **For Production:**
```php
// 1. Thêm authentication
if (!isset($_SESSION['admin_id'])) {
    http_response_code(401);
    sendResponse(false, 'Unauthorized');
}

// 2. Rate limiting
// 3. Request logging
// 4. Error logging (don't show to client)
// 5. HTTPS only
// 6. Input sanitization
```

---

## 🧪 Testing

### Using cURL
```bash
# Get all users
curl -X GET http://localhost/backend/api/get_users.php

# Update user
curl -X POST http://localhost/backend/api/update_user.php \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "name": "Test",
    "email": "test@example.com",
    "phone": "0901234567",
    "status": "ACTIVE"
  }'

# Search users
curl -X GET "http://localhost/backend/api/search_users.php?query=test&searchType=name"
```

### Using JavaScript/Fetch
```javascript
// Update user
fetch('http://localhost/backend/api/update_user.php', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    id: 1,
    name: 'New Name',
    email: 'new@example.com',
    phone: '0901234567',
    status: 'ACTIVE'
  })
})
.then(r => r.json())
.then(data => console.log(data))
.catch(e => console.error(e));
```

### Using Postman
1. New → Request
2. Method: POST
3. URL: `http://localhost/backend/api/update_user.php`
4. Headers: `Content-Type: application/json`
5. Body (raw): JSON payload
6. Send

---

## 📊 Database Schema Relations

```
users (id, name, email, phone, role, status, created_at, updated_at)
  ↓
appointments (id, user_id, service_id, appointment_date, appointment_time, status, ...)
  ↓
services (id, name, price, ...)
```

---

## 🚨 Error Handling

| Message | Cause | Solution |
|---------|-------|----------|
| "Thiếu ID người dùng" | Missing `id` in request | Include `id` in body/params |
| "Email đã tồn tại" | Email duplicated | Use different email |
| "Email không hợp lệ" | Invalid email format | Use proper email format (user@domain.com) |
| "Vui lòng điền đầy đủ thông tin" | Missing required fields | Fill all required fields |
| "Người dùng không tồn tại" | ID not found in DB | Check if ID exists |
| "Lỗi kết nối cơ sở dữ liệu" | DB connection error | Check config.php settings |

---

## 📞 Support

File: `test_api.html` - Web-based API tester
File: `README.md` - Setup guide
File: `DATABASE_SCHEMA.sql` - Database structure

---

**Last Updated:** 2024-01-15
**Version:** 1.0
**Status:** Production Ready ✅
