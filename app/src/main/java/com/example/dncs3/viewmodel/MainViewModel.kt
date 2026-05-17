package com.example.dncs3.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dncs3.model.*
import com.example.dncs3.repository.BeautyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: BeautyRepository) : ViewModel() {

    var currentUser by mutableStateOf<User?>(null)
        private set

    private val _services = MutableStateFlow<List<BeautyService>>(emptyList())
    val services: StateFlow<List<BeautyService>> = _services

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    fun login(credentials: Map<String, String>, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.login(credentials)
                if (response.success) {
                    currentUser = response.user
                    onResult(true, response.message)
                } else {
                    onResult(false, response.message)
                }
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error")
            }
        }
    }

    fun register(user: Map<String, String>, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.register(user)
                onResult(response.success, response.message)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Error")
            }
        }
    }

    fun forgotPassword(email: String, phone: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.forgotPassword(email, phone, newPass)
                onResult(response.success, response.message)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Lỗi kết nối")
            }
        }
    }

    fun fetchServices() {
        viewModelScope.launch {
            try {
                _services.value = repository.getServices()
            } catch (e: Exception) { }
        }
    }

    fun fetchCategories() {
        viewModelScope.launch {
            try {
                _categories.value = repository.getCategories()
            } catch (e: Exception) { }
        }
    }

    fun addCategory(name: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.addCategory(Category(name = name))
                if (response.success) fetchCategories()
                onResult(response.success, response.message)
            } catch (e: Exception) {
                onResult(false, "Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun updateCategory(category: Category, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.updateCategory(category)
                if (response.success) fetchCategories()
                onResult(response.success, response.message)
            } catch (e: Exception) {
                onResult(false, "Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun deleteCategory(id: Int, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.deleteCategory(id)
                if (response.success) {
                    fetchCategories()
                    onResult(true, "Xóa danh mục thành công")
                } else {
                    onResult(false, response.message)
                }
            } catch (e: Exception) {
                onResult(false, "Lỗi kết nối: ${e.message}")
            }
        }
    }

    fun addService(service: BeautyService, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.addService(service)
                if (response.success) fetchServices()
                onResult(response.success)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun updateService(service: BeautyService, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.updateService(service)
                if (response.success) fetchServices()
                onResult(response.success)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun deleteService(id: Int, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                val response = repository.deleteService(id)
                if (response.success) {
                    fetchServices()
                    onResult(true, "Xóa dịch vụ thành công")
                } else {
                    onResult(false, response.message)
                }
            } catch (e: Exception) {
                onResult(false, "Không thể xóa dịch vụ này (có thể đã có lịch hẹn sử dụng dịch vụ này)")
            }
        }
    }

    fun fetchUserAppointments() {
        val userId = currentUser?.id ?: return
        viewModelScope.launch {
            try {
                _appointments.value = repository.getUserAppointments(userId)
            } catch (e: Exception) {}
        }
    }

    fun fetchAllAppointments() {
        viewModelScope.launch {
            try {
                _appointments.value = repository.getAllAppointments()
            } catch (e: Exception) {}
        }
    }

    fun bookAppointment(appointment: Appointment, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.bookAppointment(appointment)
                if (currentUser?.role == "ADMIN") fetchAllAppointments()
                else fetchUserAppointments()
                onResult(response.success)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun updateAppointmentStatus(id: Int, status: String, reason: String = "", onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                val response = repository.updateAppointmentStatus(id, status, reason)
                if (response.success) {
                    if (currentUser?.role == "ADMIN") fetchAllAppointments()
                    else fetchUserAppointments()
                }
                onResult(response.success, response.message)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Lỗi kết nối")
            }
        }
    }

    fun updateAppointment(appointment: Appointment, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.updateAppointment(appointment)
                if (currentUser?.role == "ADMIN") fetchAllAppointments()
                else fetchUserAppointments()
                onResult(response.success)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun deleteAppointment(id: Int) {
        viewModelScope.launch {
            try {
                val response = repository.deleteAppointment(id)
                if (response.success) {
                    if (currentUser?.role == "ADMIN") fetchAllAppointments()
                    else fetchUserAppointments()
                }
            } catch (e: Exception) {}
        }
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            try {
                _users.value = repository.getAllUsers()
            } catch (e: Exception) {}
        }
    }

    fun updateUser(user: User, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.updateUser(user)
                if (response.success) {
                    if (currentUser?.id == user.id) {
                        currentUser = user
                    }
                    fetchAllUsers()
                }
                onResult(response.success)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun changePassword(oldPass: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        val userId = currentUser?.id ?: return
        viewModelScope.launch {
            try {
                val response = repository.changePassword(userId, oldPass, newPass)
                onResult(response.success, response.message)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Lỗi kết nối")
            }
        }
    }

    fun deleteUser(id: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.deleteUser(id)
                if (response.success) fetchAllUsers()
                onResult(response.success)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun blockUser(id: Int, reason: String = "", onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.blockUser(id, reason)
                if (response.success) fetchAllUsers()
                onResult(response.success)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun unblockUser(id: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.unblockUser(id)
                if (response.success) fetchAllUsers()
                onResult(response.success)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun searchUsers(query: String, searchType: String? = null) {
        viewModelScope.launch {
            try {
                _users.value = repository.searchUsers(query, searchType)
            } catch (e: Exception) {}
        }
    }

    fun logout() {
        currentUser = null
    }
}
