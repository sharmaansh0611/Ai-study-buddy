package com.sharmadipanshu.aistudybuddy.fragments

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    // Keep reference to the active popup to ensure only one shows at a time
    private var textSelectionPopup: PopupWindow? = null
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
        
        // Hide popup when the user starts scrolling the WebView bounding box
        binding.pdfWebView.setOnScrollChangeListener { _, _, _, _, _ ->
            textSelectionPopup?.dismiss()
        }

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

                // Disable WebView's built-in zoom — PDF.js controls zoom internally.
                // Enabling both would cause double-scaling and text-layer misalignment.
                setSupportZoom(false)
                builtInZoomControls = false
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
            // Re-attach JavaScript bridge before loading
            addJavascriptInterface(WebAppInterface(), "Android")

            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)
            }
        }
    }

    // ── ViewModel observation ─────────────────────────────────────────────────

    private fun observeViewModel() {
        viewModel.documentTitle.observe(viewLifecycleOwner) { title ->
            binding.textFileName.text = title
        }

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

    inner class WebAppInterface {
        /**
         * Called from viewer.html when text is selected.
         * The coordinates (x, y) are the CSS coordinates of the top-left of the bounding box
         * of the selected text relative to the WebView canvas.
         */
        @Suppress("unused")
        @JavascriptInterface
        fun onTextSelected(text: String, x: Float, y: Float) {
            Log.d(TAG, "Text selected (x=$x, y=$y): ${text.take(40)}…")
            
            requireActivity().runOnUiThread {
                Toast.makeText(context, "Text Selected", Toast.LENGTH_SHORT).show()
                aiInteractionViewModel.updateContextText(text)
                showFloatingToolbar(text, x, y)
            }
        }
    }

    /**
     * Spawns a floating PopupWindow near the selected text in Android UI coordinates.
     */
    private fun showFloatingToolbar(selectedText: String, webX: Float, webY: Float) {
        if (!isAdded) return
        
        // Dismiss existing
        textSelectionPopup?.dismiss()
        
        // Inflate the custom popup layout
        val popupView = layoutInflater.inflate(com.sharmadipanshu.aistudybuddy.R.layout.popup_text_selection, null)
        
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true // Focusable so it intercepts clicks outside to dismiss implicitly
        )
        // Adjust elevation explicitly for older SDKs if cardView elevation clip fails
        popupWindow.elevation = 16f
        
        // Bind buttons
        popupView.findViewById<TextView>(com.sharmadipanshu.aistudybuddy.R.id.btnCopy).setOnClickListener {
            popupWindow.dismiss()
            copyToClipboard(selectedText)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        popupView.findViewById<TextView>(com.sharmadipanshu.aistudybuddy.R.id.btnAskAi).setOnClickListener {
            popupWindow.dismiss()
            startActivity(
                AskAiActivity.createIntent(
                    context = requireContext(),
                    noteId = currentDocument.noteId,
                    title = currentDocument.title,
                    initialText = selectedText
                )
            )
        }

        popupView.findViewById<TextView>(com.sharmadipanshu.aistudybuddy.R.id.btnMakeNotes).setOnClickListener {
            popupWindow.dismiss()
            openStudyToolsWithText(selectedText)
        }

        popupView.findViewById<TextView>(com.sharmadipanshu.aistudybuddy.R.id.btnMakeQuiz).setOnClickListener {
            popupWindow.dismiss()
            openStudyToolsWithText(selectedText)
        }

        popupView.findViewById<TextView>(com.sharmadipanshu.aistudybuddy.R.id.btnMakeFlashcard).setOnClickListener {
            popupWindow.dismiss()
            openStudyToolsWithText(selectedText)
        }

        // PDF.js selection coordinates are already in CSS pixels, so do not
        // multiply by density again or the popup will be placed off-screen.
        val selectionX = webX.toInt()
        val selectionY = webY.toInt()

        // Get WebView's absolute position in the window to account for toolbars/offsets
        val webViewPos = IntArray(2)
        binding.pdfWebView.getLocationInWindow(webViewPos)
        
        // Calculate the absolute window coordinates
        val absoluteX = webViewPos[0] + selectionX
        val absoluteY = webViewPos[1] + selectionY
        
        // Measure popup to center it horizontally above the selection
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = popupView.measuredWidth
        val popupHeight = popupView.measuredHeight

        val visibleFrame = Rect().also {
            requireActivity().window.decorView.getWindowVisibleDisplayFrame(it)
        }
        val maxPopupWidth = (visibleFrame.width() - resources.displayMetrics.density.times(16).toInt()).coerceAtLeast(1)
        popupWindow.width = popupWidth.coerceAtMost(maxPopupWidth)
        
        // finalX centered horizontally, ensure it stays on screen
        val finalX = (absoluteX - (popupWidth / 2)).coerceIn(
            visibleFrame.left,
            (visibleFrame.right - popupWidth).coerceAtLeast(visibleFrame.left)
        )
        
        // finalY positioned above selection. If too high, show below.
        val yOffset = -20 // Small gap
        var finalY = absoluteY - popupHeight + yOffset
        if (finalY < visibleFrame.top) { // If it would be hidden by status bar
            finalY = absoluteY + 40 // Show below instead
        }

        finalY = finalY.coerceIn(
            visibleFrame.top,
            (visibleFrame.bottom - popupHeight).coerceAtLeast(visibleFrame.top)
        )
        
        this.textSelectionPopup = popupWindow
        
        // Show at calculated location relative to window
        popupWindow.showAtLocation(requireActivity().window.decorView, android.view.Gravity.NO_GRAVITY, finalX, finalY)
    }

    private fun openStudyToolsWithText(text: String) {
        val intent = android.content.Intent(requireContext(), StudyToolsActivity::class.java).apply {
            putExtra(StudyToolsActivity.EXTRA_INITIAL_TEXT, text)
        }
        startActivity(intent)
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
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
        textSelectionPopup?.dismiss()
        textSelectionPopup = null

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
