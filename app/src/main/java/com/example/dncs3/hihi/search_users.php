<?php
header("Content-Type: application/json");
include 'db_config.php';

$query = isset($_GET['query']) ? $_GET['query'] : '';
$type = isset($_GET['type']) ? $_GET['type'] : null;

$sql = "SELECT id, name, email, phone, role, status, created_at FROM users WHERE (name LIKE ? OR email LIKE ? OR phone LIKE ?)";
$params = ["%$query%", "%$query%", "%$query%"];
$types = "sss";

if ($type !== null) {
    $sql .= " AND role = ?";
    $params[] = $type;
    $types .= "s";
}

$stmt = $conn->prepare($sql);
$stmt->bind_param($types, ...$params);
$stmt->execute();
$result = $stmt->get_result();

$users = [];
while ($row = $result->fetch_assoc()) {
    $users[] = $row;
}

echo json_encode($users);

$stmt->close();
$conn->close();
?>