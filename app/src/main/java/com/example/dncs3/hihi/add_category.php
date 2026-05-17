<?php
include 'db_config.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents("php://input"), true);

if (isset($data['name']) && !empty(trim($data['name']))) {
    $name = $conn->real_escape_string(trim($data['name']));

    // Kiểm tra xem danh mục đã tồn tại chưa
    $check = $conn->query("SELECT id FROM categories WHERE name = '$name'");
    if ($check->num_rows > 0) {
        echo json_encode(["success" => false, "message" => "Tên danh mục này đã tồn tại"]);
    } else {
        $sql = "INSERT INTO categories (name) VALUES ('$name')";
        if ($conn->query($sql)) {
            echo json_encode(["success" => true, "message" => "Thêm danh mục thành công"]);
        } else {
            echo json_encode(["success" => false, "message" => "Lỗi: " . $conn->error]);
        }
    }
} else {
    echo json_encode(["success" => false, "message" => "Vui lòng nhập tên danh mục"]);
}
?>