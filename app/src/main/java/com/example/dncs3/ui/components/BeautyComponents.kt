package com.example.dncs3.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dncs3.model.BeautyService
import java.text.DecimalFormat

val PrimaryPink = Color(0xFFFF4081)
val GradientPink = listOf(Color(0xFFFF4081), Color(0xFFFF80AB))

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
                focusedLabelColor = PrimaryPink
            ),
            singleLine = true
        )
        if (error != null) {
            Text(text = error, color = Color.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "buttonScale")

    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp).graphicsLayer(scaleX = scale, scaleY = scale),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(),
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(Brush.horizontalGradient(GradientPink), RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun ServiceSummaryHeader(service: BeautyService) {
    val df = DecimalFormat("#,###")
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                    Icon(Icons.Default.Spa, null, tint = PrimaryPink)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = service.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "${df.format(service.price)}đ", color = PrimaryPink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = service.description, color = Color.Gray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
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
    val scale by animateFloatAsState(if (isPressed) 0.85f else 1f, label = "iconScale")
    Surface(
        modifier = Modifier.size(42.dp).graphicsLayer(scaleX = scale, scaleY = scale).clickable(interactionSource = interactionSource, indication = null) { onClick() }, 
        shape = CircleShape, 
        color = tint.copy(alpha = 0.12f)
    ) { 
        Box(contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp)) } 
    }
}
