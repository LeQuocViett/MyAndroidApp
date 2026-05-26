<?php
include 'db_config.php';
header('Content-Type: application/json');

// Tắt hiển thị lỗi trực tiếp để tránh làm hỏng cấu trúc JSON
error_reporting(0);
ini_set('display_errors', 0);

$userId = isset($_GET['userId']) ? intval($_GET['userId']) : 0;

if ($userId <= 0) {
    echo json_encode([]);
    exit;
}

// Thêm s.image_url as service_image để hiển thị ảnh trên App
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

    // Xử lý giá: Ưu tiên giá lúc đặt (nếu có lưu trong bảng appointments), nếu không lấy giá từ bảng services
    if (isset($row['price']) && $row['price'] > 0) {
        $row['price'] = (double)$row['price'];
    } else {
        $row['price'] = (double)($row['servicePrice'] ?? 0);
    }

    // Xử lý tên hiển thị
    $row['userName'] = $row['userName'] ?? $row['user_name_manual'] ?? "Người dùng";

    // Xử lý số điện thoại: Ưu tiên sđt trong lịch hẹn
    $row['user_phone'] = !empty($row['user_phone']) ? $row['user_phone'] : ($row['userPhoneFromUser'] ?? "");

    $row['serviceName'] = $row['serviceName'] ?? "Dịch vụ không xác định";
    $row['service_image'] = $row['service_image'] ?? "";
    $row['note'] = $row['note'] ?? "";
    $row['cancel_reason'] = $row['cancel_reason'] ?? "";

    $appointments[] = $row;
}

echo json_encode($appointments);
?>
