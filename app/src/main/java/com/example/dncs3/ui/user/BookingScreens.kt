package com.example.dncs3.ui.user

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dncs3.model.Appointment
import com.example.dncs3.model.BeautyService
import com.example.dncs3.ui.components.*
import com.example.dncs3.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

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
                    CalendarSection(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        onMonthChange = { currentMonth = it },
                        onDateSelect = { selectedDate = it },
                        onNext = { currentStep = BookingStep.TIME }
                    )
                }
                BookingStep.TIME -> {
                    TimeSelectionContent(
                        selectedDate = selectedDate,
                        selectedTime = selectedTime,
                        onTimeSelected = { 
                            selectedTime = it
                            currentStep = BookingStep.CONFIRM
                        }
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
                                serviceName = service.name
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
        }
    }
}

@Composable
fun CalendarSection(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelect: (LocalDate) -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
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
                Text(
                    text = "Tháng ${currentMonth.monthValue} năm ${currentMonth.year}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Row {
                    IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                        Icon(Icons.Default.ChevronLeft, null, tint = Color.Gray)
                    }
                    IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                        Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN").forEach { day ->
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

            CalendarGrid(currentMonth, selectedDate, Color(0xFFD81B60), onDateSelect)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Tiếp tục chọn giờ", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TimeSelectionContent(
    selectedDate: LocalDate,
    selectedTime: String?,
    onTimeSelected: (String) -> Unit
) {
    val morningTimes = listOf("08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30")
    val afternoonTimes = listOf("13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "17:30")
    val isToday = selectedDate == LocalDate.now()
    val currentTime = LocalTime.now()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Chọn giờ đến", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        TimeGroupSection("Buổi sáng", Icons.Default.LightMode, morningTimes, selectedTime, isToday, currentTime, onTimeSelected)
        Spacer(modifier = Modifier.height(24.dp))
        TimeGroupSection("Buổi chiều", Icons.Default.WbTwilight, afternoonTimes, selectedTime, isToday, currentTime, onTimeSelected)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimeGroupSection(
    title: String,
    icon: ImageVector,
    times: List<String>,
    selectedTime: String?,
    isToday: Boolean,
    currentTime: LocalTime,
    onTimeSelected: (String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color(0xFFE67E22), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 4
        ) {
            times.forEach { time ->
                val timeValue = LocalTime.parse(time)
                val isPast = isToday && timeValue.isBefore(currentTime)
                TimeSlotChip(time, time == selectedTime, !isPast) { onTimeSelected(time) }
            }
        }
    }
}

@Composable
fun TimeSlotChip(time: String, isSelected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.padding(bottom = 8.dp).clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFFD81B60) else Color(0xFFE0E0E0)),
        color = if (isSelected) Color(0xFFD81B60).copy(alpha = 0.1f) else if (enabled) Color.White else Color(0xFFF5F5F5)
    ) {
        Text(
            text = time,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color(0xFFD81B60) else if (enabled) Color.Black else Color.LightGray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun CalendarGrid(currentMonth: YearMonth, selectedDate: LocalDate, primaryColor: Color, onDateSelected: (LocalDate) -> Unit) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val startOffset = currentMonth.atDay(1).dayOfWeek.value % 7 - 1 // T2 is 1, but grid might start at T2
    val adjustedOffset = if (startOffset < 0) 6 else startOffset

    Column {
        for (row in 0 until 6) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val dayNum = row * 7 + col - adjustedOffset + 1
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                        if (dayNum in 1..daysInMonth) {
                            val date = currentMonth.atDay(dayNum)
                            val isSelected = date == selectedDate
                            val isPast = date.isBefore(LocalDate.now())
                            Surface(
                                modifier = Modifier.size(36.dp).clickable(enabled = !isPast) { onDateSelected(date) },
                                shape = CircleShape,
                                color = if (isSelected) Color(0xFF2ECC71) else Color.Transparent
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = dayNum.toString(), color = if (isSelected) Color.White else if (isPast) Color.LightGray else Color.Black)
                                }
                            }
                        }
                    }
                }
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
    onNoteChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Thông tin lịch hẹn", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = date, onValueChange = {}, label = { Text("Ngày") },
            modifier = Modifier.fillMaxWidth().clickable { onDateClick() },
            readOnly = true, enabled = false, leadingIcon = { Icon(Icons.Default.CalendarToday, null) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = time, onValueChange = {}, label = { Text("Giờ") },
            modifier = Modifier.fillMaxWidth().clickable { onTimeClick() },
            readOnly = true, enabled = false, leadingIcon = { Icon(Icons.Default.AccessTime, null) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = note, onValueChange = onNoteChange, label = { Text("Ghi chú") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            placeholder = { Text("Thêm ghi chú cho cửa hàng...") }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        BeautyButton(text = "Xác nhận đặt lịch", onClick = onConfirm)
    }
}
