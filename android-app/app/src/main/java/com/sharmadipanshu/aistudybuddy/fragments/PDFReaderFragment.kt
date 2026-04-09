package com.sharmadipanshu.aistudybuddy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.Fragment
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.sharmadipanshu.aistudybuddy.databinding.FragmentPdfReaderBinding
import com.sharmadipanshu.aistudybuddy.models.PDFDocument
import com.sharmadipanshu.aistudybuddy.utils.UiState
import com.sharmadipanshu.aistudybuddy.viewmodels.PDFReaderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PDFReaderFragment : Fragment() {

    private var _binding: FragmentPdfReaderBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PDFReaderViewModel by activityViewModels()

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

        val document = PDFDocument(
            noteId = requireArguments().getString(ARG_NOTE_ID).orEmpty(),
            title = requireArguments().getString(ARG_TITLE).orEmpty(),
            localPath = requireArguments().getString(ARG_LOCAL_PATH),
            remoteUrl = requireArguments().getString(ARG_REMOTE_URL)
        )

        observeViewModel()
        viewModel.loadDocument(document)
    }

    private fun observeViewModel() {
        viewModel.documentTitle.observe(viewLifecycleOwner) {
            binding.textFileName.text = it
        }

        viewModel.currentPageLabel.observe(viewLifecycleOwner) {
            binding.textPageIndicator.text = it
        }

        viewModel.pdfFileState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is UiState.Loading
            binding.textError.isVisible = state is UiState.Error

            when (state) {
                is UiState.Success -> {
                    binding.pdfView.fromFile(state.data)
                        .defaultPage(0)
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .enableAnnotationRendering(true)
                        .pageSnap(true)
                        .pageFling(true)
                        .autoSpacing(true)
                        .pageFitPolicy(com.github.barteksc.pdfviewer.util.FitPolicy.WIDTH)
                        .spacing(12)
                        .enableAntialiasing(true)
                        .scrollHandle(DefaultScrollHandle(requireContext()))
                        .onLoad {
                            binding.textError.isVisible = false
                        }
                        .onPageChange { page, pageCount ->
                            viewModel.onPageChanged(page, pageCount)
                        }
                        .onError { throwable ->
                            binding.textError.isVisible = true
                            binding.textError.text =
                                throwable.localizedMessage ?: "Unable to render the PDF."
                        }
                        .onPageError { _, throwable ->
                            binding.textError.isVisible = true
                            binding.textError.text =
                                throwable.localizedMessage ?: "A page could not be rendered."
                        }
                        .load()
                }
                is UiState.Error -> binding.textError.text = state.message
                UiState.Idle, UiState.Loading -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_NOTE_ID = "arg_note_id"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_LOCAL_PATH = "arg_local_path"
        private const val ARG_REMOTE_URL = "arg_remote_url"

        fun newInstance(document: PDFDocument): PDFReaderFragment {
            return PDFReaderFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_NOTE_ID, document.noteId)
                    putString(ARG_TITLE, document.title)
                    putString(ARG_LOCAL_PATH, document.localPath)
                    putString(ARG_REMOTE_URL, document.remoteUrl)
                }
            }
        }
    }
}
