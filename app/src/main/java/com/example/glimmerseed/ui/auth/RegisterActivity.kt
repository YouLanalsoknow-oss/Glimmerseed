package com.example.glimmerseed.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.glimmerseed.databinding.ActivityRegisterBinding
import androidx.appcompat.app.AppCompatActivity
import com.example.glimmerseed.viewmodel.AuthViewModel
import com.example.glimmerseed.viewmodel.AuthViewModelFactory
import com.example.glimmerseed.viewmodel.AuthState
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp = window.attributes
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authViewModel = ViewModelProvider(this, AuthViewModelFactory(this))[AuthViewModel::class.java]

        binding.registerButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()
            val email = binding.emailEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请填写用户名和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "密码至少6位", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.registerButton.text = "注册中..."
            authViewModel.register(username, password, email.ifEmpty { null })
        }

        binding.loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        lifecycleScope.launch {
            authViewModel.authEvent.collect { state ->
                when (state) {
                    is AuthState.Loading -> {
                        binding.registerButton.isEnabled = false
                        binding.registerButton.text = "注册中..."
                    }
                    is AuthState.Success -> {
                        Toast.makeText(this@RegisterActivity, "注册成功！", Toast.LENGTH_SHORT).show()
                        
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    is AuthState.Error -> {
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_LONG).show()
                        binding.registerButton.isEnabled = true
                        binding.registerButton.text = "注 册"
                    }
                    else -> {
                        binding.registerButton.isEnabled = true
                        binding.registerButton.text = "注 册"
                    }
                }
            }
        }
    }
}