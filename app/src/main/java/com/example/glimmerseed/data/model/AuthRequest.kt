package com.example.glimmerseed.data.model

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("deviceId") val deviceId: String = ""
)

data class LoginRequest(
    @SerializedName("account") val account: String,
    @SerializedName("password") val password: String,
    @SerializedName("deviceId") val deviceId: String = ""
)

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String?,
    @SerializedName("user_id") val userId: Long?,
    @SerializedName("username") val username: String?,
    @SerializedName("avatar_url") val avatarUrl: String?
)
