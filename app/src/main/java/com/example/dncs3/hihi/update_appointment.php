<?php
include 'db_config.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['id'])) {
    echo json_encode(["success" => false, "message" => "Missing ID"]);
    exit;
}

$id = intval($data['id']);
$service_id = intval($data['service_id']);
$date = $data['appointment_date'] ?? "";
$time = $data['appointment_time'] ?? "";
$phone = $data['user_phone'] ?? "";
$note = $data['note'] ?? "";
$name = $data['userName'] ?? "";

// Sử dụng Prepared Statement để xử lý ghi chú có ký tự đặc biệt an toàn
$sql = "UPDATE appointments SET
        service_id = ?,
        appointment_date = ?,
        appointment_time = ?,
        user_phone = ?,
        note = ?,
        user_name_manual = ?
        WHERE id = ?";

$stmt = $conn->prepare($sql);
$stmt->bind_param("isssssi", $service_id, $date, $time, $phone, $note, $name, $id);

if ($stmt->execute()) {
    echo json_encode(["success" => true]);
} else {
    echo json_encode(["success" => false, "message" => $conn->error]);
}

$stmt->close();
$conn->close();
?>
