<?php
require_once 'config.php';

$data = getRequestBody();

if (isset($data['id']) && isset($data['name'])) {
    $id = intval($data['id']);
    $name = $conn->real_escape_string($data['name']);

    $sql = "UPDATE categories SET name='$name' WHERE id=$id";

    if ($conn->query($sql)) {
        sendResponse(true, 'Cập nhật danh mục thành công');
    } else {
        sendResponse(false, 'Lỗi: ' . $conn->error);
    }
} else {
    sendResponse(false, 'Thiếu thông tin danh mục');
}
?>