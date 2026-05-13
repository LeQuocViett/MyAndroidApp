<?php
include 'db_config.php';
header('Content-Type: application/json');

// Nhận dữ liệu từ JSON gửi lên từ App
$data = json_decode(file_get_contents("php://input"), true);

// Lấy dữ liệu, xử lý null
$user_id = isset($data['user_id']) ? intval($data['user_id']) : 0;
$service_id = isset($data['service_id']) ? intval($data['service_id']) : 0;
$date = $data['appointment_date'] ?? "";
$time = $data['appointment_time'] ?? "";
$name_manual = $data['userName'] ?? ""; // Khớp với SerializedName("userName") trong Kotlin
$phone = $data['user_phone'] ?? "";
$note = $data['note'] ?? "";

// LOẠI BỎ trường 'price' vì database thực tế của bạn không có cột này.
// Sử dụng dấu ngoặc ngược `` cho các tên cột để tránh lỗi từ khóa hệ thống (như note).
// Thứ tự các trường này phải khớp với cấu trúc bảng appointments của bạn.
$sql = "INSERT INTO appointments (`user_id`, `service_id`, `appointment_date`, `appointment_time`, `status`, `user_phone`, `note`, `user_name_manual`)
        VALUES (?, ?, ?, ?, 'PENDING', ?, ?, ?)";

$stmt = $conn->prepare($sql);

if ($stmt) {
    // 7 dấu ? tương ứng: i, i, s, s, s, s, s
    $stmt->bind_param("iisssss", $user_id, $service_id, $date, $time, $phone, $note, $name_manual);

    if ($stmt->execute()) {
        echo json_encode(["success" => true, "message" => "Đặt lịch thành công"]);
    } else {
        // Trả về lỗi chi tiết từ MySQL nếu thực thi thất bại
        echo json_encode(["success" => false, "message" => "Lỗi thực thi: " . $stmt->error]);
    }
    $stmt->close();
} else {
    // Trả về lỗi nếu chuẩn bị câu lệnh SQL thất bại
    echo json_encode(["success" => false, "message" => "Lỗi chuẩn bị SQL: " . $conn->error]);
}

$conn->close();
?>
