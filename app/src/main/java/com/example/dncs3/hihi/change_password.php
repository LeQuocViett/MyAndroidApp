<?php
header("Content-Type: application/json");
include 'db_config.php';

$data = json_decode(file_get_contents("php://input"), true);

if (isset($data['id']) && isset($data['old_password']) && isset($data['new_password'])) {
    $id = $data['id'];
    $old_pass = $data['old_password'];
    $new_pass = $data['new_password'];

    // Lấy mật khẩu hiện tại từ database
    $stmt = $conn->prepare("SELECT password FROM users WHERE id = ?");
    $stmt->bind_param("i", $id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($row = $result->fetch_assoc()) {
        $hashed_password = $row['password'];

        // Kiểm tra mật khẩu cũ (giả sử dùng password_hash)
        if (password_verify($old_pass, $hashed_password)) {
            $new_hashed_pass = password_hash($new_pass, PASSWORD_DEFAULT);

            $update_stmt = $conn->prepare("UPDATE users SET password = ? WHERE id = ?");
            $update_stmt->bind_param("si", $new_hashed_pass, $id);

            if ($update_stmt->execute()) {
                echo json_encode(["success" => true, "message" => "Đổi mật khẩu thành công"]);
            } else {
                echo json_encode(["success" => false, "message" => "Lỗi cập nhật mật khẩu"]);
            }
            $update_stmt->close();
        } else {
            echo json_encode(["success" => false, "message" => "Mật khẩu cũ không chính xác"]);
        }
    } else {
        echo json_encode(["success" => false, "message" => "Không tìm thấy người dùng"]);
    }
    $stmt->close();
} else {
    echo json_encode(["success" => false, "message" => "Thiếu thông tin"]);
}
$conn->close();
?>
