<?php
header("Content-Type: application/json");
include 'db_config.php';

$data = json_decode(file_get_contents("php://input"), true);

if (isset($data['email']) && isset($data['phone']) && isset($data['new_password'])) {
    $email = $data['email'];
    $phone = $data['phone'];
    $new_password = $data['new_password'];

    // Kiểm tra xem email và số điện thoại có khớp với cùng một user không
    $stmt = $conn->prepare("SELECT id FROM users WHERE email = ? AND phone = ?");
    $stmt->bind_param("ss", $email, $phone);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($row = $result->fetch_assoc()) {
        $user_id = $row['id'];
        $hashed_password = password_hash($new_password, PASSWORD_DEFAULT);

        $update_stmt = $conn->prepare("UPDATE users SET password = ? WHERE id = ?");
        $update_stmt->bind_param("si", $hashed_password, $user_id);

        if ($update_stmt->execute()) {
            echo json_encode(["success" => true, "message" => "Đặt lại mật khẩu thành công"]);
        } else {
            echo json_encode(["success" => false, "message" => "Lỗi cập nhật mật khẩu"]);
        }
        $update_stmt->close();
    } else {
        echo json_encode(["success" => false, "message" => "Email hoặc số điện thoại không chính xác"]);
    }
    $stmt->close();
} else {
    echo json_encode(["success" => false, "message" => "Thiếu thông tin"]);
}
$conn->close();
?>
