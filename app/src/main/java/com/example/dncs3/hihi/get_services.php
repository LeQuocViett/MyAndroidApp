<?php
include 'db_config.php';
header('Content-Type: application/json');

$sql = "SELECT * FROM services ORDER BY id DESC";
$result = $conn->query($sql);
$services = [];

while($row = $result->fetch_assoc()) {
    $row['id'] = (int)$row['id'];
    $row['price'] = (double)$row['price'];
    $row['category_id'] = (int)$row['category_id'];
    $row['duration'] = (int)($row['duration'] ?? 30);
    $row['status'] = $row['status'] ?? 'Hoạt động';
    $services[] = $row;
}
echo json_encode($services);
?>