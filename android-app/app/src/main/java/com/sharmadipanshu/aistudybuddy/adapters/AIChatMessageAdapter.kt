package com.sharmadipanshu.aistudybuddy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sharmadipanshu.aistudybuddy.databinding.ItemChatMessageBinding
import com.sharmadipanshu.aistudybuddy.models.ChatMessage

class AIChatMessageAdapter : RecyclerView.Adapter<AIChatMessageAdapter.ChatMessageViewHolder>() {

    private val items = mutableListOf<ChatMessage>()

    fun submitList(messages: List<ChatMessage>) {
        items.clear()
        items.addAll(messages)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatMessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ChatMessageViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            binding.textMessage.text = message.text
            val params = binding.cardMessage.layoutParams as ViewGroup.MarginLayoutParams
            if (message.isUser) {
                binding.cardMessage.setCardBackgroundColor(
                    binding.root.context.getColor(com.sharmadipanshu.aistudybuddy.R.color.brand_primary)
                )
                binding.textMessage.setTextColor(
                    binding.root.context.getColor(com.sharmadipanshu.aistudybuddy.R.color.white)
                )
                params.marginStart = 72
                params.marginEnd = 0
            } else {
                binding.cardMessage.setCardBackgroundColor(
                    binding.root.context.getColor(com.sharmadipanshu.aistudybuddy.R.color.brand_card)
                )
                binding.textMessage.setTextColor(
                    binding.root.context.getColor(com.sharmadipanshu.aistudybuddy.R.color.brand_text_primary)
                )
                params.marginStart = 0
                params.marginEnd = 72
            }
            binding.cardMessage.layoutParams = params
        }
    }
}
