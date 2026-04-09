package com.sharmadipanshu.aistudybuddy.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sharmadipanshu.aistudybuddy.R
import com.sharmadipanshu.aistudybuddy.databinding.ActivityPdfReaderBinding
import com.sharmadipanshu.aistudybuddy.fragments.PDFReaderFragment
import com.sharmadipanshu.aistudybuddy.models.PDFDocument
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PDFReaderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfReaderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val document = PDFDocument(
                noteId = intent.getStringExtra(EXTRA_NOTE_ID).orEmpty(),
                title = intent.getStringExtra(EXTRA_TITLE).orEmpty(),
                localPath = intent.getStringExtra(EXTRA_LOCAL_PATH),
                remoteUrl = intent.getStringExtra(EXTRA_REMOTE_URL)
            )

            supportFragmentManager.beginTransaction()
                .replace(R.id.pdf_reader_container, PDFReaderFragment.newInstance(document))
                .commit()
        }
    }

    companion object {
        private const val EXTRA_NOTE_ID = "extra_note_id"
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_LOCAL_PATH = "extra_local_path"
        private const val EXTRA_REMOTE_URL = "extra_remote_url"

        fun createIntent(
            context: Context,
            noteId: String,
            title: String,
            localPath: String? = null,
            remoteUrl: String? = null
        ): Intent = Intent(context, PDFReaderActivity::class.java).apply {
            putExtra(EXTRA_NOTE_ID, noteId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_LOCAL_PATH, localPath)
            putExtra(EXTRA_REMOTE_URL, remoteUrl)
        }
    }
}
