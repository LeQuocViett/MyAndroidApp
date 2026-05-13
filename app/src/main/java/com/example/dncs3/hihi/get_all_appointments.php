<?php
include 'db_config.php';
header('Content-Type: application/json');

error_reporting(0);
ini_set('display_errors', 0);

$sql = "SELECT a.*,
        CASE WHEN (a.user_id = 0 OR a.user_id IS NULL) THEN a.user_name_manual ELSE u.name END as userName,
        CASE WHEN (a.user_id = 0 OR a.user_id IS NULL) THEN a.user_phone ELSE u.phone END as user_phone,
        s.name as serviceName,
        s.price as servicePrice
        FROM appointments a
        LEFT JOIN users u ON a.user_id = u.id
        LEFT JOIN services s ON a.service_id = s.id
        ORDER BY a.appointment_date DESC, a.appointment_time DESC";

$result = $conn->query($sql);
$appointments = [];

if ($result) {
    while($row = $result->fetch_assoc()) {
        $row['id'] = (int)$row['id'];
        $row['user_id'] = (int)$row['user_id'];
        $row['service_id'] = (int)$row['service_id'];

        // Xử lý giá tiền
        if (isset($row['price']) && $row['price'] > 0) {
            $row['price'] = (double)$row['price'];
        } else {
            $row['price'] = (double)($row['servicePrice'] ?? 0);
        }

        $row['userName'] = $row['userName'] ?? "Khách vãng lai";
        $row['user_phone'] = $row['user_phone'] ?? "";
        $row['serviceName'] = $row['serviceName'] ?? "Dịch vụ không xác định";

        // BÓC TÁCH LÝ DO HỦY: Nếu trong note có chuỗi "[Lý do hủy]: "
        $note = $row['note'] ?? "";
        $row['cancel_reason'] = "";
        if (strpos($note, "[Lý do hủy]: ") !== false) {
            $parts = explode("[Lý do hủy]: ", $note);
            $row['note'] = trim($parts[0]); // Lấy phần ghi chú gốc
            $row['cancel_reason'] = trim($parts[1]); // Lấy phần lý do hủy
        }

        $appointments[] = $row;
    }
}

echo json_encode($appointments);
?>
