package com.example.dncs3.ui.user

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dncs3.model.BeautyService
import com.example.dncs3.model.Category
import com.example.dncs3.ui.components.*
import com.example.dncs3.viewmodel.MainViewModel
import java.text.DecimalFormat

@Composable
fun HomeScreen(viewModel: MainViewModel, onServiceClick: (BeautyService) -> Unit) {
    val services by viewModel.services.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val user = viewModel.currentUser
    var searchText by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var selectedServiceForDetail by remember { mutableStateOf<BeautyService?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchServices()
        viewModel.fetchCategories()
    }

    val filteredServices = services.filter { service ->
        val matchesSearch = service.name.contains(searchText, ignoreCase = true) || 
                          service.description.contains(searchText, ignoreCase = true)
        val matchesCategory = selectedCategoryId == null || service.categoryId == selectedCategoryId
        matchesSearch && matchesCategory
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFFFFFBFC))) {
            item { HomeHeader(userName = user?.name ?: "Khách") }
            item { HomeSearchBar(value = searchText, onValueChange = { searchText = it }) }
            item { PromoBannerSlider() }
            item {
                CategorySection(
                    categories = categories,
                    selectedId = selectedCategoryId,
                    onCategorySelect = { id -> selectedCategoryId = if (selectedCategoryId == id) null else id }
                )
            }
            item {
                val title = if (selectedCategoryId == null) "Dịch vụ phổ biến" 
                            else "Dịch vụ: ${categories.find { it.id == selectedCategoryId }?.name}"
                PaddingRow { Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray) }
            }
            items(filteredServices) { service ->
                ServiceCardModern(service, onClick = { selectedServiceForDetail = it })
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        if (selectedServiceForDetail != null) {
            ServiceDetailDialog(
                service = selectedServiceForDetail!!,
                onDismiss = { selectedServiceForDetail = null },
                onBookClick = {
                    val s = selectedServiceForDetail!!
                    selectedServiceForDetail = null
                    onServiceClick(s)
                }
            )
        }
    }
}

@Composable
fun UserServicesScreen(viewModel: MainViewModel, onServiceClick: (BeautyService) -> Unit) {
    val services by viewModel.services.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var selectedServiceForDetail by remember { mutableStateOf<BeautyService?>(null) }

    LaunchedEffect(Unit) { viewModel.fetchServices() }

    val filteredServices = services.filter { service ->
        service.name.contains(searchText, ignoreCase = true) || service.description.contains(searchText, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFFFBFC))) {
            Surface(modifier = Modifier.fillMaxWidth(), color = Color.White, shadowElevation = 2.dp) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(text = "Dịch vụ", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    Spacer(modifier = Modifier.height(12.dp))
                    HomeSearchBar(value = searchText, onValueChange = { searchText = it })
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)) {
                items(filteredServices) { service ->
                    ServiceCardModern(service, onClick = { selectedServiceForDetail = it })
                }
            }
        }
        if (selectedServiceForDetail != null) {
            ServiceDetailDialog(service = selectedServiceForDetail!!, onDismiss = { selectedServiceForDetail = null }, onBookClick = { val s = selectedServiceForDetail!!; selectedServiceForDetail = null; onServiceClick(s) })
        }
    }
}

@Composable
fun ServiceCardModern(service: BeautyService, onClick: (BeautyService) -> Unit) {
    val isInactive = service.status != "Hoạt động"
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).shadow(4.dp, RoundedCornerShape(20.dp)).clickable { onClick(service) }.alpha(if (isInactive) 0.6f else 1f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(90.dp)) {
                if (service.imageUrl.isNotEmpty()) {
                    AsyncImage(model = service.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Color(0xFFFDE4EC)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Image, null, tint = PrimaryPink, modifier = Modifier.size(32.dp))
                    }
                }
                if (isInactive) {
                    Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)).background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                        Text("TẠM NGƯNG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = service.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text(text = service.description, color = Color.Gray, fontSize = 14.sp, maxLines = 2, modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    val df = DecimalFormat("#,###")
                    Text(text = "${df.format(service.price)} VNĐ", color = if (isInactive) Color.Gray else PrimaryPink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (!isInactive) {
                        Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = PrimaryPink, shadowElevation = 2.dp) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceDetailDialog(service: BeautyService, onDismiss: () -> Unit, onBookClick: () -> Unit) {
    val df = DecimalFormat("#,###")
    val isInactive = service.status != "Hoạt động"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (service.imageUrl.isNotEmpty()) {
                    AsyncImage(model = service.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFFDE4EC)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Spa, null, modifier = Modifier.size(64.dp), tint = PrimaryPink)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = service.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = "${df.format(service.price)} VNĐ", fontSize = 18.sp, color = if (isInactive) Color.Gray else PrimaryPink, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Thời gian: ${service.duration} phút", color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Mô tả dịch vụ:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = service.description, color = Color.DarkGray, fontSize = 14.sp, lineHeight = 20.sp)
                if (isInactive) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(color = Color.Red.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("Dịch vụ hiện đang tạm ngưng kinh doanh.", color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onBookClick, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PrimaryPink), shape = RoundedCornerShape(12.dp), enabled = !isInactive) { Text(if (isInactive) "Không thể đặt lịch" else "Đặt lịch ngay", fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Đóng", color = Color.Gray) } },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun HomeHeader(userName: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Xin chào, $userName", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
            Text(text = "Hôm nay bạn muốn làm đẹp gì?", fontSize = 14.sp, color = Color.Gray)
        }
        IconButton(onClick = { }, modifier = Modifier.padding(end = 8.dp).background(Color.White, CircleShape).shadow(1.dp, CircleShape)) { Icon(Icons.Default.Notifications, null, tint = PrimaryPink) }
        Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(Color(0xFFFDE4EC)).shadow(2.dp, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = PrimaryPink, modifier = Modifier.size(28.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(value: String, onValueChange: (String) -> Unit) {
    PaddingRow {
        TextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth().height(54.dp).shadow(4.dp, RoundedCornerShape(27.dp)), placeholder = { Text("Tìm dịch vụ làm đẹp...", fontSize = 14.sp, color = Color.Gray) }, leadingIcon = { Icon(Icons.Default.Search, null, tint = PrimaryPink) }, shape = RoundedCornerShape(27.dp), colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), singleLine = true)
    }
}

@Composable
fun PromoBannerSlider() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val banners = listOf(Pair("Ưu đãi 30%", "Cho dịch vụ làm Nail lần đầu"), Pair("Combo Tóc & Spa", "Giá chỉ từ 499k trong tháng này"), Pair("Thành viên mới", "Nhận ngay voucher làm đẹp 100k"))
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        HorizontalPager(state = pagerState, contentPadding = PaddingValues(horizontal = 20.dp), pageSpacing = 12.dp) { page ->
            Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(20.dp)).background(brush = Brush.horizontalGradient(GradientPink)).padding(20.dp)) {
                Column(modifier = Modifier.align(Alignment.CenterStart)) {
                    Text(text = banners[page].first, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text(text = banners[page].second, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = Color.White, shape = RoundedCornerShape(12.dp), modifier = Modifier.clickable { }) { Text(text = "Khám phá ngay", color = PrimaryPink, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
        Row(Modifier.height(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center) { repeat(3) { i -> val color = if (pagerState.currentPage == i) PrimaryPink else Color(0xFFFDE4EC); Box(modifier = Modifier.padding(2.dp).clip(CircleShape).background(color).size(if (pagerState.currentPage == i) 10.dp else 6.dp)) } }
    }
}

@Composable
fun CategorySection(categories: List<Category>, selectedId: Int?, onCategorySelect: (Int) -> Unit) {
    Column {
        PaddingRow { Text("Danh mục dịch vụ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray) }
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) { items(categories) { category -> CategoryItemModern(category = category, isSelected = selectedId == category.id, onClick = { onCategorySelect(category.id) }) } }
    }
}

@Composable
fun CategoryItemModern(category: Category, isSelected: Boolean, onClick: () -> Unit) {
    val icon: ImageVector = when(category.name.lowercase()) { "cắt tóc", "làm tóc" -> Icons.Default.ContentCut; "nhuộm tóc" -> Icons.Default.ColorLens; "nail", "nails" -> Icons.Default.Brush; "spa" -> Icons.Default.Spa; "makeup" -> Icons.Default.AutoFixHigh; "gội đầu" -> Icons.Default.Face; else -> Icons.Default.AutoAwesome }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(70.dp)) {
        Card(modifier = Modifier.size(64.dp).clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (isSelected) PrimaryPink else Color.White), elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 2.dp)) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = category.name, tint = if (isSelected) Color.White else PrimaryPink, modifier = Modifier.size(28.dp)) } }
        Text(text = category.name, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) PrimaryPink else Color.Gray, modifier = Modifier.padding(top = 8.dp), maxLines = 1)
    }
}

@Composable
fun PaddingRow(content: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, content = content)
}
