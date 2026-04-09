package com.sharmadipanshu.aistudybuddy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sharmadipanshu.aistudybuddy.databinding.ItemFlashcardBinding
import com.sharmadipanshu.aistudybuddy.models.Flashcard

class FlashcardsAdapter : RecyclerView.Adapter<FlashcardsAdapter.FlashcardViewHolder>() {

    private val items = mutableListOf<Flashcard>()

    fun submitList(cards: List<Flashcard>) {
        items.clear()
        items.addAll(cards)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val binding = ItemFlashcardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FlashcardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class FlashcardViewHolder(
        private val binding: ItemFlashcardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(card: Flashcard) {
            binding.textFront.text = card.question
            binding.textBack.text = card.answer
        }
    }
}
