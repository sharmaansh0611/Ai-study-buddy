package com.sharmadipanshu.aistudybuddy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sharmadipanshu.aistudybuddy.databinding.FragmentNotesBottomSheetBinding
import com.sharmadipanshu.aistudybuddy.viewmodels.AIInteractionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotesBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentNotesBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val aiInteractionViewModel: AIInteractionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aiInteractionViewModel.contextText.observe(viewLifecycleOwner) { text ->
            binding.textSummary.text = text
            binding.textKeyPoints.text =
                "Use the AI tools in the reader to summarize, generate key points, or create flashcards for the selected text."
        }
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
