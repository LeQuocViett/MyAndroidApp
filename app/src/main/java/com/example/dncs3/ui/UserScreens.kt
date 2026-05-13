package com.example.dncs3.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dncs3.model.Appointment
import com.example.dncs3.model.BeautyService
import com.example.dncs3.model.User
import com.example.dncs3.viewmodel.MainViewModel
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

enum class BookingStep { DATE, TIME, CONFIRM }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    service: BeautyService,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onBookingSuccess: () -> Unit
) {
    var currentStep by remember { mutableStateOf(BookingStep.DATE) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val context = LocalContext.current
    
    val primaryColor = Color(0xFFD81B60) 
    val displayDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = when(currentStep) {
                            BookingStep.DATE -> "Chọn ngày"
                            BookingStep.TIME -> "Chọn giờ"
                            BookingStep.CONFIRM -> "Xác nhận đặt lịch"
                        }, 
                        color = Color.White, 
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            when(currentStep) {
                                BookingStep.DATE -> onBack()
                                BookingStep.TIME -> currentStep = BookingStep.DATE
                                BookingStep.CONFIRM -> currentStep = BookingStep.TIME
                            }
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .size(36.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.ChevronLeft, "Back", tint = Color.Black)
                    }
                },
                actions = {
                    if (currentStep == BookingStep.TIME) {
                        Surface(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clickable {
                                    if (selectedTime == null) {
                                        Toast.makeText(context, "Vui lòng chọn giờ!", Toast.LENGTH_SHORT).show()
                                        return@clickable
                                    }
                                    currentStep = BookingStep.CONFIRM
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White
                        ) {
                            Text(
                                text = "Tiếp tục", 
                                color = primaryColor, 
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 14.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = primaryColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(if (currentStep == BookingStep.DATE) Color(0xFFF8F9FA) else Color.White)
                .verticalScroll(rememberScrollState())
        ) {
            ServiceSummaryHeader(service)

            when(currentStep) {
                BookingStep.DATE -> {
                    Card(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { }
                                ) {
                                    Text(
                                        text = "tháng ${currentMonth.monthValue} năm ${currentMonth.year}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                                Row {
                                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                                        Icon(Icons.Default.ChevronLeft, null, tint = Color.Gray)
                                    }
                                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                                        Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                listOf("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "CN").forEach { day ->
                                    Text(
                                        text = day,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            CalendarGrid(currentMonth, selectedDate, primaryColor) { selectedDate = it }

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Trở về",
                                    modifier = Modifier
                                        .clickable { onBack() }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Chọn",
                                    modifier = Modifier
                                        .clickable { 
                                            currentStep = BookingStep.TIME 
                                            selectedTime = null
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = Color(0xFF2ECC71),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                    NoteSection()
                    Spacer(modifier = Modifier.height(16.dp))
                    LegendSection()
                }
                BookingStep.TIME -> {
                    TimeSelectionContent(
                        selectedDate = selectedDate,
                        selectedTime = selectedTime,
                        onTimeSelected = { selectedTime = it }
                    )
                }
                BookingStep.CONFIRM -> {
                    ConfirmBookingContent(
                        service = service,
                        date = selectedDate.format(displayDateFormatter),
                        time = selectedTime ?: "",
                        note = note,
                        onNoteChange = { note = it },
                        onDateClick = { currentStep = BookingStep.DATE },
                        onTimeClick = { currentStep = BookingStep.TIME },
                        onConfirm = {
                            val user = viewModel.currentUser
                            if (user == null) {
                                Toast.makeText(context, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show()
                                return@ConfirmBookingContent
                            }
                            val appt = Appointment(
                                userId = user.id,
                                serviceId = service.id,
                                appointmentDate = selectedDate.toString(),
                                appointmentTime = selectedTime!!,
                                price = service.price,
                                note = note,
                                userName = user.name,
                                userPhone = user.phone,
                                serviceName = service.name,
                                serviceImage = service.imageUrl
                            )
                            viewModel.bookAppointment(appt) { success ->
                                if (success) {
                                    Toast.makeText(context, "Đặt lịch thành công!", Toast.LENGTH_SHORT).show()
                                    onBookingSuccess()
                                } else {
                                    Toast.makeText(context, "Có lỗi xảy ra khi đặt lịch!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ServiceSummaryHeader(service: BeautyService) {
    val df = DecimalFormat("#,###")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (service.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = service.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFDE4EC)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Spa, null, tint = Color(0xFFD81B60))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = service.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "${df.format(service.price)}đ", color = Color(0xFFD81B60), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = service.description, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun ConfirmBookingContent(
    service: BeautyService,
    date: String,
    time: String,
    note: String,
    isEdit: Boolean = false,
    onNoteChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onConfirm: () -> Unit
) {
    val df = DecimalFormat("#,###")
    val formattedPrice = df.format(service.price) + "đ"

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 24.dp)) {
            if (service.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = service.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(85.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(85.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFDE4EC)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFFD81B60), modifier = Modifier.size(32.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = service.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "Dịch vụ làm đẹp chuyên nghiệp", color = Color.Gray, fontSize = 14.sp)
            }
        }
        HorizontalDivider(modifier = Modifier.padding(bottom = 24.dp), color = Color.LightGray.copy(alpha = 0.5f))
        Text(text = "Ngày đến", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = date,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().clickable { onDateClick() },
            readOnly = true, enabled = false,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.LightGray, disabledLeadingIconColor = Color.Gray, disabledTrailingIconColor = Color.Gray),
            leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
            trailingIcon = { Icon(Icons.Default.ChevronRight, null) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Giờ đến", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = time,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth().clickable { onTimeClick() },
            readOnly = true, enabled = false,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.LightGray, disabledLeadingIconColor = Color.Gray, disabledTrailingIconColor = Color.Gray),
            leadingIcon = { Icon(Icons.Default.AccessTime, null) },
            trailingIcon = { Icon(Icons.Default.ChevronRight, null) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Ghi chú", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth().height(120.dp),
            placeholder = { Text("Ghi chú cho cửa hàng (Ví dụ: yêu cầu nhân viên,...)") },
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Tổng thanh toán", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Text(text = formattedPrice, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFFD81B60))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = if (isEdit) "Cập nhật ngay" else "Đặt lịch ngay", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun TimeSelectionContent(selectedDate: LocalDate, selectedTime: String?, onTimeSelected: (String) -> Unit) {
    val morningTimes = listOf("08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30")
    val afternoonTimes = listOf("13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00", "18:30")
    val eveningTimes = listOf("19:00", "19:30")
    val isToday = selectedDate == LocalDate.now()
    val currentTime = LocalTime.now()

    Column(modifier = Modifier.padding(16.dp)) {
        TimeGroupSection(Icons.Default.LightMode, "Buổi sáng", Color(0xFFE67E22), morningTimes, selectedTime, isToday, currentTime, onTimeSelected)
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(24.dp))
        TimeGroupSection(Icons.Default.WbTwilight, "Buổi chiều", Color(0xFFD35400), afternoonTimes, selectedTime, isToday, currentTime, onTimeSelected)
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(24.dp))
        TimeGroupSection(Icons.Default.NightsStay, "Buổi tối", Color(0xFFD81B60), eveningTimes, selectedTime, isToday, currentTime, onTimeSelected)
    }
}

@Composable
fun TimeGroupSection(icon: ImageVector, title: String, iconColor: Color, times: List<String>, selectedTime: String?, isToday: Boolean, currentTime: LocalTime, onTimeSelected: (String) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        times.chunked(3).forEach { rowTimes ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowTimes.forEach { time ->
                    val timeValue = LocalTime.parse(time)
                    val isPast = isToday && timeValue.isBefore(currentTime)
                    Box(modifier = Modifier.weight(1f)) {
                        TimeSlotChip(time, time == selectedTime, enabled = !isPast) { onTimeSelected(time) }
                    }
                }
                repeat(3 - rowTimes.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun TimeSlotChip(time: String, isSelected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, when {
            isSelected -> Color(0xFFD81B60)
            !enabled -> Color.LightGray.copy(alpha = 0.5f)
            else -> Color(0xFFE0E0E0)
        }),
        color = when {
            isSelected -> Color(0xFFD81B60).copy(alpha = 0.1f)
            !enabled -> Color(0xFFF5F5F5)
            else -> Color.White
        }
    ) {
        Text(text = time, modifier = Modifier.padding(vertical = 14.dp), textAlign = TextAlign.Center, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = when { isSelected -> Color(0xFFD81B60); !enabled -> Color.LightGray; else -> Color(0xFF616161) }, fontSize = 15.sp)
    }
}

@Composable
fun CalendarGrid(currentMonth: YearMonth, selectedDate: LocalDate, primaryColor: Color, onDateSelected: (LocalDate) -> Unit) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val startOffset = currentMonth.atDay(1).dayOfWeek.value - 1
    Column {
        for (row in 0 until 6) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - startOffset + 1
                    Box(modifier = Modifier.weight(1f).aspectRatio(1.2f), contentAlignment = Alignment.Center) {
                        if (dayNum in 1..daysInMonth) {
                            val date = currentMonth.atDay(dayNum)
                            val isSelected = date == selectedDate
                            val isToday = date == LocalDate.now()
                            val isPast = date.isBefore(LocalDate.now())
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(if (isSelected) Color(0xFF2ECC71) else Color.Transparent).clickable(enabled = !isPast) { onDateSelected(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = dayNum.toString(), color = when { isSelected -> Color.White; isPast -> Color.LightGray; else -> Color.Black }, fontSize = 14.sp, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
            if (row >= 4 && (row * 7 + 7 - startOffset) > daysInMonth) break
        }
    }
}

@Composable
fun NoteSection() {
    Row(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.size(32.dp).background(Color(0xFFFFE0B2), CircleShape), contentAlignment = Alignment.Center) {
            Text("!", color = Color(0xFFE65100), fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text("Lưu ý", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "Bạn có thể đặt những lịch trống trước giờ đến ít nhất 4 giờ để cửa hàng chuẩn bị tốt hơn khi bạn đến nhé!", fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)
        }
    }
}

@Composable
fun LegendSection() {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        LegendItem(Color(0xFFBDBDBD), "Ngày không chọn được")
        LegendItem(Color(0xFF0D1B2A), "Ngày trống")
        LegendItem(Color(0xFF2ECC71), "Ngày đang chọn")
        LegendItem(Color(0xFFFF5252), "Ngày nghỉ")
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Box(modifier = Modifier.size(14.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, color = Color.Gray, fontSize = 14.sp)
    }
}

// ========================
// LỊCH HẸN CỦA TÔI (USER)
// ========================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAppointmentsScreen(viewModel: MainViewModel) {
    val appointments by viewModel.appointments.collectAsState()
    val services by viewModel.services.collectAsState()
    
    var editingAppointment by remember { mutableStateOf<Appointment?>(null) }
    var selectedApptForDetail by remember { mutableStateOf<Appointment?>(null) }
    var selectedTab by remember { mutableStateOf("Tất cả") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    
    val tabs = listOf("Tất cả", "Chờ xác nhận", "Đã xác nhận", "Hoàn thành", "Đã huỷ")
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) { 
        viewModel.fetchUserAppointments()
        viewModel.fetchServices()
    }

    val filteredAppointments = appointments.filter { appt ->
        val matchesStatus = when (selectedTab) {
            "Chờ xác nhận" -> appt.status == "PENDING"
            "Đã xác nhận" -> appt.status == "CONFIRMED"
            "Hoàn thành" -> appt.status == "COMPLETED"
            "Đã huỷ" -> appt.status == "CANCELLED"
            else -> true
        }
        val matchesSearch = (appt.serviceName ?: "").contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesSearch
    }

    if (editingAppointment != null) {
        val service = services.find { it.id == editingAppointment!!.serviceId }
        if (service != null) {
            EditBookingScreen(
                appointment = editingAppointment!!,
                service = service,
                viewModel = viewModel,
                onBack = { editingAppointment = null },
                onUpdateSuccess = { 
                    editingAppointment = null
                    viewModel.fetchUserAppointments()
                }
            )
        } else {
            editingAppointment = null
            Toast.makeText(LocalContext.current, "Không tìm thấy thông tin dịch vụ", Toast.LENGTH_SHORT).show()
        }
    } else {
        Scaffold(
            topBar = {
                Surface(shadowElevation = 0.dp) {
                    Column(modifier = Modifier.background(Color.White)) {
                        AnimatedVisibility(visible = !isSearchExpanded) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Lịch hẹn của tôi", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                Row {
                                    IconButton(onClick = { isSearchExpanded = true }) { Icon(Icons.Default.Search, null, tint = Color.Gray) }
                                }
                            }
                        }

                        AnimatedVisibility(visible = isSearchExpanded) {
                            OutlinedTextField(
                                value = searchQuery, onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                                placeholder = { Text("Tìm kiếm dịch vụ...") },
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFFD81B60)) },
                                trailingIcon = { IconButton(onClick = { isSearchExpanded = false; searchQuery = "" }) { Icon(Icons.Default.Close, null) } },
                                shape = RoundedCornerShape(16.dp), singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFD81B60), unfocusedBorderColor = Color(0xFFF1F1F1), focusedContainerColor = Color(0xFFF8F9FA), unfocusedContainerColor = Color(0xFFF8F9FA))
                            )
                        }
                        
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(tabs) { tab ->
                                StatusTabPill(title = tab, isSelected = selectedTab == tab, onClick = { selectedTab = tab })
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF8F9FA))) {
                if (filteredAppointments.isEmpty()) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.size(100.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.AutoMirrored.Filled.List, null, modifier = Modifier.size(48.dp), tint = Color.LightGray) }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("Không tìm thấy lịch hẹn nào.", color = Color.Gray, fontWeight = FontWeight.Medium)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(filteredAppointments, key = { it.id }) { appt ->
                            AppointmentCardUser(
                                appt = appt, 
                                services = services,
                                onClick = { 
                                    selectedApptForDetail = appt 
                                    showBottomSheet = true
                                },
                                onEdit = { editingAppointment = appt }, 
                                onCancel = { viewModel.updateAppointmentStatus(appt.id, "CANCELLED") }
                            )
                        }
                    }
                }

                if (showBottomSheet && selectedApptForDetail != null) {
                    ModalBottomSheet(
                        onDismissRequest = { showBottomSheet = false },
                        sheetState = sheetState,
                        containerColor = Color.White,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    ) {
                        AppointmentDetailContentUser(
                            appt = selectedApptForDetail!!,
                            services = services,
                            onClose = { showBottomSheet = false }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusTabPill(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = if (isSelected) Color(0xFFFFD1DC) else Color.White,
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFF1F1F1)),
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Text(text = title, modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp), color = if (isSelected) Color.White else Color(0xFF424242), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
fun AppointmentCardUser(appt: Appointment, services: List<BeautyService>, onClick: () -> Unit, onEdit: () -> Unit, onCancel: () -> Unit) {
    val df = DecimalFormat("#,###")
    val displayPrice = if (appt.price > 0) appt.price else services.find { it.id == appt.serviceId }?.price ?: 0.0
    val formattedPrice = df.format(displayPrice) + "đ"
    
    // FALLBACK LOGIC: Tìm ảnh từ danh sách services nếu appt.serviceImage trống
    val imageUrl = if (appt.serviceImage.isNotEmpty()) appt.serviceImage 
                   else services.find { it.id == appt.serviceId }?.imageUrl ?: ""

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }, 
        shape = RoundedCornerShape(20.dp), 
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), 
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier.size(85.dp).clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.size(85.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFFDE4EC)), contentAlignment = Alignment.Center) { 
                        Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFFD81B60), modifier = Modifier.size(36.dp)) 
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = appt.serviceName ?: "Dịch vụ", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${appt.appointmentDate} | ${appt.appointmentTime}", fontSize = 13.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = formattedPrice, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD81B60), fontSize = 18.sp)
                }
                StatusBadgeUserAppointment(appt.status)
            }
            
            if (appt.status == "CANCELLED" && appt.cancelReason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color.Red.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Lý do hủy: ${appt.cancelReason}", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            if (appt.status == "PENDING" || appt.status == "CONFIRMED") {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(thickness = 1.dp, color = Color(0xFFF8F9FA))
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    if (appt.status == "PENDING") {
                        ActionIconButton(icon = Icons.Default.Edit, tint = Color(0xFF2196F3)) { onEdit() }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    ActionIconButton(icon = Icons.Default.Close, tint = Color(0xFFF44336)) { onCancel() }
                }
            }
        }
    }
}

@Composable
fun AppointmentDetailContentUser(appt: Appointment, services: List<BeautyService>, onClose: () -> Unit) {
    val df = DecimalFormat("#,###")
    val displayPrice = if (appt.price > 0) appt.price else services.find { it.id == appt.serviceId }?.price ?: 0.0
    val formattedPrice = df.format(displayPrice) + "đ"
    
    // FALLBACK LOGIC
    val imageUrl = if (appt.serviceImage.isNotEmpty()) appt.serviceImage 
                   else services.find { it.id == appt.serviceId }?.imageUrl ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Chi tiết lịch hẹn", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, null) }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (imageUrl.isNotEmpty()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Crop
            )
        }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRowItem("Dịch vụ", appt.serviceName ?: "N/A")
                DetailRowItem("Ngày đến", appt.appointmentDate)
                DetailRowItem("Giờ đến", appt.appointmentTime)
                DetailRowItem("Giá tiền", formattedPrice, color = Color(0xFFD81B60))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Trạng thái: ", fontWeight = FontWeight.Medium, modifier = Modifier.width(100.dp), color = Color.Gray)
                    StatusBadgeUserAppointment(appt.status)
                }
                if (appt.status == "CANCELLED" && appt.cancelReason.isNotEmpty()) {
                    DetailRowItem("Lý do hủy", appt.cancelReason, color = Color.Red)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(text = "Ghi chú của bạn", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFDE4EC).copy(alpha = 0.3f),
            border = BorderStroke(1.dp, Color(0xFFFDE4EC))
        ) {
            Text(
                text = if (appt.note.isBlank()) "(Bạn không để lại ghi chú nào)" else appt.note,
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD81B60))
        ) {
            Text("Đã hiểu", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DetailRowItem(label: String, value: String, color: Color = Color.Black) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.Medium, modifier = Modifier.width(100.dp), color = Color.Gray)
        Text(text = value, color = color, fontWeight = if (color != Color.Black) FontWeight.Bold else FontWeight.SemiBold)
    }
}

@Composable
fun StatusBadgeUserAppointment(status: String) {
    val (color, label) = when (status) {
        "PENDING" -> Color(0xFFFFB300) to "Chờ xác nhận"
        "CONFIRMED" -> Color(0xFF2196F3) to "Đã duyệt"
        "COMPLETED" -> Color(0xFF43A047) to "Hoàn thành"
        "CANCELLED" -> Color(0xFFF44336) to "Đã hủy"
        else -> Color.Gray to status
    }
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(10.dp)) { 
        Text(text = label, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold) 
    }
}

@Composable
fun ActionIconButton(icon: ImageVector, tint: Color, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.85f else 1f, label = "scale")
    Surface(
        modifier = Modifier.size(42.dp).scale(scale).clickable(interactionSource = interactionSource, indication = null) { onClick() }, 
        shape = CircleShape, 
        color = tint.copy(alpha = 0.12f)
    ) { 
        Box(contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp)) } 
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookingScreen(
    appointment: Appointment,
    service: BeautyService,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onUpdateSuccess: () -> Unit
) {
    var currentStep by remember { mutableStateOf(BookingStep.DATE) }
    var selectedDate by remember { mutableStateOf(LocalDate.parse(appointment.appointmentDate)) }
    var selectedTime by remember { mutableStateOf<String?>(appointment.appointmentTime) }
    var note by remember { mutableStateOf(appointment.note) }
    var currentMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    val context = LocalContext.current
    val primaryColor = Color(0xFFD81B60)
    val displayDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = when(currentStep) { BookingStep.DATE -> "Sửa ngày"; BookingStep.TIME -> "Sửa giờ"; BookingStep.CONFIRM -> "Xác nhận sửa" }, color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = { 
                    IconButton(onClick = { 
                        when(currentStep) { 
                            BookingStep.DATE -> onBack()
                            BookingStep.TIME -> currentStep = BookingStep.DATE
                            BookingStep.CONFIRM -> currentStep = BookingStep.TIME 
                        } 
                    }, modifier = Modifier.padding(8.dp).size(36.dp).background(Color.White, CircleShape)) { 
                        Icon(Icons.Default.ChevronLeft, "Back", tint = Color.Black) 
                    } 
                },
                actions = {
                    if (currentStep == BookingStep.TIME) {
                        Surface(modifier = Modifier.padding(end = 12.dp).clickable { 
                            if (selectedTime == null) { 
                                Toast.makeText(context, "Vui lòng chọn giờ!", Toast.LENGTH_SHORT).show()
                                return@clickable 
                            }; currentStep = BookingStep.CONFIRM 
                        }, shape = RoundedCornerShape(8.dp), color = Color.White) { 
                            Text(text = "Tiếp tục", color = primaryColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 14.sp) 
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = primaryColor)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().background(if (currentStep == BookingStep.DATE) Color(0xFFF8F9FA) else Color.White).verticalScroll(rememberScrollState())) {
            // Hiển thị thông tin dịch vụ đang sửa
            ServiceSummaryHeader(service)

            when(currentStep) {
                BookingStep.DATE -> {
                    Card(modifier = Modifier.padding(16.dp).fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { }) { Text(text = "tháng ${currentMonth.monthValue} năm ${currentMonth.year}", fontWeight = FontWeight.Bold, fontSize = 18.sp); Icon(Icons.Default.ArrowDropDown, null) }
                                Row { 
                                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) { Icon(Icons.Default.ChevronLeft, null, tint = Color.Gray) }
                                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) { Icon(Icons.Default.ChevronRight, null, tint = Color.Gray) } 
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth()) { listOf("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "CN").forEach { day -> Text(text = day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray) } }
                            Spacer(modifier = Modifier.height(12.dp)); CalendarGrid(currentMonth, selectedDate, primaryColor) { selectedDate = it }
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Hủy", modifier = Modifier.clickable { onBack() }.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.Gray, fontWeight = FontWeight.Medium)
                                Text(text = "Tiếp theo", modifier = Modifier.clickable { currentStep = BookingStep.TIME }.padding(horizontal = 16.dp, vertical = 8.dp), color = Color(0xFF2ECC71), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
                BookingStep.TIME -> { TimeSelectionContent(selectedDate = selectedDate, selectedTime = selectedTime, onTimeSelected = { selectedTime = it }) }
                BookingStep.CONFIRM -> {
                    ConfirmBookingContent(
                        service = service, 
                        date = selectedDate.format(displayDateFormatter), 
                        time = selectedTime ?: "", 
                        note = note, 
                        isEdit = true,
                        onNoteChange = { note = it }, 
                        onDateClick = { currentStep = BookingStep.DATE }, 
                        onTimeClick = { currentStep = BookingStep.TIME },
                        onConfirm = {
                            val updatedAppt = appointment.copy(appointmentDate = selectedDate.toString(), appointmentTime = selectedTime!!, note = note)
                            viewModel.updateAppointment(updatedAppt) { success ->
                                if (success) { 
                                    Toast.makeText(context, "Cập nhật lịch hẹn thành công!", Toast.LENGTH_SHORT).show()
                                    onUpdateSuccess() 
                                } else { 
                                    Toast.makeText(context, "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show() 
                                }
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EditAppointmentDialog(appointment: Appointment, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var date by remember { mutableStateOf(appointment.appointmentDate ?: "") }
    var time by remember { mutableStateOf(appointment.appointmentTime ?: "") }
    var errorMsg by remember { mutableStateOf("") }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(context, { _, y, m, d -> date = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d); errorMsg = "" }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    val timePickerDialog = TimePickerDialog(context, { _, h, m -> time = String.format(Locale.getDefault(), "%02d:%02d", h, m); errorMsg = "" }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sửa Lịch Hẹn") },
        text = {
            Column {
                OutlinedTextField(value = date, onValueChange = { }, label = { Text("Chọn Ngày") }, modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }, readOnly = true, enabled = true, trailingIcon = { Icon(Icons.Default.DateRange, null) })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = time, onValueChange = { }, label = { Text("Chọn Giờ") }, modifier = Modifier.fillMaxWidth().clickable { timePickerDialog.show() }, readOnly = true, enabled = true, trailingIcon = { Icon(Icons.Default.AccessTime, null) })
                if (errorMsg.isNotEmpty()) Text(errorMsg, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { Button(onClick = { if (date.isNotEmpty() && time.isNotEmpty()) onConfirm(date, time) else errorMsg = "Vui lòng chọn đủ ngày giờ!" }) { Text("Cập nhật") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val users by viewModel.users.collectAsState()
    val allAppointments by viewModel.appointments.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showChangePassDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isAdmin) { if (isAdmin) { viewModel.fetchAllUsers(); viewModel.fetchAllAppointments() } }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFDE4EC).copy(alpha = 0.3f))) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp).background(Color(0xFFFF4081)).padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(90.dp).clip(CircleShape).background(Color.White).padding(4.dp), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, modifier = Modifier.size(70.dp), tint = Color(0xFFFF4081)) }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = user?.name ?: "Người dùng", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                if (isAdmin) Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)) { Text(text = "ADMIN", color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { ProfileSectionCard(title = "Thông tin cá nhân", icon = Icons.Default.Info) {
                InfoRowItem(Icons.Default.Person, "Họ tên", user?.name ?: "")
                InfoRowItem(Icons.Default.Email, "Email", user?.email ?: "")
                InfoRowItem(Icons.Default.Phone, "Số điện thoại", user?.phone ?: "")
                Button(onClick = { showEditDialog = true }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)), shape = RoundedCornerShape(12.dp)) { Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Chỉnh sửa") }
            } }
            if (isAdmin) {
                item { ProfileSectionCard(title = "Quản lý hệ thống", icon = Icons.Default.Settings) {
                    ManagementActionRow(Icons.Default.People, "Quản lý người dùng", onNavigateToUsers)
                    ManagementActionRow(Icons.Default.Build, "Quản lý dịch vụ", onNavigateToServices)
                    ManagementActionRow(Icons.Default.DateRange, "Quản lý lịch hẹn", onNavigateToAppointments)
                    ManagementActionRow(Icons.Default.BarChart, "Xem thống kê chi tiết", onNavigateToStatistics)
                } }
                item { ProfileSectionCard(title = "Thống kê nhanh", icon = Icons.Default.Assessment) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val today = LocalDate.now().toString()
                        val currentMonth = today.take(7)
                        val apptsToday = allAppointments.count { it.appointmentDate == today }
                        val monthlyRevenue = allAppointments.filter { it.status == "COMPLETED" && it.appointmentDate.startsWith(currentMonth) }.sumOf { it.price }
                        val df = DecimalFormat("#,###")
                        val revenueStr = if (monthlyRevenue >= 1_000_000) "${DecimalFormat("#.#").format(monthlyRevenue / 1_000_000)}M" else "${df.format(monthlyRevenue / 1000)}K"
                        StatItem(Modifier.weight(1f), "Khách hàng", users.size.toString())
                        StatItem(Modifier.weight(1f), "Lịch hôm nay", apptsToday.toString())
                        StatItem(Modifier.weight(1f), "Doanh thu", revenueStr)
                    }
                } }
            }
            item { ProfileSectionCard(title = "Cài đặt", icon = Icons.Default.Tune) {
                SettingActionRow(Icons.Default.Lock, "Đổi mật khẩu", onClick = { showChangePassDialog = true })
                SettingActionRow(Icons.Default.Notifications, "Thông báo")
                SettingActionRow(Icons.AutoMirrored.Filled.Logout, "Đăng xuất", Color.Red, onClick = { showLogoutDialog = true })
            } }
        }
    }
    if (showLogoutDialog) AlertDialog(onDismissRequest = { showLogoutDialog = false }, title = { Text("Xác nhận đăng xuất", fontWeight = FontWeight.Bold) }, text = { Text("Bạn có chắc chắn muốn rời khỏi phiên làm việc này không?") }, confirmButton = { Button(onClick = { showLogoutDialog = false; onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Đăng xuất") } }, dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Hủy") } })
    if (showEditDialog && user != null) {
        var n by remember { mutableStateOf(user.name) }; var p by remember { mutableStateOf(user.phone) }
        AlertDialog(onDismissRequest = { showEditDialog = false }, title = { Text("Chỉnh sửa thông tin") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(value = n, onValueChange = { n = it }, label = { Text("Họ tên") }, modifier = Modifier.fillMaxWidth()); OutlinedTextField(value = p, onValueChange = { p = it }, label = { Text("Số điện thoại") }, modifier = Modifier.fillMaxWidth()) } }, confirmButton = { Button(onClick = { viewModel.updateUser(user.copy(name = n, phone = p)) { if (it) { showEditDialog = false; Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show() } } }) { Text("Lưu") } }, dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Hủy") } })
    }
    if (showChangePassDialog) {
        var o by remember { mutableStateOf("") }; var n by remember { mutableStateOf("") }; var c by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showChangePassDialog = false }, title = { Text("Đổi mật khẩu") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(value = o, onValueChange = { o = it }, label = { Text("Mật khẩu cũ") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()); OutlinedTextField(value = n, onValueChange = { n = it }, label = { Text("Mật khẩu mới") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()); OutlinedTextField(value = c, onValueChange = { c = it }, label = { Text("Xác nhận mật khẩu mới") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()) } }, confirmButton = { Button(onClick = { if (n != c) { Toast.makeText(context, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show(); return@Button }; viewModel.changePassword(o, n) { s, m -> Toast.makeText(context, m, Toast.LENGTH_SHORT).show(); if (s) showChangePassDialog = false } }) { Text("Đổi mật khẩu") } }, dismissButton = { TextButton(onClick = { showChangePassDialog = false }) { Text("Hủy") } })
    }
}

@Composable
fun ProfileSectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) { Column(modifier = Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Color(0xFFFF4081), modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp) }; Spacer(Modifier.height(12.dp)); content() } }
}

@Composable
fun InfoRowItem(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(12.dp)); Column { Text(label, fontSize = 12.sp, color = Color.Gray); Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium) } }
}

@Composable
fun ManagementActionRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = Color(0xFFFF4081), modifier = Modifier.size(24.dp)); Spacer(Modifier.width(16.dp)); Text(title, modifier = Modifier.weight(1f), fontSize = 15.sp); Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray) }
}

@Composable
fun SettingActionRow(icon: ImageVector, title: String, textColor: Color = Color.Unspecified, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = if (textColor == Color.Red) Color.Red else Color.Gray, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(16.dp)); Text(title, modifier = Modifier.weight(1f), fontSize = 15.sp, color = textColor); Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray) }
}

@Composable
fun StatItem(modifier: Modifier, label: String, value: String) {
    Surface(modifier = modifier, color = Color(0xFFFF4081).copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp)) { Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFFF4081)); Text(label, fontSize = 10.sp, color = Color.Gray, maxLines = 1) } }
}
