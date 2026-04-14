package com.sharmadipanshu.aistudybuddy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.sharmadipanshu.aistudybuddy.databinding.ActivityAskAiBinding
import com.sharmadipanshu.aistudybuddy.utils.UiState
import com.sharmadipanshu.aistudybuddy.viewmodels.AskAiViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AskAiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAskAiBinding
    private val viewModel: AskAiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAskAiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val noteId = intent.getStringExtra(EXTRA_NOTE_ID)
        val title = intent.getStringExtra(EXTRA_TITLE)

        binding.toolbar.title = if (noteId.isNullOrBlank()) {
            getString(com.sharmadipanshu.aistudybuddy.R.string.ask_ai)
        } else {
            getString(com.sharmadipanshu.aistudybuddy.R.string.ask_ai_about_note, title ?: "")
        }
        binding.toolbar.setNavigationOnClickListener { finish() }

        intent.getStringExtra(EXTRA_INITIAL_TEXT)?.let {
            binding.editQuestion.setText(it)
        }

        if (noteId.isNullOrBlank()) {
            binding.editQuestion.isEnabled = false
            binding.buttonAskAi.isEnabled = false
            binding.cardResult.isVisible = true
            binding.textAnswer.text =
                getString(com.sharmadipanshu.aistudybuddy.R.string.general_ai_coming_soon)
            binding.textSources.isVisible = false
            return
        }

        binding.buttonAskAi.setOnClickListener {
            viewModel.askQuestion(
                question = binding.editQuestion.text?.toString().orEmpty(),
                noteId = noteId
            )
        }

        viewModel.responseState.observe(this) { state ->
            binding.progressBar.isVisible = state is UiState.Loading

            when (state) {
                is UiState.Success -> {
                    binding.cardResult.isVisible = true
                    binding.textAnswer.text = state.data.reply
                    binding.textSources.isVisible = state.data.retrievedChunks.isNotEmpty()
                    binding.textSources.text =
                        getString(
                            com.sharmadipanshu.aistudybuddy.R.string.retrieved_chunks_count,
                            state.data.retrievedChunks.size
                        )
                }
                is UiState.Error -> {
                    binding.cardResult.isVisible = true
                    binding.textAnswer.text = state.message
                    binding.textSources.isVisible = false
                }
                UiState.Idle -> Unit
                UiState.Loading -> binding.cardResult.isVisible = false
            }
        }
    }

    companion object {
        private const val EXTRA_NOTE_ID = "extra_note_id"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_INITIAL_TEXT = "extra_initial_text"

        fun createIntent(
            context: Context,
            noteId: String? = null,
            title: String? = null,
            initialText: String? = null
        ): Intent = Intent(context, AskAiActivity::class.java).apply {
            putExtra(EXTRA_NOTE_ID, noteId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_INITIAL_TEXT, initialText)
        }
    }
}
