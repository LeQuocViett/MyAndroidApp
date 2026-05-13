<?php
require_once 'config.php';

// GET: Tìm kiếm users theo query
$query = $_GET['query'] ?? '';
$searchType = $_GET['searchType'] ?? null;

if (empty($query)) {
    sendResponse(false, 'Vui lòng nhập từ khóa tìm kiếm');
}

$query = '%' . $query . '%';
$users = [];

// Nếu có searchType, chỉ tìm kiếm loại đó
if ($searchType === 'name') {
    $stmt = $conn->prepare("SELECT id, name, email, phone, role, status, created_at, 
        (SELECT COUNT(*) FROM appointments WHERE user_id = users.id AND status != 'CANCELLED') as total_appointments
        FROM users WHERE name LIKE ? AND status != 'DELETED' ORDER BY created_at DESC");
    $stmt->bind_param("s", $query);
} elseif ($searchType === 'email') {
    $stmt = $conn->prepare("SELECT id, name, email, phone, role, status, created_at, 
        (SELECT COUNT(*) FROM appointments WHERE user_id = users.id AND status != 'CANCELLED') as total_appointments
        FROM users WHERE email LIKE ? AND status != 'DELETED' ORDER BY created_at DESC");
    $stmt->bind_param("s", $query);
} elseif ($searchType === 'phone') {
    $stmt = $conn->prepare("SELECT id, name, email, phone, role, status, created_at, 
        (SELECT COUNT(*) FROM appointments WHERE user_id = users.id AND status != 'CANCELLED') as total_appointments
        FROM users WHERE phone LIKE ? AND status != 'DELETED' ORDER BY created_at DESC");
    $stmt->bind_param("s", $query);
} else {
    // Tìm kiếm trong cả 3 trường: name, email, phone
    $stmt = $conn->prepare("SELECT id, name, email, phone, role, status, created_at, 
        (SELECT COUNT(*) FROM appointments WHERE user_id = users.id AND status != 'CANCELLED') as total_appointments
        FROM users WHERE (name LIKE ? OR email LIKE ? OR phone LIKE ?) AND status != 'DELETED' 
        ORDER BY created_at DESC");
    $stmt->bind_param("sss", $query, $query, $query);
}

$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    $users[] = [
        'id' => intval($row['id']),
        'name' => $row['name'],
        'email' => $row['email'],
        'phone' => $row['phone'],
        'role' => $row['role'],
        'status' => $row['status'],
        'created_at' => $row['created_at'],
        'total_appointments' => intval($row['total_appointments'])
    ];
}

echo json_encode($users);
?>
