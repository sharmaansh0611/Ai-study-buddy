package com.sharmadipanshu.aistudybuddy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sharmadipanshu.aistudybuddy.databinding.ItemNoteBinding
import com.sharmadipanshu.aistudybuddy.models.Note

class NotesAdapter : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private val items = mutableListOf<Note>()
    private var onViewClicked: ((Note) -> Unit)? = null
    private var onAskAiClicked: ((Note) -> Unit)? = null
    private var onDeleteClicked: ((Note) -> Unit)? = null

    fun setOnViewClickListener(listener: (Note) -> Unit) {
        onViewClicked = listener
    }

    fun setOnAskAiClickListener(listener: (Note) -> Unit) {
        onAskAiClicked = listener
    }

    fun setOnDeleteClickListener(listener: (Note) -> Unit) {
        onDeleteClicked = listener
    }

    fun submitList(notes: List<Note>) {
        items.clear()
        items.addAll(notes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.textTitle.text = note.title
            binding.textDate.text = note.createdAt
            binding.textFileUrl.text = note.fileUrl
            binding.textStatus.text = note.indexStatus.replaceFirstChar { it.uppercase() }
            binding.buttonView.setOnClickListener { onViewClicked?.invoke(note) }
            binding.buttonAskAi.setOnClickListener { onAskAiClicked?.invoke(note) }
            binding.buttonDelete.setOnClickListener { onDeleteClicked?.invoke(note) }
        }
    }
}
