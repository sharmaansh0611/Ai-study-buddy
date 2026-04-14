package com.sharmadipanshu.aistudybuddy.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sharmadipanshu.aistudybuddy.adapters.FlashcardsAdapter
import com.sharmadipanshu.aistudybuddy.adapters.QuizAdapter
import com.sharmadipanshu.aistudybuddy.databinding.FragmentStudyResultsBottomSheetBinding
import com.sharmadipanshu.aistudybuddy.models.Flashcard
import com.sharmadipanshu.aistudybuddy.models.QuizQuestion

class StudyResultsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentStudyResultsBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var quizData: List<QuizQuestion>? = null
    private var flashcardData: List<Flashcard>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyResultsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerResults.layoutManager = LinearLayoutManager(context)

        quizData?.let {
            binding.textTitle.text = "Generated Quiz"
            binding.textSubtitle.text = "Test your knowledge on the selected text"
            val adapter = QuizAdapter()
            binding.recyclerResults.adapter = adapter
            adapter.submitList(it)
        }

        flashcardData?.let {
            binding.textTitle.text = "Generated Flashcards"
            binding.textSubtitle.text = "Review these key concepts"
            val adapter = FlashcardsAdapter()
            binding.recyclerResults.adapter = adapter
            adapter.submitList(it)
        }

        binding.btnDone.setOnClickListener { dismiss() }
    }

    fun setQuizData(data: List<QuizQuestion>) {
        this.quizData = data
        this.flashcardData = null
    }

    fun setFlashcardData(data: List<Flashcard>) {
        this.flashcardData = data
        this.quizData = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "StudyResultsBottomSheetFragment"
        fun newInstance() = StudyResultsBottomSheetFragment()
    }
}
