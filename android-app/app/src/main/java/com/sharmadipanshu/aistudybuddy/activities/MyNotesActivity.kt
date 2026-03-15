package com.sharmadipanshu.aistudybuddy.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.sharmadipanshu.aistudybuddy.adapters.NotesAdapter
import com.sharmadipanshu.aistudybuddy.databinding.ActivityMyNotesBinding
import com.sharmadipanshu.aistudybuddy.utils.UiState
import com.sharmadipanshu.aistudybuddy.viewmodels.MyNotesViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyNotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyNotesBinding
    private val viewModel: MyNotesViewModel by viewModels()
    private val notesAdapter = NotesAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recyclerNotes.layoutManager = LinearLayoutManager(this)
        binding.recyclerNotes.adapter = notesAdapter
        binding.swipeRefresh.setOnRefreshListener { viewModel.fetchNotes() }

        viewModel.notesState.observe(this) { state ->
            binding.progressBar.isVisible = state is UiState.Loading && !binding.swipeRefresh.isRefreshing
            binding.swipeRefresh.isRefreshing = false

            when (state) {
                is UiState.Success -> {
                    notesAdapter.submitList(state.data)
                    binding.textEmptyState.isVisible = state.data.isEmpty()
                    binding.textEmptyState.text = "No notes uploaded yet."
                }

                is UiState.Error -> {
                    binding.textEmptyState.isVisible = true
                    binding.textEmptyState.text = state.message
                }

                UiState.Idle, UiState.Loading -> Unit
            }
        }

        viewModel.fetchNotes()
    }
}
