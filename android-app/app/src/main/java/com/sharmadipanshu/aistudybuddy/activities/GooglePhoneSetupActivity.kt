package com.sharmadipanshu.aistudybuddy.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.sharmadipanshu.aistudybuddy.databinding.ActivityGooglePhoneSetupBinding
import com.sharmadipanshu.aistudybuddy.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GooglePhoneSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGooglePhoneSetupBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGooglePhoneSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textBackToLogin.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }

        binding.buttonContinue.setOnClickListener {
            viewModel.completePhoneNumber(binding.editPhone.text?.toString().orEmpty())
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.authState.observe(this) { state ->
            binding.buttonContinue.isEnabled = !state.isLoading
            binding.buttonContinue.text = if (state.isLoading) "Saving..." else "Continue"

            state.errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearFeedback()
            }

            state.successMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearFeedback()
            }

            if (state.isAuthenticated) {
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
        }
    }
}
