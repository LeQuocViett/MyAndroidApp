<?php
header("Content-Type: application/json");
include 'db_config.php';
$data = json_decode(file_get_contents("php://input"), true);

if (isset($data['id'])) {
    $id = $data['id'];
    $stmt = $conn->prepare("UPDATE users SET status = 'BLOCKED' WHERE id = ?");
    $stmt->bind_param("i", $id);
    if ($stmt->execute()) {
        echo json_encode(["success" => true, "message" => "Đã khóa người dùng"]);
    } else {
        echo json_encode(["success" => false, "message" => $conn->error]);
    }
    $stmt->close();
}
$conn->close();
?>