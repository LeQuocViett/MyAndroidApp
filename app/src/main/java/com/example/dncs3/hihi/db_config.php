<?php
$host = "localhost";
$user = "root";
$pass = "";
$db   = "beauty_db";

$conn = new mysqli($host, $user, $pass, $db);

if ($conn->connect_error) {
    header('Content-Type: application/json');
    die(json_encode(["success" => false, "message" => "Kết nối thất bại: " . $conn->connect_error]));
}
$conn->set_charset("utf8");
?>