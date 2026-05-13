<?php
header("Content-Type: application/json");
include 'db_config.php';

// Quan trọng: Phải lấy cả cột status và created_at
$sql = "SELECT id, name, email, phone, role, status, created_at FROM users";
$result = $conn->query($sql);
$users = [];

if ($result) {
    while($row = $result->fetch_assoc()) {
        $users[] = $row;
    }
}
echo json_encode($users);
$conn->close();
?>