<?php
include 'db_config.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents("php://input"), true);

if (isset($data['id'])) {
    $id = intval($data['id']);
    $name = $conn->real_escape_string($data['name']);
    $price = (double)$data['price'];
    $desc = $conn->real_escape_string($data['description']);
    $catId = intval($data['category_id']);
    $duration = intval($data['duration']);
    $status = $conn->real_escape_string($data['status']);
    $img = $conn->real_escape_string($data['image_url']);

    $sql = "UPDATE services SET
            name='$name',
            price='$price',
            description='$desc',
            category_id='$catId',
            duration='$duration',
            status='$status',
            image_url='$img'
            WHERE id='$id'";

    if($conn->query($sql)) {
        echo json_encode(["success" => true, "message" => "Cập nhật thành công"]);
    } else {
        echo json_encode(["success" => false, "message" => $conn->error]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Thiếu ID dịch vụ"]);
}
?>