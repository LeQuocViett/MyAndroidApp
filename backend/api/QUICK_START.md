# ⚡ QUICK START - Backend Setup Guide

## 🚀 3 Bước Cơ Bản

### Bước 1️⃣: Tạo Database & Table (5 phút)

**Mở PhpMyAdmin → Nhập SQL này:**
```sql
-- Nếu chưa có database
CREATE DATABASE dncs3 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Chạy tất cả script trong file DATABASE_SCHEMA.sql
-- (Copy toàn bộ nội dung rồi paste vào)
```

⚠️ **Hoặc dùng Terminal:**
```bash
mysql -u root -p < DATABASE_SCHEMA.sql
```

---

### Bước 2️⃣: Điều Chỉnh Config (2 phút)

**File: `config.php`**
```php
define('DB_HOST', 'localhost');     // Thay đổi nếu cần
define('DB_USER', 'root');          // Username của MySQL
define('DB_PASS', '');              // Password (nếu có)
define('DB_NAME', 'dncs3');         // Tên database
```

---

### Bước 3️⃣: Deploy Folder (1 phút)

**Copy folder `backend` vào:**
```
XAMPP:  C:\xampp\htdocs\
WAMP:   C:\wamp\www\
MAMP:   /Applications/MAMP/htdocs/
```

**URL sẽ là:**
```
http://localhost/backend/api/
```

---

## ✅ Kiểm Tra Setup

**Truy cập URL này:**
```
http://localhost/backend/api/
```

✅ Nếu thấy JSON → Setup thành công! 🎉

---

## 🧪 Kiểm Tra API Nhanh

### Cách 1: Web Test Tool
```
http://localhost/backend/api/test_api.html
```
Click các button để test từng API

### Cách 2: Postman
1. Download [Postman](https://www.postman.com/downloads/)
2. New Request → POST
3. URL: `http://localhost/backend/api/update_user.php`
4. Body (JSON):
```json
{
  "id": 1,
  "name": "Test",
  "email": "test@example.com",
  "phone": "0901234567",
  "status": "ACTIVE"
}
```
5. Send

### Cách 3: cURL (Terminal)
```bash
curl -X POST http://localhost/backend/api/block_user.php \
  -H "Content-Type: application/json" \
  -d '{"id": 1}'
```

---

## 📱 Dùng từ Android App

Tất cả hàm đã sẵn trong `MainViewModel.kt`:

```kotlin
// 1. Khóa user
viewModel.blockUser(userId = 1) { success ->
    if (success) Toast.makeText(context, "Đã khóa", Toast.LENGTH_SHORT).show()
}

// 2. Mở khóa
viewModel.unblockUser(userId = 1) { success ->
    if (success) viewModel.fetchAllUsers()
}

// 3. Tìm kiếm
viewModel.searchUsers(query = "nguyễn", searchType = "name")

// 4. Cập nhật
val updatedUser = User(id = 1, name = "...", email = "...", phone = "...", status = "ACTIVE")
viewModel.updateUser(updatedUser) { success ->
    if (success) viewModel.fetchAllUsers()
}
```

---

## 🔧 File Structure

```
backend/
├── api/
│   ├── config.php                 ✅ Database config (SỬA ĐỔI TẠI ĐÂY)
│   ├── index.php                  ✅ Health check
│   ├── get_users.php              ✅ Lấy tất cả
│   ├── search_users.php           ✅ Tìm kiếm
│   ├── update_user.php            ✅ Cập nhật
│   ├── delete_user.php            ✅ Xóa (soft)
│   ├── block_user.php             ✅ Khóa
│   ├── unblock_user.php           ✅ Mở khóa
│   ├── DATABASE_SCHEMA.sql        📋 Script tạo table
│   ├── API_DOCUMENTATION.md       📚 Docs chi tiết
│   ├── README.md                  📖 Setup guide đầy đủ
│   ├── QUICK_START.md             ⚡ File này
│   ├── test_api.html              🧪 Web test tool
│   ├── .htaccess                  🔧 Config
│   └── (other APIs like services, appointments, etc.)
```

---

## 🐛 Troubleshooting

### ❌ Lỗi: "Cannot connect to database"
**Giải pháp:**
```php
// 1. Kiểm tra MySQL đang chạy không?
  - Mở XAMPP Control Panel → Start MySQL
  
// 2. Kiểm tra config.php
  - DB_HOST, DB_USER, DB_PASS đúng chưa?
  
// 3. Kiểm tra database tồn tại không
  - Mở PhpMyAdmin → Tìm database "dncs3"
```

### ❌ Lỗi: "Table 'dncs3.users' doesn't exist"
**Giải pháp:**
```
Chạy lại DATABASE_SCHEMA.sql trong PhpMyAdmin
```

### ❌ Lỗi: "Email đã tồn tại"
**Giải pháp:**
```
Dùng email khác hoặc update bằng email cũ:
{
  "id": 1,
  "name": "New Name",
  "email": "admin@example.com",  // Email cũ
  "phone": "0901234567",
  "status": "ACTIVE"
}
```

### ❌ Lỗi: "CORS error" từ Android
**Giải pháp:**
- File `.htaccess` đã enable CORS
- Nếu vẫn lỗi, check AndroidManifest.xml có `INTERNET` permission không:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## ⚙️ Cấu Hình Nâng Cao (Optional)

### 1. Enable API Logging
Thêm vào đầu `config.php`:
```php
// Log all requests
$logFile = 'logs/api_requests.log';
$timestamp = date('Y-m-d H:i:s');
$method = $_SERVER['REQUEST_METHOD'];
$uri = $_SERVER['REQUEST_URI'];
error_log("[$timestamp] $method $uri\n", 3, $logFile);
```

### 2. Rate Limiting
Để ngăn abuse:
```php
// Kiểm tra request count per IP
// (Thêm vào config.php)
```

### 3. Authentication
Nếu cần bảo vệ API:
```php
// Thêm session check
if (!isset($_SESSION['admin_id'])) {
    http_response_code(401);
    die(json_encode(['success' => false, 'message' => 'Unauthorized']));
}
```

---

## 📞 Restart Services

Nếu config bị lỗi:

**XAMPP:**
```
Control Panel → Stop MySQL → Start MySQL
```

**Terminal:**
```bash
# Linux/Mac
sudo systemctl restart mysql

# Windows
net stop MySQL80
net start MySQL80
```

---

## ✨ Test Checklist

- [ ] Database created
- [ ] Tables created (run DATABASE_SCHEMA.sql)
- [ ] config.php updated
- [ ] Backend folder deployed
- [ ] `http://localhost/backend/api/` shows JSON
- [ ] test_api.html works
- [ ] Android app can fetch users

---

## 📚 Liên Kết Hữu Ích

- **Setup Chi Tiết**: [README.md](README.md)
- **API Docs**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md)
- **Database**: [DATABASE_SCHEMA.sql](DATABASE_SCHEMA.sql)
- **Test Tool**: [test_api.html](test_api.html)

---

## 🎉 Done!

Backend sẵn sàng. Hãy test ngay từ Android app!

```kotlin
// Ở MainActivity hoặc AdminScreen
LaunchedEffect(Unit) {
    viewModel.fetchAllUsers()  // Nên thấy danh sách users
}
```

---

**Need Help?**
- Check error logs ở terminal
- Xem responses trong test_api.html
- Inspect network từ Android Studio → Debug mode
