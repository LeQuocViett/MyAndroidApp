<?php
require_once 'config.php';

// POST: Khóa tài khoản user
$data = getRequestBody();

if (!isset($data['id'])) {
    sendResponse(false, 'Thiếu ID người dùng');
}

$id = intval($data['id']);
$reason = $data['reason'] ?? '';

// Cập nhật status thành BLOCKED
$stmt = $conn->prepare("UPDATE users SET status = 'BLOCKED', updated_at = NOW() WHERE id = ?");
$stmt->bind_param("i", $id);

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        sendResponse(true, 'Đã khóa tài khoản người dùng');
    } else {
        sendResponse(false, 'Người dùng không tồn tại');
    }
} else {
    sendResponse(false, 'Lỗi: ' . $conn->error);
}
?>
