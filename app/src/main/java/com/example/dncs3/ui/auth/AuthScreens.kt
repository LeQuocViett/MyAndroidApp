package com.example.dncs3.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dncs3.ui.components.*
import com.example.dncs3.viewmodel.MainViewModel

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
    
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFF0F5), Color.White)))
    ) {
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
            Icon(Icons.Default.AutoAwesome, null, tint = PrimaryPink, modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Chào mừng trở lại", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Đăng nhập để tiếp tục sử dụng dịch vụ", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { 50 })
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
                            value = password,
                            onValueChange = { password = it; passwordError = null },
                            label = "Mật khẩu",
                            placeholder = "Nhập mật khẩu",
                            leadingIcon = Icons.Outlined.Lock,
                            error = passwordError,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onPasswordToggle = { passwordVisible = !passwordVisible }
                        )
                        Text(
                            text = "Quên mật khẩu?",
                            modifier = Modifier.align(Alignment.End).clickable { onNavigateToForgotPassword() }.padding(top = 12.dp),
                            color = PrimaryPink,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        BeautyButton(
                            text = "Đăng Nhập",
                            isLoading = isLoading,
                            onClick = {
                                if (email.isBlank()) { emailError = "Email không được để trống"; return@BeautyButton }
                                if (password.isBlank()) { passwordError = "Mật khẩu không được để trống"; return@BeautyButton }
                                isLoading = true
                                viewModel.login(mapOf("email" to email, "password" to password)) { success, msg ->
                                    isLoading = false
                                    if (success) onLoginSuccess() else passwordError = msg
                                }
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Chưa có tài khoản? ", color = Color.Gray)
                Text("Đăng ký ngay", color = PrimaryPink, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToRegister() })
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

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFF0F5), Color.White)))) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tạo tài khoản mới", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp))
            Text("Tham gia cùng chúng tôi để nhận ưu đãi", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(visible = visible, enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { 50 })) {
                Card(modifier = Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(28.dp)), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        BeautyTextField(value = name, onValueChange = { name = it; nameError = null }, label = "Họ tên", placeholder = "Nhập họ và tên", leadingIcon = Icons.Outlined.Person, error = nameError)
                        Spacer(modifier = Modifier.height(16.dp))
                        BeautyTextField(value = email, onValueChange = { email = it; emailError = null }, label = "Email", placeholder = "example@mail.com", leadingIcon = Icons.Outlined.Email, error = emailError, keyboardType = KeyboardType.Email)
                        Spacer(modifier = Modifier.height(16.dp))
                        BeautyTextField(value = phone, onValueChange = { phone = it; phoneError = null }, label = "Số điện thoại", placeholder = "09x xxx xxxx", leadingIcon = Icons.Outlined.Phone, error = phoneError, keyboardType = KeyboardType.Phone)
                        Spacer(modifier = Modifier.height(16.dp))
                        BeautyTextField(value = password, onValueChange = { password = it; passwordError = null }, label = "Mật khẩu", placeholder = "Tối thiểu 6 ký tự", leadingIcon = Icons.Outlined.Lock, error = passwordError, isPassword = true, passwordVisible = passwordVisible, onPasswordToggle = { passwordVisible = !passwordVisible })
                        Spacer(modifier = Modifier.height(16.dp))
                        BeautyTextField(value = confirmPassword, onValueChange = { confirmPassword = it; confirmError = null }, label = "Xác nhận mật khẩu", placeholder = "Nhập lại mật khẩu", leadingIcon = Icons.Outlined.VerifiedUser, error = confirmError, isPassword = true, passwordVisible = passwordVisible, onPasswordToggle = { passwordVisible = !passwordVisible })
                        Spacer(modifier = Modifier.height(32.dp))
                        BeautyButton(
                            text = "Đăng Ký",
                            isLoading = isLoading,
                            onClick = {
                                if (name.isBlank()) { nameError = "Vui lòng nhập tên"; return@BeautyButton }
                                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailError = "Email không hợp lệ"; return@BeautyButton }
                                if (phone.length != 10) { phoneError = "SĐT phải 10 số"; return@BeautyButton }
                                if (password.length < 6) { passwordError = "Mật khẩu quá ngắn"; return@BeautyButton }
                                if (password != confirmPassword) { confirmError = "Mật khẩu không khớp"; return@BeautyButton }
                                isLoading = true
                                viewModel.register(mapOf("name" to name, "email" to email, "phone" to phone, "password" to password)) { success, message ->
                                    isLoading = false
                                    if (success) onNavigateToLogin() else emailError = message
                                }
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Đã có tài khoản? ", color = Color.Gray)
                Text("Đăng nhập", color = PrimaryPink, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToLogin() })
            }
        }
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

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFF0F5), Color.White)))) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                Text("Quên mật khẩu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text("Nhập email và SĐT để thiết lập lại mật khẩu.", color = Color.Gray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))
            Card(modifier = Modifier.fillMaxWidth().shadow(10.dp, RoundedCornerShape(28.dp)), shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    BeautyTextField(value = email, onValueChange = { email = it; emailError = null }, label = "Email", placeholder = "Email của bạn", leadingIcon = Icons.Outlined.Email, error = emailError, keyboardType = KeyboardType.Email)
                    Spacer(modifier = Modifier.height(16.dp))
                    BeautyTextField(value = phone, onValueChange = { phone = it; phoneError = null }, label = "Số điện thoại", placeholder = "Số điện thoại", leadingIcon = Icons.Outlined.Phone, error = phoneError, keyboardType = KeyboardType.Phone)
                    Spacer(modifier = Modifier.height(16.dp))
                    BeautyTextField(value = newPassword, onValueChange = { newPassword = it; passwordError = null }, label = "Mật khẩu mới", placeholder = "Mật khẩu mới", leadingIcon = Icons.Outlined.Lock, error = passwordError, isPassword = true, passwordVisible = passwordVisible, onPasswordToggle = { passwordVisible = !passwordVisible })
                    Spacer(modifier = Modifier.height(16.dp))
                    BeautyTextField(value = confirmPassword, onValueChange = { confirmPassword = it; confirmError = null }, label = "Xác nhận mật khẩu", placeholder = "Nhập lại mật khẩu", leadingIcon = Icons.Outlined.VerifiedUser, error = confirmError, isPassword = true, passwordVisible = passwordVisible, onPasswordToggle = { passwordVisible = !passwordVisible })
                    if (message != null) {
                        Text(message!!, color = if (isSuccess) Color(0xFF4CAF50) else Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 16.dp), textAlign = TextAlign.Center)
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    BeautyButton(
                        text = "Đặt Lại Mật Khẩu",
                        isLoading = isLoading,
                        enabled = !isSuccess,
                        onClick = {
                            if (email.isBlank() || phone.isBlank() || newPassword.isBlank()) { message = "Vui lòng nhập đầy đủ"; return@BeautyButton }
                            if (newPassword != confirmPassword) { confirmError = "Mật khẩu không khớp"; return@BeautyButton }
                            isLoading = true
                            viewModel.forgotPassword(email, phone, newPassword) { success, msg ->
                                isLoading = false; isSuccess = success; message = msg
                            }
                        }
                    )
                }
            }
        }
    }
}
