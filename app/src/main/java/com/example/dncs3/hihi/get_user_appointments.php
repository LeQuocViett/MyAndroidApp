<?php
include 'db_config.php';
header('Content-Type: application/json');

error_reporting(0);
ini_set('display_errors', 0);

$userId = isset($_GET['userId']) ? intval($_GET['userId']) : 0;

if ($userId <= 0) {
    echo json_encode([]);
    exit;
}

// SQL: Lấy image_url từ bảng services và đặt tên là service_image
$sql = "SELECT a.*,
        u.name as userName,
        u.phone as userPhoneFromUser,
        s.name as serviceName,
        s.price as servicePrice,
        s.image_url as service_image
        FROM appointments a
        LEFT JOIN users u ON a.user_id = u.id
        LEFT JOIN services s ON a.service_id = s.id
        WHERE a.user_id = ?
        ORDER BY a.appointment_date DESC, a.appointment_time DESC";

$stmt = $conn->prepare($sql);
$stmt->bind_param("i", $userId);
$stmt->execute();
$result = $stmt->get_result();

$appointments = [];
while($row = $result->fetch_assoc()) {
    $row['id'] = (int)$row['id'];
    $row['user_id'] = (int)$row['user_id'];
    $row['service_id'] = (int)$row['service_id'];
    $row['price'] = (isset($row['price']) && $row['price'] > 0) ? (double)$row['price'] : (double)($row['servicePrice'] ?? 0);
    $row['userName'] = $row['userName'] ?? "Người dùng";
    $row['serviceName'] = $row['serviceName'] ?? "Dịch vụ không xác định";
    $row['service_image'] = $row['service_image'] ?? ""; // Đảm bảo trả về service_image
    $appointments[] = $row;
}

echo json_encode($appointments);
?>
