package com.example.playgod

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
//Adapter for Notes
class MyNoteAdapter(
    private val noteList: List<Notes>,
    private val onItemClick: (Notes) -> Unit
) : RecyclerView.Adapter<MyNoteAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.lists_card, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //Gets exact note
        val note = noteList[position]

        holder.tvTitle.text = note.noteName
        holder.tvDescription.text = note.noteBrfDescr

        //Navigate to Note
        holder.itemView.setOnClickListener {
            //in NotesFragment
            onItemClick(note)
        }

    }

    override fun getItemCount(): Int = noteList.size
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
    }
}