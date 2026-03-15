package com.sharmadipanshu.aistudybuddy.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.sharmadipanshu.aistudybuddy.databinding.ActivityUploadNotesBinding
import com.sharmadipanshu.aistudybuddy.utils.UiState
import com.sharmadipanshu.aistudybuddy.viewmodels.UploadNotesViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class UploadNotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadNotesBinding
    private val viewModel: UploadNotesViewModel by viewModels()

    private var selectedPdfFile: File? = null
    private var selectedFileName: String? = null

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                handleSelectedPdf(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupListeners() = with(binding) {
        buttonChooseFile.setOnClickListener {
            filePickerLauncher.launch("application/pdf")
        }

        buttonUpload.setOnClickListener {
            viewModel.uploadNote(
                title = editTitle.text?.toString().orEmpty(),
                pdfFile = selectedPdfFile
            )
        }
    }

    private fun observeViewModel() {
        viewModel.uploadState.observe(this) { state ->
            binding.progressBar.isVisible = state is UiState.Loading
            binding.buttonUpload.isEnabled = state !is UiState.Loading
            binding.buttonChooseFile.isEnabled = state !is UiState.Loading

            when (state) {
                is UiState.Success -> {
                    Toast.makeText(this, state.data.message, Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, MyNotesActivity::class.java))
                    finish()
                }

                is UiState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    viewModel.clearState()
                }

                UiState.Idle, UiState.Loading -> Unit
            }
        }
    }

    private fun handleSelectedPdf(uri: Uri) {
        val displayName = queryDisplayName(uri) ?: "study_note.pdf"
        val tempFile = File(cacheDir, displayName)

        contentResolver.openInputStream(uri)?.use { inputStream ->
            tempFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        selectedPdfFile = tempFile
        selectedFileName = displayName

        binding.textFileName.text = displayName
        binding.textFilePreview.text = "PDF selected and ready to upload"
    }

    private fun queryDisplayName(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameColumn != -1) {
                return cursor.getString(nameColumn)
            }
        }
        return null
    }
}
