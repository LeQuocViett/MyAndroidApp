<?php
include 'db_config.php';
header('Content-Type: application/json');
header('Cache-Control: no-cache, must-revalidate');

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['id']) || !isset($data['status'])) {
    echo json_encode(["success" => false, "message" => "Thiếu ID hoặc Trạng thái"]);
    exit;
}

$id = intval($data['id']);
$status = strtoupper(trim($data['status']));
$cancel_reason = isset($data['cancel_reason']) ? trim($data['cancel_reason']) : "";

if ($id <= 0) {
    echo json_encode(["success" => false, "message" => "ID không hợp lệ"]);
    exit;
}

// Chuẩn bị SQL an toàn
if ($status === "CANCELLED" && !empty($cancel_reason)) {
    // Lưu lý do hủy vào cột note với tiền tố để App bóc tách được
    $sql = "UPDATE appointments SET `status` = ?, `note` = CONCAT(IFNULL(`note`, ''), '\n[Lý do hủy]: ', ?) WHERE `id` = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ssi", $status, $cancel_reason, $id);
} else {
    $sql = "UPDATE appointments SET `status` = ? WHERE `id` = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("si", $status, $id);
}

if ($stmt->execute()) {
    // Quan trọng: Kiểm tra xem có dòng nào thực sự được cập nhật không
    if ($stmt->affected_rows > 0) {
        echo json_encode(["success" => true, "message" => "Đã cập nhật trạng thái thành $status"]);
    } else {
        // Kiểm tra xem ID có tồn tại không
        $check = $conn->query("SELECT id FROM appointments WHERE id = $id");
        if ($check->num_rows == 0) {
            echo json_encode(["success" => false, "message" => "Không tìm thấy lịch hẹn có ID: $id"]);
        } else {
            // Đã tồn tại nhưng trạng thái giống hệt cũ nên affected_rows = 0
            echo json_encode(["success" => true, "message" => "Lịch hẹn đã ở trạng thái này rồi"]);
        }
    }
} else {
    echo json_encode(["success" => false, "message" => "Lỗi thực thi: " . $stmt->error]);
}

$stmt->close();
$conn->close();
?>
