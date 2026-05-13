<?php
header("Content-Type: application/json");
include 'db_config.php';

$sql = "SELECT a.*,
               u.name as userName, u.phone as userPhone,
               s.name as serviceName, s.image_url as service_image
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
        $row['price'] = (double)$row['price'];
        $row['service_image'] = $row['service_image'] ?? "";
        $appointments[] = $row;
    }
}

echo json_encode($appointments);
$conn->close();
?>
