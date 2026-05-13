package com.example.dncs3.network

import com.example.dncs3.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("login.php")
    suspend fun login(@Body credentials: Map<String, String>): LoginResponse

    @POST("register.php")
    suspend fun register(@Body user: Map<String, String>): BaseResponse

    @GET("get_services.php")
    suspend fun getServices(): List<BeautyService>

    @POST("add_service.php")
    suspend fun addService(@Body service: BeautyService): BaseResponse

    @POST("update_service.php")
    suspend fun updateService(@Body service: BeautyService): BaseResponse

    @POST("delete_service.php")
    suspend fun deleteService(@Body body: Map<String, Int>): BaseResponse

    @POST("book_appointment.php")
    suspend fun bookAppointment(@Body appointment: Appointment): BaseResponse

    @GET("get_user_appointments.php")
    suspend fun getUserAppointments(@Query("userId") userId: Int): List<Appointment>

    @GET("get_all_appointments.php")
    suspend fun getAllAppointments(): List<Appointment>

    @POST("update_appointment_status.php")
    suspend fun updateAppointmentStatus(@Body body: Map<String, String>): BaseResponse

    @POST("update_appointment.php")
    suspend fun updateAppointment(@Body appointment: Appointment): BaseResponse

    @POST("delete_appointment.php")
    suspend fun deleteAppointment(@Body body: Map<String, Int>): BaseResponse

    @GET("get_users.php")
    suspend fun getAllUsers(): List<User>

    @POST("update_user.php")
    suspend fun updateUser(@Body user: User): BaseResponse

    @POST("delete_user.php")
    suspend fun deleteUser(@Body body: Map<String, Int>): BaseResponse

    @POST("block_user.php")
    suspend fun blockUser(@Body body: Map<String, @JvmSuppressWildcards Any>): BaseResponse

    @POST("unblock_user.php")
    suspend fun unblockUser(@Body body: Map<String, Int>): BaseResponse

    @GET("search_users.php")
    suspend fun searchUsers(
        @Query("query") query: String,
        @Query("type") type: String?
    ): List<User>

    @GET("get_categories.php")
    suspend fun getCategories(): List<Category>

    @POST("add_category.php")
    suspend fun addCategory(@Body category: Category): BaseResponse

    @POST("change_password.php")
    suspend fun changePassword(@Body body: Map<String, String>): BaseResponse

    @POST("forgot_password.php")
    suspend fun forgotPassword(@Body body: Map<String, String>): BaseResponse
}
