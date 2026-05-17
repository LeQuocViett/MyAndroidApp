package com.example.dncs3.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dncs3.model.Appointment
import com.example.dncs3.model.BeautyService
import com.example.dncs3.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

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
                        title = { Text("Quản lý lịch hẹn", fontWeight = FontWeight.ExtraBold) }
                    )

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
                            userId = appt?.userId ?: 0,
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
