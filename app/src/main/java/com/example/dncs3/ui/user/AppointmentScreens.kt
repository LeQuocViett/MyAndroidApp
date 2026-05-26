package com.example.dncs3.ui.user

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dncs3.model.Appointment
import com.example.dncs3.ui.components.*
import com.example.dncs3.viewmodel.MainViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppointmentsScreen(viewModel: MainViewModel) {
    val appointments by viewModel.appointments.collectAsState()
    val context = LocalContext.current
    
    var selectedTab by remember { mutableStateOf("Tất cả") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    
    val tabs = listOf("Tất cả", "Chờ xác nhận", "Đã xác nhận", "Hoàn thành", "Đã huỷ")
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedApptForDetail by remember { mutableStateOf<Appointment?>(null) }
    
    LaunchedEffect(Unit) { 
        viewModel.fetchUserAppointments()
    }

    val filteredAppointments = appointments.filter { appt ->
        val matchesStatus = when (selectedTab) {
            "Chờ xác nhận" -> appt.status.equals("PENDING", ignoreCase = true)
            "Đã xác nhận" -> appt.status.equals("CONFIRMED", ignoreCase = true)
            "Hoàn thành" -> appt.status.equals("COMPLETED", ignoreCase = true)
            "Đã huỷ" -> appt.status.equals("CANCELLED", ignoreCase = true)
            else -> true
        }
        val matchesSearch = appt.serviceName.contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesSearch
    }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp) {
                Column(modifier = Modifier.background(Color.White)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (!isSearchExpanded) {
                            Text(text = "Lịch hẹn của tôi", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                            IconButton(onClick = { isSearchExpanded = true }) { Icon(Icons.Default.Search, null) }
                        } else {
                            OutlinedTextField(
                                value = searchQuery, onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Tìm dịch vụ...") },
                                trailingIcon = { IconButton(onClick = { isSearchExpanded = false; searchQuery = "" }) { Icon(Icons.Default.Close, null) } },
                                shape = RoundedCornerShape(12.dp), singleLine = true
                            )
                        }
                    }
                    
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tabs) { tab ->
                            FilterChip(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                label = { Text(tab) }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF8F9FA))) {
            if (filteredAppointments.isEmpty()) {
                EmptyAppointmentsView()
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(filteredAppointments, key = { it.id }) { appt ->
                        AppointmentCardUser(
                            appt = appt, 
                            onClick = { 
                                selectedApptForDetail = appt 
                                showBottomSheet = true
                            },
                            onCancel = { 
                                viewModel.updateAppointmentStatus(appt.id, "CANCELLED") { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            if (showBottomSheet && selectedApptForDetail != null) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomSheet = false },
                    sheetState = sheetState,
                    containerColor = Color.White
                ) {
                    AppointmentDetailContentUser(
                        appt = selectedApptForDetail!!,
                        onClose = { showBottomSheet = false }
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentCardUser(appt: Appointment, onClick: () -> Unit, onCancel: () -> Unit) {
    val df = DecimalFormat("#,###")
    val formattedPrice = df.format(appt.price) + "đ"
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }, 
        shape = RoundedCornerShape(16.dp), 
        elevation = CardDefaults.cardElevation(2.dp), 
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (appt.serviceImage.isNotEmpty()) {
                    AsyncImage(
                        model = appt.serviceImage,
                        contentDescription = null,
                        modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFDE4EC)), contentAlignment = Alignment.Center) { 
                        Icon(Icons.Default.Spa, null, tint = PrimaryPink) 
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = appt.serviceName.ifEmpty { "Dịch vụ" }, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Event, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${appt.appointmentDate} | ${appt.appointmentTime}", fontSize = 13.sp, color = Color.Gray)
                    }
                    Text(text = formattedPrice, fontWeight = FontWeight.ExtraBold, color = PrimaryPink, fontSize = 16.sp)
                }
                StatusBadgeUserAppointment(appt.status)
            }
            
            if (appt.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Ghi chú: ${appt.note}", fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            if (appt.status.equals("CANCELLED", ignoreCase = true) && appt.cancelReason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(color = Color.Red.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp)) {
                    Text(text = "Lý do hủy: ${appt.cancelReason}", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(8.dp))
                }
            }

            if (appt.status.equals("PENDING", ignoreCase = true)) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancel, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                        Text("Hủy lịch", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyAppointmentsView() {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Chưa có lịch hẹn nào.", color = Color.Gray)
    }
}

@Composable
fun AppointmentDetailContentUser(appt: Appointment, onClose: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 32.dp)) {
        Text(text = "Chi tiết lịch hẹn", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        DetailRowItem("Dịch vụ", appt.serviceName.ifEmpty { "N/A" })
        DetailRowItem("Ngày đến", appt.appointmentDate)
        DetailRowItem("Giờ đến", appt.appointmentTime)
        DetailRowItem("Giá tiền", DecimalFormat("#,###").format(appt.price) + "đ")
        DetailRowItem("Trạng thái", appt.status)
        if (appt.note.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ghi chú của bạn:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Surface(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), color = Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp)) {
                Text(text = appt.note, modifier = Modifier.padding(12.dp), fontSize = 14.sp)
            }
        }
        if (appt.cancelReason.isNotEmpty()) {
            DetailRowItem("Lý do hủy", appt.cancelReason, color = Color.Red)
        }
        Spacer(modifier = Modifier.height(24.dp))
        BeautyButton(text = "Đóng", onClick = onClose)
    }
}

@Composable
fun DetailRowItem(label: String, value: String, color: Color = Color.Black) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = "$label: ", fontWeight = FontWeight.Medium, color = Color.Gray, modifier = Modifier.width(120.dp))
        Text(text = value, color = color, fontWeight = FontWeight.SemiBold)
    }
}
