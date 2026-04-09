package com.sharmadipanshu.aistudybuddy.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharmadipanshu.aistudybuddy.adapters.FlashcardsAdapter
import com.sharmadipanshu.aistudybuddy.adapters.QuizAdapter
import com.sharmadipanshu.aistudybuddy.databinding.ActivityStudyToolsBinding
import com.sharmadipanshu.aistudybuddy.utils.UiState
import com.sharmadipanshu.aistudybuddy.viewmodels.StudyToolsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudyToolsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudyToolsBinding
    private val viewModel: StudyToolsViewModel by viewModels()
    private val quizAdapter = QuizAdapter()
    private val flashcardsAdapter = FlashcardsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudyToolsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerQuiz.layoutManager = LinearLayoutManager(this)
        binding.recyclerQuiz.adapter = quizAdapter
        binding.recyclerFlashcards.layoutManager = LinearLayoutManager(this)
        binding.recyclerFlashcards.adapter = flashcardsAdapter

        binding.buttonAskNotes.setOnClickListener {
            viewModel.askNotes(binding.editQuestion.text?.toString().orEmpty())
        }
        binding.buttonSummarize.setOnClickListener {
            viewModel.summarize(binding.editStudyText.text?.toString().orEmpty())
        }
        binding.buttonGenerateQuiz.setOnClickListener {
            viewModel.generateQuiz(binding.editStudyText.text?.toString().orEmpty())
        }
        binding.buttonGenerateFlashcards.setOnClickListener {
            viewModel.generateFlashcards(binding.editStudyText.text?.toString().orEmpty())
        }

        observeState()
    }

    private fun observeState() {
        viewModel.notesChatState.observe(this) { state ->
            binding.progressNotes.isVisible = state is UiState.Loading
            when (state) {
                is UiState.Success -> {
                    binding.cardNotesAnswer.isVisible = true
                    binding.textNotesAnswer.text = state.data.reply
                }
                is UiState.Error -> {
                    binding.cardNotesAnswer.isVisible = true
                    binding.textNotesAnswer.text = state.message
                }
                UiState.Idle, UiState.Loading -> Unit
            }
        }

        viewModel.summaryState.observe(this) { state ->
            binding.progressSummary.isVisible = state is UiState.Loading
            when (state) {
                is UiState.Success -> {
                    binding.cardSummary.isVisible = true
                    binding.textSummary.text = state.data.reply
                }
                is UiState.Error -> {
                    binding.cardSummary.isVisible = true
                    binding.textSummary.text = state.message
                }
                UiState.Idle, UiState.Loading -> Unit
            }
        }

        viewModel.quizState.observe(this) { state ->
            binding.progressQuiz.isVisible = state is UiState.Loading
            when (state) {
                is UiState.Success -> {
                    binding.cardQuiz.isVisible = true
                    quizAdapter.submitList(state.data)
                }
                is UiState.Error -> Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                UiState.Idle, UiState.Loading -> Unit
            }
        }

        viewModel.flashcardsState.observe(this) { state ->
            binding.progressFlashcards.isVisible = state is UiState.Loading
            when (state) {
                is UiState.Success -> {
                    binding.cardFlashcards.isVisible = true
                    flashcardsAdapter.submitList(state.data)
                }
                is UiState.Error -> Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                UiState.Idle, UiState.Loading -> Unit
            }
        }
    }
}
