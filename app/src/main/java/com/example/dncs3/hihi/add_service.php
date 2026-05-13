<?php
include 'db_config.php';
header('Content-Type: application/json');

$data = json_decode(file_get_contents("php://input"), true);
if (!$data) { echo json_encode(["success" => false, "message" => "Dữ liệu trống"]); exit; }

$name = $conn->real_escape_string($data['name']);
$price = (double)$data['price'];
$desc = $conn->real_escape_string($data['description']);
$catId = intval($data['category_id']);
$duration = intval($data['duration']);
$status = $conn->real_escape_string($data['status']);
$img = $conn->real_escape_string($data['image_url']);

$sql = "INSERT INTO services (name, price, description, category_id, duration, status, image_url)
        VALUES ('$name', '$price', '$desc', '$catId', '$duration', '$status', '$img')";

if($conn->query($sql)) echo json_encode(["success" => true]);
else echo json_encode(["success" => false, "message" => $conn->error]);
?>
