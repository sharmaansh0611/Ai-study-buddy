package com.sharmadipanshu.aistudybuddy.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import com.sharmadipanshu.aistudybuddy.activities.AskAiActivity
import com.sharmadipanshu.aistudybuddy.activities.StudyToolsActivity
import com.sharmadipanshu.aistudybuddy.databinding.FragmentPdfReaderBinding
import com.sharmadipanshu.aistudybuddy.fragments.StudyResultsBottomSheetFragment
import com.sharmadipanshu.aistudybuddy.models.PDFDocument
import com.sharmadipanshu.aistudybuddy.utils.UiState
import com.sharmadipanshu.aistudybuddy.viewmodels.AIInteractionViewModel
import com.sharmadipanshu.aistudybuddy.viewmodels.PDFReaderViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileInputStream

@AndroidEntryPoint
class PDFReaderFragment : Fragment() {

    private var _binding: FragmentPdfReaderBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PDFReaderViewModel by activityViewModels()
    private val aiInteractionViewModel: AIInteractionViewModel by activityViewModels()

    private lateinit var currentDocument: PDFDocument

    /**
     * WebViewAssetLoader serves two path prefixes:
     *
     *  • /assets/  → Android assets directory (PDF.js viewer HTML/CSS/JS files)
     *  • /pdf/     → custom [CachePdfPathHandler] that streams the cached PDF from disk
     *
     * All URLs use the scheme+host: https://appassets.androidplatform.net
     * This satisfies CORS and same-origin rules inside PDF.js without requiring
     * any dangerous file:// or allowUniversalAccess flags.
     */
    private lateinit var assetLoader: WebViewAssetLoader

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfReaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentDocument = PDFDocument(
            noteId    = requireArguments().getString(ARG_NOTE_ID).orEmpty(),
            title     = requireArguments().getString(ARG_TITLE).orEmpty(),
            localPath = requireArguments().getString(ARG_LOCAL_PATH),
            remoteUrl = requireArguments().getString(ARG_REMOTE_URL)
        )

        setupWebView()
        observeViewModel()
        setupToolbar()
        
        binding.fabAskAi.setOnClickListener {
            startActivity(
                AskAiActivity.createIntent(
                    context = requireContext(),
                    noteId = currentDocument.noteId,
                    title = currentDocument.title
                )
            )
        }

        viewModel.loadDocument(currentDocument)
    }

    // ── WebView setup ─────────────────────────────────────────────────────────

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        // Asset loader: serves assets/ and a dynamic /pdf/ path
        assetLoader = WebViewAssetLoader.Builder()
            .setDomain("appassets.androidplatform.net")
            .addPathHandler("/assets/", AssetsPathHandler(requireContext()))
            .addPathHandler("/pdf/", CachePdfPathHandler(requireContext().cacheDir))
            .build()

        binding.pdfWebView.apply {
            // Hardware acceleration — CRITICAL for smooth PDF canvas rendering
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                // JavaScript must be enabled — PDF.js is a JS library
                javaScriptEnabled = true

                // DOM storage: PDF.js uses localStorage for preferences
                domStorageEnabled = true

                // File access: not needed (WebViewAssetLoader replaces file://)
                allowFileAccess = false

                // Allow in-page content loaded from our custom asset domain
                allowContentAccess = true

                // Mixed content: all URLs come from our same appassets domain
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

                // Let Android WebView handle pinch zoom for the full PDF viewer.
                // This scales canvas + text layer together, which keeps them aligned
                // while enabling physical-device pinch gestures.
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false

                // Do NOT set useWideViewPort=true — that activates a ~980px desktop
                // layout viewport, making window.innerWidth ≈ 980 and causing PDF pages
                // to render at desktop width and then appear as tiny zoomed-out tiles.
                // Default (false) keeps window.innerWidth == actual device CSS width.
                useWideViewPort = false
                loadWithOverviewMode = false
            }
        }

        binding.pdfWebView.apply {
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

                override fun onReceivedError(
                    view: WebView,
                    request: WebResourceRequest,
                    error: android.webkit.WebResourceError
                ) {
                    super.onReceivedError(view, request, error)
                    // If PDF.js assets fail to load, WebView can stay on the viewer's loading state.
                    // Surface a simple error to avoid a "stuck loading" experience.
                    if (request.isForMainFrame) {
                        binding.textError.isVisible = true
                        binding.textError.text = "Failed to load PDF viewer."
                    }
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            title = currentDocument.title
            setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

            inflateMenu(com.sharmadipanshu.aistudybuddy.R.menu.menu_pdf_reader)
            setOnMenuItemClickListener { menuItem ->
                handleMenuItemClick(menuItem.itemId)
                true
            }
        }
    }

    private fun handleMenuItemClick(itemId: Int) {
        binding.pdfWebView.evaluateJavascript("window.getSelection().toString()") { selectedText ->
            val cleanText = selectedText?.trim('"')?.trim()

            if (cleanText.isNullOrBlank()) {
                Toast.makeText(context, "Please select some text in the PDF first", Toast.LENGTH_SHORT).show()
                return@evaluateJavascript
            }

            when (itemId) {
                com.sharmadipanshu.aistudybuddy.R.id.action_ask_ai -> {
                    startActivity(
                        AskAiActivity.createIntent(
                            context = requireContext(),
                            noteId = currentDocument.noteId,
                            title = currentDocument.title,
                            initialText = cleanText
                        )
                    )
                }
                com.sharmadipanshu.aistudybuddy.R.id.action_make_notes,
                com.sharmadipanshu.aistudybuddy.R.id.action_make_quiz,
                com.sharmadipanshu.aistudybuddy.R.id.action_make_flashcards,
                com.sharmadipanshu.aistudybuddy.R.id.action_make_summary -> {
                    openStudyToolsWithText(cleanText)
                }
            }
        }
    }

    // ── ViewModel observation ─────────────────────────────────────────────────

    private fun observeViewModel() {
        viewModel.pdfFileState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is UiState.Loading
            binding.textError.isVisible   = state is UiState.Error

            when (state) {
                is UiState.Success -> loadPdfInWebView(state.data)
                is UiState.Error   -> binding.textError.text = state.message
                UiState.Idle, UiState.Loading -> Unit
            }
        }

        viewModel.quizState.observe(viewLifecycleOwner) { state ->
            handleStudyResults(state)
        }

        viewModel.flashcardsState.observe(viewLifecycleOwner) { state ->
            handleStudyResults(state)
        }
    }

    private fun <T> handleStudyResults(state: UiState<T>) {
        when (state) {
            is UiState.Loading -> {
                binding.progressBar.isVisible = true
            }
            is UiState.Success -> {
                binding.progressBar.isVisible = false
                val bottomSheet = StudyResultsBottomSheetFragment.newInstance()
                @Suppress("UNCHECKED_CAST")
                when {
                    state.data is List<*> && (state.data as List<*>).firstOrNull() is com.sharmadipanshu.aistudybuddy.models.QuizQuestion -> {
                        bottomSheet.setQuizData(state.data as List<com.sharmadipanshu.aistudybuddy.models.QuizQuestion>)
                    }
                    state.data is List<*> && (state.data as List<*>).firstOrNull() is com.sharmadipanshu.aistudybuddy.models.Flashcard -> {
                        bottomSheet.setFlashcardData(state.data as List<com.sharmadipanshu.aistudybuddy.models.Flashcard>)
                    }
                }
                bottomSheet.show(childFragmentManager, StudyResultsBottomSheetFragment.TAG)
                viewModel.clearAiResponseState() // Reset state after showing
            }
            is UiState.Error -> {
                binding.progressBar.isVisible = false
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                viewModel.clearAiResponseState()
            }
            UiState.Idle -> {
                binding.progressBar.isVisible = false
            }
        }
    }

    /**
     * Constructs the viewer URL using the WebViewAssetLoader scheme.
     *
     * PDF.js viewer HTML:
     *   https://appassets.androidplatform.net/assets/pdfjs/web/viewer.html
     *
     * The PDF file is served from the /pdf/ path handler using the filename only.
     * The handler reads it from [CachePdfPathHandler] (cacheDir).
     *
     * ?file= query param is consumed by viewer.html to initialise pdfjsLib.getDocument().
     */
    private fun loadPdfInWebView(pdfFile: File) {
        val pdfServedUrl = "https://appassets.androidplatform.net/pdf/${Uri.encode(pdfFile.name)}"
        val viewerUrl    = "https://appassets.androidplatform.net/assets/pdfjs/web/viewer.html" +
                           "?file=${Uri.encode(pdfServedUrl)}"

        Log.d(TAG, "Loading PDF viewer: $viewerUrl")
        binding.pdfWebView.loadUrl(viewerUrl)
    }

    // ── Inner classes ─────────────────────────────────────────────────────────
    // Bottom sheet invocation removed



    private fun openStudyToolsWithText(text: String) {
        val intent = android.content.Intent(requireContext(), StudyToolsActivity::class.java).apply {
            putExtra(StudyToolsActivity.EXTRA_INITIAL_TEXT, text)
        }
        startActivity(intent)
    }



    /**
     * Custom path handler that serves PDF files from [cacheDir].
     * The path segment expected is just the filename (no subdirectory).
     */
    private class CachePdfPathHandler(
        private val cacheDir: File
    ) : WebViewAssetLoader.PathHandler {

        override fun handle(path: String): WebResourceResponse? {
            return try {
                val decoded = Uri.decode(path)
                val file    = File(cacheDir, decoded)
                if (!file.exists() || !file.canRead()) return null

                WebResourceResponse(
                    "application/pdf",
                    null,
                    FileInputStream(file)
                )
            } catch (e: Exception) {
                Log.e(TAG, "CachePdfPathHandler error for path=$path", e)
                null
            }
        }
    }

    // Bottom sheet invocation removed

    // ── Lifecycle cleanup ─────────────────────────────────────────────────────

    override fun onDestroyView() {
        super.onDestroyView()

        // Prevent WebView memory leaks
        binding.pdfWebView.apply {
            stopLoading()
            destroy()
        }
        _binding = null
    }

    // ── Companion ─────────────────────────────────────────────────────────────

    companion object {
        private const val TAG            = "PDFReaderFragment"
        private const val ARG_NOTE_ID    = "arg_note_id"
        private const val ARG_TITLE      = "arg_title"
        private const val ARG_LOCAL_PATH = "arg_local_path"
        private const val ARG_REMOTE_URL = "arg_remote_url"

        fun newInstance(document: PDFDocument): PDFReaderFragment =
            PDFReaderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NOTE_ID,    document.noteId)
                    putString(ARG_TITLE,      document.title)
                    putString(ARG_LOCAL_PATH, document.localPath)
                    putString(ARG_REMOTE_URL, document.remoteUrl)
                }
            }
    }
}
