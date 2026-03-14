package com.sharmadipanshu.aistudybuddy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sharmadipanshu.aistudybuddy.databinding.ItemStudyRoomBinding
import com.sharmadipanshu.aistudybuddy.models.StudyRoom

class StudyRoomsAdapter : RecyclerView.Adapter<StudyRoomsAdapter.StudyRoomViewHolder>() {

    private val items = mutableListOf<StudyRoom>()

    fun submitList(newItems: List<StudyRoom>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudyRoomViewHolder {
        val binding = ItemStudyRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudyRoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudyRoomViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class StudyRoomViewHolder(
        private val binding: ItemStudyRoomBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StudyRoom) {
            binding.textRoomTitle.text = item.title
            binding.textTopic.text = item.topic
            binding.textMembers.text = "${item.memberCount} learners"
            binding.textSessionTime.text = item.nextSession
        }
    }
}
