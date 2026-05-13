<?php
require_once 'config.php';

// POST: Soft delete user (đổi status = 'DELETED')
$data = getRequestBody();

if (!isset($data['id'])) {
    sendResponse(false, 'Thiếu ID người dùng');
}

$id = intval($data['id']);

// Soft delete: chỉ cập nhật status thành DELETED
$stmt = $conn->prepare("UPDATE users SET status = 'DELETED', updated_at = NOW() WHERE id = ?");
$stmt->bind_param("i", $id);

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        sendResponse(true, 'Đã xóa người dùng');
    } else {
        sendResponse(false, 'Người dùng không tồn tại');
    }
} else {
    sendResponse(false, 'Lỗi: ' . $conn->error);
}
?>
