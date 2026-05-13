<?php
require_once 'config.php';

// POST: Cập nhật user
$data = getRequestBody();

if (!isset($data['id']) || !isset($data['email'])) {
    sendResponse(false, 'Thiếu thông tin bắt buộc');
}

$id = intval($data['id']);
$name = $data['name'] ?? '';
$email = $data['email'] ?? '';
$phone = $data['phone'] ?? '';
$status = $data['status'] ?? 'ACTIVE';

// Validate
if (empty($name) || empty($email) || empty($phone)) {
    sendResponse(false, 'Vui lòng điền đầy đủ thông tin');
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    sendResponse(false, 'Email không hợp lệ');
}

// Kiểm tra email có bị trùng không (ngoại trừ user hiện tại)
$stmt = $conn->prepare("SELECT id FROM users WHERE email = ? AND id != ?");
$stmt->bind_param("si", $email, $id);
$stmt->execute();
if ($stmt->get_result()->num_rows > 0) {
    sendResponse(false, 'Email đã tồn tại');
}

// Cập nhật user
$stmt = $conn->prepare("UPDATE users SET name = ?, email = ?, phone = ?, status = ?, updated_at = NOW() WHERE id = ?");
$stmt->bind_param("ssssi", $name, $email, $phone, $status, $id);

if ($stmt->execute()) {
    sendResponse(true, 'Cập nhật người dùng thành công');
} else {
    sendResponse(false, 'Lỗi cập nhật: ' . $conn->error);
}
?>
