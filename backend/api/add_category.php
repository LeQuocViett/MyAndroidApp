<?php
require_once 'config.php';

$data = getRequestBody();

if (isset($data['name'])) {
    $name = $conn->real_escape_string($data['name']);

    $sql = "INSERT INTO categories (name) VALUES ('$name')";

    if ($conn->query($sql)) {
        sendResponse(true, 'Thêm danh mục thành công');
    } else {
        sendResponse(false, 'Lỗi: ' . $conn->error);
    }
} else {
    sendResponse(false, 'Thiếu tên danh mục');
}
?>