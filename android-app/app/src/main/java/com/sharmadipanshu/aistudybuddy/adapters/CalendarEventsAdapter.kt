package com.sharmadipanshu.aistudybuddy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sharmadipanshu.aistudybuddy.databinding.ItemCalendarEventBinding
import com.sharmadipanshu.aistudybuddy.models.CalendarEvent

class CalendarEventsAdapter : RecyclerView.Adapter<CalendarEventsAdapter.CalendarEventViewHolder>() {

    private val items = mutableListOf<CalendarEvent>()

    fun submitList(newItems: List<CalendarEvent>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarEventViewHolder {
        val binding = ItemCalendarEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CalendarEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarEventViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class CalendarEventViewHolder(
        private val binding: ItemCalendarEventBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CalendarEvent) {
            binding.textEventTitle.text = item.title
            binding.textEventTime.text = item.time
            binding.textEventDescription.text = item.description
        }
    }
}
