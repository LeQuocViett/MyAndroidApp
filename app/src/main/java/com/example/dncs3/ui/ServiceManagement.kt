package com.example.dncs3.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dncs3.model.BeautyService
import com.example.dncs3.model.Category
import com.example.dncs3.viewmodel.MainViewModel

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
                            TextButton(onClick = onNavigateToCategories) {
                                Text("Danh mục", color = Color(0xFFFF4081))
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
            text = { Text("Bạn có chắc chắn muốn xóa dịch vụ '${currentService!!.name}' không? Thao tác này không thể hoàn tác nếu đã có lịch hẹn.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteService(currentService!!.id) { success, message ->
                            if (success) {
                                showDeleteDialog = false
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        }
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
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "$label: ", fontWeight = FontWeight.Bold, modifier = Modifier.width(100.dp), fontSize = 14.sp)
        Text(text = value, fontSize = 14.sp, color = Color.DarkGray)
    }
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
