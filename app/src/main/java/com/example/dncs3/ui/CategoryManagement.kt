package com.example.dncs3.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dncs3.model.Category
import com.example.dncs3.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoriesScreen(viewModel: MainViewModel) {
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categoryName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.fetchCategories() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Quản Lý Danh Mục", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    selectedCategory = null
                    categoryName = ""
                    showAddDialog = true 
                }, 
                containerColor = Color(0xFFFF4081), 
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        if (categories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Chưa có danh mục nào", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        ListItem(
                            headlineContent = { Text(category.name, fontWeight = FontWeight.Medium) },
                            leadingContent = { Icon(Icons.Default.Category, null, tint = Color(0xFFFF4081)) },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { 
                                        selectedCategory = category
                                        categoryName = category.name
                                        showEditDialog = true 
                                    }) {
                                        Icon(Icons.Default.Edit, "Sửa", tint = Color(0xFF2196F3))
                                    }
                                    IconButton(onClick = { 
                                        selectedCategory = category
                                        showDeleteDialog = true 
                                    }) {
                                        Icon(Icons.Default.Delete, "Xóa", tint = Color.Red)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Dialog Thêm
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Thêm danh mục mới") },
                text = { 
                    OutlinedTextField(
                        value = categoryName, 
                        onValueChange = { categoryName = it }, 
                        label = { Text("Tên danh mục") }, 
                        modifier = Modifier.fillMaxWidth()
                    ) 
                },
                confirmButton = {
                    Button(onClick = {
                        if (categoryName.isNotBlank()) {
                            viewModel.addCategory(categoryName) { success, message ->
                                if (success) {
                                    showAddDialog = false
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }) { Text("Thêm") }
                },
                dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Hủy") } }
            )
        }

        // Dialog Sửa
        if (showEditDialog && selectedCategory != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Chỉnh sửa danh mục") },
                text = { 
                    OutlinedTextField(
                        value = categoryName, 
                        onValueChange = { categoryName = it }, 
                        label = { Text("Tên danh mục") }, 
                        modifier = Modifier.fillMaxWidth()
                    ) 
                },
                confirmButton = {
                    Button(onClick = {
                        if (categoryName.isNotBlank()) {
                            viewModel.updateCategory(selectedCategory!!.copy(name = categoryName)) { success, message ->
                                if (success) {
                                    showEditDialog = false
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }) { Text("Lưu") }
                },
                dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("Hủy") } }
            )
        }

        // Dialog Xóa
        if (showDeleteDialog && selectedCategory != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc muốn xóa danh mục '${selectedCategory!!.name}'? Bạn chỉ có thể xóa nếu không có dịch vụ nào thuộc danh mục này.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCategory(selectedCategory!!.id) { success, message ->
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
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Hủy") } }
            )
        }
    }
}
