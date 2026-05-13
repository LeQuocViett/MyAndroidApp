<?php
include 'db_config.php';
$data = json_decode(file_get_contents("php://input"), true);

$email = $data['email'] ?? '';
$password = $data['password'] ?? '';

$sql = "SELECT * FROM users WHERE email = '$email'";
$result = $conn->query($sql);

if ($result && $result->num_rows > 0) {
    $user = $result->fetch_assoc();
    // Kiểm tra: Nếu khớp mã hóa (password_verify) HOẶC khớp mật khẩu thô thì cho qua
    if (password_verify($password, $user['password']) || $password === $user['password']) {
        unset($user['password']);
        echo json_encode(["success" => true, "message" => "Thành công", "user" => $user]);
    } else {
        echo json_encode(["success" => false, "message" => "Sai mật khẩu"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Email không tồn tại"]);
}
?>