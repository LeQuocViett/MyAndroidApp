<?php
include 'db_config.php';
$data = json_decode(file_get_contents("php://input"), true);

$name = $data['name'];
$email = $data['email'];
$phone = $data['phone'];
$password = password_hash($data['password'], PASSWORD_DEFAULT);

$sql = "INSERT INTO users (name, email, phone, password, role) VALUES ('$name', '$email', '$phone', '$password', 'USER')";

if ($conn->query($sql) === TRUE) {
    echo json_encode(["success" => true, "message" => "Đăng ký thành công"]);
} else {
    echo json_encode(["success" => false, "message" => "Lỗi: " . $conn->error]);
}
?>