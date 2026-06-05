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
import com.example.glimmerseed.app.data.SettingsDataStore
import com.example.glimmerseed.databinding.ActivityLoginBinding
import androidx.appcompat.app.AppCompatActivity
import com.example.glimmerseed.viewmodel.AuthViewModel
import com.example.glimmerseed.viewmodel.AuthViewModelFactory
import com.example.glimmerseed.viewmodel.AuthState
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
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

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val settings = SettingsDataStore.getInstance()

        if (settings.isLoggedInBlocking()) {
            setResult(Activity.RESULT_OK)
            finish()
            return
        }

        val savedAccount = settings.getUsernameBlocking()
        if (savedAccount.isNotEmpty()) {
            binding.emailEditText.setText(savedAccount)
        }

        authViewModel = ViewModelProvider(this, AuthViewModelFactory(this))[AuthViewModel::class.java]

        binding.loginButton.setOnClickListener {
            val account = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString()

            if (account.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "请填写用户名/邮箱和密码", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.loginButton.text = "登录中..."
            authViewModel.login(account, password)
        }

        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        lifecycleScope.launch {
            authViewModel.authEvent.collect { state ->
                when (state) {
                    is AuthState.Loading -> {
                        binding.loginButton.isEnabled = false
                        binding.loginButton.text = "登录中..."
                    }
                    is AuthState.Success -> {
                        Toast.makeText(this@LoginActivity, "登录成功！", Toast.LENGTH_SHORT).show()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    is AuthState.Error -> {
                        Toast.makeText(this@LoginActivity, state.message, Toast.LENGTH_LONG).show()
                        binding.loginButton.isEnabled = true
                        binding.loginButton.text = "登 录"
                    }
                    else -> {
                        binding.loginButton.isEnabled = true
                        binding.loginButton.text = "登 录"
                    }
                }
            }
        }
    }
}