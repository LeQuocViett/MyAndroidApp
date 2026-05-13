package com.example.dncs3.ui

import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dncs3.model.Appointment
import com.example.dncs3.model.BeautyService
import com.example.dncs3.model.Category
import com.example.dncs3.model.User
import com.example.dncs3.viewmodel.MainViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ========================
// 1. QUẢN LÝ DỊCH VỤ (ADMIN)
// ========================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminServicesScreen(viewModel: MainViewModel, onNavigateToCategories: () -> Unit) {
    val services by viewModel.services.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<Int?>(null) }

    var showServiceDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentService by remember { mutableStateOf<BeautyService?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchServices()
        viewModel.fetchCategories()
    }

    val filteredServices = services.filter { service ->
        val catName = categories.find { it.id == service.categoryId }?.name ?: ""
        val matchesSearch = service.name.contains(searchQuery, ignoreCase = true) ||
                catName.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryFilter == null || service.categoryId == selectedCategoryFilter
        matchesSearch && matchesCategory
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                    TopAppBar(
                        title = { Text("Quản Lý Dịch Vụ", fontWeight = FontWeight.Bold) },
                        actions = {
                            IconButton(onClick = onNavigateToCategories) {
                                Icon(Icons.Default.Category, contentDescription = "Danh mục")
                            }
                        }
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm theo tên hoặc danh mục...") },
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
                                selected = selectedCategoryFilter == null,
                                onClick = { selectedCategoryFilter = null },
                                label = { Text("Tất cả") }
                            )
                        }
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategoryFilter == category.id,
                                onClick = { selectedCategoryFilter = category.id },
                                label = { Text(category.name) }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    currentService = null
                    showServiceDialog = true
                },
                containerColor = Color(0xFFFF4081),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Thêm Dịch Vụ")
            }
        }
    ) { padding ->
        if (filteredServices.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy dịch vụ nào", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredServices) { service ->
                    val categoryName = categories.find { it.id == service.categoryId }?.name ?: "Chưa phân loại"
                    ServiceAdminItem(
                        service = service,
                        categoryName = categoryName,
                        onClick = {
                            currentService = service
                            showDetailDialog = true
                        },
                        onEdit = {
                            currentService = service
                            showServiceDialog = true
                        },
                        onDelete = {
                            currentService = service
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showServiceDialog) {
        ServiceEditDialog(
            service = currentService,
            categories = categories,
            onDismiss = { showServiceDialog = false },
            onConfirm = { updatedService ->
                if (currentService == null) {
                    viewModel.addService(updatedService) { success ->
                        if (success) {
                            showServiceDialog = false
                            Toast.makeText(context, "Thêm dịch vụ thành công", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    viewModel.updateService(updatedService) { success ->
                        if (success) {
                            showServiceDialog = false
                            Toast.makeText(context, "Cập nhật dịch vụ thành công", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )
    }

    if (showDetailDialog && currentService != null) {
        val categoryName = categories.find { it.id == currentService!!.categoryId }?.name ?: "Chưa phân loại"
        ServiceInfoDialog(
            service = currentService!!,
            categoryName = categoryName,
            onDismiss = { showDetailDialog = false }
        )
    }

    if (showDeleteDialog && currentService != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa dịch vụ '${currentService!!.name}' không?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteService(currentService!!.id)
                        showDeleteDialog = false
                        Toast.makeText(context, "Đã xóa dịch vụ", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Xóa") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Hủy") }
            }
        )
    }
}

@Composable
fun ServiceAdminItem(
    service: BeautyService,
    categoryName: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (service.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = service.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(85.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFDE4EC)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(85.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFDE4EC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = Color(0xFFFF4081)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(service.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Text("${service.price.toInt()} VNĐ", color = Color(0xFFFF4081), fontWeight = FontWeight.Bold)
                Text("Danh mục: $categoryName", fontSize = 12.sp, color = Color.Gray)
                Text("Thời gian: ${service.duration} phút", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadgeDichVu(status = service.status)
            }
            Column(verticalArrangement = Arrangement.Center) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Sửa", tint = Color(0xFF2196F3))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Xóa", tint = Color.Red)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceEditDialog(
    service: BeautyService?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (BeautyService) -> Unit
) {
    var name by remember { mutableStateOf(service?.name ?: "") }
    var price by remember { mutableStateOf(service?.price?.toInt()?.toString() ?: "") }
    var imageUrl by remember { mutableStateOf(service?.imageUrl ?: "") }
    var desc by remember { mutableStateOf(service?.description ?: "") }
    var duration by remember { mutableStateOf(service?.duration?.toString() ?: "45") }
    var status by remember { mutableStateOf(service?.status ?: "Hoạt động") }
    var selectedCategoryId by remember { mutableIntStateOf(service?.categoryId ?: if (categories.isNotEmpty()) categories[0].id else 0) }

    var categoryExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (service == null) "Thêm Dịch Vụ Mới" else "Chỉnh Sửa Dịch Vụ", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên dịch vụ *") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Giá (VNĐ) *") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Thời gian (phút) *") }, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Link hình ảnh") }, modifier = Modifier.fillMaxWidth()) }
                item {
                    ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                        val currentCat = categories.find { it.id == selectedCategoryId }
                        OutlinedTextField(
                            value = currentCat?.name ?: "Chọn danh mục",
                            onValueChange = {}, readOnly = true, label = { Text("Danh mục") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategoryId = category.id
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
                        OutlinedTextField(
                            value = status, onValueChange = {}, readOnly = true, label = { Text("Trạng thái") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                            listOf("Hoạt động", "Tạm ngưng").forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        status = s
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                item { OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Mô tả") }, modifier = Modifier.fillMaxWidth(), minLines = 3) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: 0.0
                    val d = duration.toIntOrNull() ?: 0
                    if (name.isNotBlank() && p > 0 && d > 0) {
                        onConfirm(
                            BeautyService(
                                id = service?.id ?: 0,
                                name = name,
                                price = p,
                                description = desc,
                                imageUrl = imageUrl,
                                categoryId = selectedCategoryId,
                                duration = d,
                                status = status
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
            ) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Composable
fun ServiceInfoDialog(service: BeautyService, categoryName: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(service.name, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (service.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = service.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFDE4EC)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFDE4EC)),
                        contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color(0xFFFF4081),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                Text("${service.price.toInt()} VNĐ", color = Color(0xFFFF4081), fontWeight = FontWeight.Bold, fontSize = 22.sp)
                DetailRow("Danh mục", categoryName)
                DetailRow("Thời gian", "${service.duration} phút")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Trạng thái: ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    StatusBadgeDichVu(service.status)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text("Mô tả:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(service.description, fontSize = 14.sp, color = Color.DarkGray)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Đóng") }
        }
    )
}

@Composable
fun StatusBadgeDichVu(status: String) {
    val color = if (status == "Hoạt động") Color(0xFF4CAF50) else Color.Red
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

// ========================
// 2. QUẢN LÝ LỊCH HẸN (ADMIN)
// ========================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppointmentsScreen(viewModel: MainViewModel) {
    val appointments by viewModel.appointments.collectAsState()
    val services by viewModel.services.collectAsState()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val tabs = listOf("Tất cả", "Chờ xác nhận", "Đã xác nhận", "Hoàn thành", "Đã huỷ")
    val statusMap = listOf("TẤT CẢ", "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED")

    var showEditDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var apptToCancel by remember { mutableStateOf<Appointment?>(null) }
    var cancelReason by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchAllAppointments()
        viewModel.fetchServices()
    }

    val filteredAppts = appointments.filter { appt ->
        val matchesSearch = appt.userName.contains(searchQuery, true) ||
                appt.serviceName.contains(searchQuery, true) ||
                appt.userPhone.contains(searchQuery)
        val matchesFilter = selectedTab == 0 || appt.status == statusMap[selectedTab]
        matchesSearch && matchesFilter
    }.sortedByDescending { it.appointmentDate + it.appointmentTime }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.shadow(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    TopAppBar(
                        title = { Text("Quản lý lịch hẹn", fontWeight = FontWeight.ExtraBold) },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )

                    // Ô tìm kiếm hiện đại
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Tìm theo tên, số điện thoại...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, null)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF4081),
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                        )
                    )

                    // Tab trạng thái chiều ngang
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 16.dp,
                        containerColor = Color.Transparent,
                        contentColor = Color(0xFFFF4081),
                        divider = {},
                        indicator = { tabPositions ->
                            if (selectedTab < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = Color(0xFFFF4081)
                                )
                            }
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showEditDialog = true },
                containerColor = Color(0xFFFF4081),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Tạo lịch hẹn mới")
            }
        }
    ) { padding ->
        if (filteredAppts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Không có lịch hẹn nào", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredAppts) { appt ->
                    AppointmentItemAdminModern(
                        appt = appt,
                        onConfirm = {
                            viewModel.updateAppointmentStatus(appt.id, "CONFIRMED") { success, msg ->
                                if (success) Toast.makeText(context, "Đã xác nhận lịch hẹn", Toast.LENGTH_SHORT).show()
                                else Toast.makeText(context, "Lỗi: $msg", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onComplete = {
                            viewModel.updateAppointmentStatus(appt.id, "COMPLETED") { success, msg ->
                                if (success) Toast.makeText(context, "Đã hoàn thành lịch hẹn", Toast.LENGTH_SHORT).show()
                                else Toast.makeText(context, "Lỗi: $msg", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onCancel = {
                            apptToCancel = appt
                            cancelReason = ""
                            showCancelDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        AppointmentEditDialogAdmin(
            appt = null,
            services = services,
            onDismiss = { showEditDialog = false },
            onConfirm = { newAppt ->
                viewModel.bookAppointment(newAppt) { success ->
                    if (success) {
                        showEditDialog = false
                        Toast.makeText(context, "Tạo lịch hẹn thành công!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Lỗi khi tạo lịch hẹn", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    if (showCancelDialog && apptToCancel != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Lý do hủy lịch hẹn", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Vui lòng nhập lý do hủy cho khách ${apptToCancel?.userName}")
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        placeholder = { Text("Ví dụ: Cửa hàng quá tải, Khách yêu cầu...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )

                    val commonReasons = listOf("Cửa hàng quá tải", "Không liên lạc được với khách", "Khách yêu cầu huỷ", "Hết khung giờ trống")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(commonReasons) { reason ->
                            SuggestionChip(
                                onClick = { cancelReason = reason },
                                label = { Text(reason, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cancelReason.isNotBlank()) {
                            viewModel.updateAppointmentStatus(apptToCancel!!.id, "CANCELLED", cancelReason) { success, msg ->
                                if (success) {
                                    showCancelDialog = false
                                    Toast.makeText(context, "Đã hủy lịch hẹn", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Lỗi: $msg", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Vui lòng nhập lý do hủy", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Xác nhận hủy") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("Quay lại") }
            }
        )
    }
}

@Composable
fun AppointmentItemAdminModern(
    appt: Appointment,
    onConfirm: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = appt.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF2D3436)
                    )
                    Text(
                        text = appt.userPhone,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                StatusBadgeAppointmentModern(appt.status)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRowModern(Icons.Default.ContentPaste, "Dịch vụ: ${appt.serviceName}")
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        InfoRowModern(Icons.Default.CalendarToday, appt.appointmentDate)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        InfoRowModern(Icons.Default.AccessTime, appt.appointmentTime)
                    }
                }
                if (appt.note.isNotEmpty()) {
                    InfoRowModern(Icons.AutoMirrored.Filled.Notes, "Ghi chú: ${appt.note}")
                }
                if (appt.status == "CANCELLED" && appt.cancelReason.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, modifier = Modifier.size(16.dp), tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lý do hủy: ${appt.cancelReason}", fontSize = 14.sp, color = Color.Red, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (appt.status == "PENDING") {
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Xác nhận", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hủy", fontWeight = FontWeight.Bold)
                    }
                } else if (appt.status == "CONFIRMED") {
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hoàn thành", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hủy", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRowModern(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFF4081))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 14.sp, color = Color.DarkGray)
    }
}

@Composable
fun StatusBadgeAppointmentModern(status: String) {
    val (color, label) = when(status) {
        "PENDING" -> Color(0xFFFFC107) to "Chờ xác nhận"
        "CONFIRMED" -> Color(0xFF4CAF50) to "Đã xác nhận"
        "COMPLETED" -> Color(0xFF2196F3) to "Hoàn thành"
        "CANCELLED" -> Color(0xFFF44336) to "Đã hủy"
        else -> Color.Gray to status
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun AppointmentItemAdmin(appt: Appointment, onClick: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(appt.serviceName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Khách: ${appt.userName}", style = MaterialTheme.typography.bodyMedium)
                Text("${appt.appointmentDate} | ${appt.appointmentTime}", color = Color.Gray, fontSize = 12.sp)
                StatusBadgeAppointment(appt.status)
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = Color.Blue) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
            }
        }
    }
}

@Composable
fun StatusBadgeAppointment(status: String) {
    val (color, label) = when(status) {
        "PENDING" -> Color(0xFFFFC107) to "Chờ duyệt"
        "CONFIRMED" -> Color(0xFF4CAF50) to "Đã xác nhận"
        "COMPLETED" -> Color(0xFF2196F3) to "Hoàn thành"
        "CANCELLED" -> Color(0xFFF44336) to "Đã hủy"
        else -> Color.Gray to status
    }
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(top = 4.dp)) {
        Text(text = label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
    }
}

@Composable
fun AppointmentDetailDialogAdmin(appt: Appointment, onDismiss: () -> Unit, onUpdateStatus: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chi tiết lịch hẹn", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Khách hàng", appt.userName)
                DetailRow("Số điện thoại", appt.userPhone)
                DetailRow("Dịch vụ", appt.serviceName)
                DetailRow("Ngày giờ", "${appt.appointmentDate} ${appt.appointmentTime}")
                DetailRow("Ghi chú", appt.note.ifEmpty { "(Không có)" })
                Row { Text("Trạng thái: ", fontWeight = FontWeight.Bold); StatusBadgeAppointment(appt.status) }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Cập nhật trạng thái:", fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (appt.status == "PENDING") {
                        Button(onClick = { onUpdateStatus("CONFIRMED") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), modifier = Modifier.weight(1f)) { Text("Xác nhận", fontSize = 12.sp) }
                    }
                    if (appt.status == "CONFIRMED") {
                        Button(onClick = { onUpdateStatus("COMPLETED") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)), modifier = Modifier.weight(1f)) { Text("Xong", fontSize = 12.sp) }
                        Button(onClick = { onUpdateStatus("CANCELLED") }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.weight(1f)) { Text("Hủy", fontSize = 12.sp) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Đóng") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentEditDialogAdmin(appt: Appointment?, services: List<BeautyService>, onDismiss: () -> Unit, onConfirm: (Appointment) -> Unit) {
    var name by remember { mutableStateOf(appt?.userName ?: "") }
    var phone by remember { mutableStateOf(appt?.userPhone ?: "") }
    var date by remember { mutableStateOf(appt?.appointmentDate ?: LocalDate.now().toString()) }
    var time by remember { mutableStateOf(appt?.appointmentTime ?: LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))) }
    var sId by remember { mutableIntStateOf(appt?.serviceId ?: if(services.isNotEmpty()) services[0].id else 0) }
    var note by remember { mutableStateOf(appt?.note ?: "") }
    var exp by remember { mutableStateOf(false) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if(appt == null) "Tạo lịch hẹn mới" else "Sửa lịch hẹn", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tên khách hàng *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Person, null) }
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Số điện thoại *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Phone, null) }
                    )
                }
                item {
                    ExposedDropdownMenuBox(expanded = exp, onExpandedChange = { exp = it }) {
                        val s = services.find { it.id == sId }
                        OutlinedTextField(
                            value = s?.name ?: "Chọn dịch vụ",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Chọn dịch vụ *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(exp) },
                            leadingIcon = { Icon(Icons.Default.Spa, null) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
                            services.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = { sId = s.id; exp = false }
                                )
                            }
                        }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Chọn ngày (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.CalendarToday, null) }
                        )
                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("Chọn giờ (HH:MM)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Default.AccessTime, null) }
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Ghi chú") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if(name.isNotBlank() && phone.isNotBlank() && date.isNotBlank() && time.isNotBlank()) {
                        val service = services.find { it.id == sId }
                        onConfirm(Appointment(
                            id = appt?.id ?: 0,
                            userId = appt?.userId ?: 0, // 0 for walk-in customer
                            serviceId = sId,
                            appointmentDate = date,
                            appointmentTime = time,
                            status = appt?.status ?: "PENDING",
                            userName = name,
                            userPhone = phone,
                            note = note,
                            price = service?.price ?: 0.0
                        ))
                    } else {
                        Toast.makeText(context, "Vui lòng điền đầy đủ thông tin bắt buộc!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Text("Tạo lịch hẹn", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text("Hủy", color = Color.Gray)
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("$label: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp), fontSize = 14.sp)
        Text(value, fontSize = 14.sp)
    }
}

// ========================
// 3. QUẢN LÝ DANH MỤC (ADMIN)
// ========================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoriesScreen(viewModel: MainViewModel) {
    val categories by viewModel.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var categoryName by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { viewModel.fetchCategories() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Quản Lý Danh Mục", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Color(0xFFFF4081), contentColor = Color.White) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(categories) { category ->
                ListItem(
                    headlineContent = { Text(category.name) },
                    leadingContent = { Icon(Icons.Default.Category, null, tint = Color(0xFFFF4081)) }
                )
                HorizontalDivider()
            }
        }
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Thêm danh mục mới") },
                text = { OutlinedTextField(value = categoryName, onValueChange = { categoryName = it }, label = { Text("Tên danh mục") }, modifier = Modifier.fillMaxWidth()) },
                confirmButton = {
                    Button(onClick = {
                        if (categoryName.isNotBlank()) {
                            viewModel.addCategory(categoryName) { if(it) showAddDialog = false }
                        }
                    }) { Text("Thêm") }
                },
                dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Hủy") } }
            )
        }
    }
}

// ========================
// 4. QUẢN LÝ NGƯỜI DÙNG (ADMIN)
// ========================

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
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                userToDelete = user
                                showDeleteDialog = true
                                false
                            } else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color.Red else Color.Transparent
                            Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(color).padding(end = 20.dp), contentAlignment = Alignment.CenterEnd) {
                                Icon(Icons.Default.Delete, null, tint = Color.White)
                            }
                        }
                    ) {
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
        DetailRow("Họ tên", user.name)
        DetailRow("Email", user.email)
        DetailRow("Số điện thoại", user.phone)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Trạng thái: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp), fontSize = 14.sp)
            StatusBadgeUser(user.status)
        }
        DetailRow("Ngày tạo", user.createdAt.take(10).ifEmpty { "N/A" })
        DetailRow("Tổng lịch hẹn", user.totalAppointments.toString())

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

// ========================
// 5. THỐNG KÊ (ADMIN)
// ========================

enum class StatPeriod { TODAY, MONTH, YEAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatisticsScreen(viewModel: MainViewModel) {
    val users by viewModel.users.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val services by viewModel.services.collectAsState()

    var selectedPeriod by remember { mutableStateOf(StatPeriod.TODAY) }
    var isLoading by remember { mutableStateOf(false) }

    val primaryPink = Color(0xFFFF4081)
    val lightPink = Color(0xFFFDE4EC)

    LaunchedEffect(Unit) {
        isLoading = true
        viewModel.fetchAllUsers()
        viewModel.fetchAllAppointments()
        viewModel.fetchServices()
        isLoading = false
    }

    val today = LocalDate.now()

    // Định dạng tiền: 15.000.000đ
    val symbols = DecimalFormatSymbols(Locale.getDefault())
    symbols.groupingSeparator = '.'
    val df = DecimalFormat("#,##0", symbols)

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val monthFormatter = DateTimeFormatter.ofPattern("MM/yyyy")

    val timelineText = when (selectedPeriod) {
        StatPeriod.TODAY -> "Dữ liệu ngày ${today.format(formatter)}"
        StatPeriod.MONTH -> "Dữ liệu tháng ${today.format(monthFormatter)}"
        StatPeriod.YEAR -> "Dữ liệu năm ${today.year}"
    }

    val totalCustomers = users.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thống kê", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(lightPink.copy(alpha = 0.3f))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Bộ lọc: Hôm nay, Tháng, Năm
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                StatPeriod.entries.forEachIndexed { index, period ->
                    val label = when(period) {
                        StatPeriod.TODAY -> "Hôm nay"
                        StatPeriod.MONTH -> "Tháng"
                        StatPeriod.YEAR -> "Năm"
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = StatPeriod.entries.size),
                        onClick = { selectedPeriod = period },
                        selected = selectedPeriod == period,
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = primaryPink,
                            activeContentColor = Color.White,
                            inactiveContainerColor = Color.White
                        )
                    ) {
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator(color = primaryPink, modifier = Modifier.size(32.dp))
            } else {
                Text(
                    text = timelineText,
                    fontSize = 15.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = selectedPeriod,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "stat_content"
                ) { targetPeriod ->
                    // Use targetPeriod instead of selectedPeriod for the animated content
                    val currentFilteredAppts = appointments.filter { appt ->
                        val apptDate = try {
                            if (appt.appointmentDate.contains("-")) LocalDate.parse(appt.appointmentDate)
                            else LocalDate.parse(appt.appointmentDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        } catch(e: Exception) { null }

                        if (apptDate == null) false
                        else {
                            when (targetPeriod) {
                                StatPeriod.TODAY -> apptDate == today
                                StatPeriod.MONTH -> apptDate.month == today.month && apptDate.year == today.year
                                StatPeriod.YEAR -> apptDate.year == today.year
                            }
                        }
                    }

                    val currentTotalAppts = currentFilteredAppts.size
                    val currentTotalRevenue = currentFilteredAppts.filter { it.status == "COMPLETED" }.sumOf { appt ->
                        if (appt.price > 0) appt.price
                        else services.find { it.id == appt.serviceId }?.price ?: 0.0
                    }
                    val currentRevenueText = df.format(currentTotalRevenue) + "đ"

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "Khách hàng",
                            value = totalCustomers.toString(),
                            icon = Icons.Default.People,
                            iconColor = Color(0xFF2196F3)
                        )
                        StatCard(
                            title = "Lịch hẹn",
                            value = currentTotalAppts.toString(),
                            icon = Icons.Default.CalendarMonth,
                            iconColor = Color(0xFFFF9800)
                        )
                        StatCard(
                            title = "Doanh thu",
                            value = currentRevenueText,
                            icon = Icons.Default.AttachMoney,
                            iconColor = Color(0xFF4CAF50),
                            isLarge = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    isLarge: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(title, fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = value,
                    fontSize = if (isLarge) 26.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLarge) Color(0xFFFF4081) else Color.Black
                )
            }
        }
    }
}
