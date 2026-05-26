package com.example.dncs3.ui.admin

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dncs3.model.User
import com.example.dncs3.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(viewModel: MainViewModel) {
    val users by viewModel.users.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("TẤT CẢ") }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showConfirmSaveDialog by remember { mutableStateOf(false) }

    var selectedUser by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var pendingUserToSave by remember { mutableStateOf<Pair<User, String>?>(null) }

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.fetchAllUsers() }

    val filteredUsers = users.filter { user ->
        val matchesSearch = user.name.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true) ||
                user.phone.contains(searchQuery)
        val matchesFilter = statusFilter == "TẤT CẢ" || user.status == statusFilter
        user.status != "DELETED" && matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                    TopAppBar(title = { Text("Quản Lý Người Dùng", fontWeight = FontWeight.Bold) })
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm tên, email, SĐT...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, null)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = statusFilter == "TẤT CẢ",
                                onClick = { statusFilter = "TẤT CẢ" },
                                label = { Text("Tất cả") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = statusFilter == "ACTIVE",
                                onClick = { statusFilter = "ACTIVE" },
                                label = { Text("🟢 ACTIVE") }
                            )
                        }
                        item {
                            FilterChip(
                                selected = statusFilter == "BLOCKED",
                                onClick = { statusFilter = "BLOCKED" },
                                label = { Text("🔴 BLOCKED") }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedUser = null
                    showEditDialog = true
                },
                containerColor = Color(0xFFFF4081),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Thêm người dùng")
            }
        }
    ) { padding ->
        if (filteredUsers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Không có dữ liệu người dùng", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredUsers, key = { it.id }) { user ->
                    UserItemCard(
                        user = user,
                        onClick = {
                            selectedUser = user
                            showBottomSheet = true
                        },
                        onEdit = {
                            selectedUser = user
                            showEditDialog = true
                        },
                        onDelete = {
                            userToDelete = user
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showBottomSheet && selectedUser != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            UserDetailContent(
                user = selectedUser!!,
                onBlock = {
                    viewModel.updateUser(selectedUser!!.copy(status = "BLOCKED")) {
                        if (it) {
                            showBottomSheet = false
                            Toast.makeText(context, "Đã khóa tài khoản", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onUnblock = {
                    viewModel.updateUser(selectedUser!!.copy(status = "ACTIVE")) {
                        if (it) {
                            showBottomSheet = false
                            Toast.makeText(context, "Đã mở khóa tài khoản", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }

    if (showEditDialog) {
        UserEditDialog(
            user = selectedUser,
            existingEmails = users.map { it.email },
            onDismiss = { showEditDialog = false },
            onConfirm = { user, pass ->
                if (selectedUser == null) {
                    val map = mapOf("name" to user.name, "email" to user.email, "phone" to user.phone, "password" to pass)
                    viewModel.register(map) { success, msg ->
                        if (success) {
                            showEditDialog = false
                            viewModel.fetchAllUsers()
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    pendingUserToSave = user to pass
                    showConfirmSaveDialog = true
                }
            }
        )
    }

    if (showConfirmSaveDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmSaveDialog = false },
            title = { Text("Xác nhận lưu") },
            text = { Text("Bạn có chắc chắn muốn cập nhật thông tin người dùng?") },
            confirmButton = {
                Button(onClick = {
                    pendingUserToSave?.let { (u, _) ->
                        viewModel.updateUser(u) { success ->
                            if (success) {
                                showConfirmSaveDialog = false
                                showEditDialog = false
                                Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { showConfirmSaveDialog = false }) { Text("Hủy") } }
        )
    }

    if (showDeleteDialog && userToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Xóa người dùng '${userToDelete?.name}'? (Chuyển sang DELETED)") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUser(userToDelete!!.copy(status = "DELETED")) {
                            if (it) {
                                showDeleteDialog = false
                                Toast.makeText(context, "Đã xóa", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Xóa") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Hủy") } }
        )
    }
}

@Composable
fun UserItemCard(user: User, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(50.dp).clip(CircleShape), color = Color(0xFFFDE4EC)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Color(0xFFFF4081), modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Text(user.email, fontSize = 13.sp, color = Color.Gray, maxLines = 1)
                Text(user.phone, fontSize = 13.sp, color = Color.Gray)
                StatusBadgeUser(user.status)
            }
            Column {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color(0xFF2196F3)) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
            }
        }
    }
}

@Composable
fun StatusBadgeUser(status: String) {
    val (color, label) = when (status) {
        "ACTIVE" -> Color(0xFF4CAF50) to "ACTIVE"
        "BLOCKED" -> Color(0xFFFF5252) to "BLOCKED"
        "DELETED" -> Color(0xFF9E9E9E) to "DELETED"
        else -> Color.Gray to status
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}

@Composable
fun UserDetailContent(user: User, onBlock: () -> Unit, onUnblock: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp)) {
        Text("Chi tiết người dùng", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Họ tên: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp), fontSize = 14.sp)
            Text(user.name, fontSize = 14.sp)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Email: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp), fontSize = 14.sp)
            Text(user.email, fontSize = 14.sp)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Số điện thoại: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp), fontSize = 14.sp)
            Text(user.phone, fontSize = 14.sp)
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Trạng thái: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp), fontSize = 14.sp)
            StatusBadgeUser(user.status)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Ngày tạo: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp), fontSize = 14.sp)
            Text(user.createdAt.take(10).ifEmpty { "N/A" }, fontSize = 14.sp)
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text("Tổng lịch hẹn: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp), fontSize = 14.sp)
            Text(user.totalAppointments.toString(), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
        if (user.status == "ACTIVE") {
            Button(onClick = onBlock, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("KHÓA TÀI KHOẢN")
            }
        } else if (user.status == "BLOCKED") {
            Button(onClick = onUnblock, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                Text("MỞ KHÓA TÀI KHOẢN")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEditDialog(user: User?, existingEmails: List<String>, onDismiss: () -> Unit, onConfirm: (User, String) -> Unit) {
    var name by remember { mutableStateOf(user?.name ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    var pass by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(user?.status ?: "ACTIVE") }
    var error by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Thêm người dùng" else "Sửa người dùng", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { OutlinedTextField(value = name, onValueChange = { name = it; error = "" }, label = { Text("Họ tên *") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = email, onValueChange = { email = it; error = "" }, label = { Text("Email *") }, modifier = Modifier.fillMaxWidth(), enabled = user == null) }
                item { OutlinedTextField(value = phone, onValueChange = { phone = it; error = "" }, label = { Text("Số điện thoại *") }, modifier = Modifier.fillMaxWidth()) }
                if (user == null) {
                    item { OutlinedTextField(value = pass, onValueChange = { pass = it; error = "" }, label = { Text("Mật khẩu *") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()) }
                } else {
                    item {
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            OutlinedTextField(
                                value = status, onValueChange = {}, readOnly = true, label = { Text("Trạng thái") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                listOf("ACTIVE", "BLOCKED").forEach { s ->
                                    DropdownMenuItem(text = { Text(s) }, onClick = { status = s; expanded = false })
                                }
                            }
                        }
                    }
                }
                if (error.isNotEmpty()) item { Text(error, color = Color.Red, fontSize = 12.sp) }
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    name.isBlank() || email.isBlank() || phone.isBlank() -> error = "Vui lòng nhập đầy đủ"
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> error = "Email không hợp lệ"
                    user == null && existingEmails.any { it.equals(email, true) } -> error = "Email đã tồn tại"
                    user == null && pass.isBlank() -> error = "Vui lòng nhập mật khẩu"
                    else -> onConfirm(User(id = user?.id ?: 0, name = name, email = email, phone = phone, status = status), pass)
                }
            }) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}
