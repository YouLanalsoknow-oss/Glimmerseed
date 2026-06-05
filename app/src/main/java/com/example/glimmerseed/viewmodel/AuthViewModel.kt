package com.example.glimmerseed.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glimmerseed.data.repository.AuthRepository
import com.example.glimmerseed.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _authEvent = MutableSharedFlow<AuthState>(replay = 1)
    val authEvent: SharedFlow<AuthState> = _authEvent.asSharedFlow()

    fun register(username: String, password: String, email: String?) {
        viewModelScope.launch {
            _authEvent.emit(AuthState.Loading)
            
            try {
                val result = authRepository.register(username, password, email)
                
                if (result.success) {
                    _authEvent.emit(AuthState.Success)
                } else {
                    _authEvent.emit(AuthState.Error(result.message))
                }
            } catch (e: Exception) {
                _authEvent.emit(AuthState.Error("系统错误: ${e.message}"))
            }
        }
    }

    fun login(account: String, password: String) {
        viewModelScope.launch {
            _authEvent.emit(AuthState.Loading)
            
            try {
                val result = authRepository.login(account, password)
                
                if (result.success) {
                    _authEvent.emit(AuthState.Success)
                } else {
                    _authEvent.emit(AuthState.Error(result.message))
                }
            } catch (e: Exception) {
                _authEvent.emit(AuthState.Error("系统错误: ${e.message}"))
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}