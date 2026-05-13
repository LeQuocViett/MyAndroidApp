<?php
include 'db_config.php';
$data = json_decode(file_get_contents("php://input"), true);
$name = $data['name'];
$conn->query("INSERT INTO categories (name) VALUES ('$name')");
echo json_encode(["success" => true]);
?>