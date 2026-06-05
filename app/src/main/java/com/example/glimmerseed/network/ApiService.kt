package com.example.glimmerseed.network

import com.example.glimmerseed.data.model.AuthResponse
import com.example.glimmerseed.data.model.LoginRequest
import com.example.glimmerseed.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
}
