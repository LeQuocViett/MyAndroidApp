package com.example.dncs3.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dncs3.model.BeautyService
import com.example.dncs3.model.Category
import com.example.dncs3.ui.theme.*
import com.example.dncs3.viewmodel.MainViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel, 
    onNavigateToRegister: () -> Unit, 
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Animations
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFF0F5), White)))
    ) {
        // Decorative Blobs
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp, y = (-50).dp)
                .background(PrimaryPink.copy(alpha = 0.1f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Logo/Icon
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = PrimaryPink,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Chào mừng trở lại",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            
            Text(
                text = "Đăng nhập để tiếp tục sử dụng dịch vụ",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { 50 })
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BeautyTextField(
                            value = email,
                            onValueChange = { 
                                email = it
                                emailError = null
                            },
                            label = "Email",
                            placeholder = "Nhập email của bạn",
                            leadingIcon = Icons.Outlined.Email,
                            error = emailError,
                            keyboardType = KeyboardType.Email
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        BeautyTextField(
                            value = password,
                            onValueChange = { 
                                password = it
                                passwordError = null
                            },
                            label = "Mật khẩu",
                            placeholder = "Nhập mật khẩu",
                            leadingIcon = Icons.Outlined.Lock,
                            error = passwordError,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onPasswordToggle = { passwordVisible = !passwordVisible }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Quên mật khẩu?",
                            modifier = Modifier.align(Alignment.End).clickable { onNavigateToForgotPassword() },
                            color = PrimaryPink,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        BeautyButton(
                            text = "Đăng Nhập",
                            isLoading = isLoading,
                            onClick = {
                                // Validation
                                var isValid = true
                                if (email.isBlank()) {
                                    emailError = "Email không được để trống"
                                    isValid = false
                                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    emailError = "Email không đúng định dạng"
                                    isValid = false
                                }
                                
                                if (password.isBlank()) {
                                    passwordError = "Mật khẩu không được để trống"
                                    isValid = false
                                } else if (password.length < 6) {
                                    passwordError = "Mật khẩu phải từ 6 ký tự"
                                    isValid = false
                                }

                                if (isValid) {
                                    isLoading = true
                                    viewModel.login(mapOf("email" to email, "password" to password)) { success, msg ->
                                        isLoading = false
                                        if (success) {
                                            onLoginSuccess()
                                        } else {
                                            passwordError = msg
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Chưa có tài khoản? ", color = TextSecondary)
                Text(
                    text = "Đăng ký ngay",
                    color = PrimaryPink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
        
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun ForgotPasswordScreen(viewModel: MainViewModel, onNavigateBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFF0F5), White)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextDark)
                }
                Text(
                    text = "Quên mật khẩu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Vui lòng nhập email và số điện thoại đã đăng ký để thiết lập lại mật khẩu.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, RoundedCornerShape(28.dp)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BeautyTextField(
                        value = email,
                        onValueChange = { email = it; emailError = null },
                        label = "Email",
                        placeholder = "Nhập email của bạn",
                        leadingIcon = Icons.Outlined.Email,
                        error = emailError,
                        keyboardType = KeyboardType.Email
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BeautyTextField(
                        value = phone,
                        onValueChange = { phone = it; phoneError = null },
                        label = "Số điện thoại",
                        placeholder = "Nhập số điện thoại",
                        leadingIcon = Icons.Outlined.Phone,
                        error = phoneError,
                        keyboardType = KeyboardType.Phone
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BeautyTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it; passwordError = null },
                        label = "Mật khẩu mới",
                        placeholder = "Nhập mật khẩu mới",
                        leadingIcon = Icons.Outlined.Lock,
                        error = passwordError,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BeautyTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; confirmError = null },
                        label = "Xác nhận mật khẩu",
                        placeholder = "Nhập lại mật khẩu mới",
                        leadingIcon = Icons.Outlined.VerifiedUser,
                        error = confirmError,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible }
                    )

                    if (message != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = message!!,
                            color = if (isSuccess) Color(0xFF4CAF50) else Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    BeautyButton(
                        text = "Đặt Lại Mật Khẩu",
                        isLoading = isLoading,
                        enabled = !isSuccess,
                        onClick = {
                            var isValid = true
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Email không hợp lệ"
                                isValid = false
                            }
                            if (phone.length != 10) {
                                phoneError = "Số điện thoại phải 10 số"
                                isValid = false
                            }
                            if (newPassword.length < 6) {
                                passwordError = "Mật khẩu tối thiểu 6 ký tự"
                                isValid = false
                            }
                            if (newPassword != confirmPassword) {
                                confirmError = "Mật khẩu không khớp"
                                isValid = false
                            }

                            if (isValid) {
                                isLoading = true
                                viewModel.forgotPassword(email, phone, newPassword) { success, msg ->
                                    isLoading = false
                                    isSuccess = success
                                    message = msg
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
fun RegisterScreen(viewModel: MainViewModel, onNavigateToLogin: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFF0F5), White)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tạo tài khoản mới",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(top = 20.dp)
            )
            
            Text(
                text = "Tham gia cùng chúng tôi để nhận ưu đãi",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { 50 })
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BeautyTextField(
                            value = name,
                            onValueChange = { name = it; nameError = null },
                            label = "Họ tên",
                            placeholder = "Nhập họ và tên",
                            leadingIcon = Icons.Outlined.Person,
                            error = nameError
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        BeautyTextField(
                            value = email,
                            onValueChange = { email = it; emailError = null },
                            label = "Email",
                            placeholder = "example@mail.com",
                            leadingIcon = Icons.Outlined.Email,
                            error = emailError,
                            keyboardType = KeyboardType.Email
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        BeautyTextField(
                            value = phone,
                            onValueChange = { phone = it; phoneError = null },
                            label = "Số điện thoại",
                            placeholder = "09x xxx xxxx",
                            leadingIcon = Icons.Outlined.Phone,
                            error = phoneError,
                            keyboardType = KeyboardType.Phone
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        BeautyTextField(
                            value = password,
                            onValueChange = { password = it; passwordError = null },
                            label = "Mật khẩu",
                            placeholder = "Tối thiểu 6 ký tự",
                            leadingIcon = Icons.Outlined.Lock,
                            error = passwordError,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onPasswordToggle = { passwordVisible = !passwordVisible }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        BeautyTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; confirmError = null },
                            label = "Xác nhận mật khẩu",
                            placeholder = "Nhập lại mật khẩu",
                            leadingIcon = Icons.Outlined.VerifiedUser,
                            error = confirmError,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onPasswordToggle = { passwordVisible = !passwordVisible }
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        BeautyButton(
                            text = "Đăng Ký",
                            isLoading = isLoading,
                            onClick = {
                                var isValid = true
                                if (name.length < 2) { nameError = "Tên quá ngắn"; isValid = false }
                                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailError = "Email không hợp lệ"; isValid = false }
                                if (phone.length != 10) { phoneError = "Số điện thoại phải 10 số"; isValid = false }
                                if (password.length < 6 || !password.any { it.isDigit() } || !password.any { it.isLetter() }) { 
                                    passwordError = "Mật khẩu cần ít nhất 6 ký tự gồm chữ và số"; isValid = false 
                                }
                                if (password != confirmPassword) { confirmError = "Mật khẩu không khớp"; isValid = false }

                                if (isValid) {
                                    isLoading = true
                                    viewModel.register(mapOf("name" to name, "email" to email, "phone" to phone, "password" to password)) { success, message ->
                                        isLoading = false
                                        if (success) {
                                            onNavigateToLogin()
                                        } else {
                                            emailError = message
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Đã có tài khoản? ", color = TextSecondary)
                Text(
                    text = "Đăng nhập",
                    color = PrimaryPink,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }
        }
    }
}

// --- Custom Components ---

@Composable
fun BeautyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    error: String? = null,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text(placeholder, color = Color.LightGray) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = PrimaryPink) },
            trailingIcon = {
                if (isPassword) {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = onPasswordToggle) {
                        Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                    }
                }
            },
            isError = error != null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryPink,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                errorBorderColor = Color.Red,
                focusedLabelColor = PrimaryPink,
                unfocusedLabelColor = TextSecondary
            ),
            singleLine = true
        )
        if (error != null) {
            Text(
                text = error,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun BeautyButton(
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f)

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(GradientPink), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(
                    text = text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
            }
        }
    }
}

// --- Existing Screens (HomeScreen, etc.) ---

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFBFC))
        ) {
            item { HomeHeader(userName = user?.name ?: "Khách") }
            item { HomeSearchBar(value = searchText, onValueChange = { searchText = it }) }
            item { PromoBannerSlider() }
            item {
                CategorySection(
                    categories = categories,
                    selectedId = selectedCategoryId,
                    onCategorySelect = { id -> 
                        selectedCategoryId = if (selectedCategoryId == id) null else id 
                    }
                )
            }
            item {
                val title = if (selectedCategoryId == null) "Dịch vụ phổ biến" 
                            else "Dịch vụ: ${categories.find { it.id == selectedCategoryId }?.name}"
                PaddingRow {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }
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

    LaunchedEffect(Unit) {
        viewModel.fetchServices()
    }

    val filteredServices = services.filter { service ->
        service.name.contains(searchText, ignoreCase = true) || 
        service.description.contains(searchText, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFBFC))
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Text(text = "Dịch vụ", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
                    Spacer(modifier = Modifier.height(12.dp))
                    HomeSearchBar(value = searchText, onValueChange = { searchText = it })
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                items(filteredServices) { service ->
                    ServiceCardModern(service, onClick = { selectedServiceForDetail = it })
                }
            }
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
fun ServiceCardModern(service: BeautyService, onClick: (BeautyService) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable { onClick(service) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (service.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = service.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(90.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.size(90.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFFDE4EC)), contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color(0xFFFF4081), modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = service.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text(text = service.description, color = Color.Gray, fontSize = 14.sp, maxLines = 2, modifier = Modifier.padding(vertical = 4.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    val df = DecimalFormat("#,###")
                    Text(text = "${df.format(service.price)} VNĐ", color = Color(0xFFFF4081), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = Color(0xFFFF4081), shadowElevation = 2.dp) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(20.dp))
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (service.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = service.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFFDE4EC)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Spa, null, modifier = Modifier.size(64.dp), tint = Color(0xFFFF4081))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = service.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = "${df.format(service.price)} VNĐ", fontSize = 18.sp, color = Color(0xFFFF4081), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Thời gian: ${service.duration} phút", color = Color.Gray, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Mô tả dịch vụ:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = service.description, color = Color.DarkGray, fontSize = 14.sp, lineHeight = 20.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = onBookClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Đặt lịch ngay", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Đóng", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun HomeHeader(userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Xin chào, $userName", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
            Text(text = "Hôm nay bạn muốn làm đẹp gì?", fontSize = 14.sp, color = Color.Gray)
        }
        IconButton(onClick = { }, modifier = Modifier.padding(end = 8.dp).background(Color.White, CircleShape).shadow(1.dp, CircleShape)) {
            Icon(imageVector = Icons.Outlined.Notifications, contentDescription = "Notifications", tint = Color(0xFFFF4081))
        }
        Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(Color(0xFFFDE4EC)).shadow(2.dp, CircleShape), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = Color(0xFFFF4081), modifier = Modifier.size(28.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(value: String, onValueChange: (String) -> Unit) {
    PaddingRow {
        TextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().height(54.dp).shadow(4.dp, RoundedCornerShape(27.dp)),
            placeholder = { Text("Tìm dịch vụ làm đẹp...", fontSize = 14.sp, color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFFF4081)) },
            shape = RoundedCornerShape(27.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, disabledContainerColor = Color.White, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
            singleLine = true
        )
    }
}

@Composable
fun PromoBannerSlider() {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val banners = listOf(Pair("Ưu đãi 30%", "Cho dịch vụ làm Nail lần đầu"), Pair("Combo Tóc & Spa", "Giá chỉ từ 499k trong tháng này"), Pair("Thành viên mới", "Nhận ngay voucher làm đẹp 100k"))
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        HorizontalPager(state = pagerState, contentPadding = PaddingValues(horizontal = 20.dp), pageSpacing = 12.dp) { page ->
            Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(20.dp)).background(brush = Brush.horizontalGradient(colors = listOf(Color(0xFFFF4081), Color(0xFFFF80AB)))).padding(20.dp)) {
                Column(modifier = Modifier.align(Alignment.CenterStart)) {
                    Text(text = banners[page].first, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold); Text(text = banners[page].second, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp); Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = Color.White, shape = RoundedCornerShape(12.dp), modifier = Modifier.clickable { }) { Text(text = "Khám phá ngay", color = Color(0xFFFF4081), modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                }
            }
        }
        Row(Modifier.height(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Center) { repeat(3) { i -> val color = if (pagerState.currentPage == i) Color(0xFFFF4081) else Color(0xFFFDE4EC); Box(modifier = Modifier.padding(2.dp).clip(CircleShape).background(color).size(if (pagerState.currentPage == i) 10.dp else 6.dp)) } }
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
        Card(modifier = Modifier.size(64.dp).clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFFF4081) else Color.White), elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 2.dp)) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = category.name, tint = if (isSelected) Color.White else Color(0xFFFF4081), modifier = Modifier.size(28.dp)) } }
        Text(text = category.name, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) Color(0xFFFF4081) else Color.Gray, modifier = Modifier.padding(top = 8.dp), maxLines = 1)
    }
}

@Composable
fun PaddingRow(content: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, content = content)
}
