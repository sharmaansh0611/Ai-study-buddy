package com.sharmadipanshu.aistudybuddy.activities

import android.R
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputLayout
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
        setupFocus(binding.editName, binding.nameLine)
        setupFocus(binding.editEmail, binding.emailLine)
        setupFocus(binding.editNumber, binding.phoneLine)
        setupFocus(binding.editPassword, binding.passwordLine)
        setupFocus(binding.editConfirmPassword, binding.confirmLine)
        setupFocusWithIcon(binding.editName, binding.nameLine, binding.nameLayout)
        setupFocusWithIcon(binding.editEmail, binding.emailLine, binding.emailLayout)
        setupFocusWithIcon(binding.editNumber, binding.phoneLine, binding.phoneLayout)
        setupFocusWithIcon(binding.editPassword, binding.passwordLine, binding.passwordLayout)
        setupFocusWithIcon(binding.editConfirmPassword, binding.confirmLine, binding.confirmLayout)
        val fullText = "Already have an Account? Login"
        val spannable = SpannableString(fullText)

// find start & end of clickable part
        val start = fullText.indexOf("Login")
        val end = fullText.length

        spannable.setSpan(
            ForegroundColorSpan(getColor(com.sharmadipanshu.aistudybuddy.R.color.blue_signup)),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.textLogin.text = spannable
    }

    private fun setupListeners() = with(binding) {
        buttonSignup.setOnClickListener {
            viewModel.signup(
                editName.text?.toString().orEmpty(),
                editNumber.text?.toString().orEmpty(),
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

//    private fun observeViewModel() {
//        viewModel.authState.observe(this) { state ->
//            binding.progressBar.isVisible = state.isLoading
//            binding.buttonSignup.isEnabled = !state.isLoading
//
//            state.errorMessage?.let {
//                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
//                viewModel.clearFeedback()
//            }
//
//            state.successMessage?.let {
//                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
//            }
//
//            if (state.shouldNavigateToLogin) {
//                startActivity(Intent(this, LoginActivity::class.java))
//                viewModel.clearFeedback()
//                finish()
//            }
//        }
//    }
private fun observeViewModel() {
    viewModel.authState.observe(this) { state ->

        // 🔥 Button Loader instead of ProgressBar
        binding.buttonSignup.apply {
            isEnabled = !state.isLoading

            if (state.isLoading) {
                text = "Creating..."
                icon = getDrawable(com.sharmadipanshu.aistudybuddy.R.drawable.progress_loader)
            } else {
                text = "Create Account"
                icon = null
            }
        }

        // ❌ Removed progressBar usage
        // binding.progressBar.isVisible = state.isLoading

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

    private fun setupFocus(editText: EditText, line: View) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                line.setBackgroundColor(resources.getColor(com.sharmadipanshu.aistudybuddy.R.color.blue_signup, theme))
            } else {
                line.setBackgroundColor(resources.getColor(com.sharmadipanshu.aistudybuddy.R.color.grey_300_signup, theme))
            }
        }
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

            line.setBackgroundColor(color)
            layout.setStartIconTintList(ColorStateList.valueOf(color))
        }
    }
}
