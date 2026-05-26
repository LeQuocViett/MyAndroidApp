package com.example.dncs3.ui.user

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dncs3.ui.components.*
import com.example.dncs3.viewmodel.MainViewModel

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit,
    onNavigateToUsers: () -> Unit = {},
    onNavigateToServices: () -> Unit = {},
    onNavigateToAppointments: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {}
) {
    val user = viewModel.currentUser
    val isAdmin = user?.role == "ADMIN"
    val context = LocalContext.current
    
    var showEditDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFDE4EC).copy(alpha = 0.3f))) {
        // Header Profile
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(PrimaryPink).padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(modifier = Modifier.size(90.dp).clip(CircleShape), color = Color.White) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(60.dp), tint = PrimaryPink)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = user?.name ?: "Người dùng", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                if (isAdmin) {
                    Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)) {
                        Text(text = "QUẢN TRỊ VIÊN", color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { 
                ProfileSectionCard(title = "Thông tin tài khoản", icon = Icons.Default.Badge) {
                    InfoRowItem(Icons.Default.Person, "Họ tên", user?.name ?: "")
                    InfoRowItem(Icons.Default.Email, "Email", user?.email ?: "")
                    InfoRowItem(Icons.Default.Phone, "Số điện thoại", user?.phone ?: "")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showEditDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink), shape = RoundedCornerShape(12.dp)) {
                        Text("Chỉnh sửa thông tin")
                    }
                } 
            }

            if (isAdmin) {
                item { 
                    ProfileSectionCard(title = "Quản lý hệ thống", icon = Icons.Default.Settings) {
                        ManagementActionRow(Icons.Default.People, "Quản lý người dùng", onNavigateToUsers)
                        ManagementActionRow(Icons.Default.Build, "Quản lý dịch vụ", onNavigateToServices)
                        ManagementActionRow(Icons.Default.DateRange, "Quản lý lịch hẹn", onNavigateToAppointments)
                        ManagementActionRow(Icons.Default.BarChart, "Xem thống kê chi tiết", onNavigateToStatistics)
                    } 
                }
            }

            item { 
                ProfileSectionCard(title = "Cài đặt", icon = Icons.Default.SettingsSuggest) {
                    SettingActionRow(Icons.AutoMirrored.Filled.Logout, "Đăng xuất", Color.Red, onClick = { showLogoutDialog = true })
                } 
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Đăng xuất", fontWeight = FontWeight.Bold) },
            text = { Text("Bạn có chắc chắn muốn thoát ứng dụng?") },
            confirmButton = { Button(onClick = { showLogoutDialog = false; onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Đăng xuất") } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Hủy") } }
        )
    }

    if (showEditDialog && user != null) {
        var name by remember { mutableStateOf(user.name) }
        var phone by remember { mutableStateOf(user.phone) }
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Sửa thông tin") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BeautyTextField(value = name, onValueChange = { name = it }, label = "Họ tên", placeholder = "", leadingIcon = Icons.Default.Person)
                    BeautyTextField(value = phone, onValueChange = { phone = it }, label = "Số điện thoại", placeholder = "", leadingIcon = Icons.Default.Phone)
                }
            },
            confirmButton = { 
                Button(onClick = { 
                    viewModel.updateUser(user.copy(name = name, phone = phone)) { success ->
                        if(success) {
                            showEditDialog = false
                            Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                        }
                    } 
                }) { Text("Lưu") } 
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Hủy") } }
        )
    }
}

@Composable
fun ProfileSectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = PrimaryPink, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun InfoRowItem(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ManagementActionRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = PrimaryPink, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
    }
}

@Composable
fun SettingActionRow(icon: ImageVector, title: String, textColor: Color = Color.Unspecified, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = if (textColor == Color.Red) Color.Red else Color.Gray, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f), fontSize = 14.sp, color = textColor)
        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
    }
}
