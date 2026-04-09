package com.sharmadipanshu.aistudybuddy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sharmadipanshu.aistudybuddy.adapters.AIChatMessageAdapter
import com.sharmadipanshu.aistudybuddy.databinding.FragmentAiChatBottomSheetBinding
import com.sharmadipanshu.aistudybuddy.utils.UiState
import com.sharmadipanshu.aistudybuddy.viewmodels.AIInteractionViewModel
import com.sharmadipanshu.aistudybuddy.viewmodels.PDFReaderViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AIChatBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentAiChatBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val pdfReaderViewModel: PDFReaderViewModel by activityViewModels()
    private val aiInteractionViewModel: AIInteractionViewModel by activityViewModels()
    private val chatAdapter = AIChatMessageAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiChatBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerChat.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerChat.adapter = chatAdapter

        aiInteractionViewModel.contextText.observe(viewLifecycleOwner) { text ->
            binding.textSectionContext.text = text.ifBlank { "No text selected." }
        }

        binding.buttonSend.setOnClickListener {
            val question = binding.editQuestion.text?.toString().orEmpty()
            val contextText = aiInteractionViewModel.contextText.value.orEmpty()
            if (question.isBlank()) {
                return@setOnClickListener
            }

            aiInteractionViewModel.addUserQuestion(question)
            pdfReaderViewModel.askFollowUpQuestion(contextText, question)
            binding.editQuestion.setText("")
        }

        aiInteractionViewModel.chatMessages.observe(viewLifecycleOwner) {
            chatAdapter.submitList(it)
            if (it.isNotEmpty()) {
                binding.recyclerChat.scrollToPosition(it.lastIndex)
            }
        }

        pdfReaderViewModel.aiResponseState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is UiState.Loading

            when (state) {
                is UiState.Success -> {
                    aiInteractionViewModel.addAiAnswer(state.data.reply)
                    pdfReaderViewModel.clearAiResponseState()
                }
                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    pdfReaderViewModel.clearAiResponseState()
                }
                UiState.Idle, UiState.Loading -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
