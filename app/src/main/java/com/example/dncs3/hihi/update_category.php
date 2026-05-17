<?php
include 'db_config.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents("php://input"), true);

if (isset($data['id']) && isset($data['name'])) {
    $id = intval($data['id']);
    $name = $conn->real_escape_string($data['name']);

    $sql = "UPDATE categories SET name='$name' WHERE id='$id'";

    if($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Cập nhật danh mục thành công"]);
    } else {
        echo json_encode(["success" => false, "message" => $conn->error]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Thiếu thông tin danh mục"]);
}
?>