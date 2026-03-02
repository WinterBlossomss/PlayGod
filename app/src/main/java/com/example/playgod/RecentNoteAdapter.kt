package com.example.playgod

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecentNoteAdapter(
    private val noteList: List<RecentNote>,
    private val onItemClick: (RecentNote) -> Unit
) : RecyclerView.Adapter<RecentNoteAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lists_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = noteList[position]

        holder.tvTitle.text = note.noteName

        // Show tag name if available, otherwise fall back to a placeholder
        holder.tvDescription.text = if (note.tagName != null) {
            "${note.noteBrfDescr}  •  ${note.tagName}"
        } else {
            note.noteBrfDescr
        }

        holder.itemView.setOnClickListener { onItemClick(note) }
    }

    override fun getItemCount(): Int = noteList.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
    }
}