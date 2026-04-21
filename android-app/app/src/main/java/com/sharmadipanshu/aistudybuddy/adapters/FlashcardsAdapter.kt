package com.sharmadipanshu.aistudybuddy.adapters

import android.view.LayoutInflater
import android.view.View
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

            // Set data
            binding.textFront.text = card.question
            binding.textBack.text = card.answer

            // Maintain correct state (VERY IMPORTANT)
            if (card.isFlipped) {
                binding.frontLayout.visibility = View.GONE
                binding.backLayout.visibility = View.VISIBLE
                binding.flashCard.rotationY = 180f
            } else {
                binding.frontLayout.visibility = View.VISIBLE
                binding.backLayout.visibility = View.GONE
                binding.flashCard.rotationY = 0f
            }

            // Click to flip
            binding.flashCard.setOnClickListener {
                flipCard(card)
            }
        }

        private fun flipCard(card: Flashcard) {

            val front = binding.frontLayout
            val back = binding.backLayout
            val cardView = binding.flashCard

            cardView.animate()
                .rotationY(90f)
                .setDuration(150)
                .withEndAction {

                    if (!card.isFlipped) {
                        front.visibility = View.GONE
                        back.visibility = View.VISIBLE
                    } else {
                        front.visibility = View.VISIBLE
                        back.visibility = View.GONE
                    }

                    cardView.rotationY = -90f
                    cardView.animate()
                        .rotationY(0f)
                        .setDuration(150)
                        .start()

                    // Toggle state
                    card.isFlipped = !card.isFlipped
                }
                .start()
        }
    }
}