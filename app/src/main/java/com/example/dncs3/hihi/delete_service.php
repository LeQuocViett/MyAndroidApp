<?php
include 'db_config.php';
$data = json_decode(file_get_contents("php://input"), true);
$id = $data['id'];
$sql = "DELETE FROM services WHERE id='$id'";
$conn->query($sql);
echo json_encode(["success" => true]);
?>