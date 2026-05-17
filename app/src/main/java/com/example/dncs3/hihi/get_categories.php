<?php
include 'db_config.php';
header('Content-Type: application/json');

$result = $conn->query("SELECT * FROM categories ORDER BY name ASC");
$data = [];

if ($result) {
    while($row = $result->fetch_assoc()) {
        $row['id'] = (int)$row['id'];
        $data[] = $row;
    }
}
echo json_encode($data);
?>