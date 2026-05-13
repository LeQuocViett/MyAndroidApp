<?php
header('Content-Type: application/json');

// API Health Check & Documentation
$baseUrl = "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']);

$response = [
    'status' => 'online',
    'version' => '1.0',
    'timestamp' => date('Y-m-d H:i:s'),
    'documentation' => $baseUrl . '/API_DOCUMENTATION.md',
    'test_tool' => $baseUrl . '/test_api.html',
    'endpoints' => [
        'users' => [
            'get_all' => [
                'method' => 'GET',
                'path' => $baseUrl . '/get_users.php',
                'description' => 'Lấy tất cả người dùng'
            ],
            'search' => [
                'method' => 'GET',
                'path' => $baseUrl . '/search_users.php?query=...&searchType=...',
                'description' => 'Tìm kiếm người dùng'
            ],
            'update' => [
                'method' => 'POST',
                'path' => $baseUrl . '/update_user.php',
                'description' => 'Cập nhật người dùng'
            ],
            'delete' => [
                'method' => 'POST',
                'path' => $baseUrl . '/delete_user.php',
                'description' => 'Xóa người dùng (soft delete)'
            ],
            'block' => [
                'method' => 'POST',
                'path' => $baseUrl . '/block_user.php',
                'description' => 'Khóa tài khoản'
            ],
            'unblock' => [
                'method' => 'POST',
                'path' => $baseUrl . '/unblock_user.php',
                'description' => 'Mở khóa tài khoản'
            ]
        ],
        'categories' => [
            'get_all' => [
                'method' => 'GET',
                'path' => $baseUrl . '/get_categories.php',
                'description' => 'Lấy tất cả danh mục'
            ],
            'add' => [
                'method' => 'POST',
                'path' => $baseUrl . '/add_category.php',
                'description' => 'Thêm danh mục'
            ]
        ],
        'services' => [
            'get_all' => [
                'method' => 'GET',
                'path' => $baseUrl . '/get_services.php',
                'description' => 'Lấy tất cả dịch vụ'
            ],
            'add' => [
                'method' => 'POST',
                'path' => $baseUrl . '/add_service.php',
                'description' => 'Thêm dịch vụ'
            ],
            'update' => [
                'method' => 'POST',
                'path' => $baseUrl . '/update_service.php',
                'description' => 'Cập nhật dịch vụ'
            ],
            'delete' => [
                'method' => 'POST',
                'path' => $baseUrl . '/delete_service.php',
                'description' => 'Xóa dịch vụ'
            ]
        ],
        'appointments' => [
            'get_all' => [
                'method' => 'GET',
                'path' => $baseUrl . '/get_all_appointments.php',
                'description' => 'Lấy tất cả lịch hẹn'
            ],
            'get_user' => [
                'method' => 'GET',
                'path' => $baseUrl . '/get_user_appointments.php?userId=...',
                'description' => 'Lấy lịch hẹn của người dùng'
            ],
            'book' => [
                'method' => 'POST',
                'path' => $baseUrl . '/book_appointment.php',
                'description' => 'Đặt lịch hẹn'
            ],
            'update' => [
                'method' => 'POST',
                'path' => $baseUrl . '/update_appointment.php',
                'description' => 'Cập nhật lịch hẹn'
            ],
            'update_status' => [
                'method' => 'POST',
                'path' => $baseUrl . '/update_appointment_status.php',
                'description' => 'Cập nhật trạng thái lịch hẹn'
            ],
            'delete' => [
                'method' => 'POST',
                'path' => $baseUrl . '/delete_appointment.php',
                'description' => 'Xóa lịch hẹn'
            ]
        ]
    ],
    'quick_links' => [
        'readme' => $baseUrl . '/README.md',
        'database_schema' => $baseUrl . '/DATABASE_SCHEMA.sql',
        'api_docs' => $baseUrl . '/API_DOCUMENTATION.md',
        'test_tool' => $baseUrl . '/test_api.html'
    ]
];

echo json_encode($response, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
?>
