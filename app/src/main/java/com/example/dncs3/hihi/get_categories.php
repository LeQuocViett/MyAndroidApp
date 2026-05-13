<?php
include 'db_config.php';
$result = $conn->query("SELECT * FROM categories");
$data = [];
while($row = $result->fetch_assoc()) { $data[] = $row; }
echo json_encode($data);
?>