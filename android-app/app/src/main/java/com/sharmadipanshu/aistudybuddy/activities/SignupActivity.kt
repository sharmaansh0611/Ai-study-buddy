package com.sharmadipanshu.aistudybuddy.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.sharmadipanshu.aistudybuddy.databinding.ActivitySignupBinding
import com.sharmadipanshu.aistudybuddy.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        setupListeners()
    }

    private fun setupListeners() = with(binding) {
        buttonSignup.setOnClickListener {
            viewModel.signup(
                editEmail.text?.toString().orEmpty(),
                editPassword.text?.toString().orEmpty(),
                editConfirmPassword.text?.toString().orEmpty()
            )
        }

        textLogin.setOnClickListener {
            startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(this) { state ->
            binding.progressBar.isVisible = state.isLoading
            binding.buttonSignup.isEnabled = !state.isLoading

            state.errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearFeedback()
            }

            state.successMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }

            if (state.shouldNavigateToLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
                viewModel.clearFeedback()
                finish()
            }
        }
    }
}
