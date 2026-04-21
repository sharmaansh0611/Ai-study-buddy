package com.sharmadipanshu.aistudybuddy.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.textfield.TextInputLayout
import com.sharmadipanshu.aistudybuddy.R
import com.sharmadipanshu.aistudybuddy.databinding.ActivityPhoneNumberBinding
import com.sharmadipanshu.aistudybuddy.viewmodels.PhoneNumberViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PhoneNumberActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneNumberBinding
    private val viewModel: PhoneNumberViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFocusWithIcon(binding.editPhoneNumber, binding.phoneLine, binding.phoneLayout)

        binding.buttonSavePhone.setOnClickListener {
            viewModel.savePhoneNumber(binding.editPhoneNumber.text?.toString().orEmpty())
        }

        binding.textBackToLogin.setOnClickListener {
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        }

        viewModel.state.observe(this) { state ->
            binding.progressBar.isVisible = state.isLoading
            binding.buttonSavePhone.isEnabled = !state.isLoading

            state.errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearFeedback()
            }

            state.successMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearFeedback()
            }

            if (state.shouldNavigateHome) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }

    private fun setupFocusWithIcon(editText: EditText, line: View, layout: TextInputLayout) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            val color = if (hasFocus) getColor(R.color.blue_signup) else getColor(R.color.grey_300_signup)
            line.setBackgroundColor(color)
            layout.setStartIconTintList(ColorStateList.valueOf(color))
            layout.setEndIconTintList(ColorStateList.valueOf(color))
        }
    }
}
