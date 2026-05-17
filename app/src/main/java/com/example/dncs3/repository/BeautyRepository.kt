package com.example.dncs3.repository

import com.example.dncs3.model.*
import com.example.dncs3.network.ApiService

class BeautyRepository(private val apiService: ApiService) {
    suspend fun login(credentials: Map<String, String>) = apiService.login(credentials)
    suspend fun register(user: Map<String, String>) = apiService.register(user)
    
    suspend fun getServices() = apiService.getServices()
    suspend fun addService(service: BeautyService) = apiService.addService(service)
    suspend fun updateService(service: BeautyService) = apiService.updateService(service)
    suspend fun deleteService(id: Int) = apiService.deleteService(mapOf("id" to id))
    
    suspend fun bookAppointment(appointment: Appointment) = apiService.bookAppointment(appointment)
    suspend fun getUserAppointments(userId: Int) = apiService.getUserAppointments(userId)
    suspend fun getAllAppointments() = apiService.getAllAppointments()
    suspend fun updateAppointmentStatus(id: Int, status: String, reason: String = "") = 
        apiService.updateAppointmentStatus(mutableMapOf(
            "id" to id.toString(), 
            "status" to status
        ).apply {
            if (reason.isNotEmpty()) put("cancel_reason", reason)
        })
    
    suspend fun updateAppointment(appointment: Appointment) = apiService.updateAppointment(appointment)
    suspend fun deleteAppointment(id: Int) = apiService.deleteAppointment(mapOf("id" to id))

    suspend fun getAllUsers() = apiService.getAllUsers()

    suspend fun updateUser(user: User) = apiService.updateUser(user)

    suspend fun deleteUser(id: Int) = apiService.deleteUser(mapOf("id" to id))

    suspend fun blockUser(id: Int, reason: String = "") = apiService.blockUser(mapOf("id" to id, "reason" to reason))

    suspend fun unblockUser(id: Int) = apiService.unblockUser(mapOf("id" to id))

    suspend fun searchUsers(query: String, searchType: String? = null) = 
        apiService.searchUsers(query, searchType)

    suspend fun getCategories() = apiService.getCategories()
    suspend fun addCategory(category: Category) = apiService.addCategory(category)
    suspend fun updateCategory(category: Category) = apiService.updateCategory(category)
    suspend fun deleteCategory(id: Int) = apiService.deleteCategory(mapOf("id" to id))

    suspend fun changePassword(userId: Int, oldPass: String, newPass: String) = 
        apiService.changePassword(mapOf(
            "id" to userId.toString(),
            "old_password" to oldPass,
            "new_password" to newPass
        ))

    suspend fun forgotPassword(email: String, phone: String, newPass: String) = 
        apiService.forgotPassword(mapOf(
            "email" to email,
            "phone" to phone,
            "new_password" to newPass
        ))
}
