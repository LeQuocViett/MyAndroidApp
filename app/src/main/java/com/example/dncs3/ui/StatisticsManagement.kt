package com.example.dncs3.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dncs3.viewmodel.MainViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

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
