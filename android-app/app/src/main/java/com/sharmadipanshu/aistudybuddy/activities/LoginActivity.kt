package com.sharmadipanshu.aistudybuddy.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.sharmadipanshu.aistudybuddy.databinding.ActivityLoginBinding
import com.sharmadipanshu.aistudybuddy.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken.isNullOrBlank()) {
                    showError("Google Sign-In is not configured. Add a valid web client ID.")
                } else {
                    viewModel.loginWithGoogle(idToken)
                }
            } catch (_: ApiException) {
                showError("Google Sign-In was cancelled or failed")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        setupListeners()
    }

    private fun setupListeners() = with(binding) {
        buttonLogin.setOnClickListener {
            viewModel.login(
                editEmail.text?.toString().orEmpty(),
                editPassword.text?.toString().orEmpty()
            )
        }

        buttonGoogleSignIn.setOnClickListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        textCreateAccount.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(this) { state ->
            binding.progressBar.isVisible = state.isLoading
            binding.buttonLogin.isEnabled = !state.isLoading
            binding.buttonGoogleSignIn.isEnabled = !state.isLoading

            state.errorMessage?.let {
                showError(it)
                viewModel.clearFeedback()
            }

            state.successMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearFeedback()
            }

            if (state.isAuthenticated) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
