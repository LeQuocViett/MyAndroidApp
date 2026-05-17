<?php
include 'db_config.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents("php://input"), true);

if (isset($data['id'])) {
    $id = intval($data['id']);

    // Check if there are services in this category
    $checkSql = "SELECT COUNT(*) as count FROM services WHERE category_id = $id";
    $checkResult = $conn->query($checkSql);
    $row = $checkResult->fetch_assoc();

    if ($row['count'] > 0) {
        echo json_encode([
            "success" => false,
            "message" => "Không thể xóa danh mục này vì đang có dịch vụ thuộc danh mục này. Hãy xóa hoặc chuyển dịch vụ sang danh mục khác trước."
        ]);
    } else {
        $sql = "DELETE FROM categories WHERE id = $id";
        if ($conn->query($sql)) {
            echo json_encode(["success" => true, "message" => "Xóa danh mục thành công"]);
        } else {
            echo json_encode(["success" => false, "message" => "Lỗi database: " . $conn->error]);
        }
    }
} else {
    echo json_encode(["success" => false, "message" => "Thiếu ID danh mục"]);
}
?>