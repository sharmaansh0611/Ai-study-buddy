package com.sharmadipanshu.aistudybuddy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sharmadipanshu.aistudybuddy.databinding.ItemDashboardBinding
import com.sharmadipanshu.aistudybuddy.models.DashboardItem

class DashboardAdapter(
    private val onItemClicked: (DashboardItem) -> Unit
) : RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder>() {

    private val items = mutableListOf<DashboardItem>()

    fun submitList(newItems: List<DashboardItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val binding = ItemDashboardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DashboardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class DashboardViewHolder(
        private val binding: ItemDashboardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DashboardItem) {
            binding.iconDashboard.setImageResource(item.iconResId)
            binding.textTitle.text = item.title
            binding.textSubtitle.text = item.subtitle
            binding.root.setOnClickListener { onItemClicked(item) }
        }
    }
}
