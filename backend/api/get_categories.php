<?php
require_once 'config.php';

$sql = "SELECT * FROM categories ORDER BY name ASC";
$result = $conn->query($sql);
$categories = [];

if ($result) {
    while($row = $result->fetch_assoc()) {
        $row['id'] = intval($row['id']);
        $categories[] = $row;
    }
    echo json_encode($categories);
} else {
    echo json_encode([]);
}
?>