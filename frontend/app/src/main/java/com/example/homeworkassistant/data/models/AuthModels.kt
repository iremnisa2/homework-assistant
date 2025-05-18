package com.example.homeworkassistant.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    @SerializedName("full_name")
    val fullName: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("status")
    val status: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("data")
    val data: AuthData?,
    @SerializedName("error")
    val errorDetails: String?
) {
    fun isSuccess(): Boolean = status?.equals("success", ignoreCase = true) == true
}

@Parcelize
data class AuthData(
    @SerializedName("access_token")
    val token: String,
    @SerializedName("user")
    val user: User,
    @SerializedName("is_first_login")
    val isFirstLogin: Boolean? = null
) : Parcelable

@Parcelize
data class User(
    @SerializedName("id")
    val id: Int,
    @SerializedName("full_name")
    val fullName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("settings")
    val settings: @RawValue Map<String, Any>? = null,
    @SerializedName("is_active")
    val isActive: Boolean? = null,
    @SerializedName("last_login")
    val lastLogin: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("roles")
    val roles: List<String>? = null
) : Parcelable 