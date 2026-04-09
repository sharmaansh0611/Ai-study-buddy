package com.sharmadipanshu.aistudybuddy.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sharmadipanshu.aistudybuddy.adapters.NotesAdapter
import com.sharmadipanshu.aistudybuddy.databinding.ActivityMyNotesBinding
import com.sharmadipanshu.aistudybuddy.models.Note
import com.sharmadipanshu.aistudybuddy.network.NetworkConstants
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
        notesAdapter.setOnViewClickListener(::openNote)
        notesAdapter.setOnAskAiClickListener(::askAboutNote)
        notesAdapter.setOnDeleteClickListener(::confirmDelete)

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

        viewModel.deleteState.observe(this) { state ->
            when (state) {
                is UiState.Success -> {
                    Toast.makeText(this, state.data.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearDeleteState()
                }
                is UiState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                    viewModel.clearDeleteState()
                }
                UiState.Idle, UiState.Loading -> Unit
            }
        }

        viewModel.fetchNotes()
    }

    private fun openNote(note: Note) {
        val remoteUrl = if (note.fileUrl.startsWith("http")) {
            note.fileUrl
        } else {
            "${NetworkConstants.SERVER_BASE_URL}${note.fileUrl.removePrefix("/")}"
        }

        startActivity(
            PDFReaderActivity.createIntent(
                context = this,
                noteId = note.noteId,
                title = note.title,
                remoteUrl = remoteUrl
            )
        )
    }

    private fun askAboutNote(note: Note) {
        startActivity(
            AskAiActivity.createIntent(
                context = this,
                noteId = note.noteId,
                title = note.title
            )
        )
    }

    private fun confirmDelete(note: Note) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(com.sharmadipanshu.aistudybuddy.R.string.delete_note_title))
            .setMessage(getString(com.sharmadipanshu.aistudybuddy.R.string.delete_note_message, note.title))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(com.sharmadipanshu.aistudybuddy.R.string.delete) { _, _ ->
                viewModel.deleteNote(note.noteId)
            }
            .show()
    }
}
