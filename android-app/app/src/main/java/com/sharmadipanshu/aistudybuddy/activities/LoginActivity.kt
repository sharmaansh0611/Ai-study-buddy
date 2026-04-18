package com.sharmadipanshu.aistudybuddy.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.textfield.TextInputLayout
import com.sharmadipanshu.aistudybuddy.R
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
        setupFocusWithIcon(binding.editEmail, binding.emailLine, binding.emailLayout)
        setupFocusWithIcon(binding.editPassword, binding.passwordLine, binding.passwordLayout)

        val fullText = "Don't have an Account? Sign up"
        val spannable = SpannableString(fullText)

// find start & end of clickable part
        val start = fullText.indexOf("Sign")
        val end = fullText.length

        spannable.setSpan(
            ForegroundColorSpan(getColor(R.color.blue_signup)),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.textCreateAccount.text = spannable
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

            // 🔥 Button loader (Login button)
            binding.buttonLogin.apply {
                isEnabled = !state.isLoading

                if (state.isLoading) {
                    text = "Signing in..."
                    icon = getDrawable(com.sharmadipanshu.aistudybuddy.R.drawable.progress_loader)
                } else {
                    text = "Login"
                    icon = null
                }
            }

            // 🔥 Disable Google button during loading
            binding.buttonGoogleSignIn.isEnabled = !state.isLoading

            // ❌ Removed old progress bar
            // binding.progressBar.isVisible = state.isLoading

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



    private fun setupFocusWithIcon(
        editText: EditText,
        line: View,
        layout: TextInputLayout
    ) {
        editText.setOnFocusChangeListener { _, hasFocus ->

            val color = if (hasFocus)
                getColor(com.sharmadipanshu.aistudybuddy.R.color.blue_signup)
            else
                getColor(com.sharmadipanshu.aistudybuddy.R.color.grey_300_signup)

            // underline
            line.setBackgroundColor(color)

            // start icon tint
            layout.setStartIconTintList(ColorStateList.valueOf(color))

            // 🔥 also tint password toggle icon
            layout.setEndIconTintList(ColorStateList.valueOf(color))
        }
    }
}
