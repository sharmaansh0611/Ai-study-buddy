package com.sharmadipanshu.aistudybuddy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sharmadipanshu.aistudybuddy.databinding.ItemQuizQuestionBinding
import com.sharmadipanshu.aistudybuddy.models.QuizQuestion

class QuizAdapter : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    private val items = mutableListOf<QuizQuestion>()

    fun submitList(questions: List<QuizQuestion>) {
        items.clear()
        items.addAll(questions)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val binding = ItemQuizQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuizViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class QuizViewHolder(
        private val binding: ItemQuizQuestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(question: QuizQuestion, position: Int) {
            binding.textQuestion.text = "${position + 1}. ${question.question}"
            binding.textOptions.text = question.options.joinToString(separator = "\n") { option ->
                "• $option"
            }
            binding.textAnswer.text = "Answer: ${question.answer}"
        }
    }
}
